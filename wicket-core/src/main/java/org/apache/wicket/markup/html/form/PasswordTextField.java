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


/**
 * A password text field component. As you type, characters show up as asterisks or some other such
 * character so that nobody can look over your shoulder and read your password.
 * <p>
 * By default this text field is required. If it is not, call {@link #setRequired(boolean)} with
 * value of <code>false</code>.
 * <p>
 * Note that by default the model object is nullified after each request to prevent the entered
 * password to be serialized along with the containing page, see {@link #setResetPassword(boolean)}
 * for details.
 * 
 * @author Jonathan Locke
 */
public class PasswordTextField extends TextField<String>
{
	private static final long serialVersionUID = 1L;

	/**
	 * Should password be reset, see {@link #setResetPassword(boolean)}.
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
	 * Should password be reset, see {@link #setResetPassword(boolean)}.
	 * 
	 * @return should password be resetted
	 */
	public final boolean getResetPassword()
	{
		return resetPassword;
	}

	/**
	 * Flag indicating whether the password should be reset after each request.
	 * Additionally any present value is not rendered into the markup.
	 * <br>
	 * If <code>true</code>, the model object is set to null after each request to prevent it
	 * being serialized along with the containing page. This is default and highly recommended
	 * for login forms. If <code>false</code> the model value is handled as in a standard
	 * {@link TextField}, this is useful for entry forms where the contents of the model should
	 * be editable, or resubmitted.
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

	/**
	 * Overriden to nullify the password.
	 */
	@Override
	protected void onDetach()
	{
		if (resetPassword) {
			clearInput();

			if (getModel() != null) {
				setModelObject(null);
			}
		}

		super.onDetach();
	}
}
