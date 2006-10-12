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
import wicket.MarkupContainer;
import wicket.ajax.AjaxEventBehavior;
import wicket.ajax.AjaxRequestTarget;
import wicket.ajax.ClientEvent;
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
	public AjaxPagingNavigationBehavior(IAjaxLink owner, IPageable pageable, ClientEvent event)
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
	@Override
	protected void onEvent(AjaxRequestTarget target)
	{
		// handle the event
		owner.onClick(target);

		// update the container (parent) of the pageable, this assumes that
		// the pageable is a component, and that it is a child of a web
		// markup container. If no parent is found, the whole page will be
		// updated.

		Component container = ((Component<?>)pageable).findParent(MarkupContainer.class);
		target.addComponent(container);

		// find the PagingNavigator parent of this link
		Component navigator = ((Component<?>)owner).findParent(AjaxPagingNavigator.class);

		if (navigator == null)
		{
			// this is an ugly cast, but we do not have IComponent to properly
			// mixin IAjaxLink
			navigator = (Component)owner;
		}

		if (navigator != null)
		{

			if (!(container instanceof MarkupContainer && ((MarkupContainer)container).contains(
					navigator, true)))
			{
				target.addComponent(navigator);
			}
		}

	}
	
	@Override
	protected void onComponentTag(ComponentTag tag)
	{
		if (getComponent().isEnabled() && getComponent().isEnableAllowed()) 
		{
			super.onComponentTag(tag);
		}
	}

	/**
	 * 
	 * @see wicket.ajax.AbstractDefaultAjaxBehavior#getAjaxCallDecorator()
	 */
	@Override
	protected IAjaxCallDecorator getAjaxCallDecorator()
	{
		return new CancelEventIfNoAjaxDecorator();
	}

}
