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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.Component;
import wicket.FeedbackMessages;
import wicket.Component.IVisitor;
import wicket.markup.html.form.validation.ValidationErrorMessage;
import wicket.markup.html.form.validation.ValidationErrorModelDecorator;

/**
 * Delegate for form validation. Implementors provide the actual validation checking.
 *
 * @author Jonathan Locke
 * @author Eelco Hillenius
 */
public interface IFormValidationDelegate
{
	/**
	 * Validates the form and return the collected feeback messages.
	 * @param form the form that the validation is applied to
	 * @return the collected feedback messages
	 */
	FeedbackMessages validate(Form form);

    /**
     * The default form validation delegate.
     */
    final class DefaultFormValidationDelegate implements IFormValidationDelegate
    {
    	/** log. */
    	private static Log log = LogFactory.getLog(DefaultFormValidationDelegate.class);

    	/**
         * Validates all children of this form, recording all messages that are returned by the
         * validators.
         * @param form the form that the validation is applied to
         * @return the list of validation messages that were recorded during validation
         */
        public FeedbackMessages validate(Form form)
        {
            final FeedbackMessages messages = FeedbackMessages.get();
            form.visitChildren(FormComponent.class, new IVisitor()
            {
                public Object component(final Component component)
                {
                    ValidationErrorMessage message = ((FormComponent) component).validate();
                    if(message != ValidationErrorMessage.NO_MESSAGE)
                    {
                        if(log.isDebugEnabled())
                        {
                            log.debug("validation error: " + message);
                        }
                        messages.add(message);
                        // replace the model
                        ValidationErrorModelDecorator deco =
                            new ValidationErrorModelDecorator(component, message.getInput());
                        component.setModel(deco);
                    }
                    return IVisitor.CONTINUE_TRAVERSAL; // continue until the end
                }
            });
            return messages;
        }
    }
}
