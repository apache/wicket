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

import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.model.IModel;

/**
 * Abstract class for links that are capable of submitting a form.
 * 
 * @author Matej Knopp
 * 
 */
public abstract class AbstractSubmitLink extends AbstractLink implements IFormSubmittingComponent
{
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Target form or null if the form is parent of the link.
	 */
	private Form<?> form;

	/**
	 * If false, all standard processing like validating and model updating is skipped.
	 */
	private boolean defaultFormProcessing = true;

	/**
	 * Construct.
	 * 
	 * @param id
	 * @param model
	 */
	public AbstractSubmitLink(String id, IModel<?> model)
	{
		super(id, model);
	}

	/**
	 * 
	 * Construct.
	 * 
	 * @param id
	 */
	public AbstractSubmitLink(String id)
	{
		super(id);
	}

	/**
	 * Construct.
	 * 
	 * @param id
	 * @param model
	 * @param form
	 */
	public AbstractSubmitLink(String id, IModel<?> model, Form<?> form)
	{
		super(id, model);
		this.form = form;
	}

	/**
	 * Construct.
	 * 
	 * @param id
	 * @param form
	 */
	public AbstractSubmitLink(String id, Form<?> form)
	{
		super(id);
		this.form = form;
	}

	/**
	 * Sets the defaultFormProcessing property. When false (default is true), all validation and
	 * form updating is bypassed and the onSubmit method of that button is called directly, and the
	 * onSubmit method of the parent form is not called. A common use for this is to create a cancel
	 * button.
	 * 
	 * TODO: This is a copy & paste from Button
	 * 
	 * @param defaultFormProcessing
	 *            defaultFormProcessing
	 * @return This
	 */
	@Override
	public final AbstractSubmitLink setDefaultFormProcessing(boolean defaultFormProcessing)
	{
		if (this.defaultFormProcessing != defaultFormProcessing)
		{
			addStateChange();
		}

		this.defaultFormProcessing = defaultFormProcessing;
		return this;
	}

	/**
	 * @see org.apache.wicket.markup.html.form.IFormSubmittingComponent#getDefaultFormProcessing()
	 */
	@Override
	public boolean getDefaultFormProcessing()
	{
		return defaultFormProcessing;
	}

	/**
	 * @see org.apache.wicket.markup.html.form.IFormSubmittingComponent#getForm()
	 */
	@Override
	public Form<?> getForm()
	{
		if (form != null)
		{
			return form;
		}
		else
		{
			return findParent(Form.class);
		}
	}

	/**
	 * @see org.apache.wicket.markup.html.form.IFormSubmittingComponent#getInputName()
	 */
	@Override
	public String getInputName()
	{
		return Form.getRootFormRelativeId(this);
	}
}
