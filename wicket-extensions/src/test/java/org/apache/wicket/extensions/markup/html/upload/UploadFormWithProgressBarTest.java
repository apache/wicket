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
package org.apache.wicket.extensions.markup.html.upload;

import junit.framework.TestCase;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.extensions.ajax.markup.html.form.upload.UploadProgressBar;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.WicketTester;

public class UploadFormWithProgressBarTest extends TestCase
{
	/**
	 * @see https://issues.apache.org/jira/browse/WICKET-3200
	 */
	public void testReCreateTheForm()
	{
		WicketTester tester = new WicketTester();
		UploadFormWithProgressBarTestPage testPage = null;
		tester.startPage(UploadFormWithProgressBarTestPage.class);
		testPage = (UploadFormWithProgressBarTestPage)tester.getLastRenderedPage();
		int oldFormInstanceId = testPage.form.getFormInstance();
		tester.clickLink("re-create");
		testPage = (UploadFormWithProgressBarTestPage)tester.getLastRenderedPage();
		assertNotSame(oldFormInstanceId, testPage.form.getFormInstance());
	}

	public static class UploadFormWithProgressBarTestPage extends WebPage
		implements
			IMarkupResourceStreamProvider
	{
		public TraceableForm form;

		public UploadFormWithProgressBarTestPage()
		{
			addForm();
			add(new Link<Void>("re-create")
			{
				@Override
				public void onClick()
				{
					form.remove();
					addForm();
				}
			});
		}

		private void addForm()
		{
			add(form = new TraceableForm("uploadForm"));
			form.add(new FileUploadField("fileInput"));
			form.add(new UploadProgressBar("progress", form));
		}

		public IResourceStream getMarkupResourceStream(final MarkupContainer container,
			final Class<?> containerClass)
		{
			return new StringResourceStream(
				"<html><body><a wicket:id=\"re-create\"></a>"
					+ "<form wicket:id=\"uploadForm\"><input wicket:id=\"fileInput\" type=\"file\" /> <span wicket:id=\"progress\"> </span></form>"
					+ "</body></html>");
		}

	}
	public static class TraceableForm extends Form
	{
		private final int formInstance;
		private static int nextInstanceId;

		public TraceableForm(final String id)
		{
			super(id);
			formInstance = nextInstanceId++;
		}

		public int getFormInstance()
		{
			return formInstance;
		}
	}

}