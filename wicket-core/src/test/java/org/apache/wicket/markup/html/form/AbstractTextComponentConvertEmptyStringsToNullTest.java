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

import java.util.Locale;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.convert.IConverter;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;

/**
 */
public class AbstractTextComponentConvertEmptyStringsToNullTest extends WicketTestCase
{

	/**
	 * @throws Exception
	 */
	@Test
	public void convertEmptyStringsToNull() throws Exception
	{
		StringArrayPage page = tester.startPage(StringArrayPage.class);

		tester.submitForm("form");

		assertNotNull(page.array);
		assertEquals(0, page.array.length);
	}

	/**
	 */
	public static class StringArrayPage extends WebPage implements IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;

		/**	 */
		public String[] array = new String[0];

		/**	 */
		public Form<Void> form;

		/**
		 * Construct.
		 */
		public StringArrayPage()
		{

			form = new Form<Void>("form");
			add(form);

			form.add(new TextField<String[]>("array", new PropertyModel<String[]>(this, "array"))
			{
				private static final long serialVersionUID = 1L;

				@Override
				@SuppressWarnings("unchecked")
				public <C> IConverter<C> getConverter(Class<C> type)
				{
					return (IConverter<C>)new StringArrayConverter();
				}
			}.setConvertEmptyInputStringToNull(false));
		}

		private class StringArrayConverter implements IConverter<String[]>
		{
			private static final long serialVersionUID = 1L;

			@Override
			public String[] convertToObject(String value, Locale locale)
			{
				return Strings.split(value, ',');
			}

			@Override
			public String convertToString(String[] value, Locale locale)
			{
				return Strings.join(",", value);
			}
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream(
				"<html><body><form wicket:id='form'><input type='text' wicket:id='array'/></form></body></html>");
		}
	}

}