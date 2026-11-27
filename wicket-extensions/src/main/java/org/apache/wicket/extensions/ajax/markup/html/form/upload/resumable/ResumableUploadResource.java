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

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardOpenOption;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.resource.FileUploadToResourceField;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.resource.IResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;

/**
 * The tus protocol 1.0, with its extensions creation, termination and expiration, over the uploads
 * of a {@link ResumableUploadResourceReference}: a {@code POST} at the path the reference is
 * mounted at creates an upload, {@code HEAD}, {@code PATCH} and {@code DELETE} at the URL of an
 * upload ask how many bytes it holds, append a chunk to it and delete it.
 * <p>
 * Each request for an upload is held to the grant it carries: an upload of another context is
 * none of its, and one larger than it allows is refused.
 * <p>
 * Refusals carry the resource key of their error in the header {@value #ERROR_HEADER}, which the
 * script of the field reports and takes as final. The answer that completes an upload, and any
 * {@code HEAD} after it, carries the name the file was stored under in the header
 * {@value #FILE_NAME_HEADER}.
 */
class ResumableUploadResource implements IResource
{
	private static final long serialVersionUID = 1L;

	private static final Logger LOG = LoggerFactory.getLogger(ResumableUploadResource.class);

	/** The version of tus spoken. */
	static final String VERSION = "1.0.0";

	/** The extensions of tus spoken. */
	static final String EXTENSIONS = "creation,termination,expiration";

	/** The content type of the chunks. */
	static final String CHUNK_TYPE = "application/offset+octet-stream";

	static final String FILE_NAME_HEADER = ResumableUploadResourceReference.FILE_NAME_HEADER;

	static final String ERROR_HEADER = ResumableUploadResourceReference.ERROR_HEADER;

	private static final int BUFFER = 64 * 1024;

	private static final DateTimeFormatter HTTP_DATE = DateTimeFormatter.RFC_1123_DATE_TIME
		.withZone(ZoneOffset.UTC);

	private final ResumableUploadResourceReference reference;

	ResumableUploadResource(ResumableUploadResourceReference reference)
	{
		this.reference = reference;
	}

	/**
	 * What the resource answers: a status and headers, never a body.
	 */
	private static final class Answer
	{
		final Map<String, String> headers = new LinkedHashMap<>();

		int status;

		Answer header(String name, String value)
		{
			headers.put(name, value);
			return this;
		}

		Answer status(int status)
		{
			this.status = status;
			return this;
		}

		Answer refuse(int status, String errorKey)
		{
			return header(ERROR_HEADER, errorKey).status(status);
		}
	}

	@Override
	public void respond(Attributes attributes)
	{
		Answer answer = answer(attributes);
		WebResponse response = (WebResponse)attributes.getResponse();
		String location = answer.headers.get("Location");
		if (location != null)
		{
			// a browser without cookies sends the session along in the URL
			answer.header("Location", response.encodeURL(location));
		}
		response.setStatus(answer.status);
		response.disableCaching();
		response.setHeader("Tus-Resumable", VERSION);
		answer.headers.forEach(response::setHeader);
		response.setContentLength(0);
	}

	private Answer answer(Attributes attributes)
	{
		WebRequest webRequest = (WebRequest)attributes.getRequest();
		HttpServletRequest request = (HttpServletRequest)webRequest.getContainerRequest();
		String method = request.getMethod().toUpperCase(Locale.ROOT);
		String override = request.getHeader("X-HTTP-Method-Override");
		if ("POST".equals(method) && override != null)
		{
			method = override.trim().toUpperCase(Locale.ROOT);
		}
		Answer answer = new Answer();
		if ("OPTIONS".equals(method))
		{
			answer.header("Tus-Version", VERSION);
			answer.header("Tus-Extension", EXTENSIONS);
			answer.header("Tus-Max-Size", String.valueOf(reference.getMaxFileSize().bytes()));
			return answer.status(204);
		}
		if (!VERSION.equals(request.getHeader("Tus-Resumable")))
		{
			return answer.header("Tus-Version", VERSION).status(412);
		}
		String owner = reference.getCurrentOwner();
		// the query only: the body of a chunk is no form
		UploadGrant grant = UploadGrant.of(webRequest.getQueryParameters(),
			reference.getGrantSigner(), owner);
		if (grant == null)
		{
			return answer.refuse(403, ResumableUploadToResourceField.GRANT_REFUSED_RESOURCE_KEY);
		}
		reference.deleteExpired(false);
		boolean expired = grant.isExpired(reference.now());
		String id = attributes.getParameters().get(0).toOptionalString();
		if (id == null || id.isEmpty())
		{
			if (!"POST".equals(method))
			{
				return answer.header("Allow", "POST, OPTIONS").status(405);
			}
			return expired
				? answer.refuse(403, ResumableUploadToResourceField.GRANT_REFUSED_RESOURCE_KEY)
				: create(grant, request, owner, answer);
		}
		ResumableUpload upload = reference.find(id, owner);
		if (upload != null && !Objects.equals(upload.getContext(), grant.context()))
		{
			upload = null;
		}
		if (upload == null && expired)
		{
			// an expired grant goes on with the uploads the server keeps, and starts no others
			return answer.refuse(403, ResumableUploadToResourceField.GRANT_REFUSED_RESOURCE_KEY);
		}
		if (upload != null && !"DELETE".equals(method) &&
			upload.getLength() > grant.fileMaxSize())
		{
			return answer.refuse(413, ResumableUploadToResourceField.UPLOAD_TOO_LARGE_RESOURCE_KEY);
		}
		return switch (method)
		{
			case "HEAD" -> head(grant, upload, answer);
			case "PATCH" -> patch(grant, upload, request, answer);
			case "DELETE" -> delete(upload, answer);
			default -> answer.header("Allow", "HEAD, PATCH, DELETE, OPTIONS").status(405);
		};
	}

