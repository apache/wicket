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

import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

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

	/** Application that this is a session of. */
	private transient Application application;

	/** Resolver for finding classes for this Session */
	private transient IClassResolver classResolver;

	/** Active request cycle */
	private transient RequestCycle cycle;

	/** URL to continue to after a given page. */
	private String interceptContinuationURL;

	/** The locale to use when loading resources for this session. */
	private Locale locale = Locale.getDefault();

	/** The converter instance. */
	private transient IConverter converter;

	/** Factory for constructing Pages for this Session */
	private transient IPageFactory pageFactory;

	/** Next available page identifier. */
	private int pageId = 0;

	/** The still-live pages for this user session. */
	private final Map pages;

	/** Any special "skin" style to use when loading resources. */
	private String style;

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
	 * Get the session for the calling thread.
	 *
	 * @return Session for calling thread
	 */
	public static Session get()
	{
		return (Session)current.get();
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API.  DO NOT CALL IT.
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
		setPageFactory(application.getSettings().getDefaultPageFactory());
		setClassResolver(application.getSettings().getDefaultClassResolver());
		this.pages = MostRecentlyUsedMap
				.newInstance(application.getSettings().getMaxSessionPages());
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
		if ( classResolver == null ) return application.getSettings().getDefaultClassResolver();
		else return classResolver;
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
			converter = getApplication().getConverterFactory().newConverter(locale);
		}
		return converter;
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API.  DO NOT CALL IT.
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
		for (final Iterator iterator = pages.values().iterator(); iterator.hasNext();)
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
		return this.locale;
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
		if ( pageFactory == null ) return application.getSettings().getDefaultPageFactory();
		else return pageFactory;
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API.  DO NOT CALL IT.
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
		return style;
	}

	/**
	 * Invalidates this session
	 */
	public abstract void invalidate();

	/**
	 * Removes the given page from the cache. This method may be useful if you
	 * have special knowledge that a given page cannot be accessed again. For
	 * example, the user may have closed a popup window.
	 *
	 * @param page
	 *            The page to remove
	 */
	public final void remove(final Page page)
	{
		pages.remove(new Integer(page.getId()));
	}

	/**
	 * Removes all pages from the session. Although this method should rarely be
	 * needed, it is available (possibly for security reasons).
	 */
	public final void removeAll()
	{
		pages.clear();
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
		this.locale = locale;
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
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API.  DO NOT CALL IT.
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
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API.  DO NOT CALL IT.
	 * <p>
	 * Sets the application that this session is associated with.
	 *
	 * @param application The application
	 */
	public final void setApplication(final Application application)
	{
		this.application = application;
	}

	/**
	 * Set the style.
	 *
	 * @param style
	 *            The style to set.
	 */
	public final void setStyle(final String style)
	{
		this.style = style;
	}

	/**
	 * Adds page to session if not already added.
	 *
	 * @param page
	 *            Page to add to this session
	 */
	final void addPage(final Page page)
	{
		// Set session and identifier
		page.setId(pageId++);

		// Add to page map
		pages.put(new Integer(page.getId()), page);
	}

	/**
	 * Get the interceptContinuationURL.
	 *
	 * @return Returns the interceptContinuationURL.
	 */
	final String getInterceptContinuationURL()
	{
		return interceptContinuationURL;
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
		return (Page)pages.get(new Integer(id));
	}

	/**
	 * Set the interceptContinuationURL.
	 *
	 * @param interceptContinuationURL
	 *            The interceptContinuationURL to set.
	 */
	final void setInterceptContinuationURL(final String interceptContinuationURL)
	{
		this.interceptContinuationURL = interceptContinuationURL;
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
		for (final Iterator iterator = pages.values().iterator(); iterator.hasNext();)
		{
			// Visit next page
			visitor.page((Page)iterator.next());
		}
	}
}


