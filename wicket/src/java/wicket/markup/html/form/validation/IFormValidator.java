/*
 * $Id$ $Revision:
 * 1.8 $ $Date$
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

import java.io.Serializable;

import wicket.markup.html.form.Form;

/**
 * THIS INTERFACE IS NOT PART OF THE WICKET PUBLIC API. DO NOT USE IT.
 * <p>
 * Instead of subclassing IFormValidator, you should subclass AbstractFormValidator
 * <p>
 * Interface to code that validates a form. When the validate() method
 * of the interface is called by the framework, the IFormValidator implementation is
 * expected to check the form (model) or any state that is relevant for the target form.
 * 
 * @author Eelco Hillenius
 */
public interface IFormValidator extends Serializable
{
	/**
	 * An implementation of IValidator that does nothing at all.
	 */
	public static final IFormValidator NULL = new NullValidator();

	/**
	 * THIS INTERFACE IS NOT PART OF THE WICKET PUBLIC API. DO NOT USE IT.
	 * <p>
	 * Instead of subclassing IFormValidator, you should subclass AbstractFormValidator
	 * <p>
	 * Interface to code that validates a form. When the validate() method of the
	 * interface is called by the framework, the IFormValidator implementation is expected
	 * to check the form (model) or any state that is relevant for the target form.
	 * <p>
	 * Validates the given form.
	 * @param form the form to validate
	 */
	public void validate(final Form form);

	/**
	 * Validator that does nothing.
	 */
	static final class NullValidator implements IFormValidator
	{
		/**
		 * Returns null.
		 *
		 * @see wicket.markup.html.form.validation.IFormValidator#validate(wicket.markup.html.form.Form)
		 */
		public synchronized void validate(final Form form)
		{
		}

		/**
		 * Returns the string representation.
		 * 
		 * @see java.lang.Object#toString()
		 */
		public String toString()
		{
			return "[NullFormValidator]";
		}
	}
}
