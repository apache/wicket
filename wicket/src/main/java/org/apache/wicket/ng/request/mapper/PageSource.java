package org.apache.wicket.ng.request.mapper;

import org.apache.wicket.ng.request.component.PageParameters;
import org.apache.wicket.ng.request.component.RequestablePage;

public interface PageSource
{
	/**
	 * Returns existing page instance if the page exists.
	 * 
	 * @param pageId
	 * @return page instance or <code>null</code> if the page does not exist.
	 */
	public RequestablePage getPageInstance(int pageId);

	/**
	 * Creates new page instance of page with given class. The page should be marked as create
	 * bookmarkable, so subsequent calls to {@link RequestablePage#wasCreatedBookmarkable()} must return
	 * <code>true</code>
	 * 
	 * @param pageMapName
	 * @param pageClass
	 * @param pageParameters
	 * @return new page instance
	 */
	public RequestablePage newPageInstance(Class<? extends RequestablePage> pageClass, PageParameters pageParameters);

}
