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
package wicket.markup.html.form.validation;

/**
 * A validator for strings designed for subclassing. A subclass implements
 * onValidate() to validate the component and its string value.
 * 
 * @author Jonathan Locke
 */
public abstract class StringValidator extends AbstractValidator
{
	/**
	 * @see wicket.markup.html.form.validation.AbstractValidator#onValidate()
	 */
	public void onValidate()
	{
		onValidate(getStringValue());		
	}

	/**
	 * Subclasses should override this method to validate the string value for a
	 * component.
	 * 
	 * @param value
	 *            The string value to validate
	 */
	public abstract void onValidate(String value);
}
