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

import org.apache.wicket.Component;
import org.apache.wicket.IGenericComponent;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.Strings;


/**
 * Component representing a single checkbox choice in a
 * org.apache.wicket.markup.html.form.CheckGroup.
 * 
 * Must be attached to an &lt;input type=&quot;checkbox&quot; ... &gt; markup.
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
 * @see org.apache.wicket.markup.html.form.CheckGroup
 * 
 * @author Igor Vaynberg
 * 
 * @param <T>
 *            The model object type
 */
public class Check<T> extends LabeledWebMarkupContainer implements IGenericComponent<T>
{
	private static final long serialVersionUID = 1L;

	private static final String ATTR_DISABLED = "disabled";

	/**
	 * page-scoped uuid of this check. this property must not be accessed directly, instead
	 * {@link #getValue()} must be used
	 */
	private int uuid = -1;

	private final CheckGroup<T> group;

	/**
	 * @see WebMarkupContainer#WebMarkupContainer(String)
	 */
	public Check(String id)
	{
		this(id, null, null);
	}

	/**
	 * @param id
	 * @param model
	 * @see WebMarkupContainer#WebMarkupContainer(String, IModel)
	 */
	public Check(String id, IModel<T> model)
	{
		this(id, model, null);
	}

	/**
	 * @param id
	 * @param group
	 *            parent {@link CheckGroup} of this check
	 * @see WebMarkupContainer#WebMarkupContainer(String)
	 */
	public Check(String id, CheckGroup<T> group)
	{
		this(id, null, group);
	}

	/**
	 * @param id
	 * @param model
	 * @param group
	 *            parent {@link CheckGroup} of this check
	 * @see WebMarkupContainer#WebMarkupContainer(String, IModel)
	 */
	public Check(String id, IModel<T> model, CheckGroup<T> group)
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
		return "check" + uuid;
	}

	@SuppressWarnings("unchecked")
	protected CheckGroup<T> getGroup()
	{
		CheckGroup<T> group = this.group;
		if (group == null)
		{
			group = findParent(CheckGroup.class);
			if (group == null)
			{
				throw new WicketRuntimeException("Check component [" + getPath() +
					"] cannot find its parent CheckGroup");
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

		// must be attached to <input type="checkbox" .../> tag
		checkComponentTag(tag, "input");
		checkComponentTagAttribute(tag, "type", "checkbox");

		CheckGroup<?> group = getGroup();

		final String uuid = getValue();

		// assign name and value
		tag.put("name", group.getInputName());
		tag.put("value", uuid);

		// check if the model collection of the group contains the model object.
		// if it does check the check box.
		Collection<?> collection = (Collection<?>)group.getDefaultModelObject();

		// check for npe in group's model object
		if (collection == null)
		{
			throw new WicketRuntimeException("CheckGroup [" + group.getPath() +
				"] contains a null model object, must be an object of type java.util.Collection");
		}

		if (group.hasRawInput())
		{
			final String raw = group.getRawInput();
			if (!Strings.isEmpty(raw))
			{
				final String[] values = raw.split(FormComponent.VALUE_SEPARATOR);
				for (String value : values)
				{
					if (uuid.equals(value))
					{
						tag.put("checked", "checked");
					}
				}
			}
		}
		else if (collection.contains(getDefaultModelObject()))
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

		if (!isActionAuthorized(ENABLE) || !isEnabledInHierarchy() || !group.isEnabledInHierarchy())
		{
			tag.put(ATTR_DISABLED, ATTR_DISABLED);
		}

	}

	/**
	 * The value will be made available to the validator property by means of ${label}. It does not
	 * have any specific meaning to Check itself.
	 * 
	 * @param labelModel
	 * @return this for chaining
	 */
	@Override
	public Check<T> setLabel(IModel<String> labelModel)
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
		// because this component uses uuid field it cannot be stateless
		return false;
	}
}
