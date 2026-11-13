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
package org.apache.wicket.markup.html.form.validation;

import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;

/**
 * Adapts {@link IFormValidator} to {@link Behavior}
 * 
 * @author igor
 */
public class FormValidatorAdapter extends Behavior implements IFormValidator
{
	private static final long serialVersionUID = 1L;

	private final IFormValidator validator;

	/**
	 * Constructor
	 * 
	 * @param validator
	 */
	public FormValidatorAdapter(IFormValidator validator)
	{
		this.validator = validator;
	}

	/** {@inheritDoc} */
	@Override
	public FormComponent<?>[] getDependentFormComponents()
	{
		return validator.getDependentFormComponents();
	}

	/** {@inheritDoc} */
	@Override
	public void validate(Form<?> form)
	{
		validator.validate(form);
	}

	/**
	 * @return form validator
	 */
	public IFormValidator getValidator()
	{
		return validator;
	}
}