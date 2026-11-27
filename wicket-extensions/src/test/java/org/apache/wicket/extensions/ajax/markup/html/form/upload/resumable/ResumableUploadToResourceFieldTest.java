/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.wicket.extensions.ajax.markup.html.form.upload.resumable;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.resource.FileListBehavior;
import org.apache.wicket.markup.html.form.upload.resource.FileUploadToResourceField;
import org.apache.wicket.markup.html.form.upload.resource.FolderUploadsFileManager;
import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.WicketTestCase;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;
import com.github.openjson.JSONTokener;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

/**
 * Tests {@link ResumableUploadToResourceField} and the tus protocol of
 * {@link ResumableUploadResourceReference}.
 */
class ResumableUploadToResourceFieldTest extends WicketTestCase
{
	private static final String PATH = "resumable-uploads";

	/** The body of the next request, with its content type; none when null. */
	private static byte[] body;

	private static String bodyType;

	/** Where the bytes of a body break off, -1 never. */
	private static int breakAt = -1;

	/** The owner of the uploads, the session when null. */
	private static String owner;

	/** Whether the file manager fails to store a file. */
	private static boolean failingToStore;

	/** What the reference's getters return instead of its settings; theirs when null. */
	private static Bytes overriddenMaxFileSize;

	private static Integer overriddenMaxUploadsPerOwner;

	@TempDir
	static Path folder;

	private MutableClock clock;

	private TestField field;

	/** The query of the requests of the field. */
	private String query;

	@Override
	protected WebApplication newApplication()
	{
		return new MockApplication()
		{
			@Override
			protected void init()
			{
				super.init();
				mountResource(PATH, new ResumableUploadResourceReference(
					new FolderUploadsFileManager(
						new org.apache.wicket.util.file.File(folder.resolve("stored").toString()))
					{
						@Override
						public void save(FileUpload fileItem, String uploadFieldId)
						{
							if (failingToStore)
							{
								throw new IllegalStateException("the disk is full");
							}
							super.save(fileItem, uploadFieldId);
						}
					},
					folder.resolve("parts").toFile())
				{
					private static final long serialVersionUID = 1L;

					@Override
					protected InputStream wrapBody(InputStream in, ResumableUpload upload)
					{
						return breakAt < 0 ? in : new BreakingStream(in, breakAt);
					}

					@Override
					protected String getCurrentOwner()
					{
						return owner != null ? owner : super.getCurrentOwner();
					}

					@Override
					public Bytes getMaxFileSize()
					{
						return overriddenMaxFileSize != null ? overriddenMaxFileSize
							: super.getMaxFileSize();
					}

					@Override
					public int getMaxUploadsPerOwner()
					{
						return overriddenMaxUploadsPerOwner != null ? overriddenMaxUploadsPerOwner
							: super.getMaxUploadsPerOwner();
					}
				}.setMaxFileSize(Bytes.bytes(100)).setMaxChunkSize(Bytes.bytes(40)));
			}

			@Override
			public WebRequest newWebRequest(HttpServletRequest servletRequest, String filterPath)
			{
				return super.newWebRequest(body == null ? servletRequest
					: new BodyRequest(servletRequest, body, bodyType), filterPath);
			}
		};
	}

	@BeforeEach
	void renderAField()
	{
		body = null;
		bodyType = null;
		breakAt = -1;
		owner = null;
		failingToStore = false;
		overriddenMaxFileSize = null;
		overriddenMaxUploadsPerOwner = null;
		clock = new MutableClock(Instant.parse("2026-11-24T10:00:00Z"));
		reference().setClock(clock);
		tester.getSession().bind();
		field = new TestField("files");
		tester.startPage(new TestPage(field));
		query = queryOf(settings());
	}

	/**
	 * Renders the field again, on a new page, and takes the query of its grant.
	 */
	private void renderAgain()
	{
		field = new TestField("files");
		tester.startPage(new TestPage(field));
		query = queryOf(settings());
	}

	/**
	 * Ends the session, and renders the field again in a new one, as after signing in again.
	 */
	private void newSession()
	{
		String session = tester.getSession().getId();
		tester.getSession().invalidateNow();
		renderAgain();
		assertNotEquals(session, tester.getSession().getId());
	}

	private static ResumableUploadResourceReference reference()
	{
		return ResumableUploadResourceReference.getInstance();
	}

	/**
	 * @return the settings the field rendered for its script
	 */
	private JSONObject settings()
	{
		String page = tester.getLastResponseAsString();
		String call = "Wicket.Timer." + field.getMarkupId() +
			" = new Wicket.ResumableUploadToResourceField(";
		int start = page.indexOf(call);
		assertTrue(start >= 0, call + " in " + page);
		return (JSONObject)new JSONTokener(page.substring(start + call.length())).nextValue();
	}

	/**
	 * @return the query the script of the field adds to its requests
	 */
	private static String queryOf(JSONObject settings)
	{
		Map<String, Object> parameters = new LinkedHashMap<>();
		parameters.put("uploadId", settings.getString("inputName"));
		parameters.put("fileMaxSize", settings.get("fileMaxSize"));
		parameters.put("uploadToken", settings.getString("uploadToken"));
		parameters.put("grantExpires", settings.get("grantExpires"));
		if (settings.has("uploadContext"))
		{
			parameters.put("uploadContext", settings.getString("uploadContext"));
		}
		StringBuilder query = new StringBuilder();
		parameters.forEach((name, value) -> query.append(query.isEmpty() ? "" : "&")
			.append(name)
			.append('=')
			.append(URLEncoder.encode(String.valueOf(value), StandardCharsets.UTF_8)));
		return query.toString();
	}

	/**
	 * Sends a request of tus with the query of the field.
	 *
	 * @return the status of the answer
	 */
	private int send(String method, String path, Map<String, String> headers, byte[] bytes)
	{
		return send(method, path + "?" + query, headers, bytes, ResumableUploadResource.CHUNK_TYPE);
	}

	private int send(String method, String url, Map<String, String> headers, byte[] bytes,
		String type)
	{
		body = bytes;
		bodyType = bytes != null ? type : null;
		tester.getRequest().setMethod(method);
		tester.getRequest().setHeader("Tus-Resumable", "1.0.0");
		headers.forEach((name, value) -> tester.getRequest().setHeader(name, value));
		tester.executeUrl(url);
		body = null;
		return tester.getLastResponse().getStatus();
	}

	private String header(String name)
	{
		return tester.getLastResponse().getHeader(name);
	}

	/**
	 * Creates an upload.
	 *
	 * @return its URL, below the root of the application
	 */
	private String create(long length, String name)
	{
		String metadata = name == null ? "" : "filename " + Base64.getEncoder()
			.encodeToString(name.getBytes(StandardCharsets.UTF_8)) + ",filetype dGV4dC9wbGFpbg==";
		assertEquals(201, send("POST", PATH, Map.of("Upload-Length", String.valueOf(length),
			"Upload-Metadata", metadata), null));
		String location = header("Location");
		assertTrue(location.startsWith(PATH + "/"), "relative to the endpoint: " + location);
		return location;
	}

	/**
	 * Creates an upload as the field does, telling the place of the field and the date of the file.
	 *
	 * @return the path of the upload
	 */
	private String createAt(long length, String name, String place, long lastModified)
	{
		Base64.Encoder base64 = Base64.getEncoder();
		String metadata = "filename " + base64.encodeToString(name.getBytes(StandardCharsets.UTF_8)) +
			",filetype dGV4dC9wbGFpbg==,lastmodified " +
			base64.encodeToString(String.valueOf(lastModified).getBytes(StandardCharsets.UTF_8)) +
			",place " + base64.encodeToString(place.getBytes(StandardCharsets.UTF_8));
		assertEquals(201, send("POST", PATH, Map.of("Upload-Length", String.valueOf(length),
			"Upload-Metadata", metadata), null));
		return header("Location");
	}

