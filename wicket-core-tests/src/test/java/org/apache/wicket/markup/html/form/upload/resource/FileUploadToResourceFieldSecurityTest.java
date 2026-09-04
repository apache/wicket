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
package org.apache.wicket.markup.html.form.upload.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.util.file.File;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class FileUploadToResourceFieldSecurityTest extends WicketTestCase
{
	private Path tempDir;

	@AfterEach
	void tearDown() throws IOException
	{
		if (tempDir != null)
		{
			try (var paths = Files.walk(tempDir))
			{
				paths.sorted(Comparator.reverseOrder()).forEach(path ->
				{
					try
					{
						Files.deleteIfExists(path);
					}
					catch (IOException e)
					{
						throw new RuntimeException(e);
					}
				});
			}
		}
	}

	@Test
	void acceptsUploadWithRenderedLimits() throws Exception
	{
		FolderUploadsFileManager fileManager = newFileManager();
		UploadPage page = new UploadPage(new TestFileUploadResourceReference(fileManager));
		tester.startPage(page);

		File upload = createUploadFile("allowed.txt", 256);
		submitUpload(page.field.buildUploadUrl(), upload);

		File storedFile = fileManager.getFile(page.field.getMarkupId(), upload.getName());
		assertTrue(storedFile.exists());
		assertEquals(upload.length(), storedFile.length());
	}

	@Test
	void rejectsTamperedUploadLimits() throws Exception
	{
		FolderUploadsFileManager fileManager = newFileManager();
		UploadPage page = new UploadPage(new TestFileUploadResourceReference(fileManager));
		tester.startPage(page);

		File upload = createUploadFile("oversized.txt", 4096);
		String validToken = AbstractFileUploadResource.createUploadToken(page.field.getMarkupId(),
			page.field.getMaxSize(), page.field.getFileMaxSize(), page.field.getFileCountMax());
		String tamperedUrl = page.field.getResourceUrl() + '?' +
			AbstractFileUploadResource.UPLOAD_ID + '=' + page.field.getMarkupId() + '&' +
			AbstractFileUploadResource.MAX_SIZE + '=' + Bytes.kilobytes(64).bytes() + '&' +
			AbstractFileUploadResource.FILE_COUNT_MAX + '=' + page.field.getFileCountMax() + '&' +
			AbstractFileUploadResource.UPLOAD_TOKEN + '=' + validToken;

		submitUpload(tamperedUrl, upload);

		File storedFile = fileManager.getFile(page.field.getMarkupId(), upload.getName());
		assertFalse(storedFile.exists());
		assertTrue(tester.getLastResponseAsString().contains("uploadFailed"));
	}

	private FolderUploadsFileManager newFileManager() throws IOException
	{
		tempDir = Files.createTempDirectory("wicket-upload-security");
		return new FolderUploadsFileManager(new File(tempDir.toFile()));
	}

	private File createUploadFile(String fileName, int size) throws IOException
	{
		Path filePath = tempDir.resolve(fileName);
		Files.write(filePath, new byte[size]);
		return new File(filePath.toFile());
	}

	private void submitUpload(String url, File upload)
	{
		tester.getRequest().setMethod("POST");
		tester.getRequest().addFile(AbstractFileUploadResource.PARAM_NAME, upload, "text/plain");
		tester.executeUrl(url);
	}

	static class UploadPage extends WebPage
	{
		private static final long serialVersionUID = 1L;

		final TestField field;

		UploadPage(TestFileUploadResourceReference resourceReference)
		{
			field = new TestField("upload", resourceReference);
			field.setMaxSize(Bytes.kilobytes(1));
			add(field);
		}
	}

	static class TestField extends FileUploadToResourceField
	{
		private static final long serialVersionUID = 1L;

		private final TestFileUploadResourceReference resourceReference;

		TestField(String id, TestFileUploadResourceReference resourceReference)
		{
			super(id);
			this.resourceReference = resourceReference;
		}

		@Override
		protected FileUploadResourceReference getFileUploadResourceReference()
		{
			return resourceReference;
		}

		@Override
		protected void onUploadSuccess(AjaxRequestTarget target, List<UploadInfo> fileInfos)
		{
		}

		String buildUploadUrl()
		{
			StringBuilder url = new StringBuilder(getResourceUrl());
			url.append('?').append(AbstractFileUploadResource.UPLOAD_ID).append('=')
				.append(getMarkupId());
			url.append('&').append(AbstractFileUploadResource.MAX_SIZE).append('=')
				.append(getMaxSize().bytes());
			url.append('&').append(AbstractFileUploadResource.FILE_COUNT_MAX).append('=')
				.append(getFileCountMax());
			url.append('&').append(AbstractFileUploadResource.UPLOAD_TOKEN).append('=')
				.append(AbstractFileUploadResource.createUploadToken(getMarkupId(), getMaxSize(),
					getFileMaxSize(), getFileCountMax()));
			return url.toString();
		}

		String getResourceUrl()
		{
			return urlFor(resourceReference, new PageParameters()).toString();
		}
	}

	static class TestFileUploadResourceReference extends FileUploadResourceReference
	{
		private static final long serialVersionUID = 1L;

		TestFileUploadResourceReference(IUploadsFileManager uploadFileManager)
		{
			super(uploadFileManager);
		}
	}
}
