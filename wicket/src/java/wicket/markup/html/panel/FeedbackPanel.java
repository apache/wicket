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
package wicket.markup.html.panel;

import java.util.Collections;

import wicket.AttributeModifier;
import wicket.FeedbackMessage;
import wicket.markup.html.basic.Label;
import wicket.markup.html.form.validation.IValidationFeedback;
import wicket.markup.html.list.ListItem;
import wicket.markup.html.list.ListView;
import wicket.model.IModel;

/**
 * A simple panel that displays {@link wicket.FeedbackMessage}s in a list view.
 * The maximum number of messages to show can be set with setMaxMessages().
 * 
 * @see wicket.FeedbackMessage
 * @see wicket.FeedbackMessages
 * @see wicket.markup.html.form.validation.IValidationFeedback
 * @author Jonathan Locke
 * @author Eelco Hillenius
 */
public final class FeedbackPanel extends Panel implements IValidationFeedback
{
	/** Serial Version ID. */
	private static final long serialVersionUID = -3385823935971399988L;

	/** The maximum number of messages to show at a time */
	private int maxMessages = Integer.MAX_VALUE;

	/**
	 * List for messages.
	 */
	private static final class MessageListView extends ListView
	{
		/**
		 * @see wicket.Component#Component(String)
		 */
		public MessageListView(final String name)
		{
			super(name, Collections.EMPTY_LIST);
		}

		/**
		 * Removes all subcomponents on each render pass, to ensure that the
		 * dynamic model is always read again.
		 * 
		 * @see wicket.Component#onRender()
		 */
		protected void onRender()
		{
            setModel(getPage().getFeedbackMessages().model());
			super.onRender();
			removeAll();
		}
        
		/**
		 * @see wicket.markup.html.list.ListView#populateItem(wicket.markup.html.list.ListItem)
		 */
		protected void populateItem(final ListItem listItem)
		{
			final FeedbackMessage message = (FeedbackMessage)listItem.getModelObject();
			IModel replacementModel = new IModel()
			{
				/**
				 * Returns feedbackPanel + the message level, eg
				 * 'feedbackPanelERROR'. This is used as the class of the li /
				 * span elements.
				 * 
				 * @see wicket.model.IModel#getObject()
				 */
				public Object getObject()
				{
					return "feedbackPanel" + message.getLevelAsString();
				}

				/**
				 * @see wicket.model.IModel#setObject(java.lang.Object)
				 */
				public void setObject(Object object)
				{
				}
			};

			final Label label = new Label("message", message, "message");
			final AttributeModifier levelModifier = new AttributeModifier(
					"class", replacementModel);
			label.add(levelModifier);
			listItem.add(levelModifier);
			listItem.add(label);
		}
	}

	/**
	 * @see wicket.Component#Component(String)
	 */
	public FeedbackPanel(final String componentName)
	{
		super(componentName);
		addComponents();
	}

	/**
	 * @return Returns the maxMessages.
	 */
	public int getMaxMessages()
	{
		return maxMessages;
	}

	/**
	 * @param maxMessages
	 *            The maximum number of feedback messages that this feedback
	 *            panel should show at one time
	 */
	public void setMaxMessages(int maxMessages)
	{
		this.maxMessages = maxMessages;
	}

	/**
	 * Sets an error message to be displayed by this feedback panel.
	 * 
	 * @see wicket.markup.html.form.validation.IValidationFeedback#updateValidationFeedback()
	 */
	public void updateValidationFeedback()
	{
		// Force re-rendering of the list
		removeAll();
		addComponents();
	}

	/**
	 * Adds the components to the panel.
	 */
	private void addComponents()
	{
        final MessageListView view = new MessageListView("messages");
        view.setViewSize(getMaxMessages());
		add(view);
	}
}