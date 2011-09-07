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
package org.apache.wicket.extensions.ajax.markup.html.repeater.data.sort;

import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxChannel;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxCallDecorator;
import org.apache.wicket.ajax.calldecorator.CancelEventIfNoAjaxDecorator;
import org.apache.wicket.ajax.markup.html.IAjaxLink;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.ISortStateLocator;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.OrderByLink;


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
public abstract class AjaxFallbackOrderByLink extends OrderByLink implements IAjaxLink
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final IAjaxCallDecorator decorator;

	/**
	 * Constructor
	 * 
	 * @param id
	 * @param property
	 * @param stateLocator
	 * @param cssProvider
	 */
	public AjaxFallbackOrderByLink(final String id, final String property,
		final ISortStateLocator stateLocator, final ICssProvider cssProvider)
	{
		this(id, property, stateLocator, cssProvider, null);
	}

	/**
	 * Constructor
	 * 
	 * @param id
	 * @param property
	 * @param stateLocator
	 */
	public AjaxFallbackOrderByLink(final String id, final String property,
		final ISortStateLocator stateLocator)
	{
		this(id, property, stateLocator, DefaultCssProvider.getInstance(), null);
	}

	/**
	 * Constructor
	 * 
	 * @param id
	 * @param property
	 * @param stateLocator
	 * @param decorator
	 */
	public AjaxFallbackOrderByLink(final String id, final String property,
		final ISortStateLocator stateLocator, final IAjaxCallDecorator decorator)
	{
		this(id, property, stateLocator, DefaultCssProvider.getInstance(), decorator);
	}

	/**
	 * Constructor
	 * 
	 * @param id
	 * @param property
	 * @param stateLocator
	 * @param cssProvider
	 * @param decorator
	 */
	public AjaxFallbackOrderByLink(final String id, final String property,
		final ISortStateLocator stateLocator, final ICssProvider cssProvider,
		final IAjaxCallDecorator decorator)
	{
		super(id, property, stateLocator, cssProvider);

		this.decorator = decorator;
	}

	@Override
	public void onInitialize()
	{
		super.onInitialize();

		add(newAjaxEventBehavior("onclick"));
	}

	/**
	 * @param event
	 *            the name of the default event on which this link will listen to
	 * @return the ajax behavior which will be executed when the user clicks the link
	 */
	protected AjaxEventBehavior newAjaxEventBehavior(final String event)
	{
		return new AjaxEventBehavior(event)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onEvent(final AjaxRequestTarget target)
			{
				onClick();
				onClick(target);
			}

			@Override
			protected IAjaxCallDecorator getAjaxCallDecorator()
			{
				return new CancelEventIfNoAjaxDecorator(decorator);
			}

			@Override
			protected AjaxChannel getChannel()
			{
				return AjaxFallbackOrderByLink.this.getChannel();
			}
		};

	}

	/**
	 * @return the channel that manages how Ajax calls are executed
	 * @see AbstractDefaultAjaxBehavior#getChannel()
	 */
	protected AjaxChannel getChannel()
	{
		return null;
	}

	/**
	 * Callback method when an ajax click occurs. All the behavior of changing the sort, etc is
	 * already performed before this is called so this method should primarily be used to configure
	 * the target.
	 * 
	 * @param target
	 */
	public abstract void onClick(AjaxRequestTarget target);

}
