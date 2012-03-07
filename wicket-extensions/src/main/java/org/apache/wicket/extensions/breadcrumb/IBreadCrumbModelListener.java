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

import java.util.EventListener;

import org.apache.wicket.util.io.IClusterable;

/**
 * Bread crumb model listeners get notified by {@link IBreadCrumbModel bread crumb models} of
 * {@link #breadCrumbActivated(IBreadCrumbParticipant, IBreadCrumbParticipant) activation},
 * {@link #breadCrumbAdded(IBreadCrumbParticipant) addition} and
 * {@link #breadCrumbRemoved(IBreadCrumbParticipant) removal} events.
 * 
 * @author Eelco Hillenius
 */
public interface IBreadCrumbModelListener extends EventListener, IClusterable
{
	/**
	 * Called when a bread crumb was activated.
	 * 
	 * @param previousParticipant
	 *            The previously active participant
	 * 
	 * @param breadCrumbParticipant
	 *            The bread crumb that was activated.
	 */
	void breadCrumbActivated(IBreadCrumbParticipant previousParticipant,
		IBreadCrumbParticipant breadCrumbParticipant);

	/**
	 * Called when a bread crumb was added to the model.
	 * 
	 * @param breadCrumbParticipant
	 *            The new bread crumb
	 */
	void breadCrumbAdded(IBreadCrumbParticipant breadCrumbParticipant);

	/**
	 * Called when a bread crumb was removed from the model.
	 * 
	 * @param breadCrumbParticipant
	 *            The bread crumb that was removed
	 */
	void breadCrumbRemoved(IBreadCrumbParticipant breadCrumbParticipant);
}
