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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
	/** Name of session attribute under which this session is stored */
	public static final String sessionAttributeName = "session";

	/** Separator for component paths. */
	private static final char componentPathSeparator = '.';

	/** Thread-local current session. */
	private static final ThreadLocal current = new ThreadLocal();

	/** Logging object */
	private static final Log log = LogFactory.getLog(Session.class);

	/** Next available page state sequence number */
	int pageStateSequenceNumber;

	/** Application that this is a session of. */
	private transient Application application;

	/** The converter instance. */
	private transient IConverter converter;

	/** Active request cycle */
	private transient RequestCycle cycle;

	/** True if session state has been changed */
	private boolean dirty = false;

	/** The locale to use when loading resources for this session. */
	private Locale locale;

	/** Factory for constructing Pages for this Session */
	private transient IPageFactory pageFactory;

	/** Maps from name to page map */
	private final Map pageMapForName = new HashMap();

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
		// Save application
		this.application = application;

		// Set locale to default locale
		setLocale(application.getSettings().getDefaultLocale());

		// Create default page map
		newPageMap(PageMap.defaultName);
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
		return application.getSettings().getDefaultClassResolver();
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
	 * Get this session's locale.
	 * 
	 * @return This session's locale
	 */
	public final Locale getLocale()
	{
		return locale;
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL IT.
	 * 
	 * Get the page for the given path.
	 * 
	 * @param pageMapName
	 *            The name of the page map where the page is
	 * @param path
	 *            Component path
	 * @param versionNumber
	 *            The version of the page required
	 * @return The page based on the first path component (the page id), or null
	 *         if the requested version of the page cannot be found.
	 */
	public final Page getPage(final String pageMapName, final String path, final int versionNumber)
	{
		if (log.isDebugEnabled())
		{
			log.debug("Getting page [path = " + path + ", versionNumber = " + versionNumber + "]");
		}

		// Retrieve the page for the first path component from this session
		Page page = getPage(pageMapName, Strings.firstPathComponent(path, componentPathSeparator));

		// Is there a page with the right id at all?
		if (page != null)
		{
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
					pageChanged(page);
				}
				return page;
			}
		}
		return null;
	}

	/**
	 * @return The page factory for this session
	 */
	public final IPageFactory getPageFactory()
	{
		if (pageFactory == null)
		{
			pageFactory = application.getSettings().getDefaultPageFactory();
		}
		return pageFactory;
	}

	/**
	 * @param page
	 *            The page, or null if no page context is available
	 * @return The page factory for the page, or the default page factory if
	 *         page was null
	 */
	public IPageFactory getPageFactory(final Page page)
	{
		if (page != null)
		{
			return page.getPageFactory();
		}
		return getPageFactory();
	}

	/**
	 * @param pageMapName
	 *            Name of page map, or null for default page map
	 * @return PageMap for name
	 */
	public final PageMap getPageMap(String pageMapName)
	{
		if (pageMapName == null)
		{
			pageMapName = PageMap.defaultName;
		}
		return (PageMap)pageMapForName.get(pageMapName);
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
		return style;
	}

	/**
	 * Invalidates this session
	 */
	public abstract void invalidate();

	/**
	 * Creates a new page map with a given name
	 * 
	 * @param name
	 *            The name for the new page map
	 * @return The newly created page map
	 */
	public final PageMap newPageMap(final String name)
	{
		final PageMap pageMap = new PageMap(name, this);
		pageMapForName.put(name, pageMap);
		return pageMap;
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL IT.
	 * 
	 * Creates a new RequestCycle for the given request and response using the
	 * session's request cycle factory.
	 * 
	 * @param request
	 *            The request
	 * @param response
	 *            The response
	 * @return The new request cycle.
	 */
	public final RequestCycle newRequestCycle(final Request request, final Response response)
	{
		return getRequestCycleFactory().newRequestCycle(this, request, response);
	}

	/**
	 * Called when an PageState record should be added to the replicated state
	 * 
	 * @param page
	 */
	public final void pageChanged(final Page page)
	{
		// Create PageState for page
		final PageState pageState = newPageState(page);
		pageState.addedToSession = true;
		pageState.pageMapName = page.getPageMap().getName();

		// Set HttpSession attribute for new PageState
		setAttribute(page.getId(), pageState);
	}

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
		page.getPageMap().remove(page);
	}

	/**
	 * Removes all pages from the session. Although this method should rarely be
	 * needed, it is available (possibly for security reasons).
	 */
	public final void removeAll()
	{
		// Go through each page map in the session
		for (final Iterator iterator = pageMapForName.values().iterator(); iterator.hasNext();)
		{
			// Remove all pages from the current page map
			((PageMap)iterator.next()).removeAll();
		}
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
	 * Set the locale for this session.
	 * 
	 * @param locale
	 *            New locale
	 */
	public final void setLocale(final Locale locale)
	{
		this.locale = locale;
		this.converter = null;
		dirty();
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
		this.style = style;
		dirty();
	}

	/**
	 * Replicates this session to the cluster if it has changed.
	 */
	public void updateCluster()
	{
		// If state is dirty
		if (dirty)
		{
			log.debug("updateCluster(): Session is dirty.  Replicating.");

			// State is no longer dirty
			this.dirty = false;

			// Set attribute.
			setAttribute(sessionAttributeName, this);
		}
		else
		{
			log.debug("updateCluster(): Session not dirty.");
		}
	}

	/**
	 * Updates this session using changed state information that may have been
	 * replicated to this node on a cluster.
	 */
	public final void updateSession()
	{
		// Go through each page map in the session
		log.debug("updateSession(): Updating session.");
		for (final Iterator iterator = pageMapForName.values().iterator(); iterator.hasNext();)
		{
			// Remove all pages from the current page map
			final PageMap pageMap = ((PageMap)iterator.next());
			pageMap.setSession(this);

			log.debug("updateSession(): Attaching session to PageMap " + pageMap);
		}

		// Get PageStates from session attributes
		log.debug("updateSession(): Getting PageState attributes.");
		final List pageStates = getPageStateAttributes();

		// Sort page states so that they can be added in reverse order of
		// creation. This ensures that any newer pages will bump out older ones.
		sortBySequenceNumber(pageStates);

		// Adds pages to session
		addPages(pageStates);
		log.debug("updateSession(): Done updating session.");
	}

	/**
	 * Adds page to session if not already added.
	 * 
	 * @param page
	 *            Page to add to this session
	 */
	protected final void add(final Page page)
	{
		// Set page map for page. If cycle is null, we may be being called from
		// some kind of test harness, so we will just use the default page map
		final String pageMapName = cycle == null ? PageMap.defaultName : cycle.getRequest()
				.getParameter("pagemap");
		page.setPageMap(pageMapName);

		// Add to page local transient page map
		final Page removedPage = page.getPageMap().add(page);

		// Get any page that was removed
		if (removedPage != null)
		{
			removeAttribute(removedPage.getId());
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
	 * @return Request cycle factory for this kind of session.
	 */
	protected abstract IRequestCycleFactory getRequestCycleFactory();

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
	 * Marks session state as dirty
	 */
	final void dirty()
	{
		this.dirty = true;
	}

	/**
	 * Get the page with the given id.
	 * 
	 * @param pageMapName
	 *            Page map name
	 * @param id
	 *            Page id
	 * @return Page with the given id
	 */
	final Page getPage(final String pageMapName, final String id)
	{
		return (Page)getPageMap(pageMapName).get(id);
	}

	/**
	 * @return The next access number
	 */
	final int nextPageStateSequenceNumber()
	{
		dirty();
		return this.pageStateSequenceNumber++;
	}

	/**
	 * @param pageStates
	 */
	private final void addPages(final List pageStates)
	{
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
				log.debug("addPages(): Adding replicated page state " + pageState
						+ ", which produced page " + page);

				// Add to page map specified in page state info
				getPageMap(pageState.pageMapName).put(page);

				// Page has been added to session now
				pageState.addedToSession = true;
			}
		}
	}

	/**
	 * @return List of PageState values set as session attributes
	 */
	private final List getPageStateAttributes()
	{
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
		return pageStates;
	}

	/**
	 * @param pageStates
	 */
	private final void sortBySequenceNumber(final List pageStates)
	{
		// Sort in ascending order by access number so that pages which have
		// a higher access number (which means they were accessed more recently)
		// are added /last/.
		Collections.sort(pageStates, new Comparator()
		{
			public int compare(Object object1, Object object2)
			{
				int sequenceNumber1 = ((PageState)object1).sequenceNumber;
				int sequenceNumber2 = ((PageState)object2).sequenceNumber;
				if (sequenceNumber1 < sequenceNumber2)
				{
					return -1;
				}
				if (sequenceNumber1 > sequenceNumber2)
				{
					return 1;
				}
				return 0;
			}
		});
	}
}
