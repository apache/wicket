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
package wicket.markup.html.form.validation;

import java.io.Serializable;

import wicket.markup.html.form.FormComponent;

/**
 * Interface to code that validates Form components. When the validate() method of the
 * interface is called by the framework, the IValidator implementation is expected to
 * check the input String it is passed
 * @author Jonathan Locke
 */
public interface IValidator extends Serializable
{
	/**
	 * An implementation of IValidator that does nothing at all.
	 */
	public static final IValidator NULL = new NullValidator();

	/**
	 * Validates the given input. The input corresponds to the input from the request for a
	 * component.
	 * @param component Component to validate
	 */
	public void validate(final FormComponent component);

	/**
	 * Validator that does nothing.
	 */
	static final class NullValidator implements IValidator
	{
		/**
		 * Returns null.
		 * @see wicket.markup.html.form.validation.IValidator#validate(wicket.markup.html.form.FormComponent)
		 */
		public void validate(final FormComponent component)
		{
		}

		/**
		 * Returns the string representation.
		 * @see java.lang.Object#toString()
		 */
		public String toString()
		{
			return "[NullValidator]";
		}
	}
}
