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
package org.apache.wicket.stateless;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;

/**
 * @author Pedro Santos
 */
public class StatelessFormUrlTest extends WicketTestCase
{
	@Override
	protected WebApplication newApplication()
	{
		return new MockApplication() {
			@Override
			public Class<? extends Page> getHomePage()
			{
				return TestPage.class;
			}
		};
	}

	/**
	 * Preventing WICKET-3438
	 */
	@Test
	public void submitLinkInputNameNotEncodedIntoFormAction()
	{
		tester.executeUrl("?0-1.IFormSubmitListener-form&text=newValue&submitLink=x");
		assertFalse(tester.getLastResponseAsString().contains("submitLink=x"));
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-4365
	 */
	@Test
	public void formComponentNameNotEncodedIntoFormAction()
	{
		tester.executeUrl("?0-1.IFormSubmitListener-form&text=newValue");
		assertFalse(tester.getLastResponseAsString().contains("text=newValue"));
	}

	/** */
	public static class TestPage extends WebPage implements IMarkupResourceStreamProvider
	{
		/** */
		private static final long serialVersionUID = 1L;

		/**
		 * @param pageParameters
		 */
		public TestPage(PageParameters pageParameters)
		{
			super(pageParameters);
			StatelessForm<Void> form = new StatelessForm<Void>("form");
			add(form);
			TextField textField = new TextField("text", Model.of("textValue"));
			form.add(textField);
			SubmitLink submitLink = new SubmitLink("submitLink");
			form.add(submitLink);
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream(
				"<html><body><form wicket:id=\"form\"><input wicket:id=\"text\"><a wicket:id=\"submitLink\"></a></form></body></html>");
		}

	}
}