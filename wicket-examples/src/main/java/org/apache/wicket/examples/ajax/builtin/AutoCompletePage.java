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
package org.apache.wicket.examples.ajax.builtin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.extensions.ajax.markup.html.autocomplete.AutoCompleteTextField;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.string.Strings;


/**
 * Page that demos the ajax auto complete text field
 * 
 * @author ivaynberg
 */
public class AutoCompletePage extends BasePage
{

	private StringBuilder values = new StringBuilder();

	/**
	 * Constructor
	 */
	public AutoCompletePage()
	{
		Form<Void> form = new Form<>("form");
		add(form);

		final IModel<String> model = new IModel<String>()
		{
			private String value = null;

			@Override
			public String getObject()
			{
				return value;
			}

			@Override
			public void setObject(String object)
			{
				value = object;

				values.append("\n");
				values.append(value);
			}

			@Override
			public void detach()
			{
			}
		};

		final AutoCompleteTextField<String> field = new AutoCompleteTextField<String>("ac", model)
		{
			@Override
			protected Iterator<String> getChoices(String input)
			{
				if (Strings.isEmpty(input))
				{
					List<String> emptyList = Collections.emptyList();
					return emptyList.iterator();
				}

				List<String> choices = new ArrayList<>(10);

				Locale[] locales = Locale.getAvailableLocales();

				for (final Locale locale : locales)
				{
					final String country = locale.getDisplayCountry();

					if (country.toUpperCase().startsWith(input.toUpperCase()))
					{
						choices.add(country);
						if (choices.size() == 10)
						{
							break;
						}
					}
				}

				return choices.iterator();
			}
		};
		form.add(field);

		final MultiLineLabel label = new MultiLineLabel("history", new PropertyModel<String>(this,
			"values"));
		label.setOutputMarkupId(true);
		form.add(label);

		field.add(new AjaxFormSubmitBehavior(form, "change")
		{
			@Override
			protected void onSubmit(AjaxRequestTarget target)
			{
				target.add(label);
			}

			@Override
			protected void onError(AjaxRequestTarget target)
			{
			}
		});
	}
}
