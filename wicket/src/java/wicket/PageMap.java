/*
 * $Id:PageMap.java 5583 2006-04-30 22:23:23 +0000 (zo, 30 apr 2006) joco01 $
 * $Revision:5583 $ $Date:2006-04-30 22:23:23 +0000 (zo, 30 apr 2006) $
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

import wicket.session.pagemap.IPageMapEntry;
import wicket.util.lang.Objects;

/**
 * @author jcompagner
 */
public abstract class PageMap implements Serializable
{
	private static final long serialVersionUID = 1L;


	/** Name of default pagemap */
	public static final String DEFAULT_NAME = null;

	/** URL to continue to after a given page. */
	private String interceptContinuationURL;

	/** Name of this page map */
	private final String name;

	/** Next available page identifier in this page map. */
	private int pageId = 0;

	/** The session where this PageMap resides */
	private transient Session session;

	/**
	 * Gets a page map for a page map name, automatically creating the page map
	 * if it does not exist. If you do not want the pagemap to be automatically
	 * created, you can call Session.pageMapForName(pageMapName, false).
	 * 
	 * @param pageMapName
	 *            The name of the page map to get
	 * @return The PageMap with the given name from the current session
	 */
	public static PageMap forName(final String pageMapName)
	{
		Session session = Session.get();
		return (session != null) ? session.pageMapForName(pageMapName, true) : null;
	}

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
	public PageMap(String name, Session session)
	{
		this.name = name;
		if (session == null)
		{
			throw new IllegalArgumentException("session must be not null");
		}
		this.session = session;
	}


	/**
	 * Retrieves entry with given id.
	 * 
	 * @param id
	 *            The page identifier
	 * @return Any entry having the given id
	 */
	public final IPageMapEntry getEntry(final int id)
	{
		return (IPageMapEntry)session.getAttribute(attributeForId(id));
	}

	/**
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
	 * @return True if this is the default page map
	 */
	public final boolean isDefault()
	{
		return name == PageMap.DEFAULT_NAME;
	}

	/**
	 * @return The next id for this pagemap
	 */
	final int nextId()
	{
		dirty();
		return this.pageId++;
	}

	protected final void dirty()
	{
		session.dirtyPageMap(this);
	}

	/**
	 * @param id
	 *            The page id to create an attribute for
	 * @return The session attribute for the given page (for replication of
	 *         state)
	 */
	public final String attributeForId(final int id)
	{
		return attributePrefix() + id;
	}

	/**
	 * @return The attribute prefix for this page map
	 */
	final String attributePrefix()
	{
		return Session.pageMapEntryAttributePrefix + name + ":";
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
		final RequestCycle cycle = RequestCycle.get();

		// If there's a place to go to
		if (interceptContinuationURL != null)
		{
			cycle.setRequestTarget(new IRequestTarget()
			{
				final String responseUrl = interceptContinuationURL;

				public void detach(RequestCycle requestCycle)
				{
				}

				public Object getLock(RequestCycle requestCycle)
				{
					return null;
				}

				public void respond(RequestCycle requestCycle)
				{
					// Redirect there
					cycle.getResponse().redirect(responseUrl);
				}

			});

			// Reset interception URL
			interceptContinuationURL = null;

			// Force session to replicate page maps
			dirty();
			return true;
		}
		return false;
	}

	/**
	 * Redirects browser to an intermediate page such as a sign-in page. The
	 * current request's URL is saved exactly as it was requested for future use
	 * by continueToOriginalDestination(); Only use this method when you plan to
	 * continue to the current URL at some later time; otherwise just use
	 * setResponsePage or, when you are in a constructor, redirectTo.
	 * 
	 * @param page
	 *            The page to temporarily redirect to
	 */
	final void redirectToInterceptPage(final Page page)
	{
		// Get the request cycle
		final RequestCycle cycle = RequestCycle.get();

		// The intercept continuation URL should be saved exactly as the
		// original request specified.
		interceptContinuationURL = cycle.getRequest().getURL();

		// Page map is dirty
		dirty();

		// Redirect to the page
		cycle.setRedirect(true);
		cycle.setResponsePage(page);
	}
	
    /**
	 * Redirects browser to an intermediate page such as a sign-in page. The
	 * current request's URL is saved exactly as it was requested for future use
	 * by continueToOriginalDestination(); Only use this method when you plan to
	 * continue to the current URL at some later time; otherwise just use
	 * setResponsePage or, when you are in a constructor, redirectTo.
	 * 
	 * @param pageClazz
	 *            The page clazz to temporarily redirect to
	 */
	final void redirectToInterceptPage(final Class<? extends Page> pageClazz)
	{
		// Get the request cycle
		final RequestCycle cycle = RequestCycle.get();

		// The intercept continuation URL should be saved exactly as the
		// original request specified.
		interceptContinuationURL = cycle.getRequest().getURL();

		// Page map is dirty
		session.dirtyPageMap(this);

		// Redirect to the page
		cycle.setRedirect(true);
		cycle.setResponsePage(pageClazz);
	}	

	/**
	 * Removes all pages from this map
	 */
	public void clear()
	{
		// Remove all entries
		visitEntries(new IVisitor()
		{
			public void entry(IPageMapEntry entry)
			{
				removeEntry(entry);
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

	/**
	 * Removes this PageMap from the Session.
	 */
	public final void remove()
	{
		// First clear all pages from the session for this pagemap
		clear();

		// Then remove the pagemap itself
		session.removePageMap(this);
	}

	/**
	 * Removes the page from the pagemap
	 * 
	 * @param page
	 *            page to be removed from the pagemap
	 */
	public final void remove(final Page page)
	{
		// Remove the pagemap entry from session
		removeEntry(page.getPageMapEntry());
	}

	/**
	 * @param entry
	 *            The entry to remove
	 */
	protected abstract void removeEntry(final IPageMapEntry entry);

	/**
	 * @param page
	 *            The page to put into this map
	 */
	protected abstract void put(final Page page);


	/**
	 * Retrieves page with given id.
	 * 
	 * @param id
	 *            The page identifier
	 * @param versionNumber
	 *            The version to get
	 * @return Any page having the given id
	 */
	protected abstract Page get(final int id, int versionNumber);

	/**
	 * @return Size of this page map in bytes, including a sum of the sizes of
	 *         all the pages it contains.
	 */
	public final long getSizeInBytes()
	{
		long size = Objects.sizeof(this);
		for (Object object : getEntries())
		{
			IPageMapEntry entry = (IPageMapEntry)object;
			if (entry instanceof Page)
			{
				size += ((Page)entry).getSizeInBytes();
			}
			else
			{
				size += Objects.sizeof(entry);
			}
		}
		return size;
	}

	/**
	 * @return List of entries in this page map
	 */
	private final List<Object> getEntries()
	{
		final List attributes = session.getAttributeNames();
		final List<Object> list = new ArrayList<Object>();
		for (final Iterator iterator = attributes.iterator(); iterator.hasNext();)
		{
			final String attribute = (String)iterator.next();
			if (attribute.startsWith(attributePrefix()))
			{
				list.add(session.getAttribute(attribute));
			}
		}
		return list;
	}


	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "[PageMap name=" + name + "]";
	}
}
