/*
 * $Id$
 * $Revision$ $Date$
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.markup.html.form;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.markup.ComponentTag;

/**
 * A password text field component. As you type, characters show up as asterisks
 * or some other such character so that nobody can look over your shoulder and
 * read your password.
 * 
 * @author Jonathan Locke
 */
public class PasswordTextField extends TextField
{
	/** Log. */
	private static final Log log = LogFactory.getLog(PasswordTextField.class);

	/** Serial Version ID. */
	private static final long serialVersionUID = 1776665507834380353L;

	/**
	 * Flag indicating whether the contents of the field should be reset each
	 * time it is rendered. If <code>true</code>, the contents are emptied
	 * when the field is rendered. This is useful for login forms. If
	 * <code>false</code>, the contents of the model are put into the field.
	 * This is useful for entry forms where the contents of the model should be
	 * editable, or resubmitted.
	 */
	private boolean resetPassword = true;

	/**
	 * @see wicket.Component#Component(String)
	 */
	public PasswordTextField(final String name)
	{
		super(name);
	}

	/**
	 * @see wicket.Component#Component(String, Serializable)
	 */
	public PasswordTextField(String name, Serializable object)
	{
		super(name, object);
	}

	/**
	 * @see wicket.Component#Component(String, Serializable, String)
	 */
	public PasswordTextField(String name, Serializable object, String expression)
	{
		super(name, object, expression);
	}

	/**
	 * Flag indicating whether the contents of the field should be reset each
	 * time it is rendered. If <code>true</code>, the contents are emptied
	 * when the field is rendered. This is useful for login forms. If
	 * <code>false</code>, the contents of the model are put into the field.
	 * This is useful for entry forms where the contents of the model should be
	 * editable, or resubmitted.
	 * 
	 * @return Returns the resetPassword.
	 */
	public final boolean getResetPassword()
	{
		return resetPassword;
	}

	/**
	 * @see wicket.markup.html.form.FormComponent#getValue()
	 */
	public final String getValue()
	{
		final String value = getModelObjectAsString();
		try
		{
			return getApplication().getCrypt().encryptString(value);
		}
		catch (Exception ex)
		{
			log.error("Failed to instantiate encryption object. Continue without encryption");
		}

		return value;
	}

	/**
	 * Flag indicating whether the contents of the field should be reset each
	 * time it is rendered. If <code>true</code>, the contents are emptied
	 * when the field is rendered. This is useful for login forms. If
	 * <code>false</code>, the contents of the model are put into the field.
	 * This is useful for entry forms where the contents of the model should be
	 * editable, or resubmitted.
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
	 * @see wicket.markup.html.form.FormComponent#setValue(java.lang.String)
	 */
	public final void setValue(String value)
	{
		String decryptedValue;
		try
		{
			decryptedValue = getApplication().getCrypt().decryptString(value);
		}
		catch (Exception ex)
		{
			decryptedValue = value;
			log.error("Failed to instantiate encryption object. Continue without encryption");
		}

		setModelObject(decryptedValue);
	}

	/**
	 * Processes the component tag.
	 * 
	 * @param tag
	 *            Tag to modify
	 * @see wicket.Component#onComponentTag(ComponentTag)
	 */
	protected final void onComponentTag(final ComponentTag tag)
	{
		checkComponentTag(tag, "input");
		checkComponentTagAttribute(tag, "type", "password");
		super.onComponentTag(tag);
		tag.put("value", getResetPassword() ? "" : getModelObjectAsString());
	}
}
