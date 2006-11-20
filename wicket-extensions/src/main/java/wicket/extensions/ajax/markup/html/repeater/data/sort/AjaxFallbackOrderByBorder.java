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
package wicket.extensions.ajax.markup.html.repeater.data.sort;

import wicket.ajax.AjaxRequestTarget;
import wicket.ajax.IAjaxCallDecorator;
import wicket.extensions.markup.html.repeater.data.sort.ISortStateLocator;
import wicket.extensions.markup.html.repeater.data.sort.OrderByBorder;
import wicket.markup.html.border.Border;


/**
 * Ajaxified version of {@link OrderByBorder}
 * 
 * @see OrderByBorder
 * 
 * @since 1.2.1
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public abstract class AjaxFallbackOrderByBorder extends Border
{
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 * 
	 * @param id
	 * @param property
	 * @param stateLocator
	 */
	public AjaxFallbackOrderByBorder(String id, String property, ISortStateLocator stateLocator)
	{
		this(id, property, stateLocator, AjaxFallbackOrderByLink.DefaultCssProvider.getInstance(),
				null);
	}


	/**
	 * Constructor
	 * 
	 * @param id
	 * @param property
	 * @param stateLocator
	 * @param cssProvider
	 */
	public AjaxFallbackOrderByBorder(String id, String property, ISortStateLocator stateLocator,
			AjaxFallbackOrderByLink.ICssProvider cssProvider)
	{
		this(id, property, stateLocator, cssProvider, null);
	}


	/**
	 * Constructor
	 * 
	 * @param id
	 * @param property
	 * @param stateLocator
	 * @param decorator
	 */
	public AjaxFallbackOrderByBorder(String id, String property, ISortStateLocator stateLocator,
			IAjaxCallDecorator decorator)
	{
		this(id, property, stateLocator, AjaxFallbackOrderByLink.DefaultCssProvider.getInstance(),
				decorator);
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
	public AjaxFallbackOrderByBorder(String id, String property, ISortStateLocator stateLocator,
			AjaxFallbackOrderByLink.ICssProvider cssProvider, final IAjaxCallDecorator decorator)
	{
		super(id);
		AjaxFallbackOrderByLink link = new AjaxFallbackOrderByLink("orderByLink", property,
				stateLocator, cssProvider, decorator)
		{

			private static final long serialVersionUID = 1L;

			protected void onSortChanged()
			{
				AjaxFallbackOrderByBorder.this.onSortChanged();
			}

			protected void onAjaxClick(AjaxRequestTarget target)
			{
				AjaxFallbackOrderByBorder.this.onAjaxClick(target);

			}
		};
		add(link);
		add(new AjaxFallbackOrderByLink.CssModifier(link, cssProvider));
	}

	/**
	 * This method is a hook for subclasses to perform an action after sort has
	 * changed
	 */
	protected void onSortChanged()
	{
		// noop
	}

	protected abstract void onAjaxClick(AjaxRequestTarget target);


}
