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
package org.apache.wicket.markup.html.basic;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.parser.XmlTag.TagType;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.lang.Classes;

/**
 * A Label component that is used to render an enum value. The value renderered will be the result
 * of a i18n resource lookup of the following form:
 * {@code <value.getClass().getSimpleName()>.<value.name()>}, this format can be changed by
 * overriding {@link #resourceKey(Enum)}
 * 
 * @author igor.vaynberg
 * @param <T>
 *            enum type
 */
public class EnumLabel<T extends Enum<T>> extends WebComponent
{
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 * 
	 * @param id
	 *            See Component
	 */
	public EnumLabel(final String id)
	{
		super(id);
	}

	/**
	 * Convenience constructor. Same as Label(String, new Model&lt;String&gt;(String))
	 * 
	 * @param id
	 *            See Component
	 * @param value
	 *            Enum value to render
	 * 
	 * @see org.apache.wicket.Component#Component(String, IModel)
	 */
	public EnumLabel(final String id, T value)
	{
		this(id, new Model<T>(value));
	}

	/**
	 * @param id
	 * @param model
	 * @see org.apache.wicket.Component#Component(String, IModel)
	 */
	public EnumLabel(final String id, IModel<T> model)
	{
		super(id, model);
	}

	@Override
	public void onComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag)
	{
		replaceComponentTagBody(markupStream, openTag, getStringValue());
	}

	/**
	 * Converts model object into the display value
	 * 
	 * @return display value
	 */
	private String getStringValue()
	{
		T value = getModelObject();
		String converted = value != null ? getString(resourceKey(value)) : nullValue();
		return getDefaultModelObjectAsString(converted);
	}

	/**
	 * Converts enum value into a resource key that should be used to lookup the text the label will
	 * display
	 * 
	 * @param value
	 * @return resource key
	 */
	protected String resourceKey(T value)
	{
		return Classes.simpleName(value.getDeclaringClass()) + '.' + value.name();
	}

	/**
	 * @return value that should be displayed if model object is {@code null}
	 */
	protected String nullValue()
	{
		return "";
	}

	@Override
	protected void onComponentTag(ComponentTag tag)
	{
		super.onComponentTag(tag);
		// always transform the tag to <span></span> so even labels defined as <span/> render
		tag.setType(TagType.OPEN);
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