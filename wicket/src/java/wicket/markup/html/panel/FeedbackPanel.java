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

import java.util.ArrayList;
import java.util.List;

import wicket.AttributeModifier;
import wicket.Component;
import wicket.FeedbackMessage;
import wicket.IFeedback;
import wicket.markup.html.basic.Label;
import wicket.markup.html.list.ListItem;
import wicket.markup.html.list.ListView;
import wicket.model.AbstractModel;
import wicket.model.IModel;

/**
 * A simple panel that displays {@link wicket.FeedbackMessage}s in a list view.
 * The maximum number of messages to show can be set with setMaxMessages().
 * 
 * @see wicket.FeedbackMessage
 * @see wicket.FeedbackMessages
 * @see wicket.IFeedback
 * @author Jonathan Locke
 * @author Eelco Hillenius
 */
public final class FeedbackPanel extends Panel implements IFeedback
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
		public MessageListView(final String id)
		{
			super(id, (List)new ArrayList());
		}

		/**
		 * @see wicket.markup.html.list.ListView#populateItem(wicket.markup.html.list.ListItem)
		 */
		protected void populateItem(final ListItem listItem)
		{
			final FeedbackMessage message = (FeedbackMessage)listItem.getModelObject();
			IModel replacementModel = new AbstractModel()
			{
				/**
				 * Returns feedbackPanel + the message level, eg
				 * 'feedbackPanelERROR'. This is used as the class of the li /
				 * span elements.
				 * 
				 * @see wicket.model.IModel#getObject(Component)
				 */
				public Object getObject(final Component component)
				{
					return "feedbackPanel" + message.getLevelAsString();
				}

				/**
				 * @see wicket.model.IModel#setObject(Component, Object)
				 */
				public void setObject(final Component component, final Object object)
				{
				}

				public Object getNestedModel()
				{
					return message;
				}
			};

			final Label label = new Label("message", message.getMessage());
			final AttributeModifier levelModifier = new AttributeModifier("class", replacementModel);
			label.add(levelModifier);
			listItem.add(levelModifier);
			listItem.add(label);
		}
	}

	/**
	 * @see wicket.Component#Component(String)
	 */
	public FeedbackPanel(final String id)
	{
		super(id);
		this.messageListView = new MessageListView("messages");
		messageListView.setVersioned(false);
		add(messageListView);
	}

	/**
	 * Sets the model for the list view of feedback messages based on the
	 * messages set on components in a given form.
	 * 
	 * @see IFeedback#addFeedbackMessages(Component, boolean)
	 */
	public void addFeedbackMessages(final Component component, final boolean recurse)
	{
		// Force re-rendering of the list
		messageListView.modelChangeImpending();
		messageListView.getList().addAll(getPage().getFeedbackMessages().messages(component, recurse));
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
	 * @see Component#onEndRender()
	 */
	protected void onEndRender()
	{
		// Clear feedback
		messageListView.getList().clear();
	}
}
