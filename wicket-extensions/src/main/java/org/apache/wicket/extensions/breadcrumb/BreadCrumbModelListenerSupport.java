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

import org.apache.wicket.util.io.IClusterable;


/**
 * Utility class for working with {@link IBreadCrumbModelListener bread crumb model listeners}.
 * 
 * @author Eelco Hillenius
 */
public final class BreadCrumbModelListenerSupport implements IClusterable
{
	private static final long serialVersionUID = 1L;

	/** listeners for bread crumb events. */
	private final List<IBreadCrumbModelListener> listeners = new ArrayList<IBreadCrumbModelListener>(
		1);

	/**
	 * Adds a bread crumb model listener.
	 * 
	 * @param listener
	 *            The listener to add
	 */
	public final void addListener(final IBreadCrumbModelListener listener)
	{
		if (listener == null)
		{
			throw new IllegalArgumentException("argument listener must be not null");
		}
		listeners.add(listener);
	}

	/**
	 * Notifies all listeners that a bread crumb was activated.
	 * 
	 * @param previousParticipant
	 *            The previously active participant
	 * 
	 * @param breadCrumbParticipant
	 *            The activated bread crumb
	 */
	public final void fireBreadCrumbActivated(final IBreadCrumbParticipant previousParticipant,
		final IBreadCrumbParticipant breadCrumbParticipant)
	{
		for (IBreadCrumbModelListener listener1 : listeners)
		{
			listener1.breadCrumbActivated(previousParticipant, breadCrumbParticipant);
		}
	}

	/**
	 * Notifies all listeners that a new bread crumb was added.
	 * 
	 * @param breadCrumbParticipant
	 *            The newly added bread crumb
	 */
	public final void fireBreadCrumbAdded(final IBreadCrumbParticipant breadCrumbParticipant)
	{
		for (IBreadCrumbModelListener listener : listeners)
		{
			listener.breadCrumbAdded(breadCrumbParticipant);
		}
	}

	/**
	 * Notifies all listeners that a bread crumb was removed.
	 * 
	 * @param breadCrumbParticipant
	 *            The removed bread crumb
	 */
	public final void fireBreadCrumbRemoved(final IBreadCrumbParticipant breadCrumbParticipant)
	{
		for (IBreadCrumbModelListener listener : listeners)
		{
			listener.breadCrumbRemoved(breadCrumbParticipant);
		}
	}

	/**
	 * Removes a bread crumb model listener.
	 * 
	 * @param listener
	 *            The listener to remove
	 */
	public final void removeListener(final IBreadCrumbModelListener listener)
	{
		if (listener != null)
		{
			listeners.remove(listener);
		}
	}
}
