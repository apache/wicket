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
package org.apache.wicket.extensions.breadcrumb;

import java.util.ArrayList;
import java.util.List;

/**
 * Default breadcrumb model implementation that should be good for 99% of the use cases out there.
 * 
 * @author eelcohillenius
 */
public class DefaultBreadCrumbsModel implements IBreadCrumbModel
{
	private static final long serialVersionUID = 1L;

	/** The currently active participant, if any (possibly null). */
	private IBreadCrumbParticipant activeParticipant = null;

	/** Holds the current list of crumbs. */
	private final List<IBreadCrumbParticipant> crumbs = new ArrayList<>();

	/** listeners utility. */
	private final BreadCrumbModelListenerSupport listenerSupport = new BreadCrumbModelListenerSupport();

	/**
	 * Construct.
	 */
	public DefaultBreadCrumbsModel()
	{
	}

	/**
	 * @see org.apache.wicket.extensions.breadcrumb.IBreadCrumbModel#addListener(org.apache.wicket.extensions.breadcrumb.IBreadCrumbModelListener)
	 */
	@Override
	public final void addListener(final IBreadCrumbModelListener listener)
	{
		listenerSupport.addListener(listener);
	}

	/**
	 * @see org.apache.wicket.extensions.breadcrumb.IBreadCrumbModel#allBreadCrumbParticipants()
	 */
	@Override
	public final List<IBreadCrumbParticipant> allBreadCrumbParticipants()
	{
		return crumbs;
	}

	/**
	 * @see org.apache.wicket.extensions.breadcrumb.IBreadCrumbModel#getActive()
	 */
	@Override
	public IBreadCrumbParticipant getActive()
	{
		return activeParticipant;
	}

	/**
	 * @see org.apache.wicket.extensions.breadcrumb.IBreadCrumbModel#removeListener(org.apache.wicket.extensions.breadcrumb.IBreadCrumbModelListener)
	 */
	@Override
	public final void removeListener(final IBreadCrumbModelListener listener)
	{
		listenerSupport.removeListener(listener);
	}

	/**
	 * @see org.apache.wicket.extensions.breadcrumb.IBreadCrumbModel#setActive(org.apache.wicket.extensions.breadcrumb.IBreadCrumbParticipant)
	 */
	@Override
	public final void setActive(final IBreadCrumbParticipant breadCrumbParticipant)
	{
		// see if the bread crumb was already added, and if so,
		// clean up the stack after (on top of) this bred crumb
		// and notify listeners of the removal
		int len = crumbs.size() - 1;
		int i = len;
		while (i > -1)
		{
			IBreadCrumbParticipant temp = crumbs.get(i);

			// if we found the bread crumb
			if (breadCrumbParticipant.equals(temp))
			{
				// remove the bread crumbs after this one
				int j = len;
				while (j > i)
				{
					// remove and fire event
					IBreadCrumbParticipant removed = crumbs.remove(j--);
					listenerSupport.fireBreadCrumbRemoved(removed);
				}

				// activate the bread crumb participant
				activate(breadCrumbParticipant);

				// we're done; the provided bread crumb is on top
				// and the content is replaced, so just return this function
				return;
			}

			i--;
		}

		// arriving here means we weren't able to find the bread crumb
		// add the new crumb
		crumbs.add(breadCrumbParticipant);

		// and notify listeners
		listenerSupport.fireBreadCrumbAdded(breadCrumbParticipant);

		// activate the bread crumb participant
		activate(breadCrumbParticipant);
	}

	/**
	 * Activates the bread crumb participant.
	 * 
	 * @param breadCrumbParticipant
	 *            The participant to activate
	 */
	protected final void activate(final IBreadCrumbParticipant breadCrumbParticipant)
	{
		// get old value
		IBreadCrumbParticipant previousParticipant = activeParticipant;

		// and set the provided participant as the active one
		activeParticipant = breadCrumbParticipant;

		// fire bread crumb activated event
		listenerSupport.fireBreadCrumbActivated(previousParticipant, breadCrumbParticipant);

		// signal the bread crumb participant that it is selected as the
		// currently active one
		breadCrumbParticipant.onActivate(previousParticipant);
	}
}
