/*
 * $Id$ $Revision$
 * $Date$
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

import wicket.model.IModel;

/**
 * Abstract base class for TextArea and TextField.
 * 
 * @author Jonathan Locke
 */
abstract class AbstractTextComponent extends FormComponent
{
	/**
	 * @see wicket.Component#Component(String)
	 */
	public AbstractTextComponent(String id)
	{
		super(id);
		setIsInputNullOnEmptyString(true);
	}

	/**
	 * @see wicket.Component#Component(String, IModel)
	 */
	AbstractTextComponent(final String id, final IModel model)
	{
		super(id, model);
	}

	/**
	 * @see FormComponent#supportsPersistence()
	 */
	protected final boolean supportsPersistence()
	{
		return true;
	}

	/**
	 * Should the bound object become <code>null</code> when the input is
	 * empty?
	 * 
	 * @return <code>true</code> when the value will be set to
	 *         <code>null</code> when the input is empty.
	 */
	protected final boolean isInputNullOnEmptyString()
	{
		return getFlag(FLAG_EMPTY_STRING_TO_NULL);
	}

	/**
	 * Should the bound object become <code>null</code> when the input is
	 * empty?
	 * 
	 * @param flag
	 *            the value to set this flag.
	 * @return this
	 */
	public final AbstractTextComponent setIsInputNullOnEmptyString(boolean flag)
	{
		setFlag(FLAG_EMPTY_STRING_TO_NULL, flag);
		return this;
	}

	/**
	 * Updates this components' model from the request.
	 * 
	 * @see wicket.markup.html.form.FormComponent#updateModel()
	 */
	protected void updateModel()
	{
		String input = getInput();
		if (isInputNullOnEmptyString() && "".equals(input))
			input = null;
		setModelObject(input);
	}
}
