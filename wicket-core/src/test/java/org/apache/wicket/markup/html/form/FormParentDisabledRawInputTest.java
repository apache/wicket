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

import org.apache.wicket.Component;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.core.request.handler.ListenerInvocationNotAllowedException;
import org.junit.Test;

/**
 */
public class FormParentDisabledRawInputTest extends WicketTestCase
{
	/**
	 */
	public static class TestPage extends WebPage
	{
		private static final long serialVersionUID = 1L;
		boolean property = true;
		boolean enabled = true;

		/**
		 * Construct.
		 */
		public TestPage()
		{
			setStatelessHint(false);

			WebMarkupContainer container = new WebMarkupContainer("container")
			{
				private static final long serialVersionUID = 1L;

				@Override
				public boolean isEnabled()
				{
					return enabled;
				}
			};
			Form<?> form = new Form<Void>("form");
			container.add(form);
			form.add(new CheckBox("check", new PropertyModel<Boolean>(this, "property")));
			add(container);
		}
	}

	/**
	 * @throws Exception
	 */
	@Test(expected = ListenerInvocationNotAllowedException.class)
	public void disabledParent() throws Exception
	{
		TestPage page = new TestPage();
		page.enabled = false;
		tester.startPage(page);
		tester.assertContains("checked=\"checked\"");
		tester.assertContains("disabled=\"disabled\"");
		Component check = tester.getComponentFromLastRenderedPage("container:form:check");
		assertTrue(check.isEnabled());
		assertFalse(check.isEnabledInHierarchy());

		// nothing should change with a submit that changes no values
		tester.newFormTester("container:form").submit();
	}
}
