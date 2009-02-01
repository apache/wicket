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
import org.apache.wicket.RequestContext;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.protocol.http.portlet.PortletRequestContext;
import org.apache.wicket.util.lang.Objects;

/**
 * Component representing a single radio choice in a org.apache.wicket.markup.html.form.RadioGroup.
 * 
 * Must be attached to an &lt;input type=&quot;radio&quot; ... &gt; markup.
 * 
 * @see org.apache.wicket.markup.html.form.RadioGroup
 * 
 * @author Igor Vaynberg
 * @author Sven Meier (svenmeier)
 * 
 * @param <T>
 *            The model object type
 */
public class Radio<T> extends LabeledWebMarkupContainer
{
	private static final long serialVersionUID = 1L;
	private static final String ATTR_DISABLED = "disabled";

	/**
	 * page-scoped uuid of this check. this property must not be accessed directly, instead
	 * {@link #getValue()} must be used
	 */
	private short uuid = -1;

	private final RadioGroup<T> group;


	/**
	 * @see WebMarkupContainer#WebMarkupContainer(String)
	 */
	public Radio(String id)
	{
		this(id, null, null);
	}

	/**
	 * @param id
	 * @param model
	 * @see WebMarkupContainer#WebMarkupContainer(String, IModel)
	 */
	public Radio(String id, IModel<T> model)
	{
		this(id, model, null);
	}

	/**
	 * @param group
	 *            parent {@link RadioGroup}
	 * @see WebMarkupContainer#WebMarkupContainer(String)
	 */
	public Radio(String id, RadioGroup<T> group)
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
	public Radio(String id, IModel<T> model, RadioGroup<T> group)
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
	public final String getValue()
	{
		if (uuid < 0)
		{
			uuid = getPage().getAutoIndex();
		}
		return "radio" + uuid;
	}

	/** {@inheritDoc} */
	@Override
	protected void onBeforeRender()
	{
		// prefix markup id of this radio with its group's id
		// this will make it easier to identify all radios that belong to a specific group
		setMarkupId(getGroup().getMarkupId() + "-" + getMarkupId());
		super.onBeforeRender();
	}


	@SuppressWarnings("unchecked")
	private RadioGroup<T> getGroup()
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
		else if (Objects.equal(group.getDefaultModelObject(), getDefaultModelObject()))
		{
			tag.put("checked", "checked");
		}

		if (group.wantOnSelectionChangedNotifications())
		{
			// url that points to this components IOnChangeListener method
			CharSequence url = group.urlFor(IOnChangeListener.INTERFACE);

			Form<?> form = group.findParent(Form.class);
			if (form != null)
			{
				RequestContext rc = RequestContext.get();
				if (rc.isPortletRequest())
				{
					// restore url back to real wicket path as its going to be interpreted by the
					// form itself
					url = ((PortletRequestContext)rc).getLastEncodedPath();
				}
				tag.put("onclick", form.getJsForInterfaceUrl(url));
			}
			else
			{
				// TODO: following doesn't work with portlets, should be posted to a dynamic hidden
				// form
				// with an ActionURL or something
				// NOTE: do not encode the url as that would give invalid
				// JavaScript
				tag.put("onclick", "window.location.href='" + url +
					(url.toString().indexOf('?') > -1 ? "&amp;" : "?") + group.getInputName() +
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
	public Radio<T> setLabel(IModel<String> labelModel)
	{
		setLabelInternal(labelModel);
		return this;
	}


	/**
	 * Gets model
	 * 
	 * @return model
	 */
	@SuppressWarnings("unchecked")
	public final IModel<T> getModel()
	{
		return (IModel<T>)getDefaultModel();
	}

	/**
	 * Sets model
	 * 
	 * @param model
	 */
	public final void setModel(IModel<T> model)
	{
		setDefaultModel(model);
	}

	/**
	 * Gets model object
	 * 
	 * @return model object
	 */
	@SuppressWarnings("unchecked")
	public final T getModelObject()
	{
		return (T)getDefaultModelObject();
	}

	/**
	 * Sets model object
	 * 
	 * @param object
	 */
	public final void setModelObject(T object)
	{
		setDefaultModelObject(object);
	}


}
