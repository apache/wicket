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
package org.apache.wicket;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.WicketURLEncoder;
import org.apache.wicket.request.target.basic.RedirectRequestTarget;
import org.apache.wicket.session.pagemap.IPageMapEntry;
import org.apache.wicket.util.file.Files;
import org.apache.wicket.util.lang.Objects;
import org.apache.wicket.util.string.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * FIXME javadoc
 * 
 * @author Jonathan Locke
 * @author jcompagner
 */
public abstract class PageMap implements IClusterable, IPageMap
{
	/** Log. */
	private static final Logger log = LoggerFactory.getLogger(PageMap.class);

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

	/** Name of default pagemap */
	public static final String DEFAULT_NAME = null;

	/** */
	private static final long serialVersionUID = 1L;

	/**
	 * MetaDataEntry array.
	 */
	private MetaDataEntry<?>[] metaData;

	/**
	 * Gets a page map for a page map name, automatically creating the page map if it does not
	 * exist. If you do not want the pagemap to be automatically created, you can call
	 * Session.pageMapForName(pageMapName, false).
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

	/** URL to continue to after a given page. */
	private String interceptContinuationURL;

	/** Name of this page map */
	private final String name;

	/** Next available page identifier in this page map. */
	private int pageId = 0;

	/**
	 * Constructor
	 * 
	 * @param nameForNewPagemap
	 *            The name of this page map
	 */
	public PageMap(String nameForNewPagemap)
	{
		if (nameForNewPagemap == null)
		{
			name = null;
		}
		else if (Strings.isEmpty(nameForNewPagemap))
		{
			throw new IllegalStateException("Empty string name for pagemaps is not allowed");
		}
		else
		{
			name = Files.cleanupFilename(nameForNewPagemap);
		}
	}

	/**
	 * @see org.apache.wicket.IPageMap#attributeForId(int)
	 */
	public final String attributeForId(final int id)
	{
		return attributePrefix() + id;
	}

	/**
	 * @see org.apache.wicket.IPageMap#clear()
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
	 * Redirects to any intercept page previously specified by a call to redirectToInterceptPage.
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
			cycle.setRequestTarget(new RedirectRequestTarget(interceptContinuationURL));

			// Reset interception URL
			interceptContinuationURL = null;

			// Force session to replicate page maps
			dirty();
			return true;
		}
		return false;
	}

	/**
	 * @see org.apache.wicket.IPageMap#get(int, int)
	 */
	public abstract Page get(final int id, int versionNumber);

	/**
	 * @see org.apache.wicket.IPageMap#getEntry(int)
	 */
	public final IPageMapEntry getEntry(final int id)
	{
		return (IPageMapEntry)Session.get().getAttribute(attributeForId(id));
	}

	/**
	 * @see org.apache.wicket.IPageMap#getName()
	 */
	public final String getName()
	{
		return name;
	}

	/**
	 * @return Session this page map is in
	 */
	public final Session getSession()
	{
		return Session.get();
	}

