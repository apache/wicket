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
package org.apache.wicket.extensions.markup.html.form;

import java.util.Date;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.MarkupException;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 *
 */
public class DateTextFieldTest extends WicketTestCase
{
	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	/**
	 * https://issues.apache.org/jira/browse/WICKET-5286
	 */
	@Test
	public void validInputType()
	{
		String[] validInputTypes = { "text", "date", "datetime", "datetime-local", "month", "time", "week"};

		for (String validType : validInputTypes)
		{
			TestPage testPage = new TestPage(validType);
			tester.startPage(testPage);
		}
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-5286
	 */
	@Test
	public void invalidInputType()
	{
		TestPage testPage = new TestPage("unsupportedType");

		expectedException.expect(MarkupException.class);
		expectedException.expectMessage("Component [text] (path = [0:form:text]) must be applied to a tag" +
				" with [type] attribute matching any of [text, date, datetime, datetime-local, month, time, week], " +
				"not [unsupportedType]");
		tester.startPage(testPage);
	}

	/** */
	public static class TestPage extends WebPage implements IMarkupResourceStreamProvider
	{
		private final String inputType;
		DateTextField dateField;
		IModel<Date> textModel = Model.of(new Date());

		/** */
		public TestPage(String inputType)
		{
			this.inputType = inputType;
			Form<Void> form;
			add(form = new Form<>("form"));
			form.add(dateField = new DateTextField("text", textModel));
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
		                                               Class<?> containerClass)
		{
			return new StringResourceStream(String.format("<html><body>"
					+ "<form wicket:id=\"form\"><input wicket:id=\"text\" type=\"%s\"/></form></body></html>", inputType));
		}
	}
}
