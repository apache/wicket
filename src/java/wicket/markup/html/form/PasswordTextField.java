/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket.markup.html.form;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.IModel;
import wicket.Model;
import wicket.PropertyModel;
import wicket.RenderException;
import wicket.RequestCycle;
import wicket.markup.ComponentTag;


/**
 * A password text field component. As you type, characters show up as asterisks or some
 * other such character so that nobody can look over your shoulder and read your password.
 * @author Jonathan Locke
 */
public final class PasswordTextField extends FormComponent implements FormComponent.ICookieValue
{
    // Code broadcaster for reporting
    private static final Log log = LogFactory.getLog(PasswordTextField.class);

    /** Serial Version ID */
	private static final long serialVersionUID = 1776665507834380353L;


	/**
	 * Flag indicating whether the contents of the field should be reset each time it is rendered.
	 * If <code>true</code>, the contents are emptied when the field is rendered. This is useful
	 * for login forms. If <code>false</code>, the contents of the model are put into the field.
	 * This is useful for entry forms where the contents of the model should be editable, or
	 * resubmitted.
	 */
	private boolean resetPassword = true;
	
	/**
     * Constructor that uses the provided {@link IModel}as its model. All components have
     * names. A component's name cannot be null.
     * @param name The non-null name of this component
     * @param model
     * @throws RenderException Thrown if the component has been given a null name.
     */
    public PasswordTextField(String name, IModel model)
    {
        super(name, model);
    }

    /**
     * Constructor that uses the provided instance of {@link IModel}as a dynamic model.
     * This model will be wrapped in an instance of {@link PropertyModel}using the
     * provided expression. Thus, using this constructor is a short-hand for:
     * 
     * <pre>
     * new MyComponent(name, new PropertyModel(myIModel, expression));
     * </pre>
     * 
     * All components have names. A component's name cannot be null.
     * @param name The non-null name of this component
     * @param model the instance of {@link IModel}from which the model object will be
     *            used as the subject for the given expression
     * @param expression the OGNL expression that works on the given object
     * @throws RenderException Thrown if the component has been given a null name.
     */
    public PasswordTextField(String name, IModel model, String expression)
    {
        super(name, model, expression);
    }

    /**
     * Constructor that uses the provided object as a simple model. This object will be
     * wrapped in an instance of {@link Model}. All components have names. A component's
     * name cannot be null.
     * @param name The non-null name of this component
     * @param object the object that will be used as a simple model
     * @throws RenderException Thrown if the component has been given a null name.
     */
    public PasswordTextField(String name, Serializable object)
    {
        super(name, object);
    }

    /**
     * Constructor that uses the provided object as a dynamic model. This object will be
     * wrapped in an instance of {@link Model}that will be wrapped in an instance of
     * {@link PropertyModel}using the provided expression. Thus, using this constructor
     * is a short-hand for:
     * 
     * <pre>
     * new MyComponent(name, new PropertyModel(new Model(object), expression));
     * </pre>
     * 
     * All components have names. A component's name cannot be null.
     * @param name The non-null name of this component
     * @param object the object that will be used as the subject for the given expression
     * @param expression the OGNL expression that works on the given object
     * @throws RenderException Thrown if the component has been given a null name.
     */
    public PasswordTextField(String name, Serializable object, String expression)
    {
        super(name, object, expression);
    }

    /**
     * @see wicket.Component#handleComponentTag(RequestCycle, ComponentTag)
     */
    protected void handleComponentTag(final RequestCycle cycle, final ComponentTag tag)
    {
        checkTag(tag, "input");
        checkAttribute(tag, "type", "password");
        super.handleComponentTag(cycle, tag);
		if (isResetPassword())
		{
			tag.put("value", "");
		}
		else
		{
			tag.put("value", getModelObjectAsString());
		}
    }

    /**
     * @param cycle The request cycle
     */
    public void updateModel(final RequestCycle cycle)
    {
        setModelObject(getRequestString(cycle));
    }

    /**
     * @see wicket.markup.html.form.FormComponent.ICookieValue#getCookieValue()
     */
    public String getCookieValue()
    {
        final String value = getModelObjectAsString();
        try
        {
            return getPage().getApplicationSettings().getCryptInstance().encryptStringToString(value);
        }
        catch (Exception ex) 
        {
            log.error("Failed to instantiate encryption object. Continue without encryption");
        }
        
        return value;
    }

    /**
     * @see wicket.markup.html.form.FormComponent.ICookieValue#setCookieValue(java.lang.String)
     */
    public void setCookieValue(String value)
    {
        String decryptedValue;
        try
        {
            decryptedValue = getPage().getApplicationSettings().getCryptInstance().decryptStringToString(value);
        } 
        catch (Exception ex) 
        {
            decryptedValue = value;
            log.error("Failed to instantiate encryption object. Continue without encryption");
        }
        
        setModelObject(decryptedValue);
    }

    /**
	 * Flag indicating whether the contents of the field should be reset each time it is rendered.
	 * If <code>true</code>, the contents are emptied when the field is rendered. This is useful
	 * for login forms. If <code>false</code>, the contents of the model are put into the field.
	 * This is useful for entry forms where the contents of the model should be editable, or
	 * resubmitted.
	 * 
	 * @return Returns the resetPassword.
	 */
	public final boolean isResetPassword()
	{
		return resetPassword;
	}

	/**
	 * Flag indicating whether the contents of the field should be reset each time it is rendered.
	 * If <code>true</code>, the contents are emptied when the field is rendered. This is useful
	 * for login forms. If <code>false</code>, the contents of the model are put into the field.
	 * This is useful for entry forms where the contents of the model should be editable, or
	 * resubmitted.
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
}

///////////////////////////////// End of File /////////////////////////////////
