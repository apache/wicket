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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import wicket.session.pagemap.IPageMapEntry;
import wicket.util.lang.Objects;

/**
 * @author jcompagner
 */
public abstract class PageMap implements Serializable, IPageMap
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
	public static IPageMap forName(final String pageMapName)
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
	 * @see wicket.IPageMap#getEntry(int)
	 */
	public final IPageMapEntry getEntry(final int id)
	{
		return (IPageMapEntry)session.getAttribute(attributeForId(id));
	}

	/**
	 * @see wicket.IPageMap#getName()
	 */
	public final String getName()
	{
		return name;
	}

	/**
	 * @see wicket.IPageMap#getSession()
	 */
	public final Session getSession()
	{
		return session;
	}

	/**
	 * @see wicket.IPageMap#isDefault()
	 */
	public final boolean isDefault()
	{
		return name == PageMap.DEFAULT_NAME;
	}

	/**
	 * @see wicket.IPageMap#nextId()
	 */
	public final int nextId()
	{
		dirty();
		return this.pageId++;
	}

	protected final void dirty()
	{
		session.dirtyPageMap(this);
	}

	/**
	 * @see wicket.IPageMap#attributeForId(int)
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
	public final boolean continueToOriginalDestination()
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
	public final void redirectToInterceptPage(final Page page)
	{
		Session.get().bind();
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
	@SuppressWarnings("unchecked")
	public final void redirectToInterceptPage(final Class pageClazz)
	{
		Session.get().bind();
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
	 * @see wicket.IPageMap#clear()
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
	 * @see wicket.IPageMap#setSession(wicket.Session)
	 */
	public final void setSession(final Session session)
	{
		this.session = session;
	}

	/**
	 * @param visitor
	 *            The visitor to call at each Page in this PageMap.
	 */
	protected final void visitEntries(final IVisitor visitor)
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
	 * @see wicket.IPageMap#remove()
	 */
	public final void remove()
	{
		// First clear all pages from the session for this pagemap
		clear();

		// Then remove the pagemap itself
		session.removePageMap(this);
	}

	/**
	 * @see wicket.IPageMap#remove(wicket.Page)
	 */
	public final void remove(final Page page)
	{
		// Remove the pagemap entry from session
		removeEntry(page.getPageMapEntry());
	}

	/**
	 * @see wicket.IPageMap#removeEntry(wicket.session.pagemap.IPageMapEntry)
	 */
	public abstract void removeEntry(final IPageMapEntry entry);

	/**
	 * @see wicket.IPageMap#put(wicket.Page)
	 */
	public abstract void put(final Page page);


	/**
	 * @see wicket.IPageMap#get(int, int)
	 */
	public abstract Page get(final int id, int versionNumber);

	/**
	 * @see wicket.IPageMap#getSizeInBytes()
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
