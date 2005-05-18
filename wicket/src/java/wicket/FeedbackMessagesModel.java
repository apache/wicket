/*
 * $Id$
 * $Revision$
 * $Date$
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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import wicket.model.AbstractDetachableModel;
import wicket.model.IModel;

/**
 * Model for extracting feedback messages.
 *
 * @author Eelco Hillenius
 */
public final class FeedbackMessagesModel extends AbstractDetachableModel
{
	/**
	 * Whether to include the messages of any nested component.
	 */
	private boolean includeNestedComponents;

	/**
	 * Optional collecting component. When this is not set explicitly, the first occurence
	 * of {@link IFeedbackBoundary} will be searched for higher up in the run-time
	 * hierarchy.
	 */
	private Component collectingComponent;

	/**
	 * Comparator used for sorting the messages.
	 */
	private Comparator sortingComparator;

	/** lazy loaded, temporary list. */
	private transient List current;

	/**
	 * Construct. The first occurence of {@link IFeedbackBoundary}
	 * 	will be searched for higher up in the run-time hierarchy for collecting messages
	 * @param includeNestedComponents
	 * 		Whether to include the messages of any nested component
	 */
	public FeedbackMessagesModel(boolean includeNestedComponents)
	{
		this(includeNestedComponents, null, null);
	}

	/**
	 * Construct.
	 * @param includeNestedComponents
	 * 		Whether to include the messages of any nested component
	 * @param collectingComponent Optional collecting component.
	 * 	When this is not set explicitly, the first occurence of {@link IFeedbackBoundary}
	 * 	will be searched for higher up in the run-time hierarchy.
	 */
	public FeedbackMessagesModel(boolean includeNestedComponents, Component collectingComponent)
	{
		this(includeNestedComponents, collectingComponent, null);
	}

	/**
	 * Construct.
	 * @param includeNestedComponents
	 * 		Whether to include the messages of any nested component
	 * @param collectingComponent Optional collecting component.
	 * 	When this is not set explicitly, the first occurence of {@link IFeedbackBoundary}
	 * 	will be searched for higher up in the run-time hierarchy.
	 * @param sortingComparator 
	 */
	public FeedbackMessagesModel(boolean includeNestedComponents,
			Component collectingComponent, Comparator sortingComparator)
	{
		this.includeNestedComponents = includeNestedComponents;
		this.collectingComponent = collectingComponent;
		this.sortingComparator = sortingComparator;
	}

	/**
	 * @see wicket.model.AbstractDetachableModel#onGetObject(wicket.Component)
	 */
	public Object onGetObject(Component component)
	{
		if(current != null)
		{
			return current;
		}

		// get the message queue
		Page page = component.getPage();
		FeedbackMessages feedbackMessages = page.getFeedbackMessages();

		// if the queue is empty, just return an empty list
		if(feedbackMessages.isEmpty())
		{
			current = Collections.EMPTY_LIST;
		}
		else
		{
			final Component collector;
			if(collectingComponent != null)
			{
				// use the one that was explicitly set
				collector = collectingComponent;
			}
			else
			{
				// find the feedback enabled component that nests the component that uses this model
				// for example, this could be a FeedbackPanel
				collector = component.findParent(IFeedbackBoundary.class);
			}
	
			// get the messages for the target component, recurse depending
			// on property 'includeNestedComponents'
			current = feedbackMessages.messages(collector, includeNestedComponents,
					true, FeedbackMessage.DEBUG);
		}

		// sort the list before returning it
		sort(current);

		return current;
	}

	/**
	 * Sorts the list if property sortingComparator was set, otherwise it
	 * doesn't do any sorting.
	 * @param list list to sort; contains elements of {@link FeedbackMessage}.
	 */
	protected void sort(List list)
	{
		if(sortingComparator != null)
		{
			Collections.sort(list, sortingComparator);
		}
	}

	/**
	 * @see wicket.model.AbstractDetachableModel#onDetach()
	 */
	protected void onDetach()
	{
		current = null;
	}

	/**
	 * @see wicket.model.AbstractDetachableModel#onAttach()
	 */
	protected void onAttach()
	{
		// ignore
	}

	/**
	 * @see wicket.model.AbstractDetachableModel#onSetObject(wicket.Component, java.lang.Object)
	 */
	protected void onSetObject(Component component, Object object)
	{
		// ignore
	}

	/**
	 * @see wicket.model.IModel#getNestedModel()
	 */
	public IModel getNestedModel()
	{
		return null;
	}

	/**
	 * Sets the optional collecting component. When this is not set explicitly, the first occurence
	 * of {@link IFeedbackBoundary} will be searched for higher up in the run-time
	 * hierarchy.
	 * @param collectingComponent the collecting component
	 */
	public void setCollectingComponent(Component collectingComponent)
	{
		this.collectingComponent = collectingComponent;
	}

	/**
	 * Sets the comparator used for sorting the messages.
	 * @param sortingComparator comparator used for sorting the messages
	 */
	public void setSortingComparator(Comparator sortingComparator)
	{
		this.sortingComparator = sortingComparator;
	}
}