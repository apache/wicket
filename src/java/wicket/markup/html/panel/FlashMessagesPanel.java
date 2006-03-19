/*
 * $Id: org.eclipse.jdt.ui.prefs 5004 2006-03-17 20:47:08 -0800 (Fri, 17 Mar
 * 2006) eelco12 $ $Revision: 5004 $ $Date: 2006-03-17 20:47:08 -0800 (Fri, 17
 * Mar 2006) $
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

import wicket.Component;
import wicket.markup.html.basic.Label;
import wicket.markup.html.list.ListItem;
import wicket.markup.html.list.ListView;
import wicket.model.AbstractReadOnlyModel;
import wicket.model.IModel;

/**
 * A panel used to display flash messages. Unless {@link #getClearMessages()} is
 * overridden to return false, this panel will clear any flash messages after it
 * displays them.
 * 
 * @see wicket.Session
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public class FlashMessagesPanel extends Panel
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private transient boolean visible = false;

	/**
	 * Construct.
	 * 
	 * @param id
	 */
	public FlashMessagesPanel(String id)
	{
		super(id);

		IModel messages = new AbstractReadOnlyModel()
		{

			private static final long serialVersionUID = 1L;

			public Object getObject(Component component)
			{
				System.out.println("retrieving messages: " + getPage().getClass());
				return getSession().getFlashMessages();
			}

		};

		add(new ListView("messages", messages)
		{

			private static final long serialVersionUID = 1L;

			protected void populateItem(ListItem item)
			{
				Label msg = new Label("message", item.getModel());
				msg.setEscapeModelStrings(getEscapeMessageMarkup());
				item.add(msg);
			}

		});
	}

	protected final void onBeginRequest()
	{
		visible = !getSession().getFlashMessages().isEmpty();
		System.out.println("setting visibility to: " + visible);
		setVisible(visible);
	}

	protected final void onEndRequest()
	{
		if (getClearMessages() && visible)
		{
			System.out.println("clearing");
			getSession().clearFlashMessages();
		}
	}

	/**
	 * 
	 * @return true if any markup inside a message should be escaped, false
	 *         otherwise
	 */
	protected boolean getEscapeMessageMarkup()
	{
		return true;
	}

	/**
	 * @return true if flash messages should be cleared after being displayed,
	 *         false otherwise
	 */
	protected boolean getClearMessages()
	{
		return true;
	}


}
