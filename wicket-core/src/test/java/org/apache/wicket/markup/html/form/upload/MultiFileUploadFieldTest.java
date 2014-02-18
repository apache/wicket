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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.util.file.File;
import org.apache.wicket.util.io.IOUtils;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.FormTester;
import org.junit.Test;

/**
 * Tests for MultiFileUploadField
 */
public class MultiFileUploadFieldTest extends WicketTestCase
{
	/**
	 * FormTester supports MultiFileUploadField
	 *
	 * https://issues.apache.org/jira/browse/WICKET-5346
	 */
	@Test
	public void submitMultiFileUploadFields()
	{
		final AtomicBoolean submitted = new AtomicBoolean(false);
		final ListModel<FileUpload> filesModel = new ListModel<>(new ArrayList<FileUpload>());

		TestPage page = new TestPage(filesModel)
		{
			@Override
			protected void onSubmit()
			{
				super.onSubmit();

				List<FileUpload> uploads = filesModel.getObject();
				assertEquals(2, uploads.size());

				for (int i = 1; i < 2; i++)
				{
					FileUpload fileUpload = uploads.get(i);
					String clientFileName = fileUpload.getClientFileName();
					String id = clientFileName.replaceAll(MultiFileUploadFieldTest.class.getSimpleName() + "(\\d).txt", "$1");
					try
					{
						assertEquals("Test"+id, IOUtils.toString(fileUpload.getInputStream()));
					} catch (IOException e)
					{
						fail("Reading file upload '"+id+"' failed: " + e.getMessage());
					}
				}
				submitted.set(true);
			}
		};
		tester.startPage(page);

		FormTester ft = tester.newFormTester("f");

		ft.setFile("muf", new File("target/test-classes/org/apache/wicket/markup/html/form/upload/MultiFileUploadFieldTest0.txt"), "plain/text");
		ft.setFile("muf", new File("target/test-classes/org/apache/wicket/markup/html/form/upload/MultiFileUploadFieldTest1.txt"), "plain/text");
		ft.submit();

		assertEquals("The form is not submitted", true, submitted.get());
	}

	private static class TestPage extends WebPage implements IMarkupResourceStreamProvider
	{
		public TestPage(final ListModel<FileUpload> model)
		{
			Form f = new Form("f")
			{
				@Override
				protected void onSubmit()
				{
					super.onSubmit();

					TestPage.this.onSubmit();
				}
			};
			add(f);

			f.add(new MultiFileUploadField("muf", model));
		}

		protected void onSubmit()
		{
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container, Class<?> containerClass)
		{
			return new StringResourceStream("<html><body>\n" +
					"\t\t<form wicket:id=\"f\">\n" +
					"\t\t\t<input type=\"file\" wicket:id=\"muf\" />\n" +
					"\t\t\t<input type=\"submit\" value=\"Submit!\" />\t\n" +
					"\t\t</form>\n" +
					"\t</body></html>");
		}
	}
}
