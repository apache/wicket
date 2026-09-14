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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.file.File;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;

/**
 * Tests the upload of files to a resource by {@link FileUploadToResourceField}, and how the field
 * takes what the browser reports of it.
 */
class FileUploadToResourceFieldTest extends WicketTestCase
{
	@TempDir
	Path tempDir;

	private UploadPage page;

	@Test
	void storesFilesOfTheSameClientFileNameApart() throws IOException
	{
		FolderUploadsFileManager fileManager = startPage();

		submitUpload(createUploadFile("first", "same.txt", "first content"),
			createUploadFile("second", "same.txt", "second content"));

		JSONArray filesInfo = new JSONArray(tester.getLastResponseAsString());
		assertEquals(2, filesInfo.length());
		JSONObject first = filesInfo.getJSONObject(0);
		JSONObject second = filesInfo.getJSONObject(1);
		assertEquals("same.txt", first.getString("clientFileName"));
		assertEquals("same.txt", second.getString("clientFileName"));
		assertNotEquals(first.getString("fileName"), second.getString("fileName"));
		assertEquals("first content", readStoredFile(fileManager, first.getString("fileName")));
		assertEquals("second content", readStoredFile(fileManager, second.getString("fileName")));
	}

	@Test
	void givesTheStoredFilesToOnUploadSuccess() throws IOException
	{
		startPage();
		submitUpload(createUploadFile("first", "same.txt", "first content"),
			createUploadFile("second", "same.txt", "second content"));

		report("error", "false", "filesInfo", tester.getLastResponseAsString());

		List<FileUploadToResourceField.UploadInfo> uploadInfos = page.field.uploadInfos;
		assertEquals(2, uploadInfos.size());
		assertNotEquals(uploadInfos.get(0), uploadInfos.get(1));
		assertEquals("first content", read(uploadInfos.get(0).getFile()));
		assertEquals("second content", read(uploadInfos.get(1).getFile()));
	}

	@Test
	void answersAFailureOfTheFileManagerWithTheKeyOfAFailedUpload() throws IOException
	{
		startPage(new FolderUploadsFileManager(new File(tempDir.resolve("uploads").toFile()))
		{
			@Override
			public String store(FileUpload fileItem, String uploadFieldId)
			{
				throw new IllegalStateException("disk full");
			}
		});

		submitUpload(createUploadFile("first", "file.txt", "content"));

		JSONObject answer = new JSONObject(tester.getLastResponseAsString());
		assertTrue(answer.getBoolean("error"));
		assertEquals("uploadFailed", answer.getString("errorMessage"));
	}

	@Test
	void answersAnUploadWithoutFilesWithTheKeyOfNoFilesSelected()
	{
		startPage();

		tester.getRequest().setMethod("POST");
		tester.getRequest().setUseMultiPartContentType(true);
		tester.getRequest().getPostParameters().setParameterValue("other", "value");
		tester.executeUrl(page.field.buildUploadUrl());

		JSONObject answer = new JSONObject(tester.getLastResponseAsString());
		assertTrue(answer.getBoolean("error"));
		assertEquals(AbstractFileUploadResource.NO_FILE_SELECTED, answer.getString("errorMessage"));
	}

	@Test
	void answersFilesBeyondALimitWithTheKeyOfTheLimit() throws IOException
	{
		startPage();

		submitUpload(createUploadFile("1", "file.txt", "1"), createUploadFile("2", "file.txt", "2"),
			createUploadFile("3", "file.txt", "3"), createUploadFile("4", "file.txt", "4"));

		JSONObject answer = new JSONObject(tester.getLastResponseAsString());
		assertTrue(answer.getBoolean("error"));
		assertEquals("uploadTooManyFiles", answer.getString("errorMessage"));
	}

	@Test
	void callsOnUploadFailureForAFailedUpload()
	{
		startPage();

		report("error", "true", "errorMessage", "uploadFailed");

		assertEquals(List.of("The upload failed."), page.field.failures);
	}

	@Test
	void callsOnUploadFailureForAnErrorItHasNoTextFor()
	{
		startPage();

		report("error", "true", "errorMessage", "Internal Server Error");

		assertEquals(List.of("The upload failed."), page.field.failures);
	}

	@Test
	void callsOnUploadFailureForAnErrorWithoutKey()
	{
		startPage();

		report("error", "true");

		assertEquals(List.of("The upload failed."), page.field.failures);
	}

	@Test
	void callsOnUploadFailureWithTheTextOfABrokenLimit()
	{
		startPage();

		report("error", "true", "errorMessage", "uploadTooManyFiles");

		assertEquals(List.of("There should be no more than 3 files."), page.field.failures);
	}

	@Test
	void callsOnUploadFailureWithTheTextOfALostConnection()
	{
		startPage();

		report("error", "true", "errorMessage",
			FileUploadToResourceField.CONNECTION_LOST_RESOURCE_KEY);

		assertEquals(List.of("The upload failed: the connection to the server was lost."),
			page.field.failures);
	}

	@Test
	void callsOnUploadFailureWithTheTextOfAnErrorASubclassWords()
	{
		startPage();

		report("error", "true", "errorMessage", TestField.PAUSED_KEY);
		report("error", "true", "errorMessage", "uploadTooManyFiles");

		assertEquals(List.of("The upload was paused.", "There should be no more than 3 files."),
			page.field.failures);
	}

