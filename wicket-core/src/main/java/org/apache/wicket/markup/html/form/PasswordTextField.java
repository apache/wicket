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

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.model.IModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A password text field component. As you type, characters show up as asterisks or some other such
 * character so that nobody can look over your shoulder and read your password.
 * <p>
 * By default this text field is required. If it is not, call {@link #setRequired(boolean)} with
 * value of <code>false</code>.
 * 
 * @author Jonathan Locke
 */
public class PasswordTextField extends TextField<String>
{
	private static final long serialVersionUID = 1L;

	/** Log. */
	private static final Logger log = LoggerFactory.getLogger(PasswordTextField.class);

	/**
	 * Flag indicating whether the contents of the field should be reset each time it is rendered.
	 * If <code>true</code>, the contents are emptied when the field is rendered. This is useful for
	 * login forms. If <code>false</code>, the contents of the model are put into the field. This is
	 * useful for entry forms where the contents of the model should be editable, or resubmitted.
	 */
	private boolean resetPassword = true;

	/**
	 * @see org.apache.wicket.Component#Component(String)
	 */
	public PasswordTextField(final String id)
	{
		this(id, null);
	}

	/**
	 * @param id
	 * @param model
	 * @see org.apache.wicket.Component#Component(String, IModel)
	 */
	public PasswordTextField(final String id, IModel<String> model)
	{
		super(id, model);
		setRequired(true);
		setType(String.class);
	}

	/**
	 * Flag indicating whether the contents of the field should be reset each time it is rendered.
	 * If <code>true</code>, the contents are emptied when the field is rendered. This is useful for
	 * login forms. If <code>false</code>, the contents of the model are put into the field. This is
	 * useful for entry forms where the contents of the model should be editable, or resubmitted.
	 * 
	 * @return Returns the resetPassword.
	 */
	public final boolean getResetPassword()
	{
		return resetPassword;
	}

	/**
	 * Flag indicating whether the contents of the field should be reset each time it is rendered.
	 * If <code>true</code>, the contents are emptied when the field is rendered. This is useful for
	 * login forms. If <code>false</code>, the contents of the model are put into the field. This is
	 * useful for entry forms where the contents of the model should be editable, or resubmitted.
	 * 
	 * @param resetPassword
	 *            The resetPassword to set.
	 * @return <code>this</code>.
	 */
	public final PasswordTextField setResetPassword(final boolean resetPassword)
	{
		this.resetPassword = resetPassword;
		return this;
	}

	/**
	 * Processes the component tag.
	 * 
	 * @param tag
	 *            Tag to modify
	 * @see org.apache.wicket.Component#onComponentTag(ComponentTag)
	 */
	@Override
	protected void onComponentTag(final ComponentTag tag)
	{
		super.onComponentTag(tag);
		if (getResetPassword())
		{
			tag.put("value", "");
		}
	}

	/**
	 * @see org.apache.wicket.markup.html.form.TextField#getInputTypes()
	 */
	@Override
	protected String[] getInputTypes()
	{
		return new String[] {"password"};
	}
}