	private int patch(String url, long offset, byte[] bytes)
	{
		return send("PATCH", url, Map.of("Upload-Offset", String.valueOf(offset)), bytes);
	}

	private static String idOf(String url)
	{
		return url.substring(url.lastIndexOf('/') + 1);
	}

	private ResumableUpload find(String url)
	{
		return reference().find(idOf(url), tester.getSession().getId());
	}

	private static byte[] bytes(int length, int from)
	{
		byte[] bytes = new byte[length];
		for (int index = 0; index < length; index++)
		{
			bytes[index] = (byte)(from + index);
		}
		return bytes;
	}

	/**
	 * Tells the field how an upload ended, as its script does.
	 */
	private void report(String... parameters)
	{
		for (int index = 0; index < parameters.length; index += 2)
		{
			tester.getRequest().setParameter(parameters[index], parameters[index + 1]);
		}
		tester.executeBehavior(field.getBehaviors(AbstractDefaultAjaxBehavior.class).get(0));
	}

	@Test
	void rendersTheSettingsOfItsScript()
	{
		JSONObject settings = settings();
		assertEquals("../" + PATH, settings.getString("resourceUrl"));
		assertEquals(field.getMarkupId(), settings.getString("inputName"));
		assertEquals((1L << 53) - 1, settings.getLong("maxSize"),
			"the default of the application, as large as a script holds exactly");
		assertEquals(100, settings.getLong("fileMaxSize"), "the limit of the reference");
		assertEquals(40, settings.getLong("chunkSize"), "at most what the reference takes");
		assertEquals(3, settings.getInt("parallelUploads"));
		assertFalse(settings.getBoolean("remembering"));
		assertTrue(settings.getBoolean("askingToResume"));
		assertEquals("[0,1000,3000,5000,10000,20000,1000]",
			settings.getJSONArray("retryDelays").toString(), "40 seconds of tries again");
		assertEquals("[5000,10000,20000,30000]", settings.getJSONArray("probeDelays").toString());
		assertEquals(30000, settings.getLong("stallMillis"));
		assertTrue(settings.getBoolean("recalling"));
		assertEquals(20000, settings.getLong("inUseMillis"));
		assertEquals(-1, settings.getLong("grantExpires"),
			"never: the key of the grant ends with the session");
		assertFalse(settings.getBoolean("overridingMethod"));
		assertEquals(TestPage.class.getName() + ":" + field.getPageRelativePath(),
			settings.getString("rememberedAs"), "the same on every page, unlike the id");
		assertEquals("Pause", settings.getJSONObject("texts").getString("pause"));
		assertEquals("The connection is back. Resume the uploads?",
			settings.getJSONObject("texts").getString("reconnected"));
		assertEquals(14, settings.getJSONObject("texts").length());
		assertTrue(tester.getLastResponseAsString().contains(
			"function (res) {},function (question) { return false; });"),
			"the function asking whether to resume, last");
		String page = tester.getLastResponseAsString();
		int parent = page.indexOf("FileUploadToResourceField.js");
		int own = page.indexOf("ResumableUploadToResourceField.js");
		assertTrue(parent > 0 && own > parent, "the script of the parent first");
	}

	@Test
	void sendsAFileInChunksToTheFileManagerAndTheField() throws IOException
	{
		String url = create(50, "../notes.txt");
		assertEquals("0", header("Upload-Offset"));
		assertEquals("Tue, 24 Nov 2026 10:05:00 GMT", header("Upload-Expires"),
			"soon, while it holds no bytes");
		assertEquals("1.0.0", header("Tus-Resumable"));
		ResumableUpload upload = find(url);
		assertEquals("notes.txt", upload.getClientFileName());
		assertEquals("text/plain", upload.getContentType());
		assertEquals("../notes.txt", upload.getMetadata().get("filename"));
		assertEquals(field.getMarkupId(), upload.getUploadFieldId());

		assertEquals(204, patch(url, 0, bytes(30, 0)));
		assertEquals("30", header("Upload-Offset"));
		assertEquals("Tue, 24 Nov 2026 10:30:00 GMT", header("Upload-Expires"));
		assertNull(header(ResumableUploadResource.FILE_NAME_HEADER));
		assertEquals(200, send("HEAD", url, Map.of(), null));
		assertEquals("30", header("Upload-Offset"));
		assertEquals("50", header("Upload-Length"));
		assertTrue(header("Cache-Control").contains("no-store"), header("Cache-Control"));

		clock.add(Duration.ofMinutes(10));
		assertEquals(204, patch(url, 30, bytes(20, 30)));
		assertEquals("50", header("Upload-Offset"));
		assertEquals("Tue, 24 Nov 2026 10:15:00 GMT", header("Upload-Expires"),
			"kept briefly, for a browser that lost the answer");
		String fileName = header(ResumableUploadResource.FILE_NAME_HEADER);
		assertNotNull(fileName);
		assertFalse(Files.exists(upload.getFile()), "the parts went to the file manager");
		assertEquals(200, send("HEAD", url, Map.of(), null));
		assertEquals(fileName, header(ResumableUploadResource.FILE_NAME_HEADER),
			"for a field whose answer got lost");
		assertEquals(409, patch(url, 50, new byte[0]));

		report("error", "false", "filesInfo", new JSONArray().put(new JSONObject()
			.put("fileName", fileName)
			.put("clientFileName", "notes.txt")
			.put("contentType", "text/plain")).toString());
		assertEquals(1, field.uploaded.size());
		FileUploadToResourceFieldInfo info = field.uploaded.get(0);
		assertEquals("notes.txt", info.clientFileName);
		assertArrayEquals(bytes(50, 0), info.bytes);
	}

	@Test
	void storesTheFileForTheFieldThatCompletesIt()
	{
		String url = create(10, "a.txt");
		assertEquals(204, patch(url, 0, bytes(4, 0)));

		// the page is loaded again: a new field goes on with the upload
		String before = field.getMarkupId();
		field = new TestField("files");
		tester.startPage(new TestPage(field));
		query = queryOf(settings());
		assertEquals(204, patch(url, 4, bytes(6, 4)));
		String fileName = header(ResumableUploadResource.FILE_NAME_HEADER);
		File stored = reference().getFileManager().getFile(field.getMarkupId(), fileName);
		assertTrue(stored.isFile());
		assertFalse(reference().getFileManager().getFile(before, fileName).isFile());
	}

	@Test
	void takesTheFilesOfAClientOfItsOwn()
	{
		query = reference().newQuery("an-app", Bytes.bytes(30));
		assertEquals(413, send("POST", PATH, Map.of("Upload-Length", "31"), null));
		String url = create(30, "a.txt");
		assertEquals(204, patch(url, 0, bytes(30, 0)));
		String fileName = header(ResumableUploadResourceReference.FILE_NAME_HEADER);
		assertTrue(reference().getFileManager().getFile("an-app", fileName).isFile());
		assertThrows(IllegalArgumentException.class,
			() -> reference().newQuery("", Bytes.bytes(1)));
	}

	@Test
	void refusesRequestsWithoutAValidGrant()
	{
		tester.getRequest().setMethod("POST");
		tester.getRequest().setHeader("Upload-Length", "10");
		tester.executeUrl(PATH + "?" + query);
		assertEquals(412, tester.getLastResponse().getStatus(), "without Tus-Resumable");
		assertEquals("1.0.0", header("Tus-Version"));

		assertEquals(403, send("POST", PATH, Map.of("Upload-Length", "10"), null, null));
		assertEquals(ResumableUploadToResourceField.GRANT_REFUSED_RESOURCE_KEY,
			header(ResumableUploadResource.ERROR_HEADER));
		String raised = query.replace("fileMaxSize=", "fileMaxSize=1");
		assertEquals(403,
			send("POST", PATH + "?" + raised, Map.of("Upload-Length", "10"), null, null));
		assertEquals(0, reference().size());
	}

