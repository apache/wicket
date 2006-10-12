/*
 * $Id: AjaxPagingNavigationBehavior.java 4635 2006-02-25 16:24:23 -0800 (Sat,
 * 25 Feb 2006) dashorst $ $Revision$ $Date: 2006-02-25 16:24:23 -0800
 * (Sat, 25 Feb 2006) $
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
package wicket.ajax.markup.html.navigation.paging;

import wicket.Component;
import wicket.WicketRuntimeException;
import wicket.ajax.AjaxEventBehavior;
import wicket.ajax.AjaxRequestTarget;
import wicket.ajax.IAjaxCallDecorator;
import wicket.ajax.calldecorator.CancelEventIfNoAjaxDecorator;
import wicket.ajax.markup.html.IAjaxLink;
import wicket.markup.ComponentTag;
import wicket.markup.html.navigation.paging.IPageable;

/**
 * Ajax behavior for the paging navigation links. This behavior can only have
 * one parent: the link it is attached to.
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
	 * The ajaxian link that should recieve the event.
	 */
	private final IAjaxLink owner;

	/**
	 * The pageable component to update.
	 */
	private final IPageable pageable;

	/**
	 * Attaches the navigation behavior to the owner link and drives the
	 * pageable component. The behavior is attached to the markup event.
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
		this.pageable = pageable;
	}

	/**
	 * The ajax event handler. This will execute the event, and update the
	 * following components, when present: the navigator the owner link is part
	 * of, or when the link is a stand alone component, the link itself. Also
	 * the pageable's parent markup container is updated, so its contents can be
	 * replaced with the newly generated pageable.
	 * 
	 * @see wicket.ajax.AjaxEventBehavior#onEvent(wicket.ajax.AjaxRequestTarget)
	 */
	protected void onEvent(AjaxRequestTarget target)
	{
		// handle the event
		owner.onClick(target);

		// find the PagingNavigator parent of this link
		AjaxPagingNavigator navigator = (AjaxPagingNavigator)((Component)owner)
				.findParent(AjaxPagingNavigator.class);
		if (navigator == null)
		{
			throw new WicketRuntimeException(
					"Unable to find AjaxPagingNavigator component in hierarchy starting from "
							+ owner);
		}

		// tell the PagingNavigator to update the IPageable
		navigator.onAjaxEvent(target);		
	}

	/**
	 * 
	 * @see wicket.ajax.AbstractDefaultAjaxBehavior#getAjaxCallDecorator()
	 */
	protected IAjaxCallDecorator getAjaxCallDecorator()
	{
		return new CancelEventIfNoAjaxDecorator();
	}

	/**
	 * @see wicket.ajax.AjaxEventBehavior#onComponentTag(wicket.markup.ComponentTag)
	 */
	protected void onComponentTag(ComponentTag tag)
	{
		if (getComponent().isEnabled() && getComponent().isEnableAllowed())
		{
			super.onComponentTag(tag);
		}
	}
}
