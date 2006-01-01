/*
 * $Id$ $Revision:
 * 1.21 $ $Date$
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

import wicket.markup.html.debug.InspectorPage;
import wicket.request.IRequestCodingStrategy;
import wicket.request.IRequestCycleProcessor;
import wicket.util.lang.Objects;

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
	 * Visitor interface for visiting entries in this map
	 * 
	 * @author Jonathan Locke
	 */
	static interface IVisitor
	{
		/**
		 * @param entry
		 *            The page map entry
		 */
		public void entry(final IPageMapEntry entry);
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
	 * @return List of entries in this page map
	 */
	public final List getEntries()
	{
		final List attributes = session.getAttributeNames();
		final List list = new ArrayList();
		for (final Iterator iterator = attributes.iterator(); iterator.hasNext();)
		{
			final String attribute = (String)iterator.next();
			if (attribute.startsWith(attributePrefix()))
			{
				list.add((IPageMapEntry)session.getAttribute(attribute));
			}
		}
		return list;
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
	 * @return Size of this page map, including a sum of the sizes of all the
	 *         pages it contains.
	 */
	public int getSize()
	{
		int size = Objects.sizeof(this);
		for (Iterator iterator = getEntries().iterator(); iterator.hasNext();)
		{
			IPageMapEntry entry = (IPageMapEntry)iterator.next();
			if (entry instanceof Page)
			{
				size += ((Page)entry).getSize();
			}
			else
			{
				size += Objects.sizeof(entry);
			}
		}
		return size;
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
	 * @return Number of entires in this page map
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
		return session.pageMapEntryAttributePrefix + name + ":";
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
		final IPageMapEntry entry = (IPageMapEntry)session.getAttribute(attributeForId(id));
		if (entry != null)
		{
			// Entry has been accessed
			session.access(entry);

			// Get page as dirty
			final Page page = entry.getPage();
			page.setDirty(true);
			return page;
		}
		return null;
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
	 * @param entry
	 *            The entry to put into this map
	 */
	final synchronized void put(final IPageMapEntry entry)
	{
		if (!(entry instanceof Page && ((Page)entry).isStateless()))
		{
			// Entry has been accessed
			session.access(entry);

			// Store entry in session
			session.setAttribute(attributeForId(entry.getNumericId()), entry);
			size++;
	
			// Evict any page(s) as need be
			session.getApplication().getSettings().getPageMapEvictionStrategy().evict(this);
		}
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
		// TODO this conflicts with the use of IRequestCodingStrategy. We should
		// get
		// rid of encodeURL in favor of IRequestCodingStrategy
		interceptContinuationURL = page.getResponse().encodeURL(cycle.getRequest().getURL());
		cycle.redirectTo(page);

		// TODO why this?
		session.dirty();
	}

	/**
	 * @param entry
	 *            The entry to remove
	 * @return The removed entry
	 */
	final IPageMapEntry remove(final IPageMapEntry entry)
	{
		// Remove entry from session
		session.removeAttribute(attributeForId(entry.getNumericId()));
		size--;
		return entry;
	}

	/**
	 * Removes all pages from this map
	 */
	final void removeAll()
	{
		visitEntries(new IVisitor()
		{
			public void entry(IPageMapEntry entry)
			{
				remove(entry);
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
	final void visitEntries(final IVisitor visitor)
	{
		final List attributes = session.getAttributeNames();
		for (final Iterator iterator = attributes.iterator(); iterator.hasNext();)
		{
			final String attribute = (String)iterator.next();
			if (attribute.startsWith(attributePrefix()))
			{
				visitor.entry((IPageMapEntry)session.getAttribute(attribute));
			}
		}
	}
}
