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

import org.apache.wicket.model.IModel;

/**
 * A text field which automatically adds a Required. This is mainly for convenience, since you can
 * always add(new Required()) manually.
 * 
 * @author Jonathan Locke
 * @param <T>
 *            the model object's type
 */
public class RequiredTextField<T> extends TextField<T>
{
	private static final long serialVersionUID = 1L;

	/**
	 * @see org.apache.wicket.Component#Component(String)
	 */
	public RequiredTextField(final String id)
	{
		super(id);
		setRequired(true);
	}

	/**
	 * @see TextField#TextField(String, Class)
	 */
	public RequiredTextField(final String id, final Class<T> type)
	{
		super(id, type);
		setRequired(true);
	}

	/**
	 * @param id
	 * @param model
	 * @see org.apache.wicket.Component#Component(String, IModel)
	 */
	public RequiredTextField(final String id, final IModel<T> model)
	{
		super(id, model);
		setRequired(true);
	}

	/**
	 * @param id
	 *            See Component
	 * @param model
	 *            See Component
	 * @param type
	 *            The type to use when updating the model for this text field
	 * @see org.apache.wicket.Component#Component(String, IModel)
	 */
	public RequiredTextField(final String id, IModel<T> model, Class<T> type)
	{
		super(id, model, type);
		setRequired(true);
	}
}