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
package org.apache.wicket.examples.unicodeconverter;

import java.util.Arrays;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.examples.WicketExamplePage;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.string.Strings;


/**
 * Converts between unescaped and escaped unicode and shows a custom model. Handy for message
 * bundles.
 * 
 * @author Eelco Hillenius
 */
public class UnicodeConverter extends WicketExamplePage
{
	private static final String FROM_ESCAPED_UNICODE = "from escaped unicode";

	private static final String TO_ESCAPED_UNICODE = "to escaped unicode";

	private static List<String> translationTypes = Arrays.asList(TO_ESCAPED_UNICODE, FROM_ESCAPED_UNICODE);

	private String source = "";

	private String translationType = translationTypes.get(0);

	/**
	 * Model that does the conversion. Note that as we 'pull' the value every time we render (we get
	 * the current value of message), we don't need to update the model itself. The alternative
	 * strategy would be to have a model with it's own, translated, string representation of the
	 * source, which should be updated on every form post (e.g. by overriding {@link Form#onSubmit}
	 * and in that method explicitly setting the new value). But as you can see, this method is
	 * slighly easier, and if we wanted to use the translated value in e.g. a database, we could
	 * just query this model directly or indirectly by calling {@link Component#getDefaultModelObject()} on
	 * the component that holds it, and we would have a recent value.
	 */
	private final class ConverterModel extends Model<String>
	{
		/**
		 * @see org.apache.wicket.model.IModel#getObject()
		 */
		@Override
		public String getObject()
		{
			String result;
			if (TO_ESCAPED_UNICODE.equals(translationType))
			{
				result = Strings.toEscapedUnicode(source);
			}
			else
			{
				result = Strings.fromEscapedUnicode(source);
			}
			return result;
		}

		/**
		 * @see org.apache.wicket.model.IModel#setObject(java.lang.Object)
		 */
		@Override
		public void setObject(String object)
		{
			// Ignore. We are not interested in updating any value,
			// and we don't want to throw an exception like
			// AbstractReadOnlyModel either. Alternatively, we
			// could have overriden updateModel of FormInputComponent
			// and ignore any input there.
		}
	}

	/**
	 * Constructor.
	 */
	public UnicodeConverter()
	{
		Form<UnicodeConverter> form = new Form<UnicodeConverter>("form",
			new CompoundPropertyModel<>(this));
		form.add(new TextArea<>("source"));
		form.add(new DropDownChoice<>("translationType", translationTypes));
		form.add(new TextArea<>("target", new ConverterModel()));
		add(form);
	}

	/**
	 * @return the source to translate
	 */
	public String getSource()
	{
		return source;
	}

	/**
	 * @param source
	 *            the source to set
	 */
	public void setSource(String source)
	{
		this.source = source;
	}

	/**
	 * @return the selection
	 */
	public String getTranslationType()
	{
		return translationType;
	}

	/**
	 * @param translationType
	 *            the selection
	 */
	public void setTranslationType(String translationType)
	{
		this.translationType = translationType;
	}
}