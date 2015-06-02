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

import java.util.List;
import java.util.Map;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.settings.DebugSettings;
import org.apache.wicket.util.convert.IConverter;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.string.AppendingStringBuffer;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.value.IValueMap;


/**
 * A choice subclass that shows choices in radio style.
 * <p>
 * Java:
 * 
 * <pre>
 * List SITES = Arrays.asList(new String[] { &quot;The Server Side&quot;, &quot;Java Lobby&quot;, &quot;Java.Net&quot; });
 * // Add a radio choice component that uses Input's 'site' property to designate the
 * // current selection, and that uses the SITES list for the available options.
 * form.add(new RadioChoice(&quot;site&quot;, SITES));
 * </pre>
 * 
 * HTML:
 * 
 * <pre>
 *    &lt;span style=&quot;vertical-align: top;&quot; wicket:id=&quot;site&quot;&gt;
 *   	&lt;input type=&quot;radio&quot;&gt;site 1&lt;/input&gt;
 *   	&lt;input type=&quot;radio&quot;&gt;site 2&lt;/input&gt;
 *    &lt;/span&gt;
 * </pre>
 * 
 * </p>
 * 
 * <p>
 * You can extend this class and override method wantOnSelectionChangedNotifications() to force
 * server roundtrips on each selection change.
 * </p>
 * 
 * @author Jonathan Locke
 * @author Igor Vaynberg (ivaynberg)
 * 
 * @param <T>
 *            The model object type
 */
public class RadioChoice<T> extends AbstractSingleSelectChoice<T> implements IOnChangeListener
{
	private static final long serialVersionUID = 1L;

	private String prefix = "";
	private String suffix = "";

	private LabelPosition labelPosition = LabelPosition.AFTER;

	/**
	 * Constructor
	 * 
	 * @param id
	 *            See Component
	 * @see org.apache.wicket.Component#Component(String)
	 * @see org.apache.wicket.markup.html.form.AbstractChoice#AbstractChoice(String)
	 */
	public RadioChoice(final String id)
	{
		super(id);
	}

