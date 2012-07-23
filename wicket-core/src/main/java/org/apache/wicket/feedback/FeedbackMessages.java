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
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.wicket.Component;
import org.apache.wicket.IClusterable;
import org.apache.wicket.util.lang.Objects;
import org.apache.wicket.util.string.StringList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Holds list of feedback messages. The list can be added to, cleared, queried and filtered.
 * <p>
 * WARNING: This class should typically NOT be used directly.
 * <p>
 * 
 * @author Eelco Hillenius
 * @author Jonathan Locke
 */
public final class FeedbackMessages implements IClusterable, Iterable<FeedbackMessage>
{
	/** Log. */
	private static final Logger log = LoggerFactory.getLogger(FeedbackMessages.class);

	private static final long serialVersionUID = 1L;

	/**
	 * Holds a list of {@link org.apache.wicket.feedback.FeedbackMessage}s.
	 */
	private final List<FeedbackMessage> messages;

	/**
	 * Construct.
	 */
	public FeedbackMessages()
	{
		messages = new CopyOnWriteArrayList<FeedbackMessage>();
	}

	/**
	 * Adds a message.
	 * 
	 * @param message
	 *            the message
	 */
	public final void add(FeedbackMessage message)
	{
		log.debug("Adding feedback message '{}'", message);

		synchronized (messages)
		{
			messages.add(message);
		}
	}
	
	/**
	 * Adds a message
	 * 
	 * @param reporter
	 * @param message
	 * @param level
	 */
	public final void add(Component reporter, Serializable message, int level)
	{
		add(new FeedbackMessage(reporter, message, level));
	}

	/**
	 * Adds a new ui message with level DEBUG to the current messages.
	 * 
	 * @param reporter
	 *            the reporting component
	 * @param message
	 *            the actual message
	 */
	public final void debug(Component reporter, Serializable message)
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
	public final void info(Component reporter, Serializable message)
	{
		add(new FeedbackMessage(reporter, message, FeedbackMessage.INFO));
	}

	/**
	 * Adds a new ui message with level SUCCESS to the current messages.
	 * 
	 * @param reporter
	 *            The reporting component
	 * @param message
	 *            The actual message
	 */
	public final void success(Component reporter, Serializable message)
	{
		add(new FeedbackMessage(reporter, message, FeedbackMessage.SUCCESS));
	}

	/**
	 * Adds a new ui message with level WARNING to the current messages.
	 * 
	 * @param reporter
	 *            the reporting component
	 * @param message
	 *            the actual message
	 */
	public final void warn(Component reporter, Serializable message)
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
	public final void fatal(Component reporter, Serializable message)
	{
		add(new FeedbackMessage(reporter, message, FeedbackMessage.FATAL));
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
	 * Clears all messages that are accepted by the filter.
	 * 
	 * @param filter
	 *            Filter for selecting messages. If null, all messages will be returned
	 * @return The number of messages deleted
	 */
	public final int clear(final IFeedbackMessageFilter filter)
	{
		if (messages.isEmpty())
		{
			return 0;
		}

		List<FeedbackMessage> toDelete = messages(filter);


		for (FeedbackMessage message : toDelete)
		{
			message.detach();
		}

		synchronized(messages)
		{
			int sizeBefore = messages.size();
			messages.removeAll(toDelete);
			int sizeAfter = messages.size();
			return sizeAfter - sizeBefore;
		}
	}

	/**
	 * @param filter
	 *            Filter for selecting messages
	 * @return True if one or more messages matches the filter
	 */
	public final boolean hasMessage(final IFeedbackMessageFilter filter)
	{
		for (final FeedbackMessage message : messages)
		{
			if (filter == null || filter.accept(message))
			{
				return true;
			}
		}
		return false;
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
		return hasMessage(new ComponentFeedbackMessageFilter(component));
	}

	/**
	 * Looks up whether the given component registered a message with this list with the given
	 * level.
	 * 
	 * @param component
	 *            The component to look up whether it registered a message
	 * @param level
	 *            The level of the message
	 * @return Whether the given component registered a message with this list with the given level
	 */
	public final boolean hasMessageFor(final Component component, final int level)
	{
		return hasMessage(new IFeedbackMessageFilter()
		{
			public boolean accept(FeedbackMessage message)
			{
				return Objects.equal(message.getReporter(), component) && message.isLevel(level);
			}
		});
	}

	/**
	 * Convenience method that looks up whether the given component registered a message with this
	 * list with the level ERROR.
	 * 
	 * @param component
	 *            the component to look up whether it registered a message
	 * @return whether the given component registered a message with this list with level ERROR
	 */
	public final boolean hasErrorMessageFor(Component component)
	{
		return hasMessageFor(component, FeedbackMessage.ERROR);
	}

	/**
	 * Gets an iterator over stored messages
	 * 
	 * @return iterator over stored messages
	 */
	public final Iterator<FeedbackMessage> iterator()
	{
		return messages.iterator();
	}

	/**
	 * Gets a list of messages from the page using a filter.
	 * 
	 * @param filter
	 *            Filter for selecting messages. If null, all messages will be returned
	 * @return The messages or an empty list if no messages are found
	 */
	public final List<FeedbackMessage> messages(final IFeedbackMessageFilter filter)
	{
		if (messages.isEmpty())
		{
			return Collections.emptyList();
		}

		final List<FeedbackMessage> list = new ArrayList<FeedbackMessage>();
		for (final FeedbackMessage message : messages)
		{
			if (filter == null || filter.accept(message))
			{
				list.add(message);
			}
		}
		return list;
	}

	/**
	 * Looks up a single message for the given component.
	 * 
	 * @param component
	 *            the component to look up the message for
	 * @return the message that is found for the given component (first match) or null if none was
	 *         found
	 *         
	 * @deprecated use {@link FeedbackMessages#messagesForComponent(org.apache.wicket.Component)} instead
	 */
	public final FeedbackMessage messageForComponent(final Component component)
	{
		final List<FeedbackMessage> list = messagesForComponent(component);

		return list.isEmpty() ? null : list.get(0);
	}

	/**
	 * Looks up the messages for the given component.
	 * 
	 * @param component
	 *            the component to look up the message for
	 * @return the messages that were found for the given component
	 */
	public final List<FeedbackMessage> messagesForComponent(final Component component)
	{
		return messages(new ComponentFeedbackMessageFilter(component)); 
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
	 *            Filter for counting messages. If null, the count of all messages will be returned
	 * 
	 * @return the number of messages
	 */
	public final int size(final IFeedbackMessageFilter filter)
	{
		int count = 0;
		for (final FeedbackMessage message : messages)
		{
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
	@Override
	public String toString()
	{
		return "[feedbackMessages = " + StringList.valueOf(messages) + ']';
	}
}