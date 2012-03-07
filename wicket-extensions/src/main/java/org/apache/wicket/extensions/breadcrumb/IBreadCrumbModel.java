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

import java.util.List;

import org.apache.wicket.extensions.breadcrumb.panel.BreadCrumbPanel;
import org.apache.wicket.util.io.IClusterable;


/**
 * Bread crumbs provide a means to track certain history of client actions. Bread crumbs are
 * typically rendered as a list of links, and are useful when users 'dig deeper' into the site
 * structure so that they can find their way back again and have a notion of where they currently
 * are.
 * <p>
 * Bread crumbs in the original sense just represent where people are in a site hierarchy. For
 * example, when browsing a product site, bread crumbs could look like this:
 * 
 * <pre>
 *          Home &gt; Products &amp; Solutions &gt; Hardware &gt; Desktop Systems
 * </pre>
 * 
 * or
 * 
 * <pre>
 *          World &gt; Europe &gt; The Netherlands &gt; Utrecht
 * </pre>
 * 
 * These items would be rendered as links to the corresponding site location.
 * </p>
 * Classes that implement this interface are responsible for managing such a bread crumb structure.
 * A {@link BreadCrumbBar typical implementation} regards bread crumbs as a stack. When
 * {@link #setActive(IBreadCrumbParticipant) a bread crumb is activated} that was not in the stack
 * yet, it would add it to the stack, or when a bread crumb is activated that is already on the
 * stack, it would roll back to the corresponding depth.
 * <p>
 * This model does not make any presumptions on how it should interact with components. Just that
 * there is a list of {@link IBreadCrumbParticipant bread crumb participants}, and the notion of a
 * currently active bread crumb participant.
 * </p>
 * <p>
 * A {@link IBreadCrumbParticipant bread crumb participant} is not an actual bread crumb, but rather
 * a proxy to components that represent a certain location relative to other bread crumbs in this
 * model, and a means to get the bread crumb title, which is typically rendered as a link label of
 * the actual bread crumb. The actual bread crumbs are supposed to be rendered by a component that
 * works together with this model. I choose this model as this would suit what I think is one of the
 * nicest patterns: {@link BreadCrumbPanel bread crumb aware panels}.
 * </p>
 * 
 * @author Eelco Hillenius
 */
public interface IBreadCrumbModel extends IClusterable
{
	/**
	 * Adds a bread crumb model listener.
	 * 
	 * @param listener
	 *            The listener to add
	 */
	void addListener(IBreadCrumbModelListener listener);

	/**
	 * Lists the bread crumb participants in this model.
	 * 
	 * @return The bread crumbs participants, as list with {@link IBreadCrumbParticipant bread crumb
	 *         participants}.
	 */
	List<IBreadCrumbParticipant> allBreadCrumbParticipants();

	/**
	 * Gets the currently active participant, if any.
	 * 
	 * @return The currently active participant, may be null
	 */
	IBreadCrumbParticipant getActive();

	/**
	 * Removes a bread crumb model listener.
	 * 
	 * @param listener
	 *            The listener to remove
	 */
	void removeListener(IBreadCrumbModelListener listener);

	/**
	 * Sets the {@link IBreadCrumbParticipant bread crumb} as the active one. Implementations should
	 * call {@link IBreadCrumbModelListener#breadCrumbAdded(IBreadCrumbParticipant) bread crumb
	 * added} when the bread crumb was not yet part of the model, and
	 * {@link IBreadCrumbModelListener#breadCrumbRemoved(IBreadCrumbParticipant) bread crumb
	 * removed} for every crumb that was removed as the result of this call.
	 * 
	 * @param breadCrumbParticipant
	 *            The bread crump that should be set as the currently active
	 */
	void setActive(IBreadCrumbParticipant breadCrumbParticipant);
}