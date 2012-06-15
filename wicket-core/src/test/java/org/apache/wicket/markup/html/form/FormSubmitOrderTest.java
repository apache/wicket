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
import org.apache.wicket.markup.html.form.IFormSubmitter.SubmitOrder;
import org.junit.Test;

public class FormSubmitOrderTest extends WicketTestCase
{
	public static class TestPage extends WebPage
	{
		String result = "";

		public TestPage(final SubmitOrder order)
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
			form.add(new Button("custom")
			{
				@Override
				public SubmitOrder getSubmitOrder()
				{
					return order;
				}

				@Override
				public void onSubmit()
				{
					super.onSubmit();
					result += "custom";
				}
			});
			form.add(new Button("default")
			{
				@Override
				public void onSubmit()
				{
					super.onSubmit();
					result += "default";
				}
			});
		}
	}

	@Test
	public void defaultOrder() throws Exception
	{
		TestPage page = tester.startPage(new TestPage(null));
		tester.newFormTester("form").submit("default");
		assertEquals("defaultform", page.result);
	}

	@Test
	public void customOrderBefore() throws Exception
	{
		TestPage page = tester.startPage(new TestPage(SubmitOrder.BEFORE_FORM));
		tester.newFormTester("form").submit("custom");
		assertEquals("customform", page.result);
	}

	@Test
	public void customOrderAfter() throws Exception
	{
		TestPage page = tester.startPage(new TestPage(SubmitOrder.AFTER_FORM));
		tester.newFormTester("form").submit("custom");
		assertEquals("formcustom", page.result);
	}
}
