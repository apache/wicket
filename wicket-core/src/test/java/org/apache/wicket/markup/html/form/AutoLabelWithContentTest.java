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
import org.junit.Test;

/**
 * Make sure AutoLabelForInputTagResolver works with nested {@literal <wicket:message>} and nested
 * components to support things like
 * {@literal <label wicket:for="foo"><input wicket:id="foo"/></label>}.
 * 
 * @author Carl-Eric Menzel <cmenzel@wicketbuch.de>
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class AutoLabelWithContentTest extends WicketTestCase
{
	public static class LabelWithMessagePage extends WebPage
	{
		public LabelWithMessagePage()
		{
			Form form = new Form("form");
			add(form);
			form.add(new TextField("textfield", Model.of("")));
		}
	}

	@Test
	public void labelWithMessage() throws Exception
	{
		tester.startPage(LabelWithMessagePage.class);
		tester.assertContains("<label wicket:for=\"textfield\" for=\"textfield2\" id=\"textfield2-w-lbl\"><wicket:message key=\"foo\">my test text</wicket:message></label>");
	}

	public static class LabelWithNestedComponentsPage extends WebPage
	{
		public LabelWithNestedComponentsPage()
		{
			Form form = new Form("form");
			add(form);
			form.add(new TextField("textfield", Model.of("")));
		}
	}

	@Test
	public void labelWithNestedComponent()
	{
		tester.startPage(LabelWithNestedComponentsPage.class);
		tester.assertContains("<label wicket:for=\"textfield\" for=\"textfield2\" id=\"textfield2-w-lbl\"><input type=\"text\" wicket:id=\"textfield\" value=\"\" name=\"textfield\" id=\"textfield2\"/></label>");
	}
}
