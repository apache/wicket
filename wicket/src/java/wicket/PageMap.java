/*
 * $Id$ $Revision:
 * 1.5 $ $Date$
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
package wicket;

import java.io.Serializable;
import java.util.Iterator;

import wicket.util.collections.MostRecentlyUsedMap;

/**
 * THIS CLASS IS NOT PART OF THE WICKET PUBLIC API. DO NOT ATTEMPT TO USE IT.
 * 
 * A container for pages held in the session.
 * 
 * @author Jonathan Locke
 */
public final class PageMap implements Serializable
{
	/** Default page map name */
	public static final String defaultName = "main";

	/** URL to continue to after a given page. */
	private String interceptContinuationURL;

	/** Name of this page map */
	private final String name;

	/** Next available page identifier. */
	private int pageId = 0;

	/** The still-live pages for this user session. */
	private transient MostRecentlyUsedMap pages;

	/** The session where this PageMap resides */
	private transient Session session;

	/**
	 * Visitor interface for visiting pages
	 * 
	 * @author Jonathan Locke
	 */
	static interface IVisitor
	{
		/**
		 * @param page
		 *            The page
		 */
		public void page(final Page page);
	}

	/**
	 * Constructor
	 * 
	 * @param name
	 *            The name of this page map
	 * @param session
	 *            The session holding this page map
	 */
	public PageMap(final String name, final Session session)
	{
		this.name = name;
		this.session = session;
	}

	/**
	 * @param page
	 *            The page to add
	 * @return Any Page that got bumped out of the map
	 */
	public final Page add(final Page page)
	{
		// Give page a new id
		page.setId(this.pageId++);
		session.dirty();

		// Add to map
		return put(page);
	}

	/**
	 * Redirects to any intercept page previously specified by a call to
	 * redirectToInterceptPage.
	 * 
	 * @return True if an original destination was redirected to
	 * @see PageMap#redirectToInterceptPage(Page)
	 */
	public final boolean continueToOriginalDestination()
	{
		// Get request cycle
		final RequestCycle cycle = session.getRequestCycle();

		// If there's a place to go to
		if (interceptContinuationURL != null)
		{
			// Redirect there
			cycle.getResponse().redirect(interceptContinuationURL);

			// Since we are explicitly redirecting to a page already, we do not
			// want a second redirect to occur automatically
			cycle.setRedirect(false);

			// Reset interception URL
			interceptContinuationURL = null;
			session.dirty();
			return true;
		}
		return false;
	}

	/**
	 * @param id
	 *            The identifier
	 * @return Any page having the given id
	 */
	public final Page get(final String id)
	{
		final Page page = (Page)getPages().get(id);
		if (page != null)
		{
			page.setDirty(true);
		}
		return page;
	}

	/**
	 * @return Returns the name.
	 */
	public final String getName()
	{
		return name;
	}

	/**
	 * @return True if this is the default page map
	 */
	public final boolean isDefault()
	{
		return name.equals(defaultName);
	}

	/**
	 * @param page
	 *            The page to put into this map
	 * @return Any page that was removed
	 */
	public final Page put(final Page page)
	{
		MostRecentlyUsedMap pages = getPages();
		pages.put(page.getId(), page);
		return (Page)pages.getRemovedValue();
	}

	/**
	 * Redirects browser to an intermediate page such as a sign-in page.
	 * 
	 * @param page
	 *            The sign in page
	 */
	public final void redirectToInterceptPage(final Page page)
	{
		interceptContinuationURL = page.getResponse().encodeURL(page.getRequest().getURL());
		page.redirectTo(page);
		session.dirty();
	}
	
	/**
	 * Removes this PageMap from the Session.
	 */
	public final void remove()
	{
		session.removePageMap(this);
	}

	/**
	 * @param page
	 *            The page to remove
	 */
	public final void remove(final Page page)
	{
		getPages().remove(page.getId());
	}

	/**
	 * Removes all pages from this map
	 */
	public final void removeAll()
	{
		getPages().clear();
	}

	/**
	 * @param session
	 *            Session to set
	 */
	final void setSession(final Session session)
	{
		this.session = session;
	}

	/**
	 * @param visitor
	 *            The visitor to call at each Page in this PageMap.
	 */
	final void visitPages(final IVisitor visitor)
	{
		for (final Iterator iterator = pages.values().iterator(); iterator.hasNext();)
		{
			visitor.page((Page)iterator.next());
		}
	}

	/**
	 * @return MRU map of pages
	 */
	private final MostRecentlyUsedMap getPages()
	{
		if (this.pages == null)
		{
			this.pages = new MostRecentlyUsedMap(session.getApplication().getSettings()
					.getMaxPages());
		}
		return this.pages;
	}
}
