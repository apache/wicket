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
 * Interface to code which handles validation errors.
 *
 * @author Jonathan Locke
 * @author Eelco Hillenius
 */
public interface IValidationErrorHandler
{ // TODO finalize javadoc
    /**
     * Called when any validation errors were encountered.
     * @param errors the structure where navigation errors are recorded in.
     * @see FeedbackMessages
     */
    public void validationError(final FeedbackMessages errors);
}
