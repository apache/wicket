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
package org.apache.wicket.markup.html;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.internal.HtmlHeaderContainer;
import org.apache.wicket.model.IModel;

/**
 * WebMarkupContainer with it's own markup and possibly <wicket:head> tag.
 * 
 * @author Juergen Donnerstag
 * 
 * @param <T>
 *            The model object type
 */
public class WebMarkupContainerWithAssociatedMarkup<T> extends WebMarkupContainer<T>
	implements
		IHeaderPartContainerProvider
{
	private static final long serialVersionUID = 1L;

	/** A utility class which implements the internals */
	private ContainerWithAssociatedMarkupHelper markupHelper;

	/**
	 * @see Component#Component(String)
	 */
	public WebMarkupContainerWithAssociatedMarkup(final String id)
	{
		super(id);
	}

	/**
	 * @see org.apache.wicket.Component#Component(String, IModel)
	 */
	public WebMarkupContainerWithAssociatedMarkup(final String id, IModel<T> model)
	{
		super(id, model);
	}

	/**
	 * Called by components like Panel and Border which have associated Markup and which may have a
	 * &lt;wicket:head&gt; tag.
	 * <p>
	 * Whereas 'this' might be a Panel or Border, the HtmlHeaderContainer parameter has been added
	 * to the Page as a container for all headers any of its components might wish to contribute.
	 * <p>
	 * The headers contributed are rendered in the standard way.
	 * 
	 * @param container
	 *            The HtmlHeaderContainer added to the Page
	 */
	protected final void renderHeadFromAssociatedMarkupFile(final HtmlHeaderContainer container)
	{
		if (markupHelper == null)
		{
			markupHelper = new ContainerWithAssociatedMarkupHelper(this);
		}

		markupHelper.renderHeadFromAssociatedMarkupFile(container);
	}

	/**
	 * @see org.apache.wicket.markup.html.IHeaderPartContainerProvider#newHeaderPartContainer(java.lang.String,
	 *      java.lang.String)
	 */
	public HeaderPartContainer< ? > newHeaderPartContainer(final String id, final String scope)
	{
		return new HeaderPartContainer<Object>(id, this, scope);
	}
}