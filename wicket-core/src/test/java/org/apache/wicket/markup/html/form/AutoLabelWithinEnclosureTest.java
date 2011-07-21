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
package org.apache.wicket.markup.html.form;

import org.apache.wicket.WicketTestCase;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.tester.WicketTester;

@SuppressWarnings({ "rawtypes", "serial", "unchecked" })
public class AutoLabelWithinEnclosureTest extends WicketTestCase
{
	public static class LabelWithinEnclosurePage extends WebPage
	{
		public LabelWithinEnclosurePage(final boolean textFieldVisible)
		{
			Form form = new Form("form");
			add(form);
			form.add(new TextField("textfield", Model.of(""))
			{
				@Override
				public boolean isVisible()
				{
					return textFieldVisible;
				}
			});
		}
	}

	public void testLabelWithinEnclosure_Visible() throws Exception
	{
		WicketTester tester = new WicketTester();
		tester.startPage(new LabelWithinEnclosurePage(true));
		tester.dumpPage();
		tester.assertContains("<label wicket:for=\"textfield\" for=\"textfield2\">blabla</label>");
	}

	public void testLabelWithinEnclosure_Invisible() throws Exception
	{
		WicketTester tester = new WicketTester();
		tester.startPage(new LabelWithinEnclosurePage(false));
		tester.dumpPage();
		tester.assertContainsNot("label");
	}
}
