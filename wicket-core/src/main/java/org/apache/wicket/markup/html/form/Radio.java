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

import org.apache.wicket.Component;
import org.apache.wicket.IGenericComponent;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * Component representing a single radio choice in a org.apache.wicket.markup.html.form.RadioGroup.
 * 
 * Must be attached to an &lt;input type=&quot;radio&quot; ... &gt; markup.
 * <p>
 * STATELESS NOTES: By default this component cannot be used inside a stateless form. If it is
 * desirable to use this inside a stateless form then
 * <ul>
 * <li>
 * override #getValue() and return some stateless value to uniquely identify this radio (eg relative
 * component path from group to this radio)</li>
 * <li>
 * override {@link #getStatelessHint()} and return <code>true</code></li>
 * </ul>
 * </p>
 * 
 * @see org.apache.wicket.markup.html.form.RadioGroup
 * 
 * @author Igor Vaynberg
 * @author Sven Meier (svenmeier)
 * 
 * @param <T>
 *            The model object type
 */
public class Radio<T> extends LabeledWebMarkupContainer implements IGenericComponent<T>
{
	private static final long serialVersionUID = 1L;

	private static final String ATTR_DISABLED = "disabled";

	/**
	 * page-scoped uuid of this check. this property must not be accessed directly, instead
	 * {@link #getValue()} must be used
	 */
	private int uuid = -1;

	private final RadioGroup<T> group;

	/**
	 * @see WebMarkupContainer#WebMarkupContainer(String)
	 */
	public Radio(final String id)
	{
		this(id, null, null);
	}

	/**
	 * @param id
	 * @param model
	 * @see WebMarkupContainer#WebMarkupContainer(String, IModel)
	 */
	public Radio(final String id, final IModel<T> model)
	{
		this(id, model, null);
	}

	/**
	 * @param id
	 * @param group
	 *            parent {@link RadioGroup}
	 * @see WebMarkupContainer#WebMarkupContainer(String)
	 */
	public Radio(final String id, final RadioGroup<T> group)
	{
		this(id, null, group);
	}

	/**
	 * @param id
	 * @param model
	 * @param group
	 *            parent {@link RadioGroup}
	 * @see WebMarkupContainer#WebMarkupContainer(String, IModel)
	 */
	public Radio(final String id, final IModel<T> model, final RadioGroup<T> group)
	{
		super(id, model);
		this.group = group;
		setOutputMarkupId(true);
	}

	/**
	 * Form submission value used for this radio component. This string will appear as the value of
	 * the <code>value</code> html attribute for the <code>input</code> tag.
	 * 
	 * @return form submission value
	 */
	public String getValue()
	{
		if (uuid < 0)
		{
			uuid = getPage().getAutoIndex();
		}
		return "radio" + uuid;
	}

	/**
	 * 
	 * @return The associated radio group Component
	 */
	@SuppressWarnings("unchecked")
	protected RadioGroup<T> getGroup()
	{
		RadioGroup<T> group = this.group;
		if (group == null)
		{
			group = findParent(RadioGroup.class);
			if (group == null)
			{
				throw new WicketRuntimeException(
					"Radio component [" +
						getPath() +
						"] cannot find its parent RadioGroup. All Radio components must be a child of or below in the hierarchy of a RadioGroup component.");
			}
		}
		return group;
	}

	/**
	 * @see Component#onComponentTag(ComponentTag)
	 * @param tag
	 *            the abstraction representing html tag of this component
	 */
	@Override
	protected void onComponentTag(final ComponentTag tag)
	{
		// Default handling for component tag
		super.onComponentTag(tag);

		// must be attached to <input type="radio" .../> tag
		checkComponentTag(tag, "input");
		checkComponentTagAttribute(tag, "type", "radio");

		final String value = getValue();

		RadioGroup<?> group = getGroup();

		// assign name and value
		tag.put("name", group.getInputName());
		tag.put("value", value);

		// compare the model objects of the group and self, if the same add the
		// checked attribute, first check if there was a raw input on the group.
		if (group.hasRawInput())
		{
			String rawInput = group.getRawInput();
			if (rawInput != null && rawInput.equals(value))
			{
				tag.put("checked", "checked");
			}
		}
		else if (group.getModelComparator().compare(group, getDefaultModelObject()))
		{
			tag.put("checked", "checked");
		}

		if (group.wantOnSelectionChangedNotifications())
		{
			// url that points to this components IOnChangeListener method
			CharSequence url = group.urlFor(IOnChangeListener.INTERFACE, new PageParameters());

			Form<?> form = group.findParent(Form.class);
			if (form != null)
			{
				tag.put("onclick", form.getJsForInterfaceUrl(url));
			}
			else
			{
				// NOTE: do not encode the url as that would give invalid JavaScript
				tag.put("onclick", "window.location.href='" + url +
					(url.toString().indexOf('?') > -1 ? "&" : "?") + group.getInputName() +
					"=' + this.value;");
			}
		}

		if (!isEnabledInHierarchy())
		{
			tag.put(ATTR_DISABLED, ATTR_DISABLED);
		}

	}

	/**
	 * The value will be made available to the validator property by means of ${label}. It does not
	 * have any specific meaning to Radio itself.
	 * 
	 * @param labelModel
	 * @return this for chaining
	 */
	@Override
	public Radio<T> setLabel(IModel<String> labelModel)
	{
		super.setLabel(labelModel);
		return this;
	}

	@Override
	@SuppressWarnings("unchecked")
	public final IModel<T> getModel()
	{
		return (IModel<T>)getDefaultModel();
	}

	@Override
	public final void setModel(IModel<T> model)
	{
		setDefaultModel(model);
	}

	@Override
	@SuppressWarnings("unchecked")
	public final T getModelObject()
	{
		return (T)getDefaultModelObject();
	}

	@Override
	public final void setModelObject(T object)
	{
		setDefaultModelObject(object);
	}

	/** {@inheritDoc} */
	@Override
	protected boolean getStatelessHint()
	{
		// because we keep uuid this component cannot be stateless
		return false;
	}
}
