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
import java.util.Stack;

import wicket.request.IRequestCodingStrategy;
import wicket.request.IRequestCycleProcessor;
import wicket.session.pagemap.IPageMapEntry;
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

	/** Stack of entry accesses by id */
	private final Stack/* <Access> */accessStack = new Stack();

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
	public PageMap(final String name, final Session session)
	{
		this.name = name;
		this.session = session;
	}

	/**
	 * Pops the top entry off the entry access stack and returns it. This is
	 * guaranteed to be the most recently accessed page map entry IF AND ONLY IF
	 * the user just came from a stateful page. If the user could get to the
	 * current page from a stateless page, this method may not work if the user
	 * uses the back button. See detailed explanation for getAccessStack() to
	 * understand this limitation!
	 * 
	 * @see PageMap#getAccessStack()
	 * 
	 * @return Previous pagemap entry in terms of access
	 */
	public final IPageMapEntry back()
	{
		return getEntry(popAccess().getId());
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
				remove(entry);
			}
		});

		// Clear access stack
		accessStack.clear();
	}

	/**
	 * Returns a stack of PageMap.Access entries pushed in the order that the
	 * pages and versions were accessed.
	 * <p>
	 * IMPORTANT NOTE: This stack will be in sync with the browser stack EXCEPT
	 * in certain circumstances where stateless pages and the back button are
	 * involved. The problem is this: if stateless pages are rendered to the
	 * browser, they will not be on the access stack because they are not in the
	 * PageMap at all. So, if the user goes back to a stateless page and
	 * navigates forward to a stateful page, the stack will not be correctly
	 * adjusted (unlike with stateful pages, where it will always be adjusted
	 * correctly). Instead, the new stateful page will be on top of the access
	 * stack and any unreachable page versions that the user may have backed up
	 * over will still be in the session and on the access stack instead of
	 * being eliminated. This is not a major problem, however as they will
	 * expire in the normal way (although they will take up pagemap space until
	 * they do). It's important to realize that this is a problem with stateless
	 * pages and not with the implementation of PageMap, which is now actually a
	 * better implementation than in previous versions because it at least CAN
	 * remove unused information from the map when the back button is used on
	 * stateful pages.
	 * 
	 * @return Stack containing ids of entries in access order.
	 */
	public final Stack getAccessStack()
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
		return (IPageMapEntry)session.getAttribute(attributeForId(id));
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
	 * @return Size of this page map in bytes, including a sum of the sizes of
	 *         all the pages it contains.
	 */
	public final int getSizeInBytes()
	{
		int size = Objects.sizeof(this);
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
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL IT.
	 * 
	 * @param entry
	 *            The entry to remove
	 * @return The removed entry
	 */
	public final IPageMapEntry remove(final IPageMapEntry entry)
	{
		// Remove entry from session
		session.removeAttribute(attributeForId(entry.getNumericId()));
		return entry;
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
	 * @param versionNumber
	 *            The version to get
	 * @return Any page having the given id
	 */
	final Page get(final int id, int versionNumber)
	{
		final IPageMapEntry entry = (IPageMapEntry)session.getAttribute(attributeForId(id));
		if (entry != null)
		{
			// Entry has been accessed
			access(entry, versionNumber);

			// Get page as dirty
			Page page = entry.getPage();
			page.setDirty(true);

			// Get the version of the page requested from the page
			final Page version = page.getVersion(versionNumber);

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
				throw new IllegalStateException("Unable to get version " + versionNumber
						+ " of page " + page);
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
			pushAccess(entry);

			// Store entry in session
			final String attribute = attributeForId(entry.getNumericId());

			// Set attribute
			session.setAttribute(attribute, entry);

			// Evict any page(s) as need be
			session.getApplication().getSessionSettings().getPageMapEvictionStrategy().evict(this);
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

		// TODO General: This conflicts with the use of IRequestCodingStrategy.
		// We should get rid of encodeURL in favor of IRequestCodingStrategy
		interceptContinuationURL = page.getResponse().encodeURL(cycle.getRequest().getURL());
		cycle.redirectTo(page);
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
		for (int i = 0; i < accessStack.size(); i++)
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
							// Remove versions
							topPage.getVersion(topAccess.getVersion());
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
						remove(top);
					}
				}
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
				list.add((IPageMapEntry)session.getAttribute(attribute));
			}
		}
		return list;
	}

	/**
	 * @return Access entry on top of the access stack
	 */
	private final Access popAccess()
	{
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
		accessStack.push(access);
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
		return 0;
	}
}
