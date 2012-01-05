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
package org.apache.wicket.ajax.markup.html.navigation.paging;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.IAjaxLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.navigation.paging.IPageable;

/**
 * Ajax behavior for the paging navigation links. This behavior can only have one parent: the link
 * it is attached to.
 * 
 * @since 1.2
 * 
 * @author Martijn Dashorst
 */
public class AjaxPagingNavigationBehavior extends AjaxEventBehavior
{
	/** For serialization. */
	private static final long serialVersionUID = 1L;

	/**
	 * The ajaxian link that should receive the event.
	 */
	private final IAjaxLink owner;

	/**
	 * Attaches the navigation behavior to the owner link and drives the pageable component. The
	 * behavior is attached to the markup event.
	 * 
	 * @param owner
	 *            the owner ajax link
	 * @param pageable
	 *            the pageable to update
	 * @param event
	 *            the javascript event to bind to (e.g. onclick)
	 */
	public AjaxPagingNavigationBehavior(IAjaxLink owner, IPageable pageable, String event)
	{
		super(event);
		this.owner = owner;
	}

	/**
	 * The ajax event handler. This will execute the event, and update the following components,
	 * when present: the navigator the owner link is part of, or when the link is a stand alone
	 * component, the link itself. Also the pageable's parent markup container is updated, so its
	 * contents can be replaced with the newly generated pageable.
	 * 
	 * @see org.apache.wicket.ajax.AjaxEventBehavior#onEvent(org.apache.wicket.ajax.AjaxRequestTarget)
	 */
	@Override
	protected void onEvent(AjaxRequestTarget target)
	{
		// handle the event
		owner.onClick(target);

		// find the PagingNavigator parent of this link
		AjaxPagingNavigator navigator = ((Component)owner).findParent(AjaxPagingNavigator.class);

		// if this is embedded inside a navigator
		if (navigator != null)
		{
			// tell the PagingNavigator to update the IPageable
			navigator.onAjaxEvent(target);
		}
	}

	/**
	 * @see org.apache.wicket.ajax.AjaxEventBehavior#onComponentTag(org.apache.wicket.markup.ComponentTag)
	 */
	@Override
	protected void onComponentTag(ComponentTag tag)
	{
		if (getComponent().isEnabledInHierarchy())
		{
			super.onComponentTag(tag);
		}
	}
}
