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

import wicket.Component;
import wicket.FeedbackMessages;
import wicket.markup.html.HtmlContainer;
import wicket.markup.html.border.Border;
import wicket.markup.html.form.validation.IValidationErrorHandler;


/**
 * A border that can be placed around a form bordered to indicate when the bordered has
 * a validation error.
 *
 * @author Jonathan Locke
 * @author Eelco Hillenius
 */
public final class FormComponentFeedbackBorder extends Border implements IValidationErrorHandler
{ // TODO finalize javadoc
    /** Serial Version ID. */
	private static final long serialVersionUID = -7070716217601930304L;
	
	/** The error indicator child which should be shown if an error occurs. */
    private final HtmlContainer errorIndicator;

    /** the bordered to border; is used to get whether there is an error for it. */
    private final Component bordered;

    /**
     * Constructor.
     * @param componentName The name of the bordered
     * @param bordered the bordered to border
     */
    public FormComponentFeedbackBorder(final String componentName, Component bordered)
    {
        super(componentName);
        this.bordered = bordered;
        add(bordered);
        // Create invisible error indicator bordered that will be shown
        // when a validation error occurs
        errorIndicator = new HtmlContainer("errorIndicator");
        errorIndicator.setVisible(false);
        add(errorIndicator);
    }

    /**
     * Handles validation errors. If any errors were registered, the decorated error
     * indicator will be set to invisible.
     * @param errors list with {@link wicket.markup.html.form.validation.ValidationErrorMessage}s
     * @see wicket.markup.html.form.validation.IValidationErrorHandler#validationError(wicket.FeedbackMessages)
     */
    public void validationError(final FeedbackMessages errors)
    {
        errorIndicator.setVisible(errors.hasErrorMessageFor(bordered));
    }
}

