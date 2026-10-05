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
package org.apache.wicket.extensions.ajax.markup.html.form.upload;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.extensions.markup.html.progress.ProgressBar;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.TagTester;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;

class UploadProgressBarTest extends WicketTestCase
{
	@Test
	void theBarIsTheMarkupOfAProgressBar()
	{
		tester.startPage(new UploadPage());

		String response = tester.getLastResponseAsString();
		TagTester progress = TagTester.createTagByName(response, "progress");
		assertEquals("wicket-progress-bar-value", progress.getAttribute("class"));
		assertEquals("0", progress.getAttribute("value"));
		assertTrue(response.contains("class=\"wicket-progress-bar-label\""));
		assertTrue(response.contains("wicket-progress-bar"), "the bar's style sheet");
		assertTrue(response.contains("UploadProgressBar"), "the style sheet placing the bar");
	}

	@Test
	void withoutTheDefaultCssTheBarIsLeftToTheApplication()
	{
		tester.startPage(new UploadPage(false));

		String response = tester.getLastResponseAsString();
		assertFalse(response.contains("wicket-progress-bar-ver"), "no style sheet of the bar");
		assertFalse(response.contains("UploadProgressBar-ver"));
	}

	@Test
	void theBarHasTheMarkupOfAProgressBar() throws IOException
	{
		try (InputStream in = UploadProgressBar.class.getResourceAsStream(
			"UploadProgressBar.html"))
		{
			String markup = new String(in.readAllBytes(), StandardCharsets.UTF_8);
			String bar = ProgressBar.markup("0");
			assertTrue(markup.contains(bar), "the bar of UploadProgressBar.html is " + bar);
		}
	}

	public static class UploadPage extends WebPage implements IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;

		UploadPage()
		{
			this(true);
		}

		UploadPage(boolean defaultCss)
		{
			Form<Void> form = new Form<>("form");
			add(form);
			FileUploadField file = new FileUploadField("file");
			form.add(file);
			form.add(new UploadProgressBar("progress", form, file)
			{
				private static final long serialVersionUID = 1L;

				@Override
				protected ResourceReference getCss()
				{
					return defaultCss ? super.getCss() : null;
				}
			});
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream("<html><head></head><body><form wicket:id=\"form\">" +
				"<input type=\"file\" wicket:id=\"file\"/><div wicket:id=\"progress\"></div>" +
				"</form></body></html>");
		}
	}
}
