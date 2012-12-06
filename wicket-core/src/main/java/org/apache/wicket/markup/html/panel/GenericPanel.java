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
package org.apache.wicket.markup.html.panel;

import org.apache.wicket.IGenericComponent;
import org.apache.wicket.model.IModel;

/**
 * A {@link Panel} with typesafe getters and setters for the model and its underlying object
 * 
 * @param <T>
 *            the type of the panel's model object
 */
public class GenericPanel<T> extends Panel implements IGenericComponent<T>
{
	private static final long serialVersionUID = 1L;

	/**
	 * @see org.apache.wicket.Component#Component(String)
	 */
	public GenericPanel(final String id)
	{
		this(id, null);
	}

	/**
	 * @param id
	 *            the component id
	 * @param model
	 *            the component model
	 * @see org.apache.wicket.Component#Component(String, IModel)
	 */
	public GenericPanel(final String id, final IModel<T> model)
	{
		super(id, model);
	}

	/**
	 * Typesafe getter for the model's object
	 * 
	 * @return the model object
	 */
	@SuppressWarnings("unchecked")
	@Override
	public final T getModelObject()
	{
		return (T)getDefaultModelObject();
	}

	/**
	 * Typesafe setter for the model object
	 * 
	 * @param modelObject
	 *            the new model object
	 */
	@Override
	public final void setModelObject(final T modelObject)
	{
		setDefaultModelObject(modelObject);
	}

	/**
	 * Typesafe getter for the page's model
	 * 
	 * @return the model
	 */
	@SuppressWarnings("unchecked")
	@Override
	public final IModel<T> getModel()
	{
		return (IModel<T>)getDefaultModel();
	}

	/**
	 * Typesafe setter for the model
	 * 
	 * @param model
	 *            the new model
	 */
	@Override
	public final void setModel(final IModel<T> model)
	{
		setDefaultModel(model);
	}
}
