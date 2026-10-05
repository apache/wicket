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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import org.apache.wicket.Component;
import org.apache.wicket.extensions.theme.Theme;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.form.upload.FilesSelectedBehavior;
import org.apache.wicket.util.file.File;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.TagTester;
import org.apache.wicket.util.tester.WicketTestCase;
import org.apache.wicket.util.tester.WicketTesterHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * The upload buttons of {@link FileUploadPage} are enabled only while files within the maximum
 * size are selected, and every sample shows its own feedback only.
 */
class FileUploadPageTest extends WicketTestCase
{
	private static final String SINGLE = "themed:singleFileUpload";

	private static final String MULTIPLE = "themed:multipleFileUpload";

	private static final long KB = 1024;

	private static final long MB = 1024 * KB;

	@TempDir
	Path temp;

	@BeforeEach
	void startPage()
	{
		tester.startPage(FileUploadPage.class);
	}

	@ParameterizedTest
	@ValueSource(strings = { SINGLE, MULTIPLE })
	void theButtonsAreDisabledUntilFilesAreSelected(String sample)
	{
		assertButtonsEnabled(sample, false);

		select(sample, 100 * KB);

		assertButtonsEnabled(sample, true);
		tester.assertComponentOnAjaxResponse(sample + ":form:submit");
		tester.assertComponentOnAjaxResponse(sample + ":form:ajaxSubmit");
	}

	@ParameterizedTest
	@ValueSource(strings = { SINGLE, MULTIPLE })
	void filesBeyondTheMaximumSizeKeepTheButtonsDisabled(String sample)
	{
		select(sample, 20 * MB);
		assertButtonsEnabled(sample, false);

		select(sample, 100 * KB);
		assertButtonsEnabled(sample, true);

		select(sample, 100 * KB, 20 * MB);
		assertButtonsEnabled(sample, false);
	}

	@Test
	void selectingFilesInOneSampleLeavesTheOtherAlone()
	{
		select(SINGLE, 100 * KB);

		assertButtonsEnabled(SINGLE, true);
		assertButtonsEnabled(MULTIPLE, false);
	}

	@Test
	void theSingleSampleDescribesTheSelectedFile()
	{
		select(SINGLE, 100 * KB);
		tester.assertComponentOnAjaxResponse(SINGLE + ":form:selectedFileInfo");
		assertTrue(tester.getLastResponseAsString().contains("You can click on buttons"));

		select(SINGLE, 20 * MB);
		assertTrue(tester.getLastResponseAsString().contains("File exceeds max allowed size."));
	}

	@Test
	void theMultipleSampleListsTheSelectedFiles()
	{
		select(MULTIPLE, 100 * KB, 200 * KB);

		tester.assertComponentOnAjaxResponse(MULTIPLE + ":form:selectedFileInfo");
		String response = tester.getLastResponseAsString();
		assertTrue(response.contains("file0.bin"), response);
		assertTrue(response.contains("file1.bin"), response);
		assertTrue(response.contains("You can click on buttons"), response);
	}

	@ParameterizedTest
	@ValueSource(strings = { SINGLE, MULTIPLE })
	void aRegularSubmitUploadsTheFileAndDisablesTheButtonsOfTheEmptiedField(String sample)
		throws IOException
	{
		select(sample, 100 * KB);

		FormTester form = tester.newFormTester(sample + ":form");
		form.setValue("text", "hello");
		form.setFile("file", newFile("upload.bin", 100 * KB), "application/octet-stream");
		form.submit("submit");

		assertEquals("Text: hello\nFile-Name: upload.bin File-Size: 100KB", feedback(sample));
		assertEquals("", feedback(SINGLE.equals(sample) ? MULTIPLE : SINGLE));
		assertButtonsEnabled(sample, false);
	}

	@ParameterizedTest
	@ValueSource(strings = { SINGLE, MULTIPLE })
	void anAjaxSubmitUploadsTheFileAndKeepsTheButtonsEnabled(String sample) throws IOException
	{
		select(sample, 100 * KB);

		FormTester form = tester.newFormTester(sample + ":form");
		form.setValue("text", "hello");
		form.setFile("file", newFile("upload.bin", 100 * KB), "application/octet-stream");
		tester.executeAjaxEvent(sample + ":form:ajaxSubmit", "click");

		tester.assertComponentOnAjaxResponse(sample + ":feedback");
		String response = tester.getLastResponseAsString();
		assertTrue(response.contains("File-Name: upload.bin File-Size: 100KB"), response);
		assertTrue(response.contains("This request was processed using AJAX"), response);
		assertButtonsEnabled(sample, true);
	}

