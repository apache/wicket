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

import java.util.Collection;
import java.util.List;

import org.apache.wicket.Page;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.convert.IConverter;
import org.apache.wicket.util.string.AppendingStringBuffer;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.version.undo.Change;


/**
 * A choice subclass that shows choices via checkboxes.
 * <p>
 * Java:
 * 
 * <pre>
 * List SITES = Arrays.asList(new String[] { &quot;The Server Side&quot;, &quot;Java Lobby&quot;, &quot;Java.Net&quot; });
 * // Add a set of checkboxes uses Input's 'site' property to designate the
 * // current selections, and that uses the SITES list for the available options.
 * form.add(new CheckBoxMultipleChoice(&quot;site&quot;, SITES));
 * </pre>
 * 
 * HTML:
 * 
 * <pre>
 *    &lt;span valign=&quot;top&quot; wicket:id=&quot;site&quot;&gt;
 *   	&lt;input type=&quot;checkbox&quot;&gt;site 1&lt;/input&gt;
 *   	&lt;input type=&quot;checkbox&quot;&gt;site 2&lt;/input&gt;
 *    &lt;/span&gt;
 * </pre>
 * 
 * </p>
 * 
 * @author Jonathan Locke
 * @author Johan Compagner
 * @author Martijn Dashorst
 * @author Gwyn Evans
 * @author Igor Vaynberg (ivaynberg)
 * 
 * @param <T>
 *            The model object type
 */
public class CheckBoxMultipleChoice<T> extends ListMultipleChoice<T>
{
	private static final long serialVersionUID = 1L;

	/** suffix change record. */
	private class SuffixChange extends Change
	{
		private static final long serialVersionUID = 1L;

		final String prevSuffix;

		SuffixChange(String prevSuffix)
		{
			this.prevSuffix = prevSuffix;
		}

		/**
		 * @see org.apache.wicket.version.undo.Change#undo()
		 */
		@Override
		public void undo()
		{
			setSuffix(prevSuffix);
		}

		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString()
		{
			return "SuffixChange[component: " + getPath() + ", suffix: " + prevSuffix + "]";
		}
	}

	/**
	 * Prefix change record.
	 */
	private class PrefixChange extends Change
	{
		private static final long serialVersionUID = 1L;

		private final String prevPrefix;

		/**
		 * Construct.
		 * 
		 * @param prevSuffix
		 */
		PrefixChange(String prevSuffix)
		{
			prevPrefix = prevSuffix;
		}

		/**
		 * @see org.apache.wicket.version.undo.Change#undo()
		 */
		@Override
		public void undo()
		{
			setPrefix(prevPrefix);
		}

		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString()
		{
			return "PrefixChange[component: " + getPath() + ", prefix: " + prevPrefix + "]";
		}
	}

	private String prefix = "";
	private String suffix = "<br/>\n";

	/**
	 * Constructor
	 * 
	 * @param id
	 *            See Component
	 * @see org.apache.wicket.Component#Component(String)
	 * @see AbstractChoice#AbstractChoice(String)
	 */
	public CheckBoxMultipleChoice(final String id)
	{
		super(id);
	}

	/**
	 * Constructor
	 * 
	 * @param id
	 *            See Component
	 * @param choices
	 *            The collection of choices in the radio choice
	 * @see org.apache.wicket.Component#Component(String)
	 * @see AbstractChoice#AbstractChoice(String, java.util.List)
	 */
	public CheckBoxMultipleChoice(final String id, final List<T> choices)
	{
		super(id, choices);
	}

	/**
	 * Constructor
	 * 
	 * @param id
	 *            See Component
	 * @param renderer
	 *            The rendering engine
	 * @param choices
	 *            The collection of choices in the radio choice
	 * @see org.apache.wicket.Component#Component(String)
	 * @see AbstractChoice#AbstractChoice(String,
	 *      java.util.List,org.apache.wicket.markup.html.form.IChoiceRenderer)
	 */
	public CheckBoxMultipleChoice(final String id, final List<T> choices,
		final IChoiceRenderer<T> renderer)
	{
		super(id, choices, renderer);
	}

