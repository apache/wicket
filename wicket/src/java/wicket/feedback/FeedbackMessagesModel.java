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
package wicket.feedback;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import wicket.Component;
import wicket.model.AbstractDetachableModel;
import wicket.model.IModel;

/**
 * Model for extracting feedback messages.
 * 
 * @author Eelco Hillenius
 */
public final class FeedbackMessagesModel extends AbstractDetachableModel
{
	private static final long serialVersionUID = 1L;
	
	/** Lazy loaded, temporary list. */
	private transient List messages;

	/** Message filter */
	private IFeedbackMessageFilter filter;

	/** Comparator used for sorting the messages. */
	private Comparator sortingComparator;

	/**
	 * Constructor. Creates a model for all feedback messages on the page.
	 */
	public FeedbackMessagesModel()
	{
	}

	/**
	 * Constructor. Creates a model for all feedback messags accepted by the
	 * given filter.
	 * 
	 * @param filter
	 *            The filter to apply
	 */
	public FeedbackMessagesModel(IFeedbackMessageFilter filter)
	{
		setFilter(filter);
	}

	/**
	 * @see wicket.model.IModel#getNestedModel()
	 */
	public IModel getNestedModel()
	{
		return null;
	}

	/**
	 * @see wicket.model.AbstractDetachableModel#onGetObject(wicket.Component)
	 */
	public Object onGetObject(final Component component)
	{
		if (messages == null)
		{
			// Get filtered messages from page where component lives
			messages = component.getPage().getFeedbackMessages().messages(filter);

			// Sort the list before returning it
			if (sortingComparator != null)
			{
				Collections.sort(messages, sortingComparator);
			}
		}
		return messages;
	}

	/**
	 * @param filter
	 *            Filter to apply to model
	 */
	public void setFilter(IFeedbackMessageFilter filter)
	{
		this.filter = filter;
	}

	/**
	 * Sets the comparator used for sorting the messages.
	 * 
	 * @param sortingComparator
	 *            comparator used for sorting the messages
	 */
	public void setSortingComparator(Comparator sortingComparator)
	{
		this.sortingComparator = sortingComparator;
	}

	/**
	 * @see wicket.model.AbstractDetachableModel#onAttach()
	 */
	protected void onAttach()
	{
	}

	/**
	 * @see wicket.model.AbstractDetachableModel#onDetach()
	 */
	protected void onDetach()
	{
		messages = null;
	}

	/**
	 * @see wicket.model.AbstractDetachableModel#onSetObject(wicket.Component,
	 *      java.lang.Object)
	 */
	protected void onSetObject(Component component, Object object)
	{
	}
}