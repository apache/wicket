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

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.IAjaxCallListener;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.ISortStateLocator;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.OrderByBorder;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.OrderByLink;


/**
 * Ajaxified version of {@link OrderByBorder}
 *
 * @param <S>
 *            the type of the sort property
 * @see OrderByBorder
 * 
 * @since 1.2.1
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public abstract class AjaxFallbackOrderByBorder<S> extends OrderByBorder<S>
{
	private static final long serialVersionUID = 1L;
	private IAjaxCallListener ajaxCallListener;

	/**
	 * Constructor
	 * 
	 * @param id
	 * @param sortProperty
	 * @param stateLocator
	 */
	public AjaxFallbackOrderByBorder(final String id, final S sortProperty,
		final ISortStateLocator<S> stateLocator)
	{
		this(id, sortProperty, stateLocator, null);
	}

	/**
	 * Constructor
	 * 
	 * @param id
	 * @param sortProperty
	 * @param stateLocator
	 * @param ajaxCallListener
	 */
	public AjaxFallbackOrderByBorder(final String id, final S sortProperty,
		final ISortStateLocator<S> stateLocator, final IAjaxCallListener ajaxCallListener)
	{
		super(id, sortProperty, stateLocator);

		this.ajaxCallListener = ajaxCallListener;
	}

	@Override
	protected OrderByLink<S> newOrderByLink(String id, S property,
		ISortStateLocator<S> stateLocator)
	{
		return new AjaxFallbackOrderByLink<S>("orderByLink", property, stateLocator, ajaxCallListener)
		{

			private static final long serialVersionUID = 1L;

			@Override
			protected void onSortChanged()
			{
				AjaxFallbackOrderByBorder.this.onSortChanged();
			}

			@Override
			public void onClick(final AjaxRequestTarget target)
			{
				AjaxFallbackOrderByBorder.this.onAjaxClick(target);

			}
		};
	}
	/**
	 * This method is a hook for subclasses to perform an action after sort has changed
	 */
	protected void onSortChanged()
	{
		// noop
	}

	protected abstract void onAjaxClick(AjaxRequestTarget target);


}
