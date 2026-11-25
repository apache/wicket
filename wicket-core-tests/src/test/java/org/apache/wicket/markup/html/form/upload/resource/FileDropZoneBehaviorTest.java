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

import java.util.regex.Pattern;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.upload.resource.FileUploadToResourceFieldTest.TestField;
import org.apache.wicket.markup.html.form.upload.resource.FileUploadToResourceFieldTest.TestFileUploadResourceReference;
import org.apache.wicket.util.file.File;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;

class FileDropZoneBehaviorTest extends WicketTestCase
{
	@Test
	void makesTheComponentADropZoneOfTheField()
	{
		DropZonePage page = tester.startPage(new DropZonePage());

		tester.assertContains(Pattern.quote("Wicket.FileUploadToResourceField.addDropZone(\"" +
			page.zone.getMarkupId() + "\", \"" + page.field.getMarkupId() + "\", \"" +
			FileDropZoneBehavior.DRAG_OVER_CSS_CLASS + "\");"));
	}

	static class DropZonePage extends WebPage implements IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;

		final TestField field;

		final WebMarkupContainer zone;

		DropZonePage()
		{
			zone = new WebMarkupContainer("zone");
			add(zone);
			field = new TestField("upload", new TestFileUploadResourceReference(
				new FolderUploadsFileManager(new File(System.getProperty("java.io.tmpdir")))));
			zone.add(field);
			zone.add(new FileDropZoneBehavior(field));
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream("<html><body><div wicket:id=\"zone\">" +
				"<input type=\"file\" wicket:id=\"upload\" multiple/></div></body></html>");
		}
	}
}
