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
package wicket;

import java.io.Serializable;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import wicket.session.pagemap.IPageMapEntry;
import wicket.util.collections.ArrayListStack;

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
 * public IPageMap getMyPageMap()
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
public class AccessStackPageMap extends PageMap implements Serializable
{
	private static final long serialVersionUID = 1L;

	/** Log. */
	private static final Logger log = LoggerFactory.getLogger(AccessStackPageMap.class);


	/** Stack of entry accesses by id */
	private final ArrayListStack/* <Access> */<Access> accessStack = new ArrayListStack<Access>(8);


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
		@Override
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
		@Override
		public int hashCode()
		{
			return id + (version << 16);
		}

		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString()
		{
			return "[Access id=" + id + ", version=" + version + "]";
		}
	}

	/**
	 * Constructor
	 * 
	 * @param name
	 *            The name of this page map
	 * @param session
	 *            The session holding this page map
	 */
	public AccessStackPageMap(final String name, final Session session)
	{
		super(name, session);
	}

	/**
	 * Removes all pages from this map
	 */
	@Override
	public final void clear()
	{
		super.clear();
		// Clear access stack
		accessStack.clear();
		dirty();
	}

	// TODO Post 1.2: We should encode the page id of the current page into the
	// URL for truly stateless pages so we can adjust the stack correctly

	/**
	 * Returns a stack of PageMap.Access entries pushed in the order that the
	 * pages and versions were accessed.
	 * 
	 * @return Stack containing ids of entries in access order.
	 */
	public final ArrayListStack<Access> getAccessStack()
	{
		return accessStack;
	}

	/**
	 * @return Number of page versions stored in this page map
	 */
	public final int getVersions()
	{
		return accessStack.size();
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
	 * @param entry
	 *            The entry to remove
	 */
	@Override
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
		Session session = getSession();
		synchronized (session)
		{
			session.removeAttribute(attributeForId(entry.getNumericId()));

			// Remove page from acccess stack
			final Iterator<Access> stack = accessStack.iterator();
			while (stack.hasNext())
			{
				final Access access = stack.next();
				if (access.id == entry.getNumericId())
				{
					stack.remove();
				}
			}

			// Let the session know we changed the pagemap
			dirty();
		}
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
	@Override
	public final Page get(final int id, int versionNumber)
	{
		final IPageMapEntry entry = (IPageMapEntry)getSession().getAttribute(attributeForId(id));
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
			// pushAccess(entry);
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
	 * @param page
	 *            The page to put into this map
	 */
	@Override
	public final void put(final Page page)
	{
		// Page only goes into session if it is stateless
		if (!page.isPageStateless())
		{
			Session session = getSession();
			// Get page map entry from page
			final IPageMapEntry entry = page.getPageMapEntry();

			// Entry has been accessed
			pushAccess(entry);

			// Store entry in session
			final String attribute = attributeForId(entry.getNumericId());

			if (session.getAttribute(attribute) == null)
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
		for (int i = accessStack.size() - 1; i >= 0; i--)
		{
			final Access access = accessStack.get(i);

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
							topPage.getVersion(topAccess.getVersion() - 1);
						}
						else
						{
							// Remove whole page
							remove(topPage);
						}
					}
					else if(top != null)
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
	 * @return Access entry on top of the access stack
	 */
	private final Access peekAccess()
	{
		return accessStack.peek();
	}

	/**
	 * Removes access entry on top of stack
	 * 
	 * @return Access entry on top of the access stack
	 */
	private final Access popAccess()
	{
		dirty();
		return accessStack.pop();
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
		if (accessStack.size() > 0)
		{
			if (peekAccess().equals(access))
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
		dirty();
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
