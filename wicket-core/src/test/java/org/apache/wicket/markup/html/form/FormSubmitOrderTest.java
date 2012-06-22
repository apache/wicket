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
import org.junit.Test;

public class FormSubmitOrderTest extends WicketTestCase
{
	public static class TestPage extends WebPage
	{
		String result = "";

		public TestPage()
		{
			Form form = new Form("form")
			{
				@Override
				protected void onSubmit()
				{
					super.onSubmit();
					result += "form";
				}
			};
			this.add(form);
			form.add(new Button("button")
			{
				@Override
				public void onSubmitBeforeForm()
				{
					super.onSubmitBeforeForm();
					result += "before";
				}

				@Override
				public void onSubmitAfterForm()
				{
					super.onSubmitAfterForm();
					result += "after";
				}
			});
		}
	}

	@Test
	public void submitOrder() throws Exception
	{
		TestPage page = tester.startPage(TestPage.class);
		tester.newFormTester("form").submit("button");
		assertEquals("beforeformafter", page.result);
	}
}
