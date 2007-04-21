/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.wicket.feedback;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.Component;
import org.apache.wicket.IClusterable;
import org.apache.wicket.util.concurrent.CopyOnWriteArrayList;
import org.apache.wicket.util.string.StringList;


/**
 * Holds list of feedback messages. The list can be added to, cleared, queried
 * and filtered.
 * <p>
 * WARNING: This class should typically NOT be used directly.
 * <p>
 * 
 * @author Eelco Hillenius
 * @author Jonathan Locke
 */
public final class FeedbackMessages implements IClusterable
{
	/** Log. */
	private static final Log log = LogFactory.getLog(FeedbackMessages.class);

	private static final long serialVersionUID = 1L;

	/**
	 * Holds a list of {@link org.apache.wicket.feedback.FeedbackMessage}s.
	 */
	private List messages = null;

	/**
	 * Construct.
	 */
	public FeedbackMessages()
	{
		messages = new CopyOnWriteArrayList();
	}

	/**
	 * Adds a message
	 * 
	 * @param reporter
	 * @param message
	 * @param level
	 */
	public final void add(Component reporter, String message, int level)
	{
		add(new FeedbackMessage(reporter, message, level));
	}

	/**
	 * Clears any existing messages.
	 * 
	 * @return The number of messages deleted
	 */
	public final int clear()
	{
		return clear(null);
	}

	/**
	 * Clears all messsages that are accepted by the filter.
	 * 
	 * @param filter
	 *            Filter for selecting messages. If null, all messages will be
	 *            returned
	 * @return The number of messages deleted
	 */
	public final int clear(final IFeedbackMessageFilter filter)
	{
		if (messages.size() == 0)
		{
			return 0;
		}

		int count = 0;
		for (final Iterator iterator = messages.iterator(); iterator.hasNext();)
		{
			final FeedbackMessage message = (FeedbackMessage)iterator.next();
			if (filter == null || filter.accept(message))
			{
				messages.remove(message);
				count++;
			}
		}

		trimToSize();
		return count;
	}

	/**
	 * Adds a new ui message with level DEBUG to the current messages.
	 * 
	 * @param reporter
	 *            the reporting component
	 * @param message
	 *            the actual message
	 */
	public final void debug(Component reporter, String message)
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
	public final void error(Component reporter, Serializable message)
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
	public final void fatal(Component reporter, String message)
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
	public final boolean hasErrorMessageFor(Component component)
	{
		return hasMessageFor(component, FeedbackMessage.ERROR);
	}

	/**
	 * @param filter
	 *            Filter for selecting messages
	 * @return True if one or more messages matches the filter
	 */
	public final boolean hasMessage(final IFeedbackMessageFilter filter)
	{
		return messages(filter).size() != 0;
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
	 * Adds a new ui message with level INFO to the current messages.
	 * 
	 * @param reporter
	 *            The reporting component
	 * @param message
	 *            The actual message
	 */
	public final void info(Component reporter, String message)
	{
		add(new FeedbackMessage(reporter, message, FeedbackMessage.INFO));
	}

	/**
	 * Gets whether there are no messages.
	 * 
	 * @return True when there are no messages
	 */
	public final boolean isEmpty()
	{
		return messages.isEmpty();
	}

	/**
	 * Gets an iterator over stored messages
	 * 
	 * @return iterator over stored messages
	 */
	public final Iterator iterator()
	{
		return messages.iterator();
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
		for (Iterator iterator = messages.iterator(); iterator.hasNext();)
		{
			FeedbackMessage message = (FeedbackMessage)iterator.next();
			if (message.getReporter() == component)
			{
				return message;
			}
		}
		return null;
	}

	/**
	 * Gets a list of messages from the page using a filter.
	 * 
	 * @param filter
	 *            Filter for selecting messages. If null, all messages will be
	 *            returned
	 * @return The messages or an empty list if no messages are found
	 */
	public final List messages(final IFeedbackMessageFilter filter)
	{
		if (messages.size() == 0)
		{
			return Collections.EMPTY_LIST;
		}

		final List list = new ArrayList();
		for (final Iterator iterator = messages.iterator(); iterator.hasNext();)
		{
			final FeedbackMessage message = (FeedbackMessage)iterator.next();
			if (filter == null || filter.accept(message))
			{
				list.add(message);
			}
		}
		return list;
	}

	/**
	 * Gets the number of messages
	 * 
	 * @return the number of messages
	 */
	public final int size()
	{
		return messages.size();
	}

	/**
	 * Gets the number of messages.
	 * 
	 * @param filter
	 *            Filter for counting messages. If null, the count of all
	 *            messages will be returned
	 * 
	 * @return the number of messages
	 */
	public final int size(final IFeedbackMessageFilter filter)
	{
		int count = 0;
		for (final Iterator iterator = messages.iterator(); iterator.hasNext();)
		{
			final FeedbackMessage message = (FeedbackMessage)iterator.next();
			if (filter == null || filter.accept(message))
			{
				count++;
			}
		}
		return count;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return "[feedbackMessages = " + StringList.valueOf(messages) + "]";
	}

	/**
	 * Frees any unnecessary internal storage
	 */
	public final void trimToSize()
	{
		if (messages instanceof ArrayList)
		{
			((ArrayList)messages).trimToSize();
		}
	}

	/**
	 * Adds a new ui message with level WARNING to the current messages.
	 * 
	 * @param reporter
	 *            the reporting component
	 * @param message
	 *            the actual message
	 */
	public final void warn(Component reporter, String message)
	{
		add(new FeedbackMessage(reporter, message, FeedbackMessage.WARNING));
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
		messages.add(message);
	}
}