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
package org.apache.wicket.markup.html.form.upload;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.Part;

import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.protocol.http.mock.MockHttpServletRequest;
import org.apache.wicket.protocol.http.servlet.MultipartServletWebRequestImpl;
import org.apache.wicket.request.resource.AbstractResource;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;

/**
 * Asserts that the upload limits a {@link Form} configures are enforced when
 * {@code MultipartServletWebRequestImpl#parseFileParts()} falls back to the Servlet multipart API.
 * <p>
 * {@code parseFileParts()} first parses with commons-fileupload, which {@code newFileUpload()}
 * configures from those limits. If that yields no items it falls back to
 * {@code readServlet3Parts()}, which wraps whatever {@code HttpServletRequest#getParts()} returns in
 * a {@code ServletPartFileItem}. The container has already parsed those parts, so the limits handed
 * to commons-fileupload never had a chance to apply to them and {@code readServlet3Parts()} has to
 * apply {@code maxSize}, {@code fileMaxSize} and {@code fileCountMax} itself. Nothing downstream
 * re-checks: {@code Form#handleMultiPart()} only reacts to a {@link FileUploadException} raised
 * during parsing, so a quiet return on this path means the limits are simply gone.
 * <p>
 * The fallback is not an error path. It exists for deployments where something else has already
 * consumed the request body - a {@code @MultipartConfig} servlet, Spring Boot's multipart resolver,
 * or any filter touching {@code request.getParameter()} on a multipart request - which is why it was
 * added for <a href="https://issues.apache.org/jira/browse/WICKET-5924">WICKET-5924</a>. There it is
 * the normal path, and it is the only path while {@code uploadProgressUpdatesEnabled} is left at its
 * default of {@code false}.
 * <p>
 * Two properties of the fallback make the enforcement worth pinning down in detail. Rejection has to
 * happen before a part is read, because {@code parseFileParts()} materialises every form field with
 * {@code ServletPartFileItem#getString()}, which pulls the whole part into a single heap array
 * through {@code IOUtils#toByteArray} - a check running after the parts had been collected would
 * allocate exactly what it was meant to refuse. See
 * {@link #oversizedFormFieldPartIsRejectedBeforeItIsBuffered()}. And {@code maxSize} was already
 * applied to this path: commons-fileupload compares {@code Content-Length} against it before reading
 * anything, so a request declaring an oversized length is rejected before the fallback is taken - see
 * {@link #declaredContentLengthOverMaxSizeIsRejectedBeforeTheFallback()}. The aggregate check
 * {@code readServlet3Parts()} performs is for completeness, not a fix for something observed.
 * <p>
 * {@code maxPartHeaderSize}, which {@code newFileUpload()} also configures, is deliberately not
 * covered here: on this path the container has already parsed the part headers under its own rules,
 * so Wicket's value was never going to apply to them.
 * <p>
 * These tests drive {@code MultipartServletWebRequestImpl} in the same order
 * {@code Form#handleMultiPart()} does, rather than going through a page, so that nothing but the
 * limit handling is under test. {@link #limitsAreEnforcedOnTheCommonsFileUploadPath()} is the
 * control: it shows the same limit honoured when commons-fileupload does the parsing, which keeps
 * the suite honest about which of the two paths each other test exercises.
 */
class FileUploadServletPartLimitsTest extends WicketTestCase
{
	/** Comfortably over {@link #LIMIT}, the only limit these tests configure. */
	private static final int OVERSIZED = 10_000;

	/**
	 * The size of the form field part. Not a limit, but a size at which buffering the part would be
	 * unmistakably deliberate rather than a rounding error; still small enough to be harmless in a
	 * test.
	 */
	private static final int LARGE = 1 << 20;

	private static final Bytes LIMIT = Bytes.bytes(100);

	/** Comfortably inside {@link #LIMIT}, for the tests that assert an upload is let through. */
	private static final int WITHIN_LIMIT = 10;

	private static final String FIELD = "upload";

