/*
 * $Id$ $Revision:
 * 1.67 $ $Date$
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.session.pagemap.IPageMapEntry;
import wicket.util.collections.ArrayListStack;
import wicket.util.lang.Objects;

/**
 * A container for pages held in the session. PageMap is a parameter to several
 * methods in the Wicket API. You can get a PageMap by name from a Session with
 * Session.getPageMap(String pageMapName) or more conveniently with
 * PageMap.forName(String pageMapName). But you should not hold onto a reference
 * to the pagemap (just as you should not hold onto a reference to your Session
 * but should get it each time you need it instead). Instead, create a strongly
 * typed accessor method like this:
 * 
 * <pre>
 * public PageMap getMyPageMap()
 * {
 * 	return PageMap.forName(&quot;myPageMapName&quot;);
 * }
 * </pre>
 * 
 * If the page map with the given name is not found, one will be automatically
 * created.
 * 
 * @author Jonathan Locke
 */
public final class PageMap implements Serializable
{
	/** Name of default pagemap */
	public static final String DEFAULT_NAME = null;

	/** Log. */
	private static final Log log = LogFactory.getLog(PageMap.class);

	private static final long serialVersionUID = 1L;

	/** Stack of entry accesses by id */
	private final ArrayListStack/* <Access> */accessStack = new ArrayListStack(8);

	/** URL to continue to after a given page. */
	private String interceptContinuationURL;

	/** Name of this page map */
	private final String name;

	/** Next available page identifier in this page map. */
	private int pageId = 0;

	/** The session where this PageMap resides */
	private transient Session session;

	/**
	 * Holds information about a pagemap access
	 * 
	 * @author Jonathan
	 */
	public static class Access implements Serializable
	{
		private static final long serialVersionUID = 1L;

		int id;
		int version;

		/**
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		public boolean equals(Object obj)
		{
			if (obj instanceof Access)
			{
				Access tmp = (Access)obj;
				return tmp.id == id && tmp.version == version;
			}
			return false;
		}

		/**
		 * Gets id.
		 * 
		 * @return id
		 */
		public final int getId()
		{
			return id;
		}

		/**
		 * Gets version.
		 * 
		 * @return version
		 */
		public final int getVersion()
		{
			return version;
		}

		/**
		 * @see java.lang.Object#hashCode()
		 */
		public int hashCode()
		{
			return id + (version << 16);
		}

		/**
		 * @see java.lang.Object#toString()
		 */
		public String toString()
		{
			return "[Access id=" + id + ", version=" + version + "]";
		}
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
	 * Constructor
	 * 
	 * @param name
	 *            The name of this page map
	 * @param session
	 *            The session holding this page map
	 */
	PageMap(final String name, final Session session)
	{
		this.name = name;
		if (session == null)
		{
			throw new IllegalArgumentException("session must be not null");
		}
		this.session = session;
	}

	/**
	 * Removes all pages from this map
	 */
	public final void clear()
	{
		// Remove all entries
		visitEntries(new IVisitor()
		{
			public void entry(IPageMapEntry entry)
			{
				removeEntry(entry);
			}
		});

		// Clear access stack
		accessStack.clear();
	}

	// TODO Post 1.2: We should encode the page id of the current page into the
	// URL for truly stateless pages so we can adjust the stack correctly

