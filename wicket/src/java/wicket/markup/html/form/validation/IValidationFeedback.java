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

import wicket.markup.html.form.Form;

/**
 * Interface to code which gives validation feedback. FeedbackPanel and
 * FormComponentFeedbackBorder both implement this method so they can give the
 * user feedback when a Form fails to validate. But any component can implement
 * the IValidationFeedback interface if it wishes to give the user feedback on
 * form validation.
 * <p>
 * When a form is submitted and one or more validation errors occurs, logic in
 * the Form.onError() method traverses the component hierarchy of the Form being
 * submitted, calling each IValidationFeedback interface it finds. Implementers
 * of the update() method can change their appearance depending on the state of
 * errors in a child component or in the Form as a whole.
 * 
 * @author Jonathan Locke
 * @author Eelco Hillenius
 */
public interface IValidationFeedback
{
	/**
	 * Called to update validation feedback
	 * 
	 * @param form
	 *            The form attached to this validation feedback component
	 */
	public void updateValidationFeedback(Form form);
}