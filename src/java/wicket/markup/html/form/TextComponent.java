/*
 * $Id$ $Revision$
 * $Date$
 * 
 * ==================================================================== Licensed
 * under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the
 * License at
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

/**
 * Abstract base class for TextArea and TextField.
 * 
 * @author Jonathan Locke
 */
public abstract class TextComponent extends FormComponent
{
	/** Serial Version ID. */
	private static final long serialVersionUID = -1323747673401786242L;

	/**
	 * When the user input does not validate, this is a temporary store for the
	 * input he/she provided. We have to store it somewhere as we loose the
	 * request parameter when redirecting.
	 */
	private String invalidInput;

	/**
     * @see wicket.Component#Component(String, Serializable)
	 */
	public TextComponent(final String name, final Serializable object)
	{
		super(name, object);
	}

	/**
     * @see wicket.Component#Component(String, Serializable, String)
	 */
	public TextComponent(final String name, final Serializable object, final String expression)
	{
		super(name, object, expression);
	}

    /**
     * @see FormComponent#supportsPersistence()
     */
    public final boolean supportsPersistence()
    {
        return true;
    }

	/**
	 * Updates this components' model from the request.
	 * 
	 * @see wicket.markup.html.form.FormComponent#updateModel()
	 */
	public void updateModel()
	{
        // Component validated, so clear the input
		invalidInput = null; 
		setModelObject(getRequestString());
	}
    
    /**
     * @return Returns the invalidInput.
     */
    protected String getInvalidInput()
    {
        return invalidInput;
    }

	/**
	 * Handle a validation error.
	 * 
	 * @see wicket.markup.html.form.FormComponent#invalid()
	 */
	protected void invalid()
	{
		// Store the user input for form repopulation
		invalidInput = getRequestString();
	}
}