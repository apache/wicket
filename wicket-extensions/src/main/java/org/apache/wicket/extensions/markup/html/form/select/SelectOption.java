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
package org.apache.wicket.extensions.markup.html.form.select;

import org.apache.wicket.Component;
import org.apache.wicket.IGenericComponent;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;

/**
 * Component representing a single <code>&lt;option&gt;</code> html element
 *
 * @see Select
 * @param <T>
 * 
 * @author Igor Vaynberg
 */
public class SelectOption<T> extends WebMarkupContainer implements IGenericComponent<T>
{
	private static final long serialVersionUID = 1L;

	/**
	 * page-scoped uuid of this option. this property must not be accessed directly, instead
	 * {@link #getValue()} must be used
	 */
	private int uuid = -1;

	/**
	 * @see WebMarkupContainer#WebMarkupContainer(String)
	 */
	public SelectOption(final String id)
	{
		super(id);
	}

	/**
	 * @param id
	 * @param model
	 * @see WebMarkupContainer#WebMarkupContainer(String, IModel)
	 */
	public SelectOption(final String id, final IModel<? extends T> model)
	{
		super(id, model);
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

	@SuppressWarnings("unchecked")
	@Override
	public final T getModelObject()
	{
		return (T)getDefaultModelObject();
	}

	@Override
	public void setModelObject(T object)
	{
		setDefaultModelObject(object);
	}

	/**
	 * Form submission value used for this select option. This string will appear as the value of
	 * the <code>value</code> html attribute for the <code>option</code> tag.
	 * 
	 * @return form submission value
	 */
	public String getValue()
	{
		if (uuid < 0)
		{
			uuid = getPage().getAutoIndex();
		}
		return "option" + uuid;
	}

	/**
	 * @see Component#onComponentTag(ComponentTag)
	 * @param tag
	 *            the abstraction representing html tag of this component
	 */
	@Override
	protected void onComponentTag(final ComponentTag tag)
	{
		// must be attached to <option .../> tag
		checkComponentTag(tag, "option");

		Select<?> select = findParent(Select.class);
		if (select == null)
		{
			throw new WicketRuntimeException(
				"SelectOption component [" +
					getPath() +
					"] cannot find its parent Select. All SelectOption components must be a child of or below in the hierarchy of a Select component.");
		}

		final String uuid = getValue();

		// assign value
		tag.put("value", uuid);

		if (select.isSelected(this))
		{
			tag.put("selected", "selected");
		}

		// Default handling for component tag
		super.onComponentTag(tag);
	}
}
