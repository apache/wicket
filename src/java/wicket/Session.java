/*
 * $Id$ $Revision:
 * 1.25 $ $Date$
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
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import wicket.util.collections.MostRecentlyUsedMap;
import wicket.util.convert.IConverter;
import wicket.util.string.Strings;

/**
 * Holds information about a user session, including some fixed number of most
 * recent pages (and all their nested component information).
 * <p>
 * The Session for a RequestCycle can be retrieved by calling
 * RequestCycle.getSession(). If a RequestCycle object is not available, the
 * Session can be retrieved for a Component by calling Component.getSession().
 * If neither is available, the currently active Session for the calling thread
 * can be retrieved by calling the static method Session.get(). This last form
 * should only be used if the first two forms cannot be used.
 * <p>
 * As currently implemented, each Component does not itself have a reference to
 * the session that contains it. However, the Page component at the root of the
 * containment hierarchy does have a reference to the Session that holds the
 * page. So Component.getSession() traverses the component hierarchy to the root
 * Page and then calls Page.getSession().
 * <p>
 * A session has a locale property to support localization. The locale for a
 * session can be set by calling setLocale(). The locale determines how
 * localized resources are found and loaded. Besides having an appearance based
 * on locale, resources can also have different looks in the same locale (a.k.a.
 * "skins"). The style for a session determines the look which is used within
 * the appopriate locale. The session style ("skin") can be set with the
 * setStyle() method.
 * <p>
 * Searching for resources occurs in the following order (where sourcePath is
 * set via the ApplicationSettings object for the current Application, and style
 * and locale are Session properties):
 * <p>
 * <ul>
 * 1. [sourcePath]/name[style][locale].[extension] <br>
 * 2. [sourcePath]/name[locale].[extension] <br>
 * 3. [sourcePath]/name[style].[extension] <br>
 * 4. [sourcePath]/name.[extension] <br>
 * 5. [classPath]/name[style][locale].[extension] <br>
 * 6. [classPath]/name[locale].[extension] <br>
 * 7. [classPath]/name[style].[extension] <br>
 * 8. [classPath]/name.[extension] <br>
 * </ul>
 * <p>
 * Arbitrary objects can be attached to a Session via setProperty() and
 * retrieved again via getProperty(). Session properties no longer in use can be
 * removed via removeProperty().
 * <p>
 * Sessions have a class resolver and page factory property which implement
 * IClassResolver and IPageFactory in order to find classes and instantiate
 * pages.
 * <p>
 * Pages can be removed from the Session forcibly by calling remove(Page) or
 * removeAll(), although such an action should rarely be necessary.
 * <p>
 * Although public, the removeNewerThan(Page) and getFreshestPage() methods are
 * intended for internal use only and may not be supported in the future.
 * Framework clients should not call these methods.
 * 
 * @author Jonathan Locke
 */
public abstract class Session implements Serializable
{
	/** Separator for component paths. */
	private static final char componentPathSeparator = '.';

	/** Thread-local current session. */
	private static final ThreadLocal current = new ThreadLocal();

	/** Session state that can be replicated when dirty */
	transient State state = new State();

	/** Application that this is a session of. */
	private transient Application application;

	/** Resolver for finding classes for this Session */
	private transient IClassResolver classResolver;

	/** The converter instance. */
	private transient IConverter converter;

	/** Active request cycle */
	private transient RequestCycle cycle;

	/** Factory for constructing Pages for this Session */
	private transient IPageFactory pageFactory;

	/** The still-live pages for this user session. */
	private transient MostRecentlyUsedMap pages;

	/**
	 * Interface called when visiting session pages.
	 * 
	 * @author Jonathan Locke
	 */
	static interface IPageVisitor
	{
		/**
		 * Visit method.
		 * 
		 * @param page
		 *            the page
		 */
		public void page(final Page page);
	}

	/**
	 * Clusterable session state.
	 * 
	 * @author Jonathan Locke
	 */
	static class State implements Serializable
	{
		/** Next available access number */
		int accessNumber;

		/** True if any of this state has changed */
		private boolean dirty = false;

		/** URL to continue to after a given page. */
		private String interceptContinuationURL;

		/** The locale to use when loading resources for this session. */
		private Locale locale = Locale.getDefault();

		/** Next available page identifier. */
		private int pageId = 0;

		/** Any special "skin" style to use when loading resources. */
		private String style;
	}