	@Test
	void renderingTheSamplesAgainDisablesTheButtons()
	{
		select(SINGLE, 100 * KB);

		DropDownChoice<?> theme = (DropDownChoice<?>)tester.getComponentFromLastRenderedPage(
			"theme");
		tester.getRequest().getPostParameters().setParameterValue(theme.getInputName(),
			Theme.DARK.name());
		tester.executeAjaxEvent(theme, "change");

		tester.assertComponentOnAjaxResponse("themed");
		assertButtonsEnabled(SINGLE, false);
	}

	@ParameterizedTest
	@ValueSource(strings = { SINGLE, MULTIPLE })
	void theBrowserDoesNotRestoreTheFileField(String sample)
	{
		Component file = tester.getComponentFromLastRenderedPage(sample + ":form:file");
		assertEquals("off", tester.getTagById(file.getMarkupId()).getAttribute("autocomplete"));
	}

	@ParameterizedTest
	@ValueSource(strings = { SINGLE, MULTIPLE })
	void theButtonsAreThemedAndHaveAnIcon(String sample)
	{
		for (String button : new String[] { "submit", "ajaxSubmit" })
		{
			Component component = tester.getComponentFromLastRenderedPage(
				sample + ":form:" + button);
			TagTester tag = tester.getTagById(component.getMarkupId());
			assertEquals("button", tag.getName());
			assertEquals("upload-button", tag.getAttribute("class"));
			assertTrue(tag.getValue().contains("<svg class=\"wicket-svg-icon\""),
				tag.getValue());
		}
	}

	@Test
	void uploadsAreSlowUntilTurnedOff()
	{
		assertTrue(SlowUploadWebRequest.isSlow());
		assertEquals("checked",
			tester.getTagByWicketId("slowUpload").getAttribute("checked"));

		tester.executeAjaxEvent("slowUpload", "click");

		assertFalse(SlowUploadWebRequest.isSlow());

		tester.getRequest().getPostParameters().setParameterValue("slowUpload", "on");
		tester.executeAjaxEvent("slowUpload", "click");

		assertTrue(SlowUploadWebRequest.isSlow());
	}

	private void select(String sample, long... sizes)
	{
		String fileInfos = LongStream.range(0, sizes.length)
			.mapToObj(i -> String.format(
				"{\"fileName\":\"file%d.bin\",\"fileSize\":%d,\"lastModified\":0," +
					"\"mimeType\":\"application/octet-stream\"}", i, sizes[(int)i]))
			.collect(Collectors.joining(",", "[", "]"));
		FileUploadField file = (FileUploadField)tester.getComponentFromLastRenderedPage(
			sample + ":form:file");
		tester.getRequest().setParameter("fileInfos", fileInfos);
		tester.executeBehavior(
			(FilesSelectedBehavior)WicketTesterHelper.findBehavior(file, FilesSelectedBehavior.class));
	}

	private void assertButtonsEnabled(String sample, boolean enabled)
	{
		for (String button : new String[] { "submit", "ajaxSubmit" })
		{
			Component component = tester.getComponentFromLastRenderedPage(
				sample + ":form:" + button);
			assertEquals(enabled, component.isEnabledInHierarchy(), sample + " " + button);
			TagTester tag = TagTester.createTagByAttribute(tester.getLastResponseAsString(), "id",
				component.getMarkupId());
			if (tag != null)
			{
				assertEquals(!enabled, tag.hasAttribute("disabled"), sample + " " + button);
			}
		}
	}

	private String feedback(String sample)
	{
		Component feedback = tester.getComponentFromLastRenderedPage(sample + ":feedback");
		TagTester tag = tester.getTagById(feedback.getMarkupId());
		return TagTester.createTags(tag.getValue(), t -> "li".equals(t.getName()), false)
			.stream()
			.map(li -> li.getValue().replaceAll("<[^>]+>", "").trim())
			.collect(Collectors.joining("\n"));
	}

	private File newFile(String name, long size) throws IOException
	{
		Path file = temp.resolve(name);
		Files.write(file, new byte[(int)size]);
		return new File(file.toFile());
	}
}
