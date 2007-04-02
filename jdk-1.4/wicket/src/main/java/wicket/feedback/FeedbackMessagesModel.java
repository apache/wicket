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
package wicket.feedback;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import wicket.Component;
import wicket.Page;
import wicket.Session;
import wicket.model.IModel;

/**
 * Model for extracting feedback messages.
 * 
 * @author Eelco Hillenius
 */
public class FeedbackMessagesModel implements IModel
{
	private static final long serialVersionUID = 1L;

	/** Message filter */
	private IFeedbackMessageFilter filter;

	/** Lazy loaded, temporary list. */
	private transient List messages;

	/** Comparator used for sorting the messages. */
	private Comparator sortingComparator;

	/** the page of component this model is attached to */
	private final Component component;

	/**
	 * Constructor. Creates a model for all feedback messages on the page.
	 * 
	 * @param component
	 *            The component where the page will be get from for which
	 *            messages will be displayed usually the same page as the one
	 *            feedbackpanel is attached to
	 */
	public FeedbackMessagesModel(Component component)
	{
		if (component == null)
		{
			throw new IllegalArgumentException("Argument [[page]] cannot be null");
		}
		this.component = component;
	}

	/**
	 * Constructor. Creates a model for all feedback messags accepted by the
	 * given filter.
	 * 
	 * @param filter
	 *            The filter to apply
	 * @param page
	 *            Page for which messages will be displayed - usually the same
	 *            page as the one feedbackpanel is attached to
	 * 
	 */
	public FeedbackMessagesModel(Page page, IFeedbackMessageFilter filter)
	{
		this(page);
		setFilter(filter);
	}

	/**
	 * @return The current message filter
	 */
	public final IFeedbackMessageFilter getFilter()
	{
		return filter;
	}

	/**
	 * @return The current sorting comparator
	 */
	public final Comparator getSortingComparator()
	{
		return sortingComparator;
	}

	/**
	 * @see wicket.model.IModel#getObject()
	 */
	public final Object getObject()
	{
		if (messages == null)
		{
			// Get filtered messages from page where component lives
			messages = Session.get().getFeedbackMessages().messages(filter);

			// Sort the list before returning it
			if (sortingComparator != null)
			{
				Collections.sort(messages, sortingComparator);
			}

			// Let subclass do any extra processing it wants to on the messages.
			// It may want to do something special, such as removing a given
			// message under some special condition or perhaps eliminate
			// duplicate messages. It could even add a message under certain
			// conditions.
			messages = processMessages(messages);
		}
		return messages;
	}

	/**
	 * @param filter
	 *            Filter to apply to model
	 */
	public final void setFilter(IFeedbackMessageFilter filter)
	{
		this.filter = filter;
	}

	/**
	 * Sets the comparator used for sorting the messages.
	 * 
	 * @param sortingComparator
	 *            comparator used for sorting the messages
	 */
	public final void setSortingComparator(Comparator sortingComparator)
	{
		if (!(sortingComparator instanceof Serializable))
		{
			throw new IllegalArgumentException("sortingComparator must be serializable");
		}
		this.sortingComparator = sortingComparator;
	}

	/**
	 * Override this method to post process to the FeedbackMessage list.
	 * 
	 * @param messages
	 *            List of sorted and filtered FeedbackMessages for further
	 *            processing
	 * @return The processed FeedbackMessage list
	 */
	protected List processMessages(final List messages)
	{
		return messages;
	}

	/**
	 * 
	 * @see wicket.model.IModel#setObject(java.lang.Object)
	 */
	public void setObject(Object object)
	{
	}

	/**
	 * 
	 * @see wicket.model.IDetachable#detach()
	 */
	public void detach()
	{
		messages = null;
	}
}