	@Test
	void refusesChunksThatDoNotFollowWhatItHolds()
	{
		String url = create(50, "a.txt");
		assertEquals(409, patch(url, 10, bytes(5, 0)));
		assertEquals("0", header("Upload-Offset"));
		assertEquals(413, patch(url, 0, bytes(41, 0)), "more than a chunk");
		assertEquals(204, patch(url, 0, new byte[0]), "an empty chunk is fine");
		assertEquals("0", header("Upload-Offset"));
		assertEquals(400, send("PATCH", url, Map.of("Upload-Offset", "x"), bytes(5, 0)));
		assertEquals(204, patch(url, 0, bytes(30, 0)));
		assertEquals(400, patch(url, 30, bytes(21, 0)), "beyond the length");
		assertEquals(415, send("PATCH", url + "?" + query, Map.of("Upload-Offset", "30"),
			bytes(5, 0), "text/plain"));
		assertEquals(405, send("PUT", url, Map.of(), null));
		assertEquals("HEAD, PATCH, DELETE, OPTIONS", header("Allow"));
		assertEquals(405, send("GET", PATH, Map.of(), null));
		assertEquals(404, patch(PATH + "/0123456789abcdef0123456789abcdef", 0, bytes(1, 0)));
		assertEquals(404, send("HEAD", PATH + "/not-a-name", Map.of(), null));
	}

	@Test
	void keepsWhatArrivedOfAChunkThatBrokeOff()
	{
		String url = create(50, "a.txt");
		breakAt = 12;
		assertEquals(500, patch(url, 0, bytes(30, 0)));
		assertEquals("12", header("Upload-Offset"));
		breakAt = -1;
		assertEquals(200, send("HEAD", url, Map.of(), null));
		assertEquals("12", header("Upload-Offset"));
		assertEquals(204, patch(url, 12, bytes(38, 12)));
		assertNotNull(header(ResumableUploadResource.FILE_NAME_HEADER));
	}

	@Test
	void takesChunksByPostOverridingTheMethod()
	{
		String url = create(10, "a.txt");
		assertEquals(204, send("POST", url, Map.of("Upload-Offset", "0", "X-HTTP-Method-Override",
			"PATCH"), bytes(10, 0)));
		assertEquals("10", header("Upload-Offset"));
	}

	@Test
	void deletesAnUpload()
	{
		String url = create(50, "a.txt");
		Path file = find(url).getFile();
		assertEquals(204, send("DELETE", url, Map.of(), null));
		assertFalse(Files.exists(file));
		assertEquals(404, send("HEAD", url, Map.of(), null));
		assertEquals(404, send("DELETE", url, Map.of(), null));
	}

	@Test
	void storesAnEmptyFileAtOnce()
	{
		assertEquals(201, send("POST", PATH, Map.of("Upload-Length", "0"), null));
		String fileName = header(ResumableUploadResource.FILE_NAME_HEADER);
		assertTrue(reference().getFileManager().getFile(field.getMarkupId(), fileName).isFile());
		ResumableUpload upload = find(header("Location"));
		assertEquals("upload", upload.getClientFileName());
		assertEquals("application/octet-stream", upload.getContentType());
	}

	@Test
	void refusesFilesThatBreakTheLimits()
	{
		assertEquals(413, send("POST", PATH, Map.of("Upload-Length", "101"), null));
		assertEquals(ResumableUploadToResourceField.UPLOAD_TOO_LARGE_RESOURCE_KEY,
			header(ResumableUploadResource.ERROR_HEADER));

		reference().setMaxBytesPerOwner(Bytes.bytes(150));
		create(100, "a");
		assertEquals(413, send("POST", PATH, Map.of("Upload-Length", "60"), null));
		assertEquals(ResumableUploadToResourceField.TOO_MUCH_RESOURCE_KEY,
			header(ResumableUploadResource.ERROR_HEADER));
		reference().setMaxBytesPerOwner(null);
		assertEquals(400, reference().getMaxBytesPerOwner().bytes());

		reference().setMaxTotalBytes(Bytes.bytes(20));
		assertEquals(507, send("POST", PATH, Map.of("Upload-Length", "30"), null),
			"a file that could not fit now");
		assertEquals(ResumableUploadToResourceField.NO_ROOM_RESOURCE_KEY,
			header(ResumableUploadResource.ERROR_HEADER));
		reference().setMaxTotalBytes(null);
		assertEquals(4000, reference().getMaxTotalBytes().bytes(),
			"ten times what an owner may have");
		reference().setMaxBytesPerOwner(Bytes.bytes(50));
		assertEquals(500, reference().getMaxTotalBytes().bytes());
		reference().setMaxBytesPerOwner(null);

		reference().setMaxUploadsPerOwner(2);
		create(1, "b");
		assertEquals(403, send("POST", PATH, Map.of("Upload-Length", "1"), null));
		assertEquals(ResumableUploadToResourceField.TOO_MANY_UPLOADS_RESOURCE_KEY,
			header(ResumableUploadResource.ERROR_HEADER));

		assertEquals(400, send("POST", PATH, Map.of("Upload-Length", "-1"), null));
		assertEquals(400, send("POST", PATH, Map.of(), null));
		assertEquals(400, send("POST", PATH, Map.of("Upload-Defer-Length", "1"), null));
		assertEquals(400, send("POST", PATH, Map.of("Upload-Length", "1", "Upload-Metadata",
			"name !!!"), null));
	}

	@Test
	void refusesFilesLargerThanTheFieldAllows()
	{
		field = new TestField("files");
		field.setFileMaxSize(Bytes.bytes(20));
		tester.startPage(new TestPage(field));
		JSONObject settings = settings();
		assertEquals(20, settings.getLong("fileMaxSize"));
		query = queryOf(settings);
		assertEquals(413, send("POST", PATH, Map.of("Upload-Length", "21"), null));
		create(20, "a");
	}

	@Test
	void leavesRoomOnTheDisk()
	{
		reference().setMinFreeSpace(Bytes.bytes(Long.MAX_VALUE / 2));
		assertEquals(507, send("POST", PATH, Map.of("Upload-Length", "1"), null));
	}

	@Test
	void createsNoUploadWithoutASession()
	{
		tester.getSession().invalidateNow();
		assertEquals(403, send("POST", PATH, Map.of("Upload-Length", "1"), null));
	}

	@Test
	void keepsTheUploadsOfASessionForIt()
	{
		String url = create(10, "a.txt");
		String owner = tester.getSession().getId();
		assertNotNull(reference().find(idOf(url), owner));
		assertNull(reference().find(idOf(url), "another session"));
		assertNull(reference().find(null, owner));

		Path file = find(url).getFile();
		tester.getApplication()
			.getSessionStore()
			.getUnboundListener()
			.forEach(listener -> listener.sessionUnbound(owner));
		assertNull(reference().find(idOf(url), owner));
		assertFalse(Files.exists(file));
	}

	@Test
	void triesAgainForAsLongAsItIsTold()
	{
		assertEquals(List.of(0L, 1000L, 3000L, 1000L),
			ResumableUploadToResourceField.retryDelaysOf(Duration.ofSeconds(5)));
		assertEquals(List.of(0L, 1000L, 3000L, 5000L, 10000L, 20000L, 30000L, 30000L, 1000L),
			ResumableUploadToResourceField.retryDelaysOf(Duration.ofSeconds(100)));
		assertEquals(List.of(), ResumableUploadToResourceField.retryDelaysOf(Duration.ZERO),
			"lost at the first request without an answer");
		assertThrows(IllegalArgumentException.class,
			() -> field.setRetryTimeout(Duration.ofSeconds(-1)));

		field.setRetryTimeout(Duration.ofMinutes(2));
		tester.startPage(tester.getLastRenderedPage());
		long total = 0;
		JSONArray delays = settings().getJSONArray("retryDelays");
		for (int index = 0; index < delays.length(); index++)
		{
			total += delays.getLong(index);
		}
		assertEquals(Duration.ofMinutes(2).toMillis(), total);
	}