	/**
	 * Registers {@code parts} on a multipart request that carries no body of its own, so that
	 * commons-fileupload finds nothing and the Servlet fallback is taken - the same situation as a
	 * container which has already parsed the upload.
	 */
	private MockHttpServletRequest requestWithParts(Part... parts)
	{
		MockHttpServletRequest request = tester.getRequest();
		request.setUseMultiPartContentType(true);
		for (int i = 0; i < parts.length; i++)
		{
			request.setPart(FIELD + i, parts[i]);
		}
		return request;
	}

	/**
	 * Parses exactly as {@code Form#handleMultiPart()} does: construct with {@code maxSize}, then set
	 * the per-file and count limits, then parse.
	 */
	private MultipartServletWebRequestImpl parse(HttpServletRequest request, Bytes maxSize,
		Bytes fileMaxSize, Long fileCountMax) throws FileUploadException
	{
		MultipartServletWebRequestImpl multipart =
			new MultipartServletWebRequestImpl(request, "", maxSize, "uploadId");
		multipart.setFileMaxSize(fileMaxSize);
		if (fileCountMax != null)
		{
			multipart.setFileCountMax(fileCountMax);
		}
		multipart.parseFileParts();
		return multipart;
	}

	/**
	 * {@link MockHttpServletRequest#getContentLength()} reports the length of the body it builds, which
	 * for a request carrying only pre-parsed parts is a few dozen bytes. A real request declares the
	 * length of the upload it carried, so that has to be substituted to see what happens on the
	 * commons-fileupload path.
	 */
	private HttpServletRequest withDeclaredContentLength(MockHttpServletRequest request,
		int contentLength)
	{
		return new HttpServletRequestWrapper(request)
		{
			@Override
			public int getContentLength()
			{
				return contentLength;
			}

			@Override
			public long getContentLengthLong()
			{
				return contentLength;
			}
		};
	}

	/**
	 * Records where {@code maxSize} is really enforced when the container has already parsed the body.
	 * commons-fileupload compares the declared {@code Content-Length} against {@code maxSize} before it
	 * reads anything, and that header survives the body having been consumed, so an oversized
	 * <em>declared</em> length is rejected on the commons-fileupload path and the fallback is never
	 * reached. This holds independently of the enforcement below - it is commons-fileupload doing the
	 * work - and is asserted so that the division of labour between the two is not lost.
	 * <p>
	 * So the aggregate limit was not what went unapplied here; {@code fileMaxSize} and
	 * {@code fileCountMax} were, being per-item checks that are consulted only when there are items,
	 * and the fallback is taken precisely when there are none. The other {@code maxSize} tests here
	 * exercise the aggregate check {@code readServlet3Parts()} performs regardless. That check can only
	 * matter for a request declaring no length at all - conceivable through chunked transfer encoding,
	 * but not demonstrated against a real container - so it is kept for completeness rather than as a
	 * fix for anything observed.
	 */
	@Test
	void declaredContentLengthOverMaxSizeIsRejectedBeforeTheFallback()
	{
		MockHttpServletRequest request = requestWithParts(new SizedPart(FIELD, OVERSIZED));

		FileUploadBase.SizeLimitExceededException e = assertThrows(FileUploadBase.SizeLimitExceededException.class,
			() -> parse(withDeclaredContentLength(request, OVERSIZED), LIMIT, null, null),
			"a declared Content-Length over maxSize must be rejected");

		// pins it to the request-size check rather than any other parse failure
		assertEquals(LIMIT.bytes(), e.getPermittedSize(), "permitted size should be maxSize");
		assertEquals(OVERSIZED, e.getActualSize(), "actual size should be the declared length");
	}

	/**
	 * A part far larger than {@code fileMaxSize}.
	 */
	@Test
	void fileMaxSizeIsEnforcedOnServletPartFallback()
	{
		MockHttpServletRequest request = requestWithParts(new SizedPart(FIELD, OVERSIZED));

		assertThrows(FileUploadException.class,
			() -> parse(request, Bytes.MAX, LIMIT, null),
			"a part exceeding fileMaxSize must be rejected");
	}

