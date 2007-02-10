/*
 * $Id$
 * $Revision$ $Date$
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

import wicket.ajax.AjaxRequestTarget;
import wicket.ajax.markup.html.IAjaxLink;
import wicket.markup.html.navigation.paging.IPageable;
import wicket.markup.html.navigation.paging.PagingNavigationLink;

/**
 * An Ajax version of a link to a page of a PageableListView.
 * 
 * @since 1.2
 * 
 * @author Martijn Dashorst
 */
public class AjaxPagingNavigationLink extends PagingNavigationLink implements IAjaxLink
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
	 *            The page number in the PageableListView that this link links
	 *            to. Negative pageNumbers are relative to the end of the list.
	 */
	public AjaxPagingNavigationLink(final String id, final IPageable pageable, final int pageNumber)
	{
		super(id, pageable, pageNumber);
		add(new AjaxPagingNavigationBehavior(this, pageable, "onclick"));
		setOutputMarkupId(true);
	}

	/**
	 * Fallback event listener, will redisplay the current page.
	 * 
	 * @see wicket.markup.html.link.Link#onClick()
	 */
	public void onClick()
	{
		onClick(null);

		// We do not need to redirect
		setRedirect(false);

		// Return the the current page.
		setResponsePage(getPage());
	}

	/**
	 * Performs the actual action of this component, performing a non-ajax
	 * fallback when there was no AjaxRequestTarget available.
	 * 
	 * @param target
	 *            the request target, when <code>null</code>, a full page
	 *            refresh will be generated
	 */
	public void onClick(AjaxRequestTarget target)
	{
		pageable.setCurrentPage(getPageNumber());
	}
}
