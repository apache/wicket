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
import wicket.RequestCycle;
import wicket.markup.MarkupStream;
import wicket.markup.html.HtmlContainer;
import wicket.markup.html.form.validation.ValidationErrorMessage;
import wicket.markup.html.form.validation.ValidationErrorModelDecorator;

/**
 * A panel-like component that can be used nested within a form.
 *
 * @author Eelco Hillenius
 */
public abstract class FormComponentsPanel extends FormComponent
{
	/** log. */
	private static Log log = LogFactory.getLog(FormComponentsPanel.class);

	/** the parent form. */
	private final Form form;

    /**
     * Constructor.
     * @param componentName The name of this component
     * @param form the parent form
     */
    public FormComponentsPanel(final String componentName, Form form)
    {
        super(componentName);
        if(form == null)
        {
        	throw new IllegalArgumentException("form must be not-null");
        }
        form.setValidationDelegate(new ChainedValidationDelegate());
        this.form = form;
    }

	/**
	 * Updates all registered children that are form components.
	 * @param cycle the request cycle
	 * @see wicket.markup.html.form.FormComponent#updateModel(wicket.RequestCycle)
	 */
	public void updateModel(final RequestCycle cycle)
	{
        visitChildren(FormComponent.class, new IVisitor()
        {
            public Object component(final Component component)
            {
                final FormComponent formComponent = (FormComponent) component;
                formComponent.updateModel(cycle);
                return CONTINUE_TRAVERSAL;
            }
        });
	}

    /**
     * Renders this component like a {@link wicket.markup.html.panel.Panel}.
     * @param cycle Response to write to
     */
    protected final void handleRender(final RequestCycle cycle)
    {
        // Render the tag that included this html compoment
        final MarkupStream markupStream = findMarkupStream();

        if (!markupStream.atOpenCloseTag())
        {
            markupStream.throwMarkupException(
                    "A panel must be referenced by an openclose tag.");
        }

        renderTag(cycle, markupStream);

        // Render the associated markup
        renderAssociatedMarkup(cycle, "panel",
                "Markup for a panel component must begin with a component" +
                "'<wicket:region name=panel>'");
    }

    /**
     * The form validation delegate that handles both the form components nested in this
     * panel as well as the registered form components of the parent form.
     */
    final class ChainedValidationDelegate implements IFormValidationDelegate
    {
    	/**
         * Validates all children of this form, recording all messages that are returned by the
         * validators.
         * @param form the form that the validation is applied to
         * @return the list of validation messages that were recorded during validation
         */
        public FeedbackMessages validate(Form form)
        {
            final FeedbackMessages messages = FeedbackMessages.get();
            validate(FormComponentsPanel.this, messages);
            validate(form, messages);
            return messages;
        }

		/**
		 * Handles validation by visiting the children of the given component.
		 * @param component the component with the validating children
		 * @param messages the messages to add any results to
		 */
		private void validate(HtmlContainer component, final FeedbackMessages messages)
		{
			component.visitChildren(FormComponent.class, new IVisitor()
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
		}
    }
}
