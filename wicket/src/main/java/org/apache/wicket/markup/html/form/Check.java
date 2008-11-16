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
import org.apache.wicket.RequestContext;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.protocol.http.portlet.PortletRequestContext;


/**
 * Component representing a single checkbox choice in a
 * org.apache.wicket.markup.html.form.CheckGroup.
 * 
 * Must be attached to an &lt;input type=&quot;checkbox&quot; ... &gt; markup.
 * 
 * @see org.apache.wicket.markup.html.form.CheckGroup
 * 
 * @author Igor Vaynberg
 * 
 * @param <T>
 *            The model object type
 */
public class Check<T> extends LabeledWebMarkupContainer
{
	private static final long serialVersionUID = 1L;

	private static final String ATTR_DISABLED = "disabled";

	/**
	 * page-scoped uuid of this check. this property must not be accessed directly, instead
	 * {@link #getValue()} must be used
	 */
	private short uuid = -1;

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
		return "check" + uuid;
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

		CheckGroup<?> group = this.group;
		if (group == null)
		{
			group = findParent(CheckGroup.class);
			if (group == null)
			{
				throw new WicketRuntimeException("Check component [" + getPath() +
					"] cannot find its parent CheckGroup");
			}
		}

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
			final String[] input = group.getInputAsArray();

			if (input != null)
			{
				for (int i = 0; i < input.length; i++)
				{
					if (uuid.equals(input[i]))
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

		if (!isActionAuthorized(ENABLE) || !isEnabled() || !group.isEnabled())
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
	public Check<T> setLabel(IModel<String> labelModel)
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
