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

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.IAjaxLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.navigation.paging.IPageable;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigationLink;

/**
 * An Ajax version of a link to a page of a PageableListView.
 * 
 * @since 1.2
 * 
 * @author Martijn Dashorst
 */
public class AjaxPagingNavigationLink extends PagingNavigationLink<Void> implements IAjaxLink
{
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            See Component
	 * @param pageable
	 *            The pageable component for this page link
	 * @param pageNumber
	 *            The page number in the PageableListView that this link links to. Negative
	 *            pageNumbers are relative to the end of the list.
	 */
	public AjaxPagingNavigationLink(final String id, final IPageable pageable, final long pageNumber)
	{
		super(id, pageable, pageNumber);

		setOutputMarkupId(true);
	}

	@Override
	protected void onInitialize()
	{
		super.onInitialize();
		add(newAjaxPagingNavigationBehavior(pageable, "click"));
	}

	/**
	 * @param pageable
	 *            The pageable component the page links are referring to
	 * @param event
	 *            the name of the default event on which this link will listen to
	 * @return the ajax behavior which will be executed when the user clicks the link
	 */
	protected AjaxPagingNavigationBehavior newAjaxPagingNavigationBehavior(IPageable pageable,
		String event)
	{
		return new AjaxPagingNavigationBehavior(this, pageable, event)
		{
			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes)
			{
				super.updateAjaxAttributes(attributes);
				AjaxPagingNavigationLink.this.updateAjaxAttributes(attributes);
			}
		};
	}

	protected void updateAjaxAttributes(AjaxRequestAttributes attributes)
	{
	}

	/**
	 * Fallback event listener, will redisplay the current page.
	 * 
	 * @see org.apache.wicket.markup.html.link.Link#onClick()
	 */
	@Override
	public void onClick()
	{
		onClick(null);
	}

	/**
	 * Performs the actual action of this component, performing a non-ajax fallback when there was
	 * no AjaxRequestTarget available.
	 * 
	 * @param target
	 *            the request target, when <code>null</code>, a full page refresh will be generated
	 */
	@Override
	public void onClick(AjaxRequestTarget target)
	{
		pageable.setCurrentPage(getPageNumber());
	}

	@Override
	protected void onComponentTag(ComponentTag tag)
	{
		super.onComponentTag(tag);

		// 'onclick' attribute would be set only if this component is attached
		// to HTML element different than 'a'. This 'onclick' will break Ajax's
		// event binding so here we remove it.
		// AjaxFallback is supported only with 'a' HTML element. See WICKET-4862
		tag.remove("onclick");
	}
}
