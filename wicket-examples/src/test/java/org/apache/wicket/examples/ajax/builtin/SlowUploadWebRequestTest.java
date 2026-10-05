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
package org.apache.wicket.examples.ajax.builtin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import org.apache.commons.fileupload2.core.FileUploadException;
import org.apache.wicket.protocol.http.servlet.MultipartServletWebRequest;
import org.apache.wicket.protocol.http.servlet.MultipartServletWebRequestImpl;
import org.apache.wicket.util.file.File;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * {@link SlowUploadWebRequest} makes an upload take {@link SlowUploadWebRequest#DURATION} unless the
 * session turned it off.
 */
class SlowUploadWebRequestTest extends WicketTestCase
{
	private static final int SIZE = 64 * 1024;

	@TempDir
	Path temp;

	@BeforeEach
	void enableUploadProgress() throws IOException
	{
		tester.getApplication().getApplicationSettings().setUploadProgressUpdatesEnabled(true);
		Path file = temp.resolve("upload.bin");
		Files.write(file, new byte[SIZE]);
		tester.getRequest().addFile("file", new File(file.toFile()), "application/octet-stream");
	}

	@Test
	void theAjaxExamplesSlowDownUploads()
	{
		assertInstanceOf(SlowUploadWebRequest.class,
			new AjaxApplication().newWebRequest(tester.getRequest(), ""));
	}

	@Test
	void anUploadTakesTheDuration() throws FileUploadException
	{
		Duration duration = upload();

		assertTrue(duration.compareTo(SlowUploadWebRequest.DURATION.minusMillis(100)) >= 0,
			"the upload took " + duration.toMillis() + " ms");
	}

	@Test
	void theSessionCanTurnTheSlowingDownOff() throws FileUploadException
	{
		SlowUploadWebRequest.setSlow(false);

		MultipartServletWebRequest request = newMultipartRequest();
		assertEquals(MultipartServletWebRequestImpl.class, request.getClass());

		SlowUploadWebRequest.setSlow(true);

		assertInstanceOf(SlowUploadWebRequest.SlowMultipartWebRequest.class,
			newMultipartRequest());
	}

	private Duration upload() throws FileUploadException
	{
		MultipartServletWebRequest request = newMultipartRequest();
		long start = System.nanoTime();
		request.parseFileParts();
		Duration duration = Duration.ofNanos(System.nanoTime() - start);

		assertEquals(SIZE, request.getFile("file").get(0).getSize());
		return duration;
	}

	private MultipartServletWebRequest newMultipartRequest() throws FileUploadException
	{
		return new SlowUploadWebRequest(tester.getRequest(), "")
			.newMultipartWebRequest(Bytes.megabytes(1), "upload");
	}
}
