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
package wicket.markup.html.panel;

import wicket.IModel;
import wicket.RequestCycle;
import wicket.UIMessage;
import wicket.UIMessages;
import wicket.markup.ComponentTagAttributeModifier;
import wicket.markup.html.basic.Label;
import wicket.markup.html.form.validation.IValidationErrorHandler;
import wicket.markup.html.table.ListItem;
import wicket.markup.html.table.ListView;


/**
 * A simple panel that displays {@link wicket.UIMessage}s.
 *
 * @see wicket.UIMessage
 * @see wicket.UIMessages
 * @see wicket.markup.html.form.validation.IValidationErrorHandler
 *
 * @author Jonathan Locke
 * @author Eelco Hillenius
 */
public final class FeedbackPanel extends Panel implements IValidationErrorHandler
{
    /** Serial Version ID. */
	private static final long serialVersionUID = -3385823935971399988L;

    /** list component. */
    private MessageList messageList;

    /**
     * Constructor.
     * @param componentName The name of this component
     */
    public FeedbackPanel(final String componentName)
    {
        super(componentName);
        addComponents();
        
    }

    /**
     * Adds the components to the panel.
     */
    private void addComponents()
    {
        messageList = new MessageList("messages");
        add(messageList);
    }

    /**
     * Sets an error message to be displayed by this feedback panel.
     * @param errors The errors structure
     * @see wicket.markup.html.form.validation.IValidationErrorHandler#validationError(wicket.UIMessages)
     */
    public void validationError(final UIMessages errors)
    {
        // force re-rendering of the list
        removeAll();
        addComponents();
    }

    /**
     * List for messages.
     */
    private static final class MessageList extends ListView
    {
        /**
         * Construct.
         * @param name component name
         */
        public MessageList(String name)
        {
            super(name, UIMessages.model());
        }

        /**
         * @see wicket.markup.html.table.ListView#populateItem(wicket.markup.html.table.ListItem)
         */
        protected void populateItem(final ListItem listItem)
        {
            final UIMessage message = (UIMessage)listItem.getModelObject();
            IModel replacementModel = new IModel() {

                /**
                 * Returns feedbackPanel + the message level, eg 'feedbackPanelERROR'.
                 * This is used as the class of the li/ span elements.
                 * @see wicket.IModel#getObject()
                 */
                public Object getObject()
                {
                    return "feedbackPanel" + message.getLevelAsString();
                }

                /**
                 * @see wicket.IModel#setObject(java.lang.Object)
                 */
                public void setObject(Object object)
                {
                    // nothing to do here
                }
            };
            Label label = new Label("message", message, "message");
            ComponentTagAttributeModifier levelModifier =
                new ComponentTagAttributeModifier("class", replacementModel);
            label.addAttributeModifier(levelModifier);
            listItem.addAttributeModifier(levelModifier);
            listItem.add(label);
        }

        /**
         * Removes all subcomponents on each render pass, to ensure that the
         * dynamic model is allways read again.
         * @see wicket.Component#handleRender(wicket.RequestCycle)
         */
        protected void handleRender(final RequestCycle cycle)
        {
            super.handleRender(cycle);
            removeAll();
        }
    }
}