	/**
	 * Constructor
	 * 
	 * @param id
	 *            See Component
	 * @param model
	 *            See Component
	 * @param choices
	 *            The collection of choices in the radio choice
	 * @see org.apache.wicket.Component#Component(String, org.apache.wicket.model.IModel)
	 * @see AbstractChoice#AbstractChoice(String, org.apache.wicket.model.IModel, java.util.List)
	 */
	public CheckBoxMultipleChoice(final String id, IModel<Collection<T>> model,
		final List<T> choices)
	{
		super(id, model, choices);
	}

	/**
	 * Constructor
	 * 
	 * @param id
	 *            See Component
	 * @param model
	 *            See Component
	 * @param choices
	 *            The collection of choices in the radio choice
	 * @param renderer
	 *            The rendering engine
	 * @see org.apache.wicket.Component#Component(String, org.apache.wicket.model.IModel)
	 * @see AbstractChoice#AbstractChoice(String, org.apache.wicket.model.IModel,
	 *      java.util.List,org.apache.wicket.markup.html.form.IChoiceRenderer)
	 */
	public CheckBoxMultipleChoice(final String id, IModel<Collection<T>> model,
		final List<T> choices, final IChoiceRenderer<T> renderer)
	{
		super(id, model, choices, renderer);
	}

	/**
	 * Constructor
	 * 
	 * @param id
	 *            See Component
	 * @param choices
	 *            The collection of choices in the radio choice
	 * @see org.apache.wicket.Component#Component(String)
	 * @see AbstractChoice#AbstractChoice(String, org.apache.wicket.model.IModel)
	 */
	public CheckBoxMultipleChoice(String id, IModel<List<? extends T>> choices)
	{
		super(id, choices);
	}

	/**
	 * Constructor
	 * 
	 * @param id
	 *            See Component
	 * @param model
	 *            The model that is updated with changes in this component. See Component
	 * @param choices
	 *            The collection of choices in the radio choice
	 * @see AbstractChoice#AbstractChoice(String,
	 *      org.apache.wicket.model.IModel,org.apache.wicket.model.IModel)
	 * @see org.apache.wicket.Component#Component(String, org.apache.wicket.model.IModel)
	 */
	public CheckBoxMultipleChoice(String id, IModel<Collection<T>> model,
		IModel<List<? extends T>> choices)
	{
		super(id, model, choices);
	}

	/**
	 * Constructor
	 * 
	 * @param id
	 *            See Component
	 * @param choices
	 *            The collection of choices in the radio choice
	 * @param renderer
	 *            The rendering engine
	 * @see AbstractChoice#AbstractChoice(String,
	 *      org.apache.wicket.model.IModel,org.apache.wicket.markup.html.form.IChoiceRenderer)
	 * @see org.apache.wicket.Component#Component(String)
	 */
	public CheckBoxMultipleChoice(String id, IModel<List<? extends T>> choices,
		IChoiceRenderer<T> renderer)
	{
		super(id, choices, renderer);
	}


	/**
	 * Constructor
	 * 
	 * @param id
	 *            See Component
	 * @param model
	 *            The model that is updated with changes in this component. See Component
	 * @param choices
	 *            The collection of choices in the radio choice
	 * @param renderer
	 *            The rendering engine
	 * @see org.apache.wicket.Component#Component(String, org.apache.wicket.model.IModel)
	 * @see AbstractChoice#AbstractChoice(String, org.apache.wicket.model.IModel,
	 *      org.apache.wicket.model.IModel,org.apache.wicket.markup.html.form.IChoiceRenderer)
	 */
	public CheckBoxMultipleChoice(String id, IModel<Collection<T>> model,
		IModel<List<? extends T>> choices, IChoiceRenderer<T> renderer)
	{
		super(id, model, choices, renderer);
	}

	/**
	 * @return Prefix to use before choice
	 */
	public String getPrefix()
	{
		return prefix;
	}

	/**
	 * @param prefix
	 *            Prefix to use before choice
	 * @return this
	 */
	public final CheckBoxMultipleChoice<T> setPrefix(final String prefix)
	{
		// Tell the page that this component's prefix was changed
		final Page page = findPage();
		if (page != null)
		{
			addStateChange(new PrefixChange(this.prefix));
		}

		this.prefix = prefix;
		return this;
	}

