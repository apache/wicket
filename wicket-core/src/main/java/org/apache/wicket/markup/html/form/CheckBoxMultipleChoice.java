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
import java.util.Map;

import org.apache.wicket.Page;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.convert.IConverter;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.string.AppendingStringBuffer;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.value.IValueMap;


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

	private String prefix = "";
	private String suffix = "<br/>\n";

	private LabelPosition labelPosition = LabelPosition.AFTER;

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
	public CheckBoxMultipleChoice(final String id, final List<? extends T> choices)
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
	public CheckBoxMultipleChoice(final String id, final List<? extends T> choices,
		final IChoiceRenderer<? super T> renderer)
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
	public CheckBoxMultipleChoice(final String id, IModel<? extends Collection<T>> model,
		final List<? extends T> choices)
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
	public CheckBoxMultipleChoice(final String id, IModel<? extends Collection<T>> model,
		final List<? extends T> choices, final IChoiceRenderer<? super T> renderer)
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
	public CheckBoxMultipleChoice(String id, IModel<? extends List<? extends T>> choices)
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
	public CheckBoxMultipleChoice(String id, IModel<? extends Collection<T>> model,
		IModel<? extends List<? extends T>> choices)
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
	public CheckBoxMultipleChoice(String id, IModel<? extends List<? extends T>> choices,
		IChoiceRenderer<? super T> renderer)
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
	public CheckBoxMultipleChoice(String id, IModel<? extends Collection<T>> model,
		IModel<? extends List<? extends T>> choices, IChoiceRenderer<? super T> renderer)
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
	 * @param index
	 *            index of the choice
	 * @param choice
	 *            the choice itself
	 * @return Prefix to use before choice. The default implementation just returns
	 *         {@link #getPrefix()}. Override to have a prefix dependent on the choice item.
	 */
	protected String getPrefix(int index, T choice)
	{
		return getPrefix();
	}

	/**
	 * @param index
	 *            index of the choice
	 * @param choice
	 *            the choice itself
	 * @return Separator to use between radio options. The default implementation just returns
	 *         {@link #getSuffix()}. Override to have a prefix dependent on the choice item.
	 */
	protected String getSuffix(int index, T choice)
	{
		return getSuffix();
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
			addStateChange();
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
			addStateChange();
		}

		this.suffix = suffix;
		return this;
	}

	/**
	 * Sets the preferred position of the &lt;label&gt; for each choice
	 *
	 * @param labelPosition
	 *              The preferred position for the label
	 * @return {@code this} instance, for chaining
	 */
	public CheckBoxMultipleChoice<T> setLabelPosition(LabelPosition labelPosition)
	{
		Args.notNull(labelPosition, "labelPosition");
		this.labelPosition = labelPosition;
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
	public final void onComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag)
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
			appendOptionHtml(buffer, choice, index, selected);
		}

		// Replace body
		replaceComponentTagBody(markupStream, openTag, buffer);
	}

	/**
	 * Generates and appends html for a single choice into the provided buffer
	 * 
	 * @param buffer
	 *            Appending string buffer that will have the generated html appended
	 * @param choice
	 *            Choice object
	 * @param index
	 *            The index of this option
	 * @param selected
	 *            The currently selected string value
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected void appendOptionHtml(final AppendingStringBuffer buffer, final T choice, int index,
		final String selected)
	{
		Object displayValue = getChoiceRenderer().getDisplayValue(choice);
		Class<?> objectClass = displayValue == null ? null : displayValue.getClass();
		// Get label for choice
		String label = "";
		if (objectClass != null && objectClass != String.class)
		{
			@SuppressWarnings("rawtypes")
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
			buffer.append(getPrefix(index, choice));

			String id = getChoiceRenderer().getIdValue(choice, index);
			final String idAttr = getCheckBoxMarkupId(id);

			// Add label for checkbox
			String display = label;
			if (localizeDisplayValues())
			{
				display = getLocalizer().getString(label, this, label);
			}

			final CharSequence escaped = (getEscapeModelStrings() ? Strings.escapeMarkup(display)
					: display);

			switch (labelPosition)
			{
				case BEFORE:
					buffer.append("<label for=\"");
					buffer.append(idAttr);
					buffer.append("\">").append(escaped).append("</label>");
					break;
				case WRAP_AFTER:
					buffer.append("<label>");
					break;
				case WRAP_BEFORE:
					buffer.append("<label>");
					buffer.append(escaped).append(' ');
					break;
			}

			// Add checkbox element
			buffer.append("<input name=\"");
			buffer.append(getInputName());
			buffer.append('"');
			buffer.append(" type=\"checkbox\"");
			if (isSelected(choice, index, selected))
			{
				buffer.append(" checked=\"checked\"");
			}
			if (isDisabled(choice, index, selected) || !isEnabledInHierarchy())
			{
				buffer.append(" disabled=\"disabled\"");
			}
			buffer.append(" value=\"");
			buffer.append(id);
			buffer.append("\" id=\"");
			buffer.append(idAttr);
			buffer.append('"');

			// Allows user to add attributes to the <input..> tag
			{
				IValueMap attrs = getAdditionalAttributes(index, choice);
				if (attrs != null)
				{
					for (Map.Entry<String, Object> attr : attrs.entrySet())
					{
						buffer.append(' ')
							.append(attr.getKey())
							.append("=\"")
							.append(attr.getValue())
							.append('"');
					}
				}
			}

			if (getApplication().getDebugSettings().isOutputComponentPath())
			{
				CharSequence path = getPageRelativePath();
				path = Strings.replaceAll(path, "_", "__");
				path = Strings.replaceAll(path, ":", "_");
				buffer.append(" wicketpath=\"")
					.append(path)
					.append("_input_")
					.append(index)
					.append('"');
			}

			buffer.append("/>");

			switch (labelPosition)
			{
				case WRAP_BEFORE:
					buffer.append("</label>");
					break;
				case WRAP_AFTER:
					buffer.append(' ').append(escaped).append("</label>");
					break;
				case AFTER:
					buffer.append("<label for=\"");
					buffer.append(idAttr);
					buffer.append("\">").append(escaped).append("</label>");
					break;
			}

			// Append option suffix
			buffer.append(getSuffix(index, choice));
		}
	}

	/**
	 * You may subclass this method to provide additional attributes to the &lt;input ..&gt; tag.
	 * 
	 * @param index
	 * @param choice
	 * @return tag attribute name/value pairs.
	 */
	protected IValueMap getAdditionalAttributes(final int index, final T choice)
	{
		return null;
	}

	/**
	 * Creates markup id for the input tag used to generate the checkbox for the element with the
	 * specified {@code id}.
	 * <p>
	 * NOTE It is useful to override this method if the contract for the generated ids should be
	 * fixed, for example in cases when the id generation pattern in this method is used to predict
	 * ids by some external javascript. If the contract is fixed in the user's code then upgrading
	 * wicket versions will guarantee not to break it should the default contract be changed at a
	 * later time.
	 * </p>
	 * 
	 * @param id
	 * @return markup id for the input tag
	 */
	protected String getCheckBoxMarkupId(String id)
	{
		return getMarkupId() + '-' + getInputName() + '_' + id;
	}
}