	/**
	 * A single part far larger than the total {@code maxSize}, on a request that does not declare a
	 * length large enough for commons-fileupload to have rejected it first. Covers the aggregate check
	 * kept for completeness, not something that went unapplied - see
	 * {@link #declaredContentLengthOverMaxSizeIsRejectedBeforeTheFallback()}.
	 */
	@Test
	void maxSizeIsEnforcedOnServletPartFallback()
	{
		MockHttpServletRequest request = requestWithParts(new SizedPart(FIELD, OVERSIZED));

		assertThrows(FileUploadException.class,
			() -> parse(request, LIMIT, null, null),
			"an upload exceeding the form's maxSize must be rejected");
	}

	/**
	 * More parts than {@code fileCountMax} allows.
	 */
	@Test
	void fileCountMaxIsEnforcedOnServletPartFallback()
	{
		MockHttpServletRequest request = requestWithParts(new SizedPart(FIELD, 1),
			new SizedPart(FIELD, 1), new SizedPart(FIELD, 1));

		assertThrows(FileUploadException.class,
			() -> parse(request, Bytes.MAX, null, 1L),
			"more parts than fileCountMax must be rejected");
	}

	/**
	 * Parts each within {@code fileMaxSize} but together over {@code maxSize}. Neither trips the
	 * per-part check, so only the running total catches this. For completeness, as above.
	 */
	@Test
	void aggregateSizeOfPartsIsEnforcedAgainstMaxSize()
	{
		int half = (int)LIMIT.bytes() - 20;
		MockHttpServletRequest request =
			requestWithParts(new SizedPart(FIELD, half), new SizedPart(FIELD, half));

		assertThrows(FileUploadException.class,
			() -> parse(request, LIMIT, Bytes.bytes(half), null),
			"parts summing to more than maxSize must be rejected");
	}

	/**
	 * An oversized form field part - one with no {@code Content-Type} - must be rejected without being
	 * read, since {@code parseFileParts()} would otherwise buffer it whole. The part fails the test if
	 * its stream is opened at all, which is what makes this a guard against the checks being reordered
	 * after the parts have been collected rather than just another rejection test.
	 */
	@Test
	void oversizedFormFieldPartIsRejectedBeforeItIsBuffered()
	{
		SizedPart part = new SizedPart(FIELD, LARGE, null);
		MockHttpServletRequest request = requestWithParts(part);

		assertThrows(FileUploadException.class,
			() -> parse(request, LIMIT, null, null),
			"a form field part exceeding maxSize must be rejected");

		assertFalse(part.wasRead(), "the part must be rejected before its content is read");
	}

	/**
	 * The fallback still delivers what it should. This is also the assurance that the tests above
	 * exercise the fallback at all rather than failing for an unrelated reason: the same construction
	 * that is rejected when oversized arrives as an uploaded file when it fits.
	 */
	@Test
	void fallbackDeliversPartsWithinTheLimits() throws Exception
	{
		MockHttpServletRequest request = requestWithParts(new SizedPart(FIELD, WITHIN_LIMIT));

		MultipartServletWebRequestImpl multipart = parse(request, LIMIT, LIMIT, 1L);

		List<org.apache.commons.fileupload.FileItem> files = multipart.getFile(FIELD);
		assertNotNull(files, "the part should have come through the fallback");
		assertEquals(1, files.size(), "exactly one part was uploaded");
		assertEquals(WITHIN_LIMIT, files.get(0).getSize(), "the part should arrive intact");
	}

	/**
	 * A form field part within the limits still becomes a request parameter, so the enforcement has not
	 * cost the fallback its handling of form fields.
	 */
	@Test
	void fallbackDeliversFormFieldPartsWithinTheLimits() throws Exception
	{
		MockHttpServletRequest request =
			requestWithParts(new SizedPart(FIELD, WITHIN_LIMIT, null));

		MultipartServletWebRequestImpl multipart = parse(request, LIMIT, LIMIT, null);

		assertEquals(WITHIN_LIMIT,
			multipart.getPostParameters().getParameterValue(FIELD).toString().length(),
			"the form field should arrive intact");
	}