	/**
	 * Returns a stack of PageMap.Access entries pushed in the order that the
	 * pages and versions were accessed.
	 * 
	 * @return Stack containing ids of entries in access order.
	 */
	public final ArrayListStack getAccessStack()
	{
		return accessStack;
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
		return(IPageMapEntry)session.getAttribute(attributeForId(id));
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
	 * @return Size of this page map in bytes, including a sum of the sizes of
	 *         all the pages it contains.
	 */
	public final long getSizeInBytes()
	{
		long size = Objects.sizeof(this);
		for (Iterator iterator = getEntries().iterator(); iterator.hasNext();)
		{
			IPageMapEntry entry = (IPageMapEntry)iterator.next();
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
	 * @return Number of page versions stored in this page map
	 */
	public final int getVersions()
	{
		return accessStack.size();
	}

	/**
	 * @return True if this is the default page map
	 */
	public final boolean isDefault()
	{
		return name == PageMap.DEFAULT_NAME;
	}

	/**
	 * Gets the most recently accessed page map entry off the top of the entry
	 * access stack. This is guaranteed to be the most recently accessed entry
	 * IF AND ONLY IF the user just came from a stateful page. If the user could
	 * get to the current page from a stateless page, this method may not work
	 * if the user uses the back button. For a detailed explanation of this
	 * issue, see getAccessStack().
	 * 
	 * @see PageMap#getAccessStack()
	 * 
	 * @return Previous pagemap entry in terms of access
	 */
	public final IPageMapEntry lastAccessedEntry()
	{
		return getEntry(peekAccess().getId());
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
	public final void removeEntry(final IPageMapEntry entry)
	{
		if(entry == null)
		{
			// TODO this shouldn't happen but to many people are still getting this now and then/
			// so first this "fix"
			log.warn("PageMap.removeEntry called with an null entry");
			return;
		}
		// Remove entry from session
		synchronized (session)
		{
			session.removeAttribute(attributeForId(entry.getNumericId()));
	
			// Remove page from acccess stack
			final Iterator stack = accessStack.iterator();
			while (stack.hasNext())
			{
				final Access access = (Access)stack.next();
				if (access.id == entry.getNumericId())
				{
					stack.remove();
				}
			}
	
			// Let the session know we changed the pagemap
			session.dirtyPageMap(this);
		}
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return "[PageMap name=" + name + ", access=" + accessStack + "]";
	}

	/**
	 * @param id
	 *            The page id to create an attribute for
	 * @return The session attribute for the given page (for replication of
	 *         state)
	 */
	final String attributeForId(final int id)
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
			session.dirtyPageMap(this);
			return true;
		}
		return false;
	}

	/**
	 * Retrieves page with given id.
	 * 
	 * @param id
	 *            The page identifier
	 * @param versionNumber
	 *            The version to get
	 * @return Any page having the given id
	 */
	final Page get(final int id, int versionNumber)
	{
		final IPageMapEntry entry = (IPageMapEntry)session.getAttribute(attributeForId(id));
		if (entry != null)
		{
			// Get page as dirty
			Page page = entry.getPage();

			// TODO Performance: Is this really the case is a page always dirty
			// even if we just render it again? POSSIBLE ANSWER: The page could
			// mark itself as clean to prevent replication, but the reverse is
			// probably not desirable (pages marking themselves dirty manually)
			// We ought to think about this a bit and consider whether this
			// could be tied in with version management. It's only when a page's
			// version changes that it should be considered dirty, because then
			// some kind of state changed. Right? - Jonathan
			page.dirty();

			// Get the version of the page requested from the page
			final Page version = page.getVersion(versionNumber);

			// Entry has been accessed
			//pushAccess(entry);
			// Entry has been accessed
			access(entry, versionOf(entry));
			

			// Is the requested version available?
			if (version != null)
			{
				// Need to update session with new page?
				if (version != page)
				{
					// This is our new page
					page = version;

					// Replaces old page entry
					page.getPageMap().put(page);
				}
			}
			else
			{
				if (log.isInfoEnabled())
				{
					log.info("Unable to get version " + versionNumber + " of page " + page);
				}
				return null;
			}
			return page;
		}
		return null;
	}

	/**
	 * @return The next id for this pagemap
	 */
	final int nextId()
	{
		session.dirtyPageMap(this);
		return this.pageId++;
	}

	/**
	 * @param page
	 *            The page to put into this map
	 */
	final void put(final Page page)
	{
		// Page only goes into session if it is stateless
		if (!page.isStateless())
		{
			// Get page map entry from page
			final IPageMapEntry entry = page.getPageMapEntry();

			// Entry has been accessed
			pushAccess(entry);

			// Store entry in session
			final String attribute = attributeForId(entry.getNumericId());
			
			if(session.getAttribute(attribute) == null)
			{
				// Set attribute if it is a new page, so that it will exists
				// already for other threads that can come on the same time.
				session.setAttribute(attribute, entry);
			}
			else
			{
				// Else don't set it directly but add to the dirty map
				session.dirtyPage(page);
			}

			// Evict any page(s) as need be
			session.getApplication().getSessionSettings().getPageMapEvictionStrategy().evict(this);
		}
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
		session.dirtyPageMap(this);

		// Redirect to the page
		cycle.setRedirect(true);
		cycle.setResponsePage(page);
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
	 * @param entry
	 *            Add entry to access list
	 * @param version
	 *            Version number being accessed
	 */
	private final void access(final IPageMapEntry entry, final int version)
	{
		// See if the version being accessed is already in the stack
		boolean add = true;
		int id = entry.getNumericId();
		for (int i = accessStack.size()-1; i >=0 ; i--)
		{
			final Access access = (Access)accessStack.get(i);

			// If we found id and version in access stack
			if (access.id == id && access.version == version)
			{
				// No need to add since id and version are already in stack
				add = false;

				// Pop entries to reveal that version at top of stack
				// because the user used the back button
				while (i < accessStack.size() - 1)
				{
					// Pop unreachable access off top of stack
					final Access topAccess = popAccess();

					// Get entry for access
					final IPageMapEntry top = getEntry(topAccess.getId());

					// If it's a page we can remove version info
					if (top instanceof Page)
					{
						// If there's more than one version
						Page topPage = (Page)top;
						if (topPage.getVersions() > 1)
						{
							// Remove version the top access version (-1)
							topPage.getVersion(topAccess.getVersion()-1);
						}
						else
						{
							// Remove whole page
							remove(topPage);
						}
					}
					else
					{
						// Remove entry
						removeEntry(top);
					}
				}
				break;
			}
		}

		// If the user did not use the back button
		if (add)
		{
			pushAccess(entry);
		}
	}

	/**
	 * @return List of entries in this page map
	 */
	private final List getEntries()
	{
		final List attributes = session.getAttributeNames();
		final List list = new ArrayList();
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
	 * @return Access entry on top of the access stack
	 */
	private final Access peekAccess()
	{
		return (Access)accessStack.peek();
	}

	/**
	 * Removes access entry on top of stack
	 * 
	 * @return Access entry on top of the access stack
	 */
	private final Access popAccess()
	{
		session.dirtyPageMap(this);
		return (Access)accessStack.pop();
	}

	/**
	 * @param entry
	 *            Entry that was accessed
	 */
	private final void pushAccess(IPageMapEntry entry)
	{
		// Create new access entry
		final Access access = new Access();
		access.id = entry.getNumericId();
		access.version = versionOf(entry);
		if(accessStack.size() > 0)
		{
			if(peekAccess().equals(access))
			{
				return;
			}
			int index = accessStack.indexOf(access);
			if (index >= 0)
			{
				accessStack.remove(index);
			}
		}
		accessStack.push(access);
		session.dirtyPageMap(this);
	}

	/**
	 * @param entry
	 *            Page map entry
	 * @return Version of entry
	 */
	private final int versionOf(final IPageMapEntry entry)
	{
		if (entry instanceof Page)
		{
			return ((Page)entry).getCurrentVersionNumber();
		}

		// If entry is not a page, it cannot have versions because the Page
		// is constructed on the fly.
		return 0;
	}
}