	/**
	 * @return Separator to use between radio options
	 */
	public String getSuffix()
	{
		return suffix;
	}

	/**
	 * @param suffix
	 *            Separator to use between radio options
	 * @return this
	 */
	public final CheckBoxMultipleChoice<T> setSuffix(final String suffix)
	{
		// Tell the page that this component's suffix was changed
		final Page page = findPage();
		if (page != null)
		{
			addStateChange(new SuffixChange(this.suffix));
		}

		this.suffix = suffix;
		return this;
	}

	/**
	 * @see org.apache.wicket.markup.html.form.ListMultipleChoice#onComponentTag(org.apache.wicket.markup.ComponentTag)
	 */
	@Override
	protected void onComponentTag(ComponentTag tag)
	{
		super.onComponentTag(tag);
		// No longer applicable, breaks XHTML validation.
		tag.remove("multiple");
		tag.remove("size");
		tag.remove("disabled");
		tag.remove("name");
	}

	/**
	 * @see org.apache.wicket.Component#onComponentTagBody(org.apache.wicket.markup.MarkupStream,
	 *      org.apache.wicket.markup.ComponentTag)
	 */
	@Override
	protected final void onComponentTagBody(final MarkupStream markupStream,
		final ComponentTag openTag)
	{
		// Iterate through choices
		final List<? extends T> choices = getChoices();

		// Buffer to hold generated body
		final AppendingStringBuffer buffer = new AppendingStringBuffer(70 * (choices.size() + 1));

		// Value of this choice
		final String selected = getValue();

		// Loop through choices
		for (int index = 0; index < choices.size(); index++)
		{
			// Get next choice
			final T choice = choices.get(index);

			Object displayValue = getChoiceRenderer().getDisplayValue(choice);
			Class<?> objectClass = displayValue == null ? null : displayValue.getClass();
			// Get label for choice
			String label = "";
			if (objectClass != null && objectClass != String.class)
			{
				IConverter converter = getConverter(objectClass);
				label = converter.convertToString(displayValue, getLocale());
			}
			else if (displayValue != null)
			{
				label = displayValue.toString();
			}

			// If there is a display value for the choice, then we know that the
			// choice is automatic in some way. If label is /null/ then we know
			// that the choice is a manually created checkbox tag at some random
			// location in the page markup!
			if (label != null)
			{
				// Append option suffix
				buffer.append(getPrefix());

				String id = getChoiceRenderer().getIdValue(choice, index);
				final String idAttr = getInputName() + "_" + id;

				// Add checkbox element
				buffer.append("<input name=\"")
					.append(getInputName())
					.append("\"")
					.append(" type=\"checkbox\"")
					.append((isSelected(choice, index, selected) ? " checked=\"checked\"" : ""))
					.append((isEnabled() ? "" : " disabled=\"disabled\""))
					.append(" value=\"")
					.append(id)
					.append("\" id=\"")
					.append(idAttr)
					.append("\"/>");

				// Add label for checkbox
				String display = label;
				if (localizeDisplayValues())
				{
					display = getLocalizer().getString(label, this, label);
				}

				CharSequence escaped;
				if (getEscapeModelStrings())
				{
					escaped = Strings.escapeMarkup(display, false, true);
				}
				else
				{
					escaped = display;
				}

				buffer.append("<label for=\"");
				buffer.append(idAttr);
				buffer.append("\">").append(escaped).append("</label>");

				// Append option suffix
				buffer.append(getSuffix());
			}
		}

		// Replace body
		replaceComponentTagBody(markupStream, openTag, buffer);
	}

	/**
	 * Overridable method to determine whether the label markup should be escaped.
	 * 
	 * @deprecated use {@link #setEscapeModelStrings(boolean)}
	 * 
	 * @return true if label markup should be escaped
	 */
	@Deprecated
	protected final boolean isEscapeLabelMarkup()
	{
		return getEscapeModelStrings();
	}
}