	/**
	 * Exactly {@code fileCountMax} parts are allowed through; it is the part after the limit that is
	 * refused, as in {@code FileUploadBase}.
	 */
	@Test
	void exactlyFileCountMaxPartsAreAccepted() throws Exception
	{
		MockHttpServletRequest request = requestWithParts(new SizedPart(FIELD, WITHIN_LIMIT),
			new SizedPart(FIELD, WITHIN_LIMIT), new SizedPart(FIELD, WITHIN_LIMIT));

		MultipartServletWebRequestImpl multipart = parse(request, Bytes.MAX, null, 3L);

		assertEquals(3, multipart.getFile(FIELD).size(), "all three parts are within fileCountMax");
	}

	/**
	 * An application that configures no limits keeps accepting everything: {@code maxSize} defaults to
	 * {@link Bytes#MAX}, {@code fileMaxSize} to {@code null} and {@code fileCountMax} to {@code -1}.
	 * Guards against the enforcement breaking deployments that never asked for a limit.
	 */
	@Test
	void nothingConfiguredAcceptsLargeAndNumerousParts() throws Exception
	{
		MockHttpServletRequest request = requestWithParts(new SizedPart(FIELD, OVERSIZED),
			new SizedPart(FIELD, OVERSIZED), new SizedPart(FIELD, OVERSIZED));

		MultipartServletWebRequestImpl multipart = parse(request, Bytes.MAX, null, null);

		assertEquals(3, multipart.getFile(FIELD).size(), "no limit was configured");
	}

	/**
	 * The control: with a real multipart body, commons-fileupload does the parsing and the very same
	 * limit is honoured. Holds with or without the enforcement on the fallback, which is what makes it
	 * a control.
	 */
	@Test
	void limitsAreEnforcedOnTheCommonsFileUploadPath() throws Exception
	{
		MockHttpServletRequest request = tester.getRequest();
		request.setUseMultiPartContentType(true);
		request.addFile(FIELD, oversizedFile(), "text/plain");

		assertThrows(FileUploadException.class,
			() -> parse(request, Bytes.MAX, LIMIT, null),
			"commons-fileupload should still enforce fileMaxSize");
	}

	private org.apache.wicket.util.file.File oversizedFile() throws IOException
	{
		java.io.File file = java.io.File.createTempFile("wicket-upload-limits", ".txt");
		file.deleteOnExit();
		Files.write(file.toPath(), new byte[OVERSIZED]);
		return new org.apache.wicket.util.file.File(file);
	}

	/** A {@link Part} reporting an arbitrary size, as a container-parsed upload would. */
	private static class SizedPart implements Part
	{
		private final LinkedHashMap<String, List<String>> headers = new LinkedHashMap<>();
		private final String fieldName;
		private final String contentType;
		private final byte[] data;
		private boolean read;

		private SizedPart(String fieldName, int size)
		{
			this(fieldName, size, "text/plain");
		}

		/**
		 * @param contentType
		 *            {@code null} for a part {@code ServletPartFileItem} will treat as a form field
		 */
		private SizedPart(String fieldName, int size, String contentType)
		{
			this.fieldName = fieldName;
			this.contentType = contentType;
			this.data = new byte[size];
			// printable ASCII, so that the decoded length is the part's length in any encoding
			Arrays.fill(this.data, (byte)'a');
			headers.put(AbstractResource.CONTENT_DISPOSITION_HEADER_NAME,
				Arrays.asList(contentType == null
					? "form-data;name=" + fieldName
					: "attachment;filename=oversized.txt"));
		}

		@Override
		public InputStream getInputStream()
		{
			read = true;
			return new ByteArrayInputStream(data);
		}

		/** Whether anything asked for this part's content, rather than only its metadata. */
		boolean wasRead()
		{
			return read;
		}

		@Override
		public String getContentType()
		{
			return contentType;
		}

		@Override
		public String getName()
		{
			return fieldName;
		}

		@Override
		public String getSubmittedFileName()
		{
			return "oversized.txt";
		}

		@Override
		public long getSize()
		{
			return data.length;
		}

		@Override
		public void write(String fileName)
		{
		}

		@Override
		public void delete()
		{
		}

		@Override
		public String getHeader(String name)
		{
			return headers.containsKey(name) ? headers.get(name).get(0) : null;
		}

		@Override
		public Collection<String> getHeaders(String name)
		{
			return headers.get(name);
		}

		@Override
		public Collection<String> getHeaderNames()
		{
			return headers.keySet();
		}
	}
}
