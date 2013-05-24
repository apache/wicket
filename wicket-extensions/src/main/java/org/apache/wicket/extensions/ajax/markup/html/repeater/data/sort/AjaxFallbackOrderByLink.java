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

import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.IAjaxCallListener;
import org.apache.wicket.ajax.markup.html.IAjaxLink;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.ISortStateLocator;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.OrderByLink;


/**
 * Ajaxified {@link OrderByLink}
 *
 * @param <S>
 *            the type of the sort property
 * @see OrderByLink
 * 
 * @since 1.2.1
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public abstract class AjaxFallbackOrderByLink<S> extends OrderByLink<S> implements IAjaxLink
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final IAjaxCallListener ajaxCallListener;

	/**
	 * Constructor
	 * 
	 * @param id
	 * @param sortProperty
	 * @param stateLocator
	 * @param cssProvider
	 */
	public AjaxFallbackOrderByLink(final String id, final S sortProperty,
		final ISortStateLocator<S> stateLocator, final ICssProvider<S> cssProvider)
	{
		this(id, sortProperty, stateLocator, cssProvider, null);
	}

	/**
	 * Constructor
	 * 
	 * @param id
	 * @param sortProperty
	 * @param stateLocator
	 */
	public AjaxFallbackOrderByLink(final String id, final S sortProperty,
		final ISortStateLocator<S> stateLocator)
	{
		this(id, sortProperty, stateLocator, new DefaultCssProvider<S>(), null);
	}

	/**
	 * Constructor
	 * 
	 * @param id
	 * @param sortProperty
	 * @param stateLocator
	 * @param ajaxCallListener
	 */
	public AjaxFallbackOrderByLink(final String id, final S sortProperty,
		final ISortStateLocator<S> stateLocator, final IAjaxCallListener ajaxCallListener)
	{
		this(id, sortProperty, stateLocator, new DefaultCssProvider<S>(), ajaxCallListener);
	}

	/**
	 * Constructor
	 * 
	 * @param id
	 * @param sortProperty
	 * @param stateLocator
	 * @param cssProvider
	 * @param ajaxCallListener
	 */
	public AjaxFallbackOrderByLink(final String id, final S sortProperty,
		final ISortStateLocator<S> stateLocator, final ICssProvider<S> cssProvider,
		final IAjaxCallListener ajaxCallListener)
	{
		super(id, sortProperty, stateLocator, cssProvider);

		this.ajaxCallListener = ajaxCallListener;
	}

	@Override
	public void onInitialize()
	{
		super.onInitialize();

		add(newAjaxEventBehavior("click"));
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
				AjaxFallbackOrderByLink.this.onClick(target);
			}

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes)
			{
				super.updateAjaxAttributes(attributes);
				attributes.setPreventDefault(true);
				if (ajaxCallListener != null) {
					attributes.getAjaxCallListeners().add(ajaxCallListener);
				}
			}

		};

	}

	/**
	 * Callback method when an ajax click occurs. All the behavior of changing the sort, etc is
	 * already performed before this is called so this method should primarily be used to configure
	 * the target.
	 * 
	 * @param target
	 */
	@Override
	public abstract void onClick(AjaxRequestTarget target);

}
