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
package org.apache.wicket.extensions.markup.html.form.palette.component;

import java.util.Iterator;
import java.util.Map;

import org.apache.wicket.core.util.string.JavaScriptUtils;
import org.apache.wicket.extensions.markup.html.form.palette.Palette;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.util.convert.IConverter;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.value.IValueMap;


/**
 * Generates html option elements based on iterator specified by getOptionsIterator() and
 * IChoiceRender specified by the palette
 * 
 * @param <T>
 * @author Igor Vaynberg ( ivaynberg )
 */
public abstract class AbstractOptions<T> extends FormComponent<T>
{
	private static final long serialVersionUID = 1L;

	private final Palette<T> palette;

	protected Palette<T> getPalette()
	{
		return palette;
	}

	/**
	 * @param id
	 *            component id
	 * @param palette
	 *            parent palette
	 */
	public AbstractOptions(final String id, final Palette<T> palette)
	{
		super(id);
		this.palette = palette;
		setOutputMarkupId(true);
	}

	protected abstract Iterator<T> getOptionsIterator();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag)
	{
		StringBuilder buffer = new StringBuilder(128);
		Iterator<T> options = getOptionsIterator();
		IChoiceRenderer<T> renderer = getPalette().getChoiceRenderer();

		boolean localizeDisplayValues = localizeDisplayValues();

		while (options.hasNext())
		{
			final T choice = options.next();

			final CharSequence id;
			{
				String value = renderer.getIdValue(choice, 0);

				if (getEscapeModelStrings())
				{
					id = Strings.escapeMarkup(value);
				}
				else
				{
					id = value;
				}
			}

			final CharSequence value;
			{
				Object displayValue = renderer.getDisplayValue(choice);
				Class<?> displayClass = displayValue == null ? null : displayValue.getClass();

				@SuppressWarnings("unchecked")
				IConverter<Object> converter = (IConverter<Object>)getConverter(displayClass);
				String displayString = converter.convertToString(displayValue, getLocale());
				if (localizeDisplayValues)
				{
					displayString = getLocalizer().getString(displayString, this, displayString);
				}

				if (getEscapeModelStrings())
				{
					value = Strings.escapeMarkup(displayString);
				}
				else
				{
					value = displayString;
				}
			}

			buffer.append("\n<option value=\"").append(id).append("\"");

			Map<String, String> additionalAttributesMap = getAdditionalAttributes(choice);
			if (additionalAttributesMap != null)
			{
				for (Map.Entry<String, String> entry : additionalAttributesMap.entrySet())
				{
					buffer.append(' ')
						.append(entry.getKey())
						.append("=\"")
						.append(entry.getValue())
						.append("\"");
				}
			}

			buffer.append(">").append(value).append("</option>");
		}

		buffer.append("\n");

		replaceComponentTagBody(markupStream, openTag, buffer);
	}

	/**
	 * Should display values be localized.
	 * 
	 * @return default {@code true}
	 */
	protected boolean localizeDisplayValues()
	{
		return true;
	}

	/**
	 * @param choice
	 * @return map of attribute/value pairs (String/String)
	 */
	protected Map<String, String> getAdditionalAttributes(final T choice)
	{
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onComponentTag(final ComponentTag tag)
	{
		checkComponentTag(tag, "select");

		super.onComponentTag(tag);
		IValueMap attrs = tag.getAttributes();

		attrs.put("multiple", "multiple");
		attrs.put("size", getPalette().getRows());

		if (!palette.isPaletteEnabled())
		{
			attrs.put("disabled", "disabled");
		}

		avoidAjaxSerialization();
	}

	/**
	 * A piece of javascript to avoid serializing the options during AJAX serialization.
	 */
	protected void avoidAjaxSerialization()
	{
		getResponse().write(
			JavaScriptUtils.SCRIPT_OPEN_TAG +
				"if (typeof(Wicket) != \"undefined\" && typeof(Wicket.Form) != \"undefined\")" +
				"    Wicket.Form.excludeFromAjaxSerialization." + getMarkupId() + "='true';" +
				JavaScriptUtils.SCRIPT_CLOSE_TAG);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateModel()
	{
	}
}