	@Test
	void takesTheTimingsOfItsUploads()
	{
		assertEquals(List.of(5000L, 10000L), ResumableUploadToResourceField.reconnectCheckDelaysOf(Duration.ofSeconds(10)));
		assertEquals(List.of(2000L), ResumableUploadToResourceField.reconnectCheckDelaysOf(Duration.ofSeconds(2)));
		assertEquals(List.of(5000L, 10000L, 20000L, 60000L),
			ResumableUploadToResourceField.reconnectCheckDelaysOf(Duration.ofMinutes(1)));
		assertThrows(IllegalArgumentException.class,
			() -> field.setReconnectCheckInterval(Duration.ZERO));
		assertThrows(IllegalArgumentException.class,
			() -> field.setStallTimeout(Duration.ofSeconds(-1)));
		assertThrows(IllegalArgumentException.class, () -> field.setInUseTimeout(Duration.ZERO));

		field.setReconnectCheckInterval(Duration.ofSeconds(10))
			.setStallTimeout(Duration.ZERO)
			.setInUseTimeout(Duration.ofMinutes(1))
			.setRecalling(false);
		tester.startPage(tester.getLastRenderedPage());
		JSONObject settings = settings();
		assertEquals("[5000,10000]", settings.getJSONArray("probeDelays").toString());
		assertEquals(0, settings.getLong("stallMillis"), "never given up");
		assertEquals(60000, settings.getLong("inUseMillis"));
		assertFalse(settings.getBoolean("recalling"));
	}

	@Test
	void keepsTheUploadsOfAnOwnerOtherThanTheSessionForLonger()
	{
		reference().setOwnerExpiry(Duration.ofDays(1));
		String ofSession = create(10, "a.txt");
		assertEquals(204, patch(ofSession, 0, bytes(4, 0)));
		owner = "user-1";
		renderAgain();
		String ofUser = create(10, "b.txt");
		assertEquals(204, patch(ofUser, 0, bytes(4, 0)));
		assertEquals("Wed, 25 Nov 2026 10:00:00 GMT", header("Upload-Expires"));
		clock.add(Duration.ofHours(2));
		reference().deleteExpired(true);
		assertNull(reference().find(idOf(ofSession), tester.getSession().getId()));
		assertNotNull(reference().find(idOf(ofUser), "user-1"));
		clock.add(Duration.ofDays(1));
		reference().deleteExpired(true);
		assertNull(reference().find(idOf(ofUser), "user-1"));
		assertThrows(IllegalArgumentException.class,
			() -> reference().setOwnerExpiry(Duration.ZERO));
		reference().setOwnerExpiry(null);
		assertEquals(reference().getExpiry(), reference().getOwnerExpiry());
	}

	@Test
	void looksForExpiredUploadsAsOftenAsItIsTold()
	{
		String url = create(10, "a.txt");
		reference().setExpiryCheckInterval(Duration.ofHours(1));
		reference().deleteExpired(true);
		clock.add(Duration.ofMinutes(31));
		reference().deleteExpired(false);
		assertNotNull(find(url), "looked less than an hour ago");
		reference().setExpiryCheckInterval(Duration.ZERO);
		reference().deleteExpired(false);
		assertNull(find(url));
	}

	@Test
	void refusesMetadataLongerThanItTakes()
	{
		reference().setMaxMetadataLength(20);
		assertEquals(400, send("POST", PATH, Map.of("Upload-Length", "10",
			"Upload-Metadata", "filename " + "x".repeat(20)), null));
		assertEquals(201, send("POST", PATH, Map.of("Upload-Length", "10",
			"Upload-Metadata", "filename eHg="), null));
		assertThrows(IllegalArgumentException.class, () -> reference().setMaxMetadataLength(0));
	}

	@Test
	void waitsForAnotherRequestWritingTheUploadAsLongAsItIsTold() throws Exception
	{
		String url = create(10, "a.txt");
		ResumableUpload upload = find(url);
		java.util.concurrent.CountDownLatch locked = new java.util.concurrent.CountDownLatch(1);
		java.util.concurrent.CountDownLatch done = new java.util.concurrent.CountDownLatch(1);
		Thread writer = new Thread(() -> {
			upload.lock.lock();
			try
			{
				locked.countDown();
				done.await();
			}
			catch (InterruptedException e)
			{
				Thread.currentThread().interrupt();
			}
			finally
			{
				upload.lock.unlock();
			}
		});
		writer.start();
		locked.await();
		reference().setLockTimeout(Duration.ofMillis(10));
		try
		{
			assertEquals(423, patch(url, 0, bytes(4, 0)));
		}
		finally
		{
			done.countDown();
			writer.join();
		}
		assertEquals(204, patch(url, 0, bytes(4, 0)));
		assertThrows(IllegalArgumentException.class,
			() -> reference().setLockTimeout(Duration.ofSeconds(-1)));
	}

	@Test
	void keepsTheUploadsOfAnOwnerBeyondItsSession()
	{
		owner = "user-1";
		renderAgain();
		String url = create(10, "a.txt");
		assertEquals(204, patch(url, 0, bytes(4, 0)));
		String session = tester.getSession().getId();
		String before = query;
		tester.getApplication()
			.getSessionStore()
			.getUnboundListener()
			.forEach(listener -> listener.sessionUnbound(session));
		assertNotNull(reference().find(idOf(url), "user-1"), "outlives the session");

		// the user signs in again, in a new session, and the page is loaded again
		tester.getSession().invalidateNow();
		field = new TestField("files");
		tester.startPage(new TestPage(field));
		assertNotEquals(session, tester.getSession().getId());
		String after = queryOf(settings());
		query = before;
		assertEquals(403, send("HEAD", url, Map.of(), null), "the page of the old session");
		query = after;
		assertEquals(200, send("HEAD", url, Map.of(), null));
		assertEquals("4", header("Upload-Offset"));
		owner = "user-2";
		assertEquals(403, send("HEAD", url, Map.of(), null), "the grant of another user");
		owner = "user-1";
		assertEquals(204, patch(url, 4, bytes(6, 4)));
		String fileName = header(ResumableUploadResource.FILE_NAME_HEADER);
		assertTrue(reference().getFileManager().getFile(field.getMarkupId(), fileName).isFile());

		// the upload of the first user is not one of the second, though both know its address
		String other = create(10, "b.txt");
		owner = "user-2";
		renderAgain();
		assertEquals(404, send("HEAD", other, Map.of(), null), "another user does not find it");
	}

	@Test
	void refusesTheGrantOfAnEndedSessionByDefault()
	{
		owner = "user-1";
		renderAgain();
		String url = create(10, "a.txt");
		String before = query;
		newSession();
		query = before;
		assertEquals(403, send("HEAD", url, Map.of(), null),
			"signed with the key of the old session");
		assertEquals(ResumableUploadToResourceField.GRANT_REFUSED_RESOURCE_KEY,
			header(ResumableUploadResource.ERROR_HEADER));
		assertEquals(403, send("POST", PATH, Map.of("Upload-Length", "1"), null));
	}

	@Test
	void takesTheGrantOfAnEndedSessionSignedWithAKeyOfTheApplication()
	{
		reference().setGrantSigner(HmacGrantSigner.random());
		owner = "user-1";
		renderAgain();
		String url = create(10, "a.txt");
		assertEquals(204, patch(url, 0, bytes(4, 0)));
		String before = query;
		newSession();
		query = before;
		assertEquals(200, send("HEAD", url, Map.of(), null), "the page of the ended session");
		assertEquals("4", header("Upload-Offset"));
		assertEquals(204, patch(url, 4, bytes(6, 4)));
		assertNotNull(header(ResumableUploadResource.FILE_NAME_HEADER));
		String other = create(5, "b.txt");

		owner = "user-2";
		assertEquals(403, send("HEAD", other, Map.of(), null), "the grant of another owner");
		assertEquals(ResumableUploadToResourceField.GRANT_REFUSED_RESOURCE_KEY,
			header(ResumableUploadResource.ERROR_HEADER));
		assertEquals(403, send("POST", PATH, Map.of("Upload-Length", "1"), null));
	}

