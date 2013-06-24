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

import java.util.Locale;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.string.Strings;

/**
 * @author Janne Hietam&auml;ki (janne)
 */
public class OnChangeAjaxBehaviorPage extends BasePage
{

	protected String getValue(String input)
	{
		if (Strings.isEmpty(input))
		{
			return "";
		}

	 StringBuilder buffer = new StringBuilder();

		Locale[] locales = Locale.getAvailableLocales();

		for (final Locale locale : locales)
		{
			final String country = locale.getDisplayCountry();

			if (country.toUpperCase().startsWith(input.toUpperCase()))
			{
				buffer.append(country);
				buffer.append(" ");
			}
		}

		return buffer.toString();
	}

	/**
	 * Constructor
	 */
	public OnChangeAjaxBehaviorPage()
	{
		Form<Void> form = new Form<>("form");
		add(form);

		final TextField<String> field = new TextField<String>("field", new Model<>(""));
		form.add(field);

		final Label label = new Label("selectedValue", new Model<>(""));
		label.setOutputMarkupId(true);
		form.add(label);

		OnChangeAjaxBehavior onChangeAjaxBehavior = new OnChangeAjaxBehavior()
		{
			@Override
			protected void onUpdate(AjaxRequestTarget target)
			{
				label.setDefaultModelObject(getValue(field.getDefaultModelObjectAsString()));
				target.add(label);
			}
		};
		field.add(onChangeAjaxBehavior);
	}

}
