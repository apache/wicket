/*
 * $Id$ $Revision$ $Date$
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
package wicket.extensions.ajax.markup.html.repeater.data.sort;

import wicket.ajax.AjaxEventBehavior;
import wicket.ajax.AjaxRequestTarget;
import wicket.ajax.ClientEvent;
import wicket.ajax.IAjaxCallDecorator;
import wicket.ajax.calldecorator.CancelEventIfNoAjaxDecorator;
import wicket.extensions.markup.html.repeater.data.sort.ISortStateLocator;
import wicket.extensions.markup.html.repeater.data.sort.OrderByLink;
import wicket.markup.html.WebMarkupContainer;

/**
 * Ajaxified {@link OrderByLink}
 * 
 * @see OrderByLink
 * 
 * @since 1.2.1
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public abstract class AjaxFallbackOrderByLink extends OrderByLink
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 * 
	 * @param parent
	 * @param id
	 * @param property
	 * @param stateLocator
	 * @param cssProvider
	 */
	public AjaxFallbackOrderByLink(WebMarkupContainer parent, String id, String property,
			ISortStateLocator stateLocator, ICssProvider cssProvider)
	{
		this(parent, id, property, stateLocator, cssProvider, null);
	}

	/**
	 * Constructor
	 * 
	 * @param parent
	 * @param id
	 * @param property
	 * @param stateLocator
	 */
	public AjaxFallbackOrderByLink(WebMarkupContainer parent, String id, String property,
			ISortStateLocator stateLocator)
	{
		this(parent, id, property, stateLocator, DefaultCssProvider.getInstance(), null);
	}


	/**
	 * Constructor
	 * 
	 * @param parent
	 * @param id
	 * @param property
	 * @param stateLocator
	 * @param decorator
	 */
	public AjaxFallbackOrderByLink(WebMarkupContainer parent, String id, String property,
			ISortStateLocator stateLocator, final IAjaxCallDecorator decorator)
	{
		this(parent, id, property, stateLocator, DefaultCssProvider.getInstance(), decorator);
	}

	/**
	 * Constructor
	 * 
	 * @param parent
	 * @param id
	 * @param property
	 * @param stateLocator
	 * @param cssProvider
	 * @param decorator
	 */
	public AjaxFallbackOrderByLink(WebMarkupContainer parent, String id, String property,
			ISortStateLocator stateLocator, ICssProvider cssProvider,
			final IAjaxCallDecorator decorator)
	{
		super(parent, id, property, stateLocator, cssProvider);

		add(new AjaxEventBehavior(ClientEvent.CLICK)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onEvent(AjaxRequestTarget target)
			{
				onClick();
				onAjaxClick(target);
			}

			@Override
			protected IAjaxCallDecorator getAjaxCallDecorator()
			{
				return new CancelEventIfNoAjaxDecorator(decorator);
			}

		});

	}

	/**
	 * Callback method when an ajax click occurs. All the behavior of changing
	 * the sort, etc is already performed bfore this is called so this method
	 * should primarily be used to configure the target.
	 * 
	 * @param target
	 */
	protected abstract void onAjaxClick(AjaxRequestTarget target);


}