	@Test
	void refusesAGrantChangedOrSignedWithAnotherKey()
	{
		reference().setGrantSigner(HmacGrantSigner.random());
		renderAgain();
		String signed = query;
		query = signed.replace("fileMaxSize=", "fileMaxSize=1");
		assertEquals(403, send("POST", PATH, Map.of("Upload-Length", "1"), null), "a larger size");
		query = signed.replaceAll("grantExpires=\\d+", "grantExpires=" + Long.MAX_VALUE);
		assertEquals(403, send("POST", PATH, Map.of("Upload-Length", "1"), null), "a later expiry");
		query = signed.replaceAll("&grantExpires=\\d+", "");
		assertEquals(403, send("POST", PATH, Map.of("Upload-Length", "1"), null), "no expiry");
		query = signed.replace("uploadToken=", "uploadToken=A");
		assertEquals(403, send("POST", PATH, Map.of("Upload-Length", "1"), null), "another token");
		reference().setGrantSigner(HmacGrantSigner.random());
		query = signed;
		assertEquals(403, send("POST", PATH, Map.of("Upload-Length", "1"), null), "another key");
		assertEquals(0, reference().size());
	}

	@Test
	void startsNoUploadOnceTheGrantExpired()
	{
		reference().setGrantExpiry(Duration.ofMinutes(5));
		renderAgain();
		String url = create(10, "a.txt");
		assertEquals(204, patch(url, 0, bytes(4, 0)));
		clock.add(Duration.ofMinutes(6));
		assertEquals(403, send("POST", PATH, Map.of("Upload-Length", "1"), null));
		assertEquals(ResumableUploadToResourceField.GRANT_REFUSED_RESOURCE_KEY,
			header(ResumableUploadResource.ERROR_HEADER));
		assertEquals(200, send("HEAD", url, Map.of(), null), "the upload started goes on");
		assertEquals(204, patch(url, 4, bytes(6, 4)));
		assertNotNull(header(ResumableUploadResource.FILE_NAME_HEADER));
		assertEquals(403, send("HEAD", PATH + "/0123456789abcdef0123456789abcdef", Map.of(), null),
			"not told whether the server has another one");

		assertEquals(Duration.ofMinutes(5), reference().getGrantExpiry());
		reference().setGrantExpiry(null);
		assertNull(reference().getGrantExpiry(), "the key of the grant ends with the session");
		reference().setGrantSigner(HmacGrantSigner.inSession());
		assertNull(reference().getGrantExpiry());
		reference().setGrantSigner(HmacGrantSigner.random());
		assertEquals(reference().getOwnerExpiry(), reference().getGrantExpiry());
		reference().setOwnerExpiry(Duration.ofDays(1));
		assertEquals(Duration.ofDays(1), reference().getGrantExpiry());
		assertThrows(IllegalArgumentException.class,
			() -> reference().setGrantExpiry(Duration.ZERO));
	}

	@Test
	void signsWithAKeyOfAtLeast32Bytes()
	{
		HmacGrantSigner signer = HmacGrantSigner.random();
		String signature = signer.sign("a text");
		assertTrue(signer.verify("a text", signature));
		assertFalse(signer.verify("another text", signature));
		assertFalse(signer.verify("a text", signature + "A"));
		assertFalse(signer.verify("a text", "not base64!"));
		assertFalse(signer.verify("a text", null));
		assertFalse(HmacGrantSigner.random().verify("a text", signature), "another key");
		byte[] key = new byte[32];
		assertEquals(new HmacGrantSigner(() -> key).sign("a text"),
			new HmacGrantSigner(() -> key.clone()).sign("a text"),
			"the same key, the same signature");
		assertThrows(IllegalStateException.class,
			() -> new HmacGrantSigner(() -> new byte[31]).sign("a text"));
		assertThrows(IllegalArgumentException.class, () -> new HmacGrantSigner(null));
	}

	@Test
	void signsWithTheKeyOfTheSession()
	{
		IUploadGrantSigner signer = HmacGrantSigner.inSession();
		String signature = signer.sign("a text");
		assertTrue(signer.verify("a text", signature));
		assertEquals(signature, signer.sign("a text"), "the key is made once");
		assertFalse(signer.verify("another text", signature));
		assertFalse(signer.verify("a text", "garbage"));
		newSession();
		assertFalse(signer.verify("a text", signature), "the key of another session");
		assertNotEquals(signature, signer.sign("a text"), "the key of another session");
		assertTrue(reference().getGrantSigner() instanceof HmacGrantSigner,
			"a key of the session by default");
		assertThrows(IllegalArgumentException.class, () -> reference().setGrantSigner(null));
	}

	@Test
	void deletesTheUploadsTheBrowserLeft()
	{
		String url = create(10, "a.txt");
		assertEquals(204, patch(url, 0, bytes(4, 0)));
		ResumableUpload upload = find(url);
		clock.add(Duration.ofSeconds(29 * 60 + 30));
		reference().deleteExpired(true);
		assertNotNull(find(url));
		clock.add(Duration.ofSeconds(40));
		// expired, but looked a moment ago: not again yet
		reference().deleteExpired(false);
		assertNotNull(find(url));
		reference().deleteExpired(true);
		assertNull(find(url));
		assertFalse(Files.exists(upload.getFile()));
		assertThrows(IllegalArgumentException.class, () -> reference().setExpiry(Duration.ZERO));
	}

	@Test
	void tellsItsTrackerOfTheUploads()
	{
		List<String> events = new ArrayList<>();
		reference().setUploadsTracker(new IUploadsTracker()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onCreated(String context, TrackedUpload upload)
			{
				events.add("created " + upload.clientFileName() + " " + upload.offset());
			}

			@Override
			public void onProgress(String context, TrackedUpload upload)
			{
				events.add("progress " + upload.offset());
			}

			@Override
			public void onStored(String context, TrackedUpload upload)
			{
				events.add("stored " + upload.clientFileName() + " for the field " +
					upload.storedFieldId().equals(field.getMarkupId()));
			}

			@Override
			public void onDeleted(String context, TrackedUpload upload)
			{
				events.add("deleted " + upload.clientFileName());
			}

			@Override
			public void destroy()
			{
				events.add("destroyed");
			}

			@Override
			public List<TrackedUpload> getUploads(String context, String owner)
			{
				return List.of();
			}

			@Override
			public void delete(String context, String owner, String uploadId)
			{
			}
		});
		String url = create(10, "a.txt");
		assertEquals(204, patch(url, 0, bytes(4, 0)));
		assertEquals(204, patch(url, 4, bytes(6, 4)));
		assertEquals(List.of("created a.txt 0", "progress 4", "progress 10",
			"stored a.txt for the field true"), events);

		events.clear();
		String other = create(10, "b.txt");
		assertEquals(204, send("DELETE", other, Map.of(), null));
		assertEquals(List.of("created b.txt 0", "deleted b.txt"), events);

		events.clear();
		String expiring = create(10, "c.txt");
		clock.add(Duration.ofHours(1));
		reference().deleteExpired(true);
		assertNull(find(expiring));
		assertEquals(Set.of("created c.txt 0", "deleted a.txt", "deleted c.txt"), Set.copyOf(events),
			"the stored upload expires too, its file stays with the file manager");

		events.clear();
		renderAgain();
		create(10, "d.txt");
		tester.getApplication()
			.getApplicationListeners()
			.onBeforeDestroyed(tester.getApplication());
		assertEquals(List.of("created d.txt 0", "deleted d.txt", "destroyed"), events,
			"the uploads deleted, then the tracker destroyed");
	}

	@Test
	void destroysItsTrackerWhenTheApplicationStops()
	{
		List<String> events = new ArrayList<>();
		reference().setUploadsTracker(new IUploadsTracker()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public List<TrackedUpload> getUploads(String context, String owner)
			{
				return List.of();
			}

			@Override
			public void delete(String context, String owner, String uploadId)
			{
			}

			@Override
			public void destroy()
			{
				events.add("destroyed");
			}
		});
		tester.getApplication()
			.getApplicationListeners()
			.onBeforeDestroyed(tester.getApplication());
		assertEquals(List.of("destroyed"), events, "though no upload came");
	}

	@Test
	void goesOnWhenItsTrackerFails()
	{
		reference().setUploadsTracker(new IUploadsTracker()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onCreated(String context, TrackedUpload upload)
			{
				throw new IllegalStateException("the database is down");
			}

			@Override
			public void onProgress(String context, TrackedUpload upload)
			{
				throw new IllegalStateException("the database is down");
			}

			@Override
			public List<TrackedUpload> getUploads(String context, String owner)
			{
				return List.of();
			}

			@Override
			public void delete(String context, String owner, String uploadId)
			{
			}
		});
		String url = create(10, "a.txt");
		assertEquals(204, patch(url, 0, bytes(10, 0)));
		assertNotNull(header(ResumableUploadResource.FILE_NAME_HEADER));
		assertThrows(IllegalArgumentException.class, () -> reference().setUploadsTracker(null));
	}

	@Test
	void offersTheUploadsTheServerKeepsForThePlaceOfTheField()
	{
		String place = field.getRememberedAs();
		String url = createAt(10, "a.txt", place, 1000);
		assertEquals(204, patch(url, 0, bytes(4, 0)));
		clock.add(Duration.ofMillis(1));
		createAt(10, "b.txt", "another place", 1000);
		clock.add(Duration.ofMillis(1));
		String whole = createAt(5, "c.txt", place, 1000);
		assertEquals(204, patch(whole, 0, bytes(5, 0)));

		List<TrackedUpload> uploads = reference().getUploads(null, tester.getSession().getId());
		assertEquals(List.of("a.txt", "b.txt"),
			uploads.stream().map(TrackedUpload::clientFileName).toList(),
			"the oldest first, and not the stored one");
		TrackedUpload first = uploads.get(0);
		assertEquals(place, first.place());
		assertEquals(1000, first.lastModified());
		assertEquals(4, first.offset());
		assertFalse(first.isStored());
		assertEquals(List.of(), reference().getUploads(null, "another owner"));
		assertTrue(reference().getUploadsTracker().getUploads(null, tester.getSession().getId())
			.stream().anyMatch(upload -> upload.id().equals(first.id())), "the tracker by default");

		field = new TestField("files");
		field.setRemembering(true);
		tester.startPage(new TestPage(field));
		JSONArray recalled = settings().getJSONArray("recalled");
		assertEquals(1, recalled.length(), "unfinished, and started in the place of the field");
		JSONObject offered = recalled.getJSONObject(0);
		assertEquals(url.substring(url.lastIndexOf('/') + 1), offered.getString("id"));
		assertEquals("a.txt", offered.getString("name"));
		assertEquals(10, offered.getLong("size"));
		assertEquals(1000, offered.getLong("lastModified"));
		assertEquals("text/plain", offered.getString("type"));
		assertEquals(4, offered.getLong("sent"));

		field = new TestField("files");
		field.setRemembering(true);
		field.setRecalling(false);
		tester.startPage(new TestPage(field));
		assertEquals(0, settings().getJSONArray("recalled").length(), "when not recalling");
	}

	@Test
	void keepsTheUploadsOfEachContextApart()
	{
		field = new TestField("files");
		field.setUploadContext("videos");
		tester.startPage(new TestPage(field));
		query = queryOf(settings());
		assertTrue(query.contains("uploadContext=videos"), query);
		String video = createAt(10, "film.mp4", "a page", 1000);
		assertEquals(403, send("POST", PATH + "?" + query.replace("uploadContext=videos",
			"uploadContext=attachments"), Map.of("Upload-Length", "1"), null, null),
			"a context the grant does not sign");
		assertEquals(403, send("POST", PATH + "?" + query.replace("&uploadContext=videos", ""),
			Map.of("Upload-Length", "1"), null, null), "no context");

		String session = tester.getSession().getId();
		assertEquals(List.of("videos"), reference().getUploads("videos", session)
			.stream()
			.map(TrackedUpload::context)
			.toList());
		assertEquals(List.of(), reference().getUploads("attachments", session));
		renderAgain();
		String attachment = create(5, "a.txt");
		assertNull(reference().getUploads(null, session)
			.stream()
			.filter(upload -> upload.id().equals(idOf(attachment)))
			.findFirst()
			.orElseThrow()
			.context(), "a field without");
		assertEquals(2, reference().getUploads(null, session).size(), "every context");

		field = new TestField("files");
		field.setRemembering(true);
		field.setUploadContext("videos");
		tester.startPage(new TestPage(field));
		JSONArray recalled = settings().getJSONArray("recalled");
		assertEquals(1, recalled.length(), "the video, started elsewhere");
		assertEquals("film.mp4", recalled.getJSONObject(0).getString("name"));
		assertEquals("videos", settings().getString("uploadContext"));

		reference().getUploadsTracker().delete("videos", session, idOf(video));
		assertNull(find(video), "deleted by the tracker by default");
		assertFalse(reference().deleteUpload(session, idOf(video)), "deleted already");
		assertFalse(reference().deleteUpload("another owner", idOf(attachment)), "of another owner");
		assertTrue(reference().deleteUpload(session, idOf(attachment)));
		assertNull(find(attachment));
		assertThrows(IllegalArgumentException.class, () -> field.setUploadContext("two\nlines"));
	}

	@Test
	void deletesItsPartsWhenTheApplicationStops() throws IOException
	{
		String url = create(10, "a.txt");
		Path file = find(url).getFile();
		Path other = Files.writeString(file.resolveSibling("notes.txt"), "kept");
		tester.getApplication()
			.getApplicationListeners()
			.onBeforeDestroyed(tester.getApplication());
		assertFalse(Files.exists(file));
		assertTrue(Files.exists(other), "not a part of an upload");
		assertEquals(0, reference().size());
	}

	@Test
	void refusesFinallyAFileTheFileManagerCannotStore()
	{
		String url = create(10, "a.txt");
		failingToStore = true;
		assertEquals(500, patch(url, 0, bytes(10, 0)));
		assertEquals(FileUploadToResourceField.UPLOAD_FAILED_RESOURCE_KEY,
			header(ResumableUploadResource.ERROR_HEADER), "which the script does not try again");
		assertNull(find(url), "deleted with its bytes");
		assertEquals(0, reference().heldBytes());
		assertEquals(404, send("HEAD", url, Map.of(), null));
	}

	@Test
	void holdsEveryRequestOfAnUploadToTheGrantItCarries()
	{
		String url = create(50, "a.txt");
		assertEquals(204, patch(url, 0, bytes(10, 0)));

		field = new TestField("files");
		field.setFileMaxSize(Bytes.bytes(20));
		tester.startPage(new TestPage(field));
		query = queryOf(settings());
		assertEquals(413, send("HEAD", url, Map.of(), null), "the file is too large for the field");
		assertEquals(ResumableUploadToResourceField.UPLOAD_TOO_LARGE_RESOURCE_KEY,
			header(ResumableUploadResource.ERROR_HEADER));
		assertEquals(413, patch(url, 10, bytes(10, 10)));
		assertEquals(10, find(url).getOffset(), "nothing appended");

		field = new TestField("files");
		field.setUploadContext("avatars");
		tester.startPage(new TestPage(field));
		query = queryOf(settings());
		assertEquals(404, send("HEAD", url, Map.of(), null), "none of its context");
		assertEquals(404, patch(url, 10, bytes(10, 10)));
		assertEquals(404, send("DELETE", url, Map.of(), null));
		String avatar = create(10, "b.txt");

		renderAgain();
		assertEquals(404, send("HEAD", avatar, Map.of(), null), "a field without a context");
		field = new TestField("files");
		field.setFileMaxSize(Bytes.bytes(20));
		tester.startPage(new TestPage(field));
		query = queryOf(settings());
		assertEquals(204, send("DELETE", url, Map.of(), null), "deleted whatever its size");
		assertNull(find(url));
	}

	@Test
	void offersNoUploadLargerThanTheFieldTakes()
	{
		String place = field.getRememberedAs();
		createAt(50, "large.txt", place, 1000);
		createAt(10, "small.txt", place, 1000);
		field = new TestField("files");
		field.setRemembering(true);
		field.setFileMaxSize(Bytes.bytes(20));
		tester.startPage(new TestPage(field));
		JSONArray recalled = settings().getJSONArray("recalled");
		assertEquals(1, recalled.length());
		assertEquals("small.txt", recalled.getJSONObject(0).getString("name"));
	}

	@Test
	void countsTheBytesOfAllOwnersAsTheyArrive()
	{
		reference().setMaxTotalBytes(Bytes.bytes(60));
		String first = create(50, "a.txt");
		String second = create(50, "b.txt");
		assertEquals(0, reference().heldBytes(), "uploads created and not sent take no room");
		assertEquals(204, patch(first, 0, bytes(40, 0)));
		assertEquals(40, reference().heldBytes());
		assertEquals(507, patch(second, 0, bytes(30, 0)), "more than the room left");
		assertEquals(ResumableUploadToResourceField.NO_ROOM_RESOURCE_KEY,
			header(ResumableUploadResource.ERROR_HEADER));
		assertEquals("0", header("Upload-Offset"));
		assertEquals(40, reference().heldBytes(), "the room reserved given back");
		assertEquals(204, patch(second, 0, bytes(20, 0)));
		assertEquals(204, send("DELETE", first, Map.of(), null));
		assertEquals(20, reference().heldBytes());
		assertEquals(204, patch(second, 20, bytes(30, 20)));
		assertNotNull(header(ResumableUploadResource.FILE_NAME_HEADER));
		assertEquals(0, reference().heldBytes(), "the file went to the file manager");

		String broken = create(50, "c.txt");
		breakAt = 12;
		assertEquals(500, patch(broken, 0, bytes(30, 0)));
		assertEquals(12, reference().heldBytes(), "what arrived of a chunk that broke off");

		reference().setMinFreeSpace(Bytes.bytes(Long.MAX_VALUE / 2));
		breakAt = -1;
		assertEquals(507, patch(broken, 12, bytes(10, 12)), "checked as each chunk arrives");
	}

	@Test
	void keepsUploadsThatHoldNoBytesBriefly()
	{
		String empty = create(10, "a.txt");
		String started = create(10, "b.txt");
		assertEquals(204, patch(started, 0, bytes(1, 0)));
		clock.add(Duration.ofMinutes(5));
		reference().deleteExpired(true);
		assertNull(find(empty), "created and never sent");
		assertNotNull(find(started));

		reference().setEmptyExpiry(Duration.ofHours(1));
		assertEquals(Duration.ofHours(1), reference().getEmptyExpiry());
		String later = create(10, "c.txt");
		clock.add(Duration.ofMinutes(31));
		reference().deleteExpired(true);
		assertNull(find(later), "no longer than the expiry");
		assertThrows(IllegalArgumentException.class,
			() -> reference().setEmptyExpiry(Duration.ZERO));
	}

	@Test
	void remembersAsManyStoredUploadsOfAnOwnerAsItRunsAtMost()
	{
		reference().setMaxUploadsPerOwner(2);
		List<String> urls = new ArrayList<>();
		for (int index = 0; index < 3; index++)
		{
			assertEquals(201, send("POST", PATH, Map.of("Upload-Length", "0"), null));
			urls.add(header("Location"));
			clock.add(Duration.ofSeconds(1));
		}
		assertEquals(2, reference().size());
		assertEquals(404, send("HEAD", urls.get(0), Map.of(), null), "the oldest forgotten");
		assertEquals(200, send("HEAD", urls.get(2), Map.of(), null));
		assertNotNull(header(ResumableUploadResource.FILE_NAME_HEADER));
		assertEquals(List.of(), reference().getUploads(null, tester.getSession().getId()),
			"not listed once stored");
	}

	@Test
	void tellsTheBrowserToAskAgainWhileTheFileIsStored()
	{
		String url = create(10, "a.txt");
		ResumableUpload upload = find(url);
		upload.setOffset(10, clock.instant());
		assertEquals(423, send("HEAD", url, Map.of(), null), "complete, not yet stored");
		assertNull(header("Upload-Offset"));
	}

	@Test
	void deletesTheUploadsOfAnEndedSessionOnceTheRequestWritingThemIsDone() throws Exception
	{
		List<String> events = new ArrayList<>();
		reference().setUploadsTracker(new IUploadsTracker()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onDeleted(String context, TrackedUpload upload)
			{
				events.add("deleted " + upload.clientFileName());
			}

			@Override
			public void destroy()
			{
				events.add("destroyed");
			}

			@Override
			public List<TrackedUpload> getUploads(String context, String owner)
			{
				return List.of();
			}

			@Override
			public void delete(String context, String owner, String uploadId)
			{
			}
		});
		String url = create(10, "a.txt");
		assertEquals(204, patch(url, 0, bytes(4, 0)));
		ResumableUpload upload = find(url);
		String session = tester.getSession().getId();
		Writer writer = new Writer(upload);
		try
		{
			tester.getApplication()
				.getSessionStore()
				.getUnboundListener()
				.forEach(listener -> listener.sessionUnbound(session));
			assertNotNull(find(url), "not under the request writing it");
			assertTrue(Files.exists(upload.getFile()));
			assertEquals(List.of(), events);
		}
		finally
		{
			writer.done();
		}
		reference().endIfEnding(upload);
		assertNull(find(url), "once the request is done");
		assertFalse(Files.exists(upload.getFile()));
		assertEquals(List.of("deleted a.txt"), events);
		assertEquals(0, reference().heldBytes());

		renderAgain();
		String other = create(10, "b.txt");
		ResumableUpload stopping = find(other);
		writer = new Writer(stopping);
		try
		{
			tester.getApplication()
				.getApplicationListeners()
				.onBeforeDestroyed(tester.getApplication());
			assertTrue(Files.exists(stopping.getFile()), "not under the request writing it");
		}
		finally
		{
			writer.done();
		}
		reference().endIfEnding(stopping);
		assertFalse(Files.exists(stopping.getFile()));
		assertEquals(List.of("deleted a.txt", "destroyed"), events,
			"the tracker hears nothing once destroyed");
	}

	@Test
	void sweepsAnUploadThatEndedWhileARequestWroteIt() throws Exception
	{
		String url = create(10, "a.txt");
		assertEquals(204, patch(url, 0, bytes(4, 0)));
		ResumableUpload upload = find(url);
		String session = tester.getSession().getId();
		Writer writer = new Writer(upload);
		try
		{
			tester.getApplication()
				.getSessionStore()
				.getUnboundListener()
				.forEach(listener -> listener.sessionUnbound(session));
		}
		finally
		{
			writer.done();
		}
		assertNotNull(find(url));
		reference().deleteExpired(true);
		assertNull(find(url), "though it did not expire");
	}

	@Test
	void tellsTheAddressOfANewUploadRelativeToTheEndpoint()
	{
		assertEquals(201, send("POST", PATH, Map.of("Upload-Length", "10"), null));
		String location = header("Location");
		assertTrue(location.matches(PATH + "/[0-9a-f]{32}"), location);
	}

	@Test
	void takesTheDeletionOfAnUploadByPostOverridingTheMethod()
	{
		String url = create(10, "a.txt");
		assertEquals(204, send("POST", url, Map.of("X-HTTP-Method-Override", "DELETE"), null));
		assertNull(find(url));
	}

	@Test
	void overridesTheMethodWhenToldTo()
	{
		field.setOverridingMethod(true);
		tester.startPage(tester.getLastRenderedPage());
		assertTrue(settings().getBoolean("overridingMethod"));
		assertTrue(field.isOverridingMethod());
	}

	@Test
	void holdsUploadsToTheLimitsItsGettersReturn()
	{
		overriddenMaxFileSize = Bytes.bytes(200);
		overriddenMaxUploadsPerOwner = 2;
		renderAgain();
		assertEquals(200, settings().getLong("fileMaxSize"));
		assertEquals(800, reference().getMaxBytesPerOwner().bytes(), "four files at most");
		create(150, "a.txt");
		assertEquals(413, send("POST", PATH, Map.of("Upload-Length", "201"), null));
		create(10, "b.txt");
		assertEquals(403, send("POST", PATH, Map.of("Upload-Length", "1"), null));
		assertEquals(ResumableUploadToResourceField.TOO_MANY_UPLOADS_RESOURCE_KEY,
			header(ResumableUploadResource.ERROR_HEADER));
	}

	@Test
	void wordsTheErrorsOfItsUploads()
	{
		report("error", "true", "errorMessage",
			ResumableUploadToResourceField.TOO_MANY_UPLOADS_RESOURCE_KEY);
		assertEquals(List.of(
			"Too many uploads at once: at most 10. Wait until one ends, or cancel one."),
			field.failures);
		report("error", "true", "errorMessage", "uploadSingleFileTooLarge");
		assertEquals("Each of files must be less than 100 bytes.", field.failures.get(1));
		report("error", "true", "errorMessage", "${anything}");
		assertEquals("The upload failed.", field.failures.get(2));
		report("error", "true", "errorMessage", "upload.canceled");
		assertEquals(1, field.canceled);
	}

	@Test
	void findsTheReferenceOfTheApplication()
	{
		assertSame(reference(), field.getResumableUploadResourceReference());
		WicketTester other = new WicketTester(new MockApplication());
		try
		{
			assertThrows(IllegalStateException.class,
				ResumableUploadResourceReference::getInstance);
		}
		finally
		{
			other.destroy();
		}
	}

	@Test
	void readsTheHeadersOfTus()
	{
		assertEquals(Map.of(), ResumableUploadResource.metadataOf(null, 4096));
		assertEquals(Map.of("a", "", "b", "ü"),
			ResumableUploadResource.metadataOf("a,b w7w=", 4096));
		assertNull(ResumableUploadResource.metadataOf("a x y", 4096));
		assertNull(ResumableUploadResource.metadataOf("a,a", 4096));
		assertNull(ResumableUploadResource.metadataOf("x".repeat(5000), 4096));
		assertNull(ResumableUploadResource.metadataOf("a,b w7w=", 7), "too long");
		assertEquals(12, ResumableUploadResource.number("12"));
		assertEquals(-1, ResumableUploadResource.number("+12"));
		assertEquals(-1, ResumableUploadResource.number("9999999999999999999"));
		assertEquals(-1, ResumableUploadResource.number(""));
		assertEquals("upload", ResumableUpload.clientFileNameOf(".."));
		assertEquals("b.txt", ResumableUpload.clientFileNameOf("a\\b.txt"));
		assertEquals("ab.txt", ResumableUpload.clientFileNameOf("a\u0000b.txt"));
		assertEquals(255, ResumableUpload.clientFileNameOf("x".repeat(300)).length());
	}

	/** What the field was given of an uploaded file. */
	private record FileUploadToResourceFieldInfo(String clientFileName, byte[] bytes)
	{
	}

	private static class TestField extends ResumableUploadToResourceField
	{
		private static final long serialVersionUID = 1L;

		final List<FileUploadToResourceFieldInfo> uploaded = new ArrayList<>();

		final List<String> failures = new ArrayList<>();

		int canceled;

		TestField(String id)
		{
			super(id);
			setParallelUploads(3);
			setRemembering(false);
		}

		@Override
		protected void onUploadSuccess(AjaxRequestTarget target, List<UploadInfo> fileInfos)
		{
			fileInfos.forEach(info -> uploaded.add(
				new FileUploadToResourceFieldInfo(info.getClientFileName(), info.get())));
		}

		@Override
		protected void onUploadFailure(AjaxRequestTarget target, String errorInfo)
		{
			failures.add(errorInfo);
		}

		@Override
		protected void onUploadCanceled(AjaxRequestTarget target)
		{
			canceled++;
		}
	}

	/** A page with a field and its list. */
	public static class TestPage extends WebPage implements IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;

		TestPage(ResumableUploadToResourceField field)
		{
			add(field);
			add(new org.apache.wicket.markup.html.WebMarkupContainer("list")
				.add(new FileListBehavior(field)));
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream("<html><head></head><body>" +
				"<input type=\"file\" multiple=\"multiple\" wicket:id=\"files\"/>" +
				"<ul wicket:id=\"list\"></ul></body></html>");
		}
	}

	/** A request with a body of its own. */
	private static final class BodyRequest extends HttpServletRequestWrapper
	{
		private final byte[] bytes;

		private final String type;

		BodyRequest(HttpServletRequest request, byte[] bytes, String type)
		{
			super(request);
			this.bytes = bytes;
			this.type = type;
		}

		@Override
		public String getContentType()
		{
			return type;
		}

		@Override
		public long getContentLengthLong()
		{
			return bytes.length;
		}

		@Override
		public int getContentLength()
		{
			return bytes.length;
		}

		@Override
		public ServletInputStream getInputStream()
		{
			ByteArrayInputStream in = new ByteArrayInputStream(bytes);
			return new ServletInputStream()
			{
				@Override
				public int read()
				{
					return in.read();
				}

				@Override
				public int read(byte[] buffer, int offset, int length)
				{
					return in.read(buffer, offset, length);
				}

				@Override
				public boolean isFinished()
				{
					return in.available() == 0;
				}

				@Override
				public boolean isReady()
				{
					return true;
				}

				@Override
				public void setReadListener(ReadListener listener)
				{
				}
			};
		}
	}

	/** Holds the lock of an upload in a thread of its own, as a request writing it does. */
	private static final class Writer
	{
		private final java.util.concurrent.CountDownLatch done = new java.util.concurrent.CountDownLatch(
			1);

		private final Thread thread;

		Writer(ResumableUpload upload) throws InterruptedException
		{
			java.util.concurrent.CountDownLatch locked = new java.util.concurrent.CountDownLatch(1);
			thread = new Thread(() -> {
				upload.lock.lock();
				try
				{
					locked.countDown();
					done.await();
				}
				catch (InterruptedException e)
				{
					Thread.currentThread().interrupt();
				}
				finally
				{
					upload.lock.unlock();
				}
			});
			thread.start();
			locked.await();
		}

		void done() throws InterruptedException
		{
			done.countDown();
			thread.join();
		}
	}

	/** A stream breaking off after some bytes, as a connection does. */
	private static final class BreakingStream extends InputStream
	{
		private final InputStream in;

		private int left;

		BreakingStream(InputStream in, int left)
		{
			this.in = in;
			this.left = left;
		}

		@Override
		public int read() throws IOException
		{
			byte[] one = new byte[1];
			return read(one, 0, 1) < 0 ? -1 : one[0] & 0xff;
		}

		@Override
		public int read(byte[] buffer, int offset, int length) throws IOException
		{
			if (left <= 0)
			{
				throw new IOException("broken off");
			}
			int read = in.read(buffer, offset, Math.min(length, left));
			left -= Math.max(0, read);
			return read;
		}
	}

	/** A clock the test moves. */
	private static final class MutableClock extends Clock
	{
		private Instant now;

		MutableClock(Instant now)
		{
			this.now = now;
		}

		void add(Duration duration)
		{
			now = now.plus(duration);
		}

		@Override
		public ZoneOffset getZone()
		{
			return ZoneOffset.UTC;
		}

		@Override
		public Clock withZone(ZoneId zone)
		{
			return this;
		}

		@Override
		public Instant instant()
		{
			return now;
		}
	}
}
