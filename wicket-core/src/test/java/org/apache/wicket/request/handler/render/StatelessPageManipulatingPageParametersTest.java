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
package org.apache.wicket.request.handler.render;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.mapper.parameter.INamedParameters;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.FormTester;
import org.junit.Before;
import org.junit.Test;

/**
 * https://issues.apache.org/jira/browse/WICKET-5666
 * https://issues.apache.org/jira/browse/WICKET-5643
 */
public class StatelessPageManipulatingPageParametersTest extends WicketTestCase {

	@Before
	public void before()
	{
		WebApplication application = tester.getApplication();
		application.mountPage("first", FirstPage.class);
		application.mountPage("second", SecondPage.class);
	}

	@Test
	public void submitAndRedirect() {
		tester.startPage(FirstPage.class);
		FormTester failingFormTester = tester.newFormTester("form");
		failingFormTester.submit();
		tester.assertRenderedPage(SecondPage.class);
	}

	public static class FirstPage extends WebPage implements IMarkupResourceStreamProvider {

		public FirstPage() {

			StatelessForm form = new StatelessForm("form")
			{
				@Override
				protected void onSubmit()
				{
					PageParameters parameters = new PageParameters();
					parameters.add("login", "", INamedParameters.Type.MANUAL);
					setResponsePage(SecondPage.class, parameters);
				}
			};

			add(form);
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container, Class<?> containerClass) 
		{
			String markup = 
					"<html><body>" + 
						"<form wicket:id=\"form\">"
							+ "<input type=\"submit\"/> \n" + 
						"</form>" + 
					"</body></html>";

			return new StringResourceStream(markup);
		}
	}
	
	public static class SecondPage extends WebPage implements IMarkupResourceStreamProvider
	{
		public SecondPage(PageParameters parameters)
		{
			super(parameters);

			parameters.clearNamed();
		}


		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container, Class<?> containerClass)
		{
			String markup = "<html/>";

			return new StringResourceStream(markup);
		}
	}
}