	@Test
	void callsOnUploadCanceledForACanceledUpload()
	{
		startPage();

		report("error", "true", "errorMessage", FileUploadToResourceField.UPLOAD_CANCELED);

		assertTrue(page.field.canceled);
		assertTrue(page.field.failures.isEmpty());
	}

	@SuppressWarnings("removal")
	@Test
	void anUploadInfoWithoutStoredNameIsStoredUnderItsClientFileName()
	{
		FileUploadToResourceField.UploadInfo info =
			new FileUploadToResourceField.UploadInfo("file.txt", 3, "text/plain");

		assertEquals("file.txt", info.getFileName());
		assertEquals("file.txt", info.getClientFileName());
	}

	@Test
	void uploadInfosAreEqualByTheirStoredName()
	{
		FileUploadToResourceField.UploadInfo info = new FileUploadToResourceField.UploadInfo(
			"stored-1", "same.txt", "text/plain");
		FileUploadToResourceField.UploadInfo sameStoredName =
			new FileUploadToResourceField.UploadInfo("stored-1", "other.txt", "text/html");
		FileUploadToResourceField.UploadInfo otherStoredName =
			new FileUploadToResourceField.UploadInfo("stored-2", "same.txt", "text/plain");

		assertEquals(info, sameStoredName);
		assertEquals(info.hashCode(), sameStoredName.hashCode());
		assertNotEquals(info, otherStoredName);
	}

	private FolderUploadsFileManager startPage()
	{
		FolderUploadsFileManager fileManager = new FolderUploadsFileManager(
			new File(tempDir.resolve("uploads").toFile()));
		startPage(fileManager);
		return fileManager;
	}

	private void startPage(IUploadsFileManager fileManager)
	{
		page = new UploadPage(new TestFileUploadResourceReference(fileManager));
		tester.startPage(page);
	}

	private File createUploadFile(String folder, String fileName, String content)
		throws IOException
	{
		Path filePath = Files.createDirectories(tempDir.resolve(folder)).resolve(fileName);
		Files.writeString(filePath, content, StandardCharsets.UTF_8);
		return new File(filePath.toFile());
	}

	private void submitUpload(File... uploads)
	{
		tester.getRequest().setMethod("POST");
		for (File upload : uploads)
		{
			tester.getRequest().addFile(AbstractFileUploadResource.PARAM_NAME, upload, "text/plain");
		}
		tester.executeUrl(page.field.buildUploadUrl());
	}

	/**
	 * Reports to the field how the upload ended, as its script does.
	 */
	private void report(String... parameters)
	{
		tester.getRequest().setMethod("POST");
		for (int i = 0; i < parameters.length; i += 2)
		{
			tester.getRequest().getPostParameters().setParameterValue(parameters[i],
				parameters[i + 1]);
		}
		tester.executeBehavior(page.field.getBehaviors(AbstractDefaultAjaxBehavior.class).get(0));
	}

	private String readStoredFile(FolderUploadsFileManager fileManager, String fileName)
		throws IOException
	{
		return read(fileManager.getFile(page.field.getMarkupId(), fileName));
	}

	private static String read(java.io.File file) throws IOException
	{
		return Files.readString(file.toPath(), StandardCharsets.UTF_8);
	}

	static class UploadPage extends WebPage
	{
		private static final long serialVersionUID = 1L;

		final TestField field;

		UploadPage(TestFileUploadResourceReference resourceReference)
		{
			field = new TestField("upload", resourceReference);
			field.setMaxSize(Bytes.kilobytes(1));
			field.setFileCountMax(3);
			add(field);
		}
	}

	static class TestField extends FileUploadToResourceField
	{
		private static final long serialVersionUID = 1L;

		static final String PAUSED_KEY = "uploadPaused";

		private final TestFileUploadResourceReference resourceReference;

		List<UploadInfo> uploadInfos;

		final List<String> failures = new ArrayList<>();

		boolean canceled;

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
			uploadInfos = fileInfos;
		}

		@Override
		protected void onUploadFailure(AjaxRequestTarget target, String errorInfo)
		{
			failures.add(errorInfo);
		}

		@Override
		protected void onUploadCanceled(AjaxRequestTarget target)
		{
			canceled = true;
		}

		@Override
		protected String getUploadErrorMessage(String errorKey)
		{
			return PAUSED_KEY.equals(errorKey) ? "The upload was paused."
				: super.getUploadErrorMessage(errorKey);
		}

		String buildUploadUrl()
		{
			return urlFor(resourceReference, new PageParameters()).toString() + '?' +
				AbstractFileUploadResource.UPLOAD_ID + '=' + getMarkupId() + '&' +
				AbstractFileUploadResource.MAX_SIZE + '=' + getMaxSize().bytes() + '&' +
				AbstractFileUploadResource.FILE_COUNT_MAX + '=' + getFileCountMax() + '&' +
				AbstractFileUploadResource.UPLOAD_TOKEN + '=' +
				AbstractFileUploadResource.createUploadToken(getMarkupId(), getMaxSize(),
					getFileMaxSize(), getFileCountMax());
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