	/**
	 * Get the session for the calling thread.
	 * 
	 * @return Session for calling thread
	 */
	public static Session get()
	{
		return (Session)current.get();
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL IT.
	 * <p>
	 * Sets session for calling thread.
	 * 
	 * @param session
	 *            The session
	 */
	public static void set(final Session session)
	{
		current.set(session);
	}

	/**
	 * Constructor.
	 * 
	 * @param application
	 *            The application that this is a session of
	 */
	protected Session(final Application application)
	{
		this.application = application;
	}

	/**
	 * Get the application that is currently working with this session.
	 * 
	 * @return Returns the application.
	 */
	public final Application getApplication()
	{
		return application;
	}

	/**
	 * @return The class resolver for this Session
	 */
	public final IClassResolver getClassResolver()
	{
		return classResolver != null ? classResolver : application.getSettings()
				.getDefaultClassResolver();
	}

	/**
	 * Gets the converter instance.
	 * 
	 * @return the converter
	 */
	public final IConverter getConverter()
	{
		if (converter == null)
		{
			// Let the factory create a new converter
			converter = getApplication().getConverterFactory().newConverter(state.locale);
		}
		return converter;
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL IT.
	 * 
	 * Get the freshest page in the session.
	 * 
	 * @return The freshest page in the session
	 */
	public final Page getFreshestPage()
	{
		// No fresh page found at first
		Page freshest = null;

		// Loop through session pages
		for (final Iterator iterator = getPageMap().values().iterator(); iterator.hasNext();)
		{
			// Get next page
			final Page current = (Page)iterator.next();

			// If the page isn't stale
			if (!current.isStale())
			{
				// and we don't yet have a freshest page OR the current page is
				// fresher
				if ((freshest == null) || (current.getId() < freshest.getId()))
				{
					// then we found a fresher page
					freshest = current;
				}
			}
		}

		return freshest;
	}

	/**
	 * Get this session's locale.
	 * 
	 * @return This session's locale
	 */
	public final Locale getLocale()
	{
		return this.state.locale;
	}

	/**
	 * Get the page for the given path.
	 * 
	 * @param path
	 *            Component path
	 * @return The page based on the first path component (the page id)
	 */
	public final Page getPage(final String path)
	{
		// Retrieve the page for the first path component from this session
		return getPage(Integer.parseInt(Strings.firstPathComponent(path, componentPathSeparator)));
	}

	/**
	 * @return The page factory for this session
	 */
	public final IPageFactory getPageFactory()
	{
		return pageFactory != null ? pageFactory : application.getSettings()
				.getDefaultPageFactory();
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL IT.
	 * 
	 * @return The currently active request cycle for this session
	 */
	public final RequestCycle getRequestCycle()
	{
		return cycle;
	}

	/**
	 * Get the style.
	 * 
	 * @return Returns the style.
	 */
	public final String getStyle()
	{
		return state.style;
	}

	/**
	 * Invalidates this session
	 */
	public abstract void invalidate();

	/**
	 * Called when an PageState record should be added to the replicated state
	 * 
	 * @param page
	 */
	public final void pageChanged(final Page page)
	{
		// Create PageState for page
		final PageState pageState = newPageState(page);

		// The page must have been added to the session already
		pageState.addedToSession = true;

		// Set HttpSession attribute for new PageState
		setAttribute(page.getName(), pageState);
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL IT.
	 * <p>
	 * Sets the application that this session is associated with.
	 * 
	 * @param application
	 *            The application
	 */
	public final void setApplication(final Application application)
	{
		this.application = application;
	}

	/**
	 * Set class resolver for this session
	 * 
	 * @param classResolver
	 *            The class resolver
	 */
	public final void setClassResolver(final IClassResolver classResolver)
	{
		this.classResolver = classResolver;
	}

	/**
	 * Set the locale.
	 * 
	 * @param locale
	 *            New locale
	 */
	public final void setLocale(final Locale locale)
	{
		this.state.locale = locale;
		this.state.dirty = true;
		this.converter = null;
	}

	/**
	 * Set page factory for this session
	 * 
	 * @param pageFactory
	 *            The page factory
	 */
	public final void setPageFactory(final IPageFactory pageFactory)
	{
		this.pageFactory = pageFactory;
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL IT.
	 * 
	 * Sets the currently active request cycle for this session.
	 * 
	 * @param cycle
	 *            The request cycle
	 */
	public final void setRequestCycle(final RequestCycle cycle)
	{
		this.cycle = cycle;
	}

	/**
	 * Set the style.
	 * 
	 * @param style
	 *            The style to set.
	 */
	public final void setStyle(final String style)
	{
		this.state.style = style;
		this.state.dirty = true;
	}

	/**
	 * Replicates any changed data to the cluser.
	 */
	public final void updateCluster()
	{
		if (this.state.dirty)
		{
			setAttribute("state", state);
		}
	}

	/**
	 * Updates this session using changed state information that may have been
	 * replicated to this node on a cluster.
	 */
	public final void updateSession()
	{
		// Get any replicated state from the session
		final State state = (State)getAttribute("state");
		if (state != null)
		{
			// Copy state into Session
			this.state = state;
		}

		// PageStates to add
		final List pageStates = new ArrayList();

		// Copy any changed pages into our (transient) Session
		for (final Iterator iterator = getAttributeNames().iterator(); iterator.hasNext();)
		{
			// Get attribute value
			final Object value = getAttribute((String)iterator.next());
			if (value instanceof PageState)
			{
				pageStates.add(value);
			}
		}

		// Sort in ascending order by access number so that pages which have
		// a higher access number (which means they were accessed more recently)
		// are added /last/.
		Collections.sort(pageStates, new Comparator()
		{
			public int compare(Object object1, Object object2)
			{
				int accessNumber1 = ((PageState)object1).accessNumber;
				int accessNumber2 = ((PageState)object2).accessNumber;
				if (accessNumber1 < accessNumber2)
				{
					return -1;
				}
				if (accessNumber1 > accessNumber2)
				{
					return 1;
				}
				return 0;
			}
		});

		// Add page states as need be
		for (final Iterator iterator = pageStates.iterator(); iterator.hasNext();)
		{
			// Get next attribute name
			final PageState pageState = (PageState)iterator.next();

			// If PageState has not been added to the session
			if (!pageState.addedToSession)
			{
				// Get page from page state
				final Page page = pageState.getPage();

				// Add to page map
				getPageMap().put(new Integer(page.getId()), page);

				// Page has been added to session now
				pageState.addedToSession = true;
			}
		}
	}

	/**
	 * Adds page to session if not already added.
	 * 
	 * @param page
	 *            Page to add to this session
	 */
	protected final void addPage(final Page page)
	{
		// Set session and identifier
		page.setId(this.state.pageId++);
		state.dirty = true;

		// Add to page local transient page map
		final MostRecentlyUsedMap pageMap = getPageMap();
		pageMap.put(new Integer(page.getId()), page);

		// Get any page that was removed
		final Page removedPage = (Page)pageMap.getRemovedValue();
		if (removedPage != null)
		{
			removeAttribute(removedPage.getName());
		}

		// Add PageState entry to list of changed pages
		pageChanged(page);
	}

	/**
	 * @param name
	 *            The name of the attribute to store
	 * @return The value of the attribute
	 */
	protected abstract Object getAttribute(final String name);

	/**
	 * @return List of attributes for this session
	 */
	protected abstract List getAttributeNames();

	/**
	 * Gets a PageState record for a given Page. The default, inefficient
	 * implementation is to simply wrap the entire Page object. More intimate
	 * knowledge of a Page, however, may allow significant compression of state
	 * information.
	 * 
	 * @param page
	 *            The page
	 * @return The page state record
	 */
	protected PageState newPageState(final Page page)
	{
		return page.newPageState();
	}

	/**
	 * @param name
	 *            The name of the attribute to remove
	 */
	protected abstract void removeAttribute(final String name);

	/**
	 * @param name
	 *            The name of the attribute to store
	 * @param object
	 *            The attribute value
	 */
	protected abstract void setAttribute(final String name, final Object object);

	/**
	 * Get the interceptContinuationURL.
	 * 
	 * @return Returns the interceptContinuationURL.
	 */
	final String getInterceptContinuationURL()
	{
		return state.interceptContinuationURL;
	}

	/**
	 * Get the page with the given id.
	 * 
	 * @param id
	 *            Page id
	 * @return Page with the given id
	 */
	final Page getPage(final int id)
	{
		return (Page)getPageMap().get(new Integer(id));
	}

	/**
	 * Set the interceptContinuationURL.
	 * 
	 * @param interceptContinuationURL
	 *            The interceptContinuationURL to set.
	 */
	final void setInterceptContinuationURL(final String interceptContinuationURL)
	{
		this.state.interceptContinuationURL = interceptContinuationURL;
		this.state.dirty = true;
	}

	/**
	 * Visits the pages in this session.
	 * 
	 * @param visitor
	 *            The visitor to call
	 */
	final void visitPages(final IPageVisitor visitor)
	{
		// Loop through pages in page map
		for (final Iterator iterator = getPageMap().values().iterator(); iterator.hasNext();)
		{
			// Visit next page
			visitor.page((Page)iterator.next());
		}
	}

	/**
	 * @return Most recently used Page map
	 */
	private MostRecentlyUsedMap getPageMap()
	{
		if (this.pages == null)
		{
			this.pages = MostRecentlyUsedMap.newInstance(application.getSettings()
					.getMaxSessionPages());
		}
		return this.pages;
	}
}
