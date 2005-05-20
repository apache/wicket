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
	public final void clear()
	{
		if (messages != null)
		{
			messages.clear();
		}
	}

	/**
	 * Gets whether there are no messages.
	 * @return True when there are no messages
	 */
	public final boolean isEmpty()
	{
		return (messages == null || messages.isEmpty());
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
	public final boolean hasErrorMessageFor(Component component)
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
	public final boolean hasMessageFor(Component component)
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
	public final boolean hasMessageFor(Component component, int level)
	{
		final FeedbackMessage message = messageForComponent(component);
		return message != null && message.isLevel(level);
	}

	/**
	 * Looks up a message for the given component.
	 * 
	 * @param component
	 *            the component to look up the message for
	 * @return the message that is found for the given component (first match)
	 *         or null if none was found
	 */
	public final FeedbackMessage messageForComponent(final Component component)
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
	 * Gets the list of messages relevant to a given component. If the component
	 * is a container, the list returned will be a list of all messages reported
	 * by children of the container.
	 * 
	 * @param component
	 *            The component to get messages for
	 * @param recurse
	 *            True if children of the component should be considered
	 * @param stopAtBoundary whether to stop collecting messages when
	 * 	a child component is of type {@link IFeedbackBoundary}.
	 * 	Only has effect when recurse == true.
	 * @param fromLevel from which level the messages are collected; DEBUG traps all,
	 * 		but e.g. ERROR only traps ERROR and FATAL
	 *
	 * @return The messages or an empty list when no messages are found
	 */
	public final List messages(final Component component, boolean recurse,
			boolean stopAtBoundary, int fromLevel)
	{
		List result = null;
		if (component != null && messages != null)
		{
			appendMessages(result = new ArrayList(), component, recurse, stopAtBoundary, fromLevel);
		}
		return (result != null) ? result : Collections.EMPTY_LIST;
	}

	/**
	 * Gets the messages for the given component and append them to the given list.
	 * @param list list to append messages to
	 * @param component the component to get the messages for
	 * @param recurse whether to append any messages of child components
	 * @param stopAtBoundary whether to stop collecting messages when
	 * 	a child component is of type {@link IFeedbackBoundary}.
	 * 	Only has effect when recurse == true.
	 * @param fromLevel from which level the messages are collected; DEBUG traps all,
	 * 		but e.g. ERROR only traps ERROR and FATAL
	 */
	private final void appendMessages(final List list, final Component component,
			boolean recurse, boolean stopAtBoundary, int fromLevel)
	{
		for (Iterator iterator = messages.iterator(); iterator.hasNext();)
		{
			FeedbackMessage message = (FeedbackMessage)iterator.next();
			Component reporter = message.getReporter();
			if (reporter == component && message.isLevel(fromLevel))
			{
				list.add(message);
			}
		}
		if (recurse && component instanceof MarkupContainer)
		{
			MarkupContainer container = (MarkupContainer)component;
			for (Iterator i = container.iterator(); i.hasNext();)
			{
				Component child = (Component)i.next();
				if ((!stopAtBoundary) || (!(child instanceof IFeedbackBoundary)))
				{
					appendMessages(list, child, recurse, stopAtBoundary, fromLevel);
				}
			}
		}
	}

	/**
	 * Adds a message.
	 * 
	 * @param message
	 *            the message
	 */
	final void add(FeedbackMessage message)
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
	final void debug(Component reporter, String message)
	{
		add(new FeedbackMessage(reporter, message, FeedbackMessage.DEBUG));
	}

	/**
	 * Adds a new ui message with level INFO to the current messages.
	 * 
	 * @param reporter
	 *            The reporting component
	 * @param message
	 *            The actual message
	 */
	final void info(Component reporter, String message)
	{
		add(new FeedbackMessage(reporter, message, FeedbackMessage.INFO));
	}

	/**
	 * Adds a new ui message with level WARNING to the current messages.
	 * 
	 * @param reporter
	 *            the reporting component
	 * @param message
	 *            the actual message
	 */
	final void warn(Component reporter, String message)
	{
		add(new FeedbackMessage(reporter, message, FeedbackMessage.WARNING));
	}

	/**
	 * Adds a new ui message with level ERROR to the current messages.
	 * 
	 * @param reporter
	 *            the reporting component
	 * @param message
	 *            the actual message
	 */
	final void error(Component reporter, String message)
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
	final void fatal(Component reporter, String message)
	{
		add(new FeedbackMessage(reporter, message, FeedbackMessage.FATAL));
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return "[feedbackMessages = " + StringList.valueOf(messages) + "]";
	}
}