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
import wicket.markup.html.form.Form;
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

	/** Message view */
	private final MessageListView messageListView;

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
			final AttributeModifier levelModifier = new AttributeModifier("class", replacementModel);
			label.add(levelModifier);
			listItem.add(levelModifier);
			listItem.add(label);
		}
	}

	/**
	 * @see wicket.Component#Component(String)
	 */
	public FeedbackPanel(final String name)
	{
		super(name);
		this.messageListView = new MessageListView("messages");
		add(messageListView);
	}

	/**
	 * @param maxMessages
	 *            The maximum number of feedback messages that this feedback
	 *            panel should show at one time
	 */
	public void setMaxMessages(int maxMessages)
	{
		this.messageListView.setViewSize(maxMessages);
	}

	/**
	 * Sets an error message to be displayed by this feedback panel.
	 * 
	 * @see IValidationFeedback#updateValidationFeedback(Form)
	 */
	public void updateValidationFeedback(final Form form)
	{
		// Force re-rendering of the list
		messageListView.setModelObject(getPage().getFeedbackMessages().messages(form));
		messageListView.modelChangedStructure();
	}
}
