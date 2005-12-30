/*
 * $Id$ $Revision$
 * $Date$
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import wicket.request.IRequestCycleProcessor;
import wicket.request.IRequestCodingStrategy;

/**
 * THIS CLASS IS NOT PART OF THE WICKET PUBLIC API. DO NOT ATTEMPT TO USE IT.
 * 
 * A container for pages held in the session.
 * 
 * @author Jonathan Locke
 */
public final class PageMap implements Serializable
{
	private static final long serialVersionUID = 1L;

	/** URL to continue to after a given page. */
	private String interceptContinuationURL;

	/** Name of this page map */
	private final String name;

	/** Next available page identifier in this page map. */
	private int pageId = 0;

	/** The session where this PageMap resides */
	private transient Session session;

	/** Number of pages in the map */
	private int size = 0;

	/**
	 * Visitor interface for visiting page sources in this map
	 * 
	 * @author Jonathan Locke
	 */
	static interface IVisitor
	{
		/**
		 * @param pageSource
		 *            The page source
		 */
		public void pageSource(final IPageSource pageSource);
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
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL IT.
	 * 
	 * @return Returns the name.
	 */
	public final String getName()
	{
		return name;
	}
	
	/**
	 * @return The session that this PageMap is in.
	 */
	public final Session getSession()
	{
		return session;
	}
	
	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL IT.
	 * 
	 * @return True if this is the default page map
	 */
	public final boolean isDefault()
	{
		return name == null;
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL IT.
	 * 
	 * Removes this PageMap from the Session.
	 */
	public final void remove()
	{
		session.removePageMap(this);
	}

	/**
	 * @return Number of page sources in the page map
	 */
	public final int size()
	{
		return size;
	}

	/**
	 * @param id
	 *            The page id to create an attribute for
	 * @return The session attribute for the given page (for replication of
	 *         state)
	 */
	String attributeForId(final int id)
	{
		return attributePrefix() + id;
	}
	
	/**
	 * @return The attribute prefix for this page map
	 */
	String attributePrefix()
	{
		return session.pageSourceAttributePrefix + name + ":";
	}

	/**
	 * Redirects to any intercept page previously specified by a call to
	 * redirectToInterceptPage.
	 * 
	 * @return True if an original destination was redirected to
	 * @see PageMap#redirectToInterceptPage(Page)
	 */
	final boolean continueToOriginalDestination()
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

			// Force session to replicate page maps
			session.dirty();
			return true;
		}
		return false;
	}

	/**
	 * Retrieves page with given id.
	 * 
	 * @param id
	 *            The page identifier
	 * @return Any page having the given id
	 */
	final Page get(final int id)
	{
		final IPageSource pageSource = (IPageSource)session.getAttribute(attributeForId(id));
		if (pageSource != null)
		{	
			// Page source has been accessed
			session.access(pageSource);

			// Get page as dirty
			final Page page = pageSource.getPage();
			page.setDirty(true);
			return page;
		}
		return null;
	}
	
	/**
	 * @return List of page sources in this page map
	 */
	final List getPageSources()
	{
		final List attributes = session.getAttributeNames();
		final List list = new ArrayList();
		for (final Iterator iterator = attributes.iterator(); iterator.hasNext();)
		{
			final String attribute = (String)iterator.next();
			if (attribute.startsWith(attributePrefix()))
			{
				list.add((IPageSource)session.getAttribute(attribute));
			}
		}
		return list;
	}

	/**
	 * @return The next id for this pagemap
	 */
	final int nextId()
	{
		session.dirty();
		return this.pageId++;
	}

	/**
	 * @param pageSource
	 *            The page source to put into this map
	 */
	final synchronized void put(final IPageSource pageSource)
	{
		// Page source has been accessed
		session.access(pageSource);

		// Store page source in session
		session.setAttribute(attributeForId(pageSource.getNumericId()), pageSource);
		size++;

		// Evict any page(s) as need be
		session.getApplication().getSettings().getPageMapEvictionStrategy().evict(this);
	}

	/**
	 * Redirects browser to an intermediate page such as a sign-in page. The
	 * current request's url is saved for future use by method
	 * continueToOriginalDestination(); Only use this method when you plan to
	 * continue to the current url at some later time; otherwise just use
	 * setResponsePage or - when you are in a constructor or checkAccessMethod,
	 * call redirectTo.
	 * 
	 * @param page
	 *            The sign in page
	 */
	final void redirectToInterceptPage(final Page page)
	{
		final RequestCycle cycle = session.getRequestCycle();
		IRequestCycleProcessor processor = cycle.getProcessor();
		IRequestCodingStrategy encoder = processor.getRequestCodingStrategy();
		// TODO this conflicts with the use of IRequestCodingStrategy. We should get
		// rid of encodeURL in favor of IRequestCodingStrategy
		interceptContinuationURL = page.getResponse().encodeURL(cycle.getRequest().getURL());
		cycle.redirectTo(page);

		// TODO why this?
		session.dirty();
	}

	/**
	 * @param pageSource
	 *            The page source to remove
	 * @return The removed pageSource
	 */
	final IPageSource remove(final IPageSource pageSource)
	{
		// Remove page source from session
		session.removeAttribute(attributeForId(pageSource.getNumericId()));
		size--;
		return pageSource;
	}
	
	/**
	 * Removes all pages from this map
	 */
	final void removeAll()
	{
		visitPageSources(new IVisitor()
		{
			public void pageSource(IPageSource pageSource)
			{
				remove(pageSource);
			}
		});
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
	final void visitPageSources(final IVisitor visitor)
	{
		final List attributes = session.getAttributeNames();
		for (final Iterator iterator = attributes.iterator(); iterator.hasNext();)
		{
			final String attribute = (String)iterator.next();
			if (attribute.startsWith(attributePrefix()))
			{
				visitor.pageSource((IPageSource)session.getAttribute(attribute));
			}
		}
	}
}