	/**
	 * @see org.apache.wicket.IPageMap#getSizeInBytes()
	 */
	public final long getSizeInBytes()
	{
		long size = Objects.sizeof(this);
		Iterator<IPageMapEntry> it = getEntries().iterator();
		while (it.hasNext())
		{
			IPageMapEntry entry = it.next();
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
	 * @see org.apache.wicket.IPageMap#isDefault()
	 */
	public final boolean isDefault()
	{
		return name == PageMap.DEFAULT_NAME;
	}

	/**
	 * @see org.apache.wicket.IPageMap#nextId()
	 */
	public final int nextId()
	{
		dirty();
		return pageId++;
	}

	/**
	 * @see org.apache.wicket.IPageMap#put(org.apache.wicket.Page)
	 */
	public abstract void put(final Page page);

	/**
	 * Redirects browser to an intermediate page such as a sign-in page. The current request's URL
	 * is saved exactly as it was requested for future use by continueToOriginalDestination(); Only
	 * use this method when you plan to continue to the current URL at some later time; otherwise
	 * just use setResponsePage or, when you are in a constructor, redirectTo.
	 * 
	 * @param pageClazz
	 *            The page clazz to temporarily redirect to
	 */
	public final <T extends Page> void redirectToInterceptPage(final Class<T> pageClazz)
	{
		final RequestCycle cycle = RequestCycle.get();
		setUpRedirect(cycle);
		cycle.setResponsePage(pageClazz);
	}

	/**
	 * Redirects browser to an intermediate page such as a sign-in page. The current request's URL
	 * is saved exactly as it was requested for future use by continueToOriginalDestination(); Only
	 * use this method when you plan to continue to the current URL at some later time; otherwise
	 * just use setResponsePage or, when you are in a constructor, redirectTo.
	 * 
	 * @param page
	 *            The page to temporarily redirect to
	 */
	public final void redirectToInterceptPage(final Page page)
	{
		final RequestCycle cycle = RequestCycle.get();
		setUpRedirect(cycle);
		cycle.setResponsePage(page);
	}

	private void setUpRedirect(final RequestCycle cycle)
	{
		Session session = Session.get();
		if (session.isTemporary())
		{
			session.bind();
		}

		// The intercept continuation URL should be saved exactly as the
		// original request specified.
		// Only if it is an ajax request just redirect to the page where the request is from.
		if (cycle.getRequest() instanceof WebRequest && ((WebRequest)cycle.getRequest()).isAjax())
		{
			interceptContinuationURL = cycle.urlFor(cycle.getRequest().getPage()).toString();
		}
		else
		{
			// wicket-2061: getURL() returns a properly <b>decoded</b> URL. But we need is a
			// properly <b>encoded</b> URL.
			interceptContinuationURL = "/" + cycle.getRequest().getURL();
			interceptContinuationURL = WicketURLEncoder.FULL_PATH_INSTANCE.encode(interceptContinuationURL);
		}

		// Page map is dirty
		dirty();

		// Redirect to the page
		cycle.setRedirect(true);
	}

	/**
	 * @see org.apache.wicket.IPageMap#remove()
	 */
	public final void remove()
	{
		// First remove the pagemap itself
		Session.get().removePageMap(this);

		// Then clear all pages from the session for this pagemap
		clear();
	}

	/**
	 * @see org.apache.wicket.IPageMap#remove(org.apache.wicket.Page)
	 */
	public void remove(final Page page)
	{
		// Remove the pagemap entry from session
		removeEntry(page.getPageMapEntry());

		// Make sure it doesn't get added again at the end of the request cycle
		Session.get().untouch(page);
	}

	/**
	 * @see org.apache.wicket.IPageMap#removeEntry(org.apache.wicket.session.pagemap.IPageMapEntry)
	 */
	public abstract void removeEntry(final IPageMapEntry entry);

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "[PageMap name=" + name + "]";
	}

	/**
	 * @return List of entries in this page map
	 */
	private final List<IPageMapEntry> getEntries()
	{
		final Session session = Session.get();
		final List<String> attributes = session.getAttributeNames();
		final List<IPageMapEntry> list = new ArrayList<IPageMapEntry>();
		for (final Iterator<String> iterator = attributes.iterator(); iterator.hasNext();)
		{
			final String attribute = iterator.next();
			if (attribute.startsWith(attributePrefix()))
			{
				list.add((IPageMapEntry)session.getAttribute(attribute));
			}
		}
		return list;
	}

	/**
	 * Marking this PageMap as the most recently used only if it isn't already removed from session.
	 */
	protected final void dirty()
	{
		if (Session.get().getPageMaps().contains(this))
		{
			Session.get().dirtyPageMap(this);
		}
	}

	/**
	 * @param visitor
	 *            The visitor to call at each Page in this PageMap.
	 */
	protected final void visitEntries(final IVisitor visitor)
	{
		final Session session = Session.get();
		final List<String> attributes = session.getAttributeNames();
		for (final Iterator<String> iterator = attributes.iterator(); iterator.hasNext();)
		{
			final String attribute = iterator.next();
			if (attribute.startsWith(attributePrefix()))
			{
				visitor.entry((IPageMapEntry)session.getAttribute(attribute));
			}
		}
	}

	/**
	 * Sets the metadata for this PageMap using the given key. If the metadata object is not of the
	 * correct type for the metadata key, an IllegalArgumentException will be thrown. For
	 * information on creating MetaDataKeys, see {@link MetaDataKey}.
	 * 
	 * @param key
	 *            The singleton key for the metadata
	 * @param object
	 *            The metadata object
	 * @throws IllegalArgumentException
	 * @see MetaDataKey
	 */
	public final void setMetaData(final MetaDataKey<?> key, final Serializable object)
	{
		metaData = key.set(metaData, object);
	}

	/**
	 * Gets metadata for this PageMap using the given key.
	 * 
	 * @param key
	 *            The key for the data
	 * @return The metadata or null of no metadata was found for the given key
	 * @see MetaDataKey
	 */
	public final Serializable getMetaData(final MetaDataKey<?> key)
	{
		return (Serializable)key.get(metaData);
	}

	/**
	 * @return The attribute prefix for this page map
	 */
	final String attributePrefix()
	{
		return Session.pageMapEntryAttributePrefix + name + ":";
	}
}
