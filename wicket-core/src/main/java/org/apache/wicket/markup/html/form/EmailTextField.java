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
import org.apache.wicket.model.Model;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.validator.EmailAddressValidator;

/**
 * A {@link TextField} for HTML5 &lt;input&gt; with type <em>email</em>.
 * 
 * <p>
 * Automatically validates that the input is a valid email address.
 * </p>
 * <p>
 * <strong>Note</strong>: This component does <strong>not</strong> support multiple values! That is
 * &lt;input type=&quot;email&quot; multiple .../&gt; cannot be validated with the default email
 * validator
 */
public class EmailTextField extends TextField<String>
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 * 
	 * @param id
	 *            component id
	 * @param emailAddress
	 *            the email input value
	 */
	public EmailTextField(String id, final String emailAddress)
	{
		this(id, new Model<String>(emailAddress));
	}

	/**
	 * Construct.
	 * 
	 * @param id
	 *            see Component
	 * @param model
	 *            the model
	 */
	public EmailTextField(String id, IModel<String> model)
	{
		this(id, model, EmailAddressValidator.getInstance());
	}

	/**
	 * Construct.
	 * 
	 * @param id
	 *            see Component
	 */
	public EmailTextField(String id)
	{
		this(id, null, EmailAddressValidator.getInstance());
	}

	/**
	 * Construct.
	 * 
	 * @param id
	 *            the component id
	 * @param model
	 *            the input value
	 * @param emailValidator
	 *            the validator that will check the correctness of the input value
	 */
	public EmailTextField(String id, IModel<String> model, IValidator<String> emailValidator)
	{
		super(id, model, String.class);

		add(emailValidator);
	}

	@Override
	protected String[] getInputTypes()
	{
		return new String[] {"email"};
	}
}