	private Answer create(UploadGrant grant, HttpServletRequest request, String owner,
		Answer answer)
	{
		if (owner == null)
		{
			return answer.refuse(403, FileUploadToResourceField.UPLOAD_FAILED_RESOURCE_KEY);
		}
		if (request.getHeader("Upload-Defer-Length") != null)
		{
			return answer.status(400);
		}
		long length = number(request.getHeader("Upload-Length"));
		Map<String, String> metadata = metadataOf(request.getHeader("Upload-Metadata"),
			reference.getMaxMetadataLength());
		if (length < 0 || metadata == null)
		{
			return answer.status(400);
		}
		answer.header("Tus-Max-Size", String.valueOf(reference.getMaxFileSize().bytes()));
		ResumableUpload upload;
		try
		{
			upload = reference.create(owner, grant, length, metadata);
			reference.track(upload, IUploadsTracker::onCreated);
		}
		catch (ResumableUploadResourceReference.Refusal refusal)
		{
			return answer.refuse(refusal.status, refusal.key);
		}
		answer.header("Location", locationOf(request, upload));
		answer.header("Upload-Offset", "0");
		if (upload.isComplete())
		{
			upload.lock.lock();
			try
			{
				return completed(grant, upload, answer.status(201));
			}
			finally
			{
				upload.lock.unlock();
			}
		}
		expires(upload, answer);
		return answer.status(201);
	}

	/**
	 * @return the URL of an upload relative to that of the request creating it, whatever path a
	 *         proxy maps the application to
	 */
	private static String locationOf(HttpServletRequest request, ResumableUpload upload)
	{
		String path = request.getRequestURI().replaceAll(";.*$", "");
		if (path.endsWith("/"))
		{
			return upload.getId();
		}
		return path.substring(path.lastIndexOf('/') + 1) + "/" + upload.getId();
	}

	private Answer head(UploadGrant grant, ResumableUpload upload, Answer answer)
	{
		if (upload == null)
		{
			return answer.status(404);
		}
		if (upload.isComplete() && !upload.isStored())
		{
			// the file manager is storing it: the browser asks again
			return answer.status(423);
		}
		answer.header("Upload-Offset", String.valueOf(upload.getOffset()));
		answer.header("Upload-Length", String.valueOf(upload.getLength()));
		if (upload.isStored() && grant.uploadFieldId().equals(upload.getStoredFieldId()))
		{
			answer.header(FILE_NAME_HEADER, upload.getStoredName());
		}
		expires(upload, answer);
		return answer.status(200);
	}

	private Answer patch(UploadGrant grant, ResumableUpload upload, HttpServletRequest request,
		Answer answer)
	{
		if (upload == null)
		{
			return answer.status(404);
		}
		String type = request.getContentType();
		if (type == null || !type.toLowerCase(Locale.ROOT).startsWith(CHUNK_TYPE))
		{
			return answer.status(415);
		}
		long offset = number(request.getHeader("Upload-Offset"));
		if (offset < 0)
		{
			return answer.status(400);
		}
		long contentLength = request.getContentLengthLong();
		if (contentLength > reference.getMaxChunkSize().bytes())
		{
			return answer.refuse(413, Form.UPLOAD_TOO_LARGE_RESOURCE_KEY);
		}
		try
		{
			if (!upload.lock.tryLock(reference.getLockTimeout().toMillis(),
				TimeUnit.MILLISECONDS))
			{
				return answer.status(423);
			}
		}
		catch (InterruptedException e)
		{
			Thread.currentThread().interrupt();
			return answer.status(423);
		}
		try
		{
			return patchLocked(grant, upload, request, offset, contentLength, answer);
		}
		finally
		{
			upload.lock.unlock();
			reference.endIfEnding(upload);
		}
	}

	private Answer patchLocked(UploadGrant grant, ResumableUpload upload,
		HttpServletRequest request, long offset, long contentLength, Answer answer)
	{
		if (upload.isForgotten() || upload.isStored())
		{
			// deleted while the request waited, or whole already
			return answer.header("Upload-Offset", String.valueOf(upload.getOffset()))
				.status(upload.isStored() ? 409 : 404);
		}
		if (offset != upload.getOffset())
		{
			return answer.header("Upload-Offset", String.valueOf(upload.getOffset())).status(409);
		}
		long left = upload.getLength() - offset;
		if (contentLength > left)
		{
			return answer.status(400);
		}
		long most = Math.min(reference.getMaxChunkSize().bytes(), left);
		long incoming = contentLength >= 0 ? contentLength : most;
		if (!reference.reserve(incoming))
		{
			return answer.header("Upload-Offset", String.valueOf(upload.getOffset()))
				.refuse(507, ResumableUploadToResourceField.NO_ROOM_RESOURCE_KEY);
		}
		long before = upload.getOffset();
		boolean broken;
		try
		{
			broken = !append(upload, request, incoming);
		}
		finally
		{
			reference.release(incoming - (upload.getOffset() - before));
		}
		if (upload.getOffset() > before)
		{
			reference.track(upload, IUploadsTracker::onProgress);
		}
		answer.header("Upload-Offset", String.valueOf(upload.getOffset()));
		if (upload.isComplete())
		{
			return completed(grant, upload, answer.status(204));
		}
		expires(upload, answer);
		// what arrived is kept: the browser asks how far it got and sends the rest
		return answer.status(broken ? 500 : 204);
	}

	/**
	 * Appends the body of a request to the bytes of an upload, at most a number of them, keeping
	 * all that arrived when the body breaks off.
	 *
	 * @return whether the body was read to its end, or to the most
	 */
	private boolean append(ResumableUpload upload, HttpServletRequest request, long most)
	{
		long position = upload.getOffset();
		try (FileChannel out = FileChannel.open(upload.getFile(), StandardOpenOption.WRITE))
		{
			// what a request that broke off wrote beyond the offset is not part of the upload
			out.truncate(position);
			try (InputStream in = reference.wrapBody(request.getInputStream(), upload))
			{
				byte[] buffer = new byte[BUFFER];
				long left = most;
				while (left > 0)
				{
					int read = in.read(buffer, 0, (int)Math.min(buffer.length, left));
					if (read < 0)
					{
						break;
					}
					ByteBuffer bytes = ByteBuffer.wrap(buffer, 0, read);
					while (bytes.hasRemaining())
					{
						position += out.write(bytes, position);
					}
					left -= read;
					upload.setOffset(position, reference.now());
				}
				return true;
			}
			catch (IOException e)
			{
				LOG.debug("The chunk of {} broke off at {}", upload, position, e);
				return false;
			}
		}
		catch (IOException e)
		{
			LOG.warn("Could not write {}", upload, e);
			return false;
		}
	}

	/**
	 * Hands the whole file of an upload to the file manager, for the field of the request.
	 */
	private Answer completed(UploadGrant grant, ResumableUpload upload, Answer answer)
	{
		try
		{
			reference.store(upload, grant.uploadFieldId());
		}
		catch (RuntimeException e)
		{
			LOG.error("Could not store {}", upload, e);
			reference.delete(upload);
			answer.headers.remove("Location");
			return answer.refuse(500, FileUploadToResourceField.UPLOAD_FAILED_RESOURCE_KEY);
		}
		answer.header(FILE_NAME_HEADER, upload.getStoredName());
		expires(upload, answer);
		return answer;
	}

	private Answer delete(ResumableUpload upload, Answer answer)
	{
		if (upload == null)
		{
			return answer.status(404);
		}
		return answer.status(reference.delete(upload) ? 204 : 423);
	}

	private void expires(ResumableUpload upload, Answer answer)
	{
		answer.header("Upload-Expires", HTTP_DATE.format(reference.expiresAt(upload)));
	}

	/**
	 * @return a header as a number of bytes, -1 when it is none
	 */
	static long number(String header)
	{
		if (header == null || header.isEmpty() || header.length() > 18 ||
			!header.chars().allMatch(c -> c >= '0' && c <= '9'))
		{
			return -1;
		}
		return Long.parseLong(header);
	}

	/**
	 * @param header
	 *            the header
	 * @param longest
	 *            its length at most
	 * @return the pairs of Upload-Metadata, their values decoded from base64 as UTF-8; empty
	 *         without the header, {@code null} when it is not valid or too long
	 */
	static Map<String, String> metadataOf(String header, int longest)
	{
		Map<String, String> metadata = new LinkedHashMap<>();
		if (header == null || header.isBlank())
		{
			return metadata;
		}
		if (header.length() > longest)
		{
			return null;
		}
		for (String pair : header.split(","))
		{
			String[] parts = pair.trim().split(" ");
			if (parts.length > 2 || parts[0].isEmpty() || metadata.containsKey(parts[0]))
			{
				return null;
			}
			try
			{
				metadata.put(parts[0], parts.length == 1 ? ""
					: new String(Base64.getDecoder().decode(parts[1]), StandardCharsets.UTF_8));
			}
			catch (IllegalArgumentException e)
			{
				return null;
			}
		}
		return metadata;
	}
}