	/**
	 * Constructor
	 * 
	 * @param id
	 *            See Component
	 * @param choices
	 *            The list of choices in the radio choice
	 * @see org.apache.wicket.Component#Component(String)
	 * @see org.apache.wicket.markup.html.form.AbstractChoice#AbstractChoice(String, List)
	 */
	public RadioChoice(final String id, final List<? extends T> choices)
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
	 *            The list of choices in the radio choice
	 * @see org.apache.wicket.Component#Component(String)
	 * @see org.apache.wicket.markup.html.form.AbstractChoice#AbstractChoice(String,
	 *      List,IChoiceRenderer)
	 */
	public RadioChoice(final String id, final List<? extends T> choices,
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
	 *            The list of choices in the radio choice
	 * @see org.apache.wicket.Component#Component(String, IModel)
	 * @see org.apache.wicket.markup.html.form.AbstractChoice#AbstractChoice(String, IModel, List)
	 */
	public RadioChoice(final String id, IModel<T> model, final List<? extends T> choices)
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
	 *            The list of choices in the radio choice
	 * @param renderer
	 *            The rendering engine
	 * @see org.apache.wicket.Component#Component(String, IModel)
	 * @see org.apache.wicket.markup.html.form.AbstractChoice#AbstractChoice(String, IModel,
	 *      List,IChoiceRenderer)
	 */
	public RadioChoice(final String id, IModel<T> model, final List<? extends T> choices,
		final IChoiceRenderer<? super T> renderer)
	{
		super(id, model, choices, renderer);
	}

	/**
	 * Constructor
	 * 
	 * @param id
	 *            See Component
	 * @param choices
	 *            The list of choices in the radio choice
	 * @see org.apache.wicket.Component#Component(String)
	 * @see org.apache.wicket.markup.html.form.AbstractChoice#AbstractChoice(String, IModel)
	 */
	public RadioChoice(String id, IModel<? extends List<? extends T>> choices)
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
	 *            The list of choices in the radio choice
	 * @see org.apache.wicket.markup.html.form.AbstractChoice#AbstractChoice(String, IModel,IModel)
	 * @see org.apache.wicket.Component#Component(String, IModel)
	 */
	public RadioChoice(String id, IModel<T> model, IModel<? extends List<? extends T>> choices)
	{
		super(id, model, choices);
	}

	/**
	 * Constructor
	 * 
	 * @param id
	 *            See Component
	 * @param choices
	 *            The list of choices in the radio choice
	 * @param renderer
	 *            The rendering engine
	 * @see org.apache.wicket.markup.html.form.AbstractChoice#AbstractChoice(String,
	 *      IModel,IChoiceRenderer)
	 * @see org.apache.wicket.Component#Component(String)
	 */
	public RadioChoice(String id, IModel<? extends List<? extends T>> choices,
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
	 *            The list of choices in the radio choice
	 * @param renderer
	 *            The rendering engine
	 * @see org.apache.wicket.Component#Component(String, IModel)
	 * @see org.apache.wicket.markup.html.form.AbstractChoice#AbstractChoice(String, IModel,
	 *      IModel,IChoiceRenderer)
	 */
	public RadioChoice(String id, IModel<T> model, IModel<? extends List<? extends T>> choices,
		IChoiceRenderer<? super T> renderer)
	{
		super(id, model, choices, renderer);
	}

	/**
	 * @see org.apache.wicket.markup.html.form.FormComponent#onComponentTag(org.apache.wicket.markup.ComponentTag)
	 */
	@Override
	protected void onComponentTag(ComponentTag tag)
	{
		super.onComponentTag(tag);
		// since this component cannot be attached to input tag the name
		// variable is illegal
		tag.remove("name");
	}

	/**
	 * @see org.apache.wicket.markup.html.form.IOnChangeListener#onSelectionChanged()
	 */
	@Override
	public void onSelectionChanged()
	{
		convertInput();
		updateModel();
		onSelectionChanged(getModelObject());
	}

	/**
	 * Template method that can be overridden by clients that implement IOnChangeListener to be
	 * notified by onChange events of a select element. This method does nothing by default.
	 * <p>
	 * Called when a option is selected of a dropdown list that wants to be notified of this event.
	 * This method is to be implemented by clients that want to be notified of selection events.
	 * 
	 * @param newSelection
	 *            The newly selected object of the backing model NOTE this is the same as you would
	 *            get by calling getModelObject() if the new selection were current
	 * @see #wantOnSelectionChangedNotifications()
	 */
	protected void onSelectionChanged(T newSelection)
	{
	}

	/**
	 * Whether this component's onSelectionChanged event handler should called using javascript if
	 * the selection changes. If true, a roundtrip will be generated with each selection change,
	 * resulting in the model being updated (of just this component) and onSelectionChanged being
	 * called. This method returns false by default.
	 * 
	 * @return True if this component's onSelectionChanged event handler should called using
	 *         javascript if the selection changes
	 */
	protected boolean wantOnSelectionChangedNotifications()
	{
		return false;
	}

	/**
	 * @see org.apache.wicket.MarkupContainer#getStatelessHint()
	 */
	@Override
	protected boolean getStatelessHint()
	{
		if (wantOnSelectionChangedNotifications())
		{
			return false;
		}
		return super.getStatelessHint();
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
	public final RadioChoice<T> setPrefix(String prefix)
	{
		// Tell the page that this component's prefix was changed
		addStateChange();
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
	public final RadioChoice<T> setSuffix(String suffix)
	{
		// Tell the page that this component's suffix was changed
		addStateChange();
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
	public RadioChoice<T> setLabelPosition(LabelPosition labelPosition)
	{
		Args.notNull(labelPosition, "labelPosition");
		this.labelPosition = labelPosition;
		return this;
	}

	/**
	 * @see org.apache.wicket.Component#onComponentTagBody(MarkupStream, ComponentTag)
	 */
	@Override
	public final void onComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag)
	{
		// Iterate through choices
		final List<? extends T> choices = getChoices();

		// Buffer to hold generated body
		final AppendingStringBuffer buffer = new AppendingStringBuffer((choices.size() + 1) * 70);

		// The selected value
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
		Class<?> objectClass = (displayValue == null ? null : displayValue.getClass());

		// Get label for choice
		String label = "";

		if (objectClass != null && objectClass != String.class)
		{
			@SuppressWarnings("rawtypes")
			final IConverter converter = getConverter(objectClass);
			label = converter.convertToString(displayValue, getLocale());
		}
		else if (displayValue != null)
		{
			label = displayValue.toString();
		}

		// If there is a display value for the choice, then we know that the
		// choice is automatic in some way. If label is /null/ then we know
		// that the choice is a manually created radio tag at some random
		// location in the page markup!
		if (label != null)
		{
			// Append option suffix
			buffer.append(getPrefix(index, choice));

			String id = getChoiceRenderer().getIdValue(choice, index);
			final String idAttr = getMarkupId() + "-" + id;

			boolean enabled = isEnabledInHierarchy() && !isDisabled(choice, index, selected);

			// Add label for radio button
			String display = label;
			if (localizeDisplayValues())
			{
				display = getLocalizer().getString(label, this, label);
			}

			CharSequence escaped = display;
			if (getEscapeModelStrings())
			{
				escaped = Strings.escapeMarkup(display);
			}

			// Allows user to add attributes to the <label..> tag
			IValueMap labelAttrs = getAdditionalAttributesForLabel(index, choice);
			StringBuilder extraLabelAttributes = new StringBuilder();
			if (labelAttrs != null)
			{
				for (Map.Entry<String, Object> attr : labelAttrs.entrySet())
				{
					extraLabelAttributes.append(' ')
							.append(attr.getKey())
							.append("=\"")
							.append(attr.getValue())
							.append('"');
				}
			}

			switch (labelPosition)
			{
				case BEFORE:

					buffer.append("<label for=\"")
							.append(idAttr)
							.append('"')
							.append(extraLabelAttributes)
							.append('>')
							.append(escaped)
							.append("</label>");
					break;
				case WRAP_BEFORE:
					buffer.append("<label")
							.append(extraLabelAttributes)
							.append('>')
							.append(escaped)
							.append(' ');
					break;
				case WRAP_AFTER:
					buffer.append("<label")
							.append(extraLabelAttributes)
							.append('>');
					break;
			}

			// Add radio tag
			buffer.append("<input name=\"")
				.append(getInputName())
				.append('"')
				.append(" type=\"radio\"")
				.append((isSelected(choice, index, selected) ? " checked=\"checked\"" : ""))
				.append((enabled ? "" : " disabled=\"disabled\""))
				.append(" value=\"")
				.append(id)
				.append("\" id=\"")
				.append(idAttr)
				.append('"');

			// Should a roundtrip be made (have onSelectionChanged called)
			// when the option is clicked?
			if (wantOnSelectionChangedNotifications())
			{
				CharSequence url = urlFor(IOnChangeListener.INTERFACE, new PageParameters());

				Form<?> form = findParent(Form.class);
				if (form != null)
				{
					buffer.append(" onclick=\"")
						.append(form.getJsForInterfaceUrl(url))
						.append(";\"");
				}
				else
				{
					// NOTE: do not encode the url as that would give
					// invalid JavaScript
					buffer.append(" onclick=\"window.location.href='")
						.append(url)
						.append((url.toString().indexOf('?') > -1 ? '&' : '?') + getInputName())
						.append('=')
						.append(id)
						.append("';\"");
				}
			}

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

			DebugSettings debugSettings = getApplication().getDebugSettings();
			String componentPathAttributeName = debugSettings.getComponentPathAttributeName();
			if (Strings.isEmpty(componentPathAttributeName) && debugSettings.isOutputComponentPath())
			{
				// fallback to the old 'wicketpath'
				componentPathAttributeName = "wicketpath";
			}
			if (Strings.isEmpty(componentPathAttributeName) == false)
			{
				CharSequence path = getPageRelativePath();
				path = Strings.replaceAll(path, "_", "__");
				path = Strings.replaceAll(path, ":", "_");
				buffer.append(' ').append(componentPathAttributeName).append("=\"")
					.append(path)
					.append("_input_")
					.append(index)
					.append('"');
			}

			buffer.append("/>");

			switch (labelPosition)
			{
				case AFTER:
					buffer.append("<label for=\"")
							.append(idAttr)
							.append('"')
							.append(extraLabelAttributes)
							.append('>')
							.append(escaped)
							.append("</label>");
					break;
				case WRAP_BEFORE:
					buffer.append("</label>");
					break;
				case WRAP_AFTER:
					buffer.append(' ')
							.append(escaped)
							.append("</label>");
					break;
			}

			// Append option suffix
			buffer.append(getSuffix(index, choice));
		}
	}

	/**
	 * You may subclass this method to provide additional attributes to the &lt;label ..&gt; tag.
	 *
	 @param index
	  *            index of the choice
	  * @param choice
	 *            the choice itself
	 * @return tag attribute name/value pairs.
	 */
	protected IValueMap getAdditionalAttributesForLabel(int index, T choice)
	{
		return null;
	}

	/**
	 * You may subclass this method to provide additional attributes to the &lt;input ..&gt; tag.
	 * 
	 * @param index
	 *            index of the choice
	 * @param choice
	 *            the choice itself
	 * @return tag attribute name/value pairs.
	 */
	protected IValueMap getAdditionalAttributes(final int index, final T choice)
	{
		return null;
	}
}
