/*
 * $Id$ $Revision:
 * 3618 $ $Date$
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.markup.ComponentTag;
import wicket.model.IModel;

/**
 * A password text field component. As you type, characters show up as asterisks
 * or some other such character so that nobody can look over your shoulder and
 * read your password.
 * <p>
 * By default this text field is required. If it is not, call
 * {@link #setRequired(boolean)} with value of <code>false</code>.
 * 
 * @author Jonathan Locke
 */
public class PasswordTextField extends TextField
{
	private static final long serialVersionUID = 1L;

	/** Log. */
	private static final Log log = LogFactory.getLog(PasswordTextField.class);

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
	public PasswordTextField(final String id)
	{
		super(id);
		setRequired(true);
	}

	/**
	 * @see wicket.Component#Component(String, IModel)
	 */
	public PasswordTextField(final String id, IModel model)
	{
		super(id, model);
		setRequired(true);
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
	 * @see FormComponent#getModelValue()
	 */
	public final String getModelValue()
	{
		final String value = getModelObjectAsString();
		if(value != null)
		{
			try
			{
				// TODO kept for backwards compatibility. Replace with encryptUrlSafe after 1.2
				return getApplication().getSecuritySettings().getCryptFactory().newCrypt().encrypt(
						value);
			}
			catch (Exception ex)
			{
				log.error("Failed to instantiate encryption object. Continue without encryption");
			}
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
	 * @see wicket.markup.html.form.FormComponent#setModelValue(java.lang.String[])
	 */
	public final void setModelValue(String[] valueArray)
	{
		String value = valueArray != null && valueArray.length > 0? valueArray[0]: null;
		String decryptedValue;
		try
		{
			// TODO kept for backwards compatibility. Replace with decryptUrlSafe after 1.2
			decryptedValue = getApplication().getSecuritySettings().getCryptFactory().newCrypt()
					.decrypt(value);
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
		super.onComponentTag(tag);
		tag.put("value", getResetPassword() ? "" : getModelObjectAsString());
	}
	
	protected String getInputType()
	{
		return "password";
	}
	
}
