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
 * TextField doesn't permit the html <input type='hidden'> so this is a simple subclass to allow
 * this
 * 
 * A HiddenField is useful when you have a javascript based component that updates the form state.
 * Either
 * 
 * <ul>
 * <li>add a AttributeModified to set the id attribute, then use document.getElementById(id)</li>
 * <li>lookup the field name=getPath() within the form</li>
 * </ul>
 * 
 * @author Cameron Braid
 * @param <T>
 *            the model object's type
 */
public class HiddenField<T> extends TextField<T>
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 * 
	 * @param id
	 *            component id
	 */
	public HiddenField(String id)
	{
		super(id);
	}

	/**
	 * Construct.
	 * 
	 * @param id
	 *            component id
	 * @param type
	 *            the type to use when updating the model for this text field
	 */
	public HiddenField(String id, Class<T> type)
	{
		super(id, type);
	}

	/**
	 * Construct.
	 * 
	 * @param id
	 *            see Component
	 * @param model
	 *            the model
	 */
	public HiddenField(String id, IModel<T> model)
	{
		super(id, model);
	}

	/**
	 * @param id
	 *            component id
	 * @param model
	 *            the model
	 * @param type
	 *            the type to use when updating the model for this text field
	 * @see org.apache.wicket.Component#Component(String, IModel)
	 */
	public HiddenField(String id, IModel<T> model, Class<T> type)
	{
		super(id, model, type);
	}

	/**
	 * @see org.apache.wicket.markup.html.form.TextField#getInputType()
	 */
	@Override
	protected String[] getInputType()
	{
		return new String[] {"hidden"};
	}
}
