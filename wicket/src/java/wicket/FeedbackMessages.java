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
package wicket;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.markup.html.form.Form;
import wicket.util.string.StringList;

/**
 * Structure for recording {@link wicket.FeedbackMessage}s; wraps a list and
 * acts as a {@link wicket.model.IModel}.
 * 
 * @author Eelco Hillenius
 * @author Jonathan Locke
 */
public final class FeedbackMessages implements Serializable
{
	/** Log. */
	private static Log log = LogFactory.getLog(FeedbackMessages.class);

	/**
	 * Holds a list of {@link wicket.FeedbackMessage}s.
	 */
	private List messages = null;

	/**
	 * Package local constructor; clients are not allowed to create instances as
	 * this class is managed by the framework.
	 */
	FeedbackMessages()
	{
	}

	/**
	 * Clears any existing messages
	 */
	public void clear()
	{
		if (messages != null)
		{
			messages.clear();
		}
	}

	/**
	 * Gets the list of messages relevant to this model's form.
	 * 
	 * @param form
	 *            The form to get messages for
	 * @return The messages
	 */
	public List messages(final Form form)
	{
		if (messages != null)
		{
			// List of messages reported by children of the form
			final List list = new ArrayList();

			// Loop through messages
			for (Iterator iterator = messages.iterator(); iterator.hasNext();)
			{
				// Get next message
				final FeedbackMessage message = (FeedbackMessage)iterator.next();

				// If the reporter is the form itself of the report is contained
				// by the form
				if (form == message.getReporter() || form.contains(message.getReporter(), true))
				{
					// add the message to the list
					list.add(message);
				}
			}

			// Return list of messages reported by children of the form.
			return Collections.unmodifiableList(list);
		}
		return Collections.EMPTY_LIST;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return "[feedbackMessages = " + StringList.valueOf(messages) + "]";
	}

	/**
	 * Adds a message.
	 * 
	 * @param message
	 *            the message
	 */
	void add(FeedbackMessage message)
	{
		if (log.isDebugEnabled())
		{
			log.debug("Adding feedback message " + message);
		}
		if (messages == null)
		{
			messages = new ArrayList();
		}
		messages.add(message);
	}

	/**
	 * Adds a new ui message with level DEBUG to the current messages.
	 * 
	 * @param reporter
	 *            the reporting component
	 * @param message
	 *            the actual message
	 */
	void debug(Component reporter, String message)
	{
		add(new FeedbackMessage(reporter, message, FeedbackMessage.DEBUG));
	}

	/**
	 * Adds a new ui message with level ERROR to the current messages.
	 * 
	 * @param reporter
	 *            the reporting component
	 * @param message
	 *            the actual message
	 */
	void error(Component reporter, String message)
	{
		add(new FeedbackMessage(reporter, message, FeedbackMessage.ERROR));
	}

	/**
	 * Adds a new ui message with level FATAL to the current messages.
	 * 
	 * @param reporter
	 *            the reporting component
	 * @param message
	 *            the actual message
	 */
	void fatal(Component reporter, String message)
	{
		add(new FeedbackMessage(reporter, message, FeedbackMessage.FATAL));
	}

	/**
	 * Convenience method that looks up whether the given component registered a
	 * message with this list with the level ERROR.
	 * 
	 * @param component
	 *            the component to look up whether it registered a message
	 * @return whether the given component registered a message with this list
	 *         with level ERROR
	 */
	boolean hasErrorMessageFor(Component component)
	{
		return hasMessageFor(component, FeedbackMessage.ERROR);
	}

	/**
	 * Looks up whether the given component registered a message with this list.
	 * 
	 * @param component
	 *            the component to look up whether it registered a message
	 * @return whether the given component registered a message with this list
	 */
	boolean hasMessageFor(Component component)
	{
		return messageForComponent(component) != null;
	}

	/**
	 * Looks up whether the given component registered a message with this list
	 * with the given level.
	 * 
	 * @param component
	 *            The component to look up whether it registered a message
	 * @param level
	 *            The level of the message
	 * @return Whether the given component registered a message with this list
	 *         with the given level
	 */
	boolean hasMessageFor(Component component, int level)
	{
		final FeedbackMessage message = messageForComponent(component);
		return message != null && message.isLevel(level);
	}

	/**
	 * Adds a new ui message with level INFO to the current messages.
	 * 
	 * @param reporter
	 *            The reporting component
	 * @param message
	 *            The actual message
	 */
	void info(Component reporter, String message)
	{
		add(new FeedbackMessage(reporter, message, FeedbackMessage.INFO));
	}

	/**
	 * Looks up a message for the given component.
	 * 
	 * @param component
	 *            the component to look up the message for
	 * @return the message that is found for the given component (first match)
	 *         or null if none was found
	 */
	FeedbackMessage messageForComponent(final Component component)
	{
		if (messages != null)
		{
			for (Iterator iterator = messages.iterator(); iterator.hasNext();)
			{
				FeedbackMessage message = (FeedbackMessage)iterator.next();
				if (message.getReporter() == component)
				{
					return message;
				}
			}
		}
		return null;
	}

	/**
	 * Adds a new ui message with level WARNING to the current messages.
	 * 
	 * @param reporter
	 *            the reporting component
	 * @param message
	 *            the actual message
	 */
	void warn(Component reporter, String message)
	{
		add(new FeedbackMessage(reporter, message, FeedbackMessage.WARNING));
	}
}