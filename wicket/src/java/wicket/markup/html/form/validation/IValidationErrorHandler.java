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

import wicket.FeedbackMessages;

/**
 * Interface to code which handles validation errors.  FeedbackPanel and
 * FormComponentFeedbackBorder both implement this method so they can act
 * as error handlers when a Form fails to validate.  But any component can 
 * implement the IValidationErrorHandler interface if it wishes to 
 * participate in the process of form validation and user feedback.
 * <p>
 * When a form is submitted and one or more validation errors occurs, 
 * logic in the Form.handleErrors() method traverses the component hierarchy
 * of the Form being submitted, calling any IValidationErrorHandler interfaces 
 * it finds and passing a FeedbackMessages object containing one or more
 * errors to the interface implementer's validationError() method.
 *
 * @author Jonathan Locke
 * @author Eelco Hillenius
 */
public interface IValidationErrorHandler
{
    /**
     * Called when validation errors are encountered.
     * @param messages A structure holding any validation errors.
     * @see FeedbackMessages
     */
    public void validationError(final FeedbackMessages messages);
}

///////////////////////////////// End of File /////////////////////////////////
