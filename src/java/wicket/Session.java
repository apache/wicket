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

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.application.IClassResolver;
import wicket.authorization.IAuthorizationStrategy;
import wicket.request.ClientInfo;
import wicket.session.ISessionStore;
import wicket.session.ISessionStoreFactory;
import wicket.session.pagemap.IPageMapEntry;
import wicket.util.convert.IConverter;
import wicket.util.lang.Bytes;
import wicket.util.lang.Objects;
import wicket.util.string.Strings;

/**
 * Holds information about a user session, including some fixed number of most
 * recent pages (and all their nested component information).
 * <ul>
 * <li><b>Access via RequestCycle </b>- The Session for a {@link RequestCycle}
 * can be retrieved by calling {@link RequestCycle#getSession()}.
 * 
 * <li><b>Access via Component </b>- If a RequestCycle object is not available,
 * the Session can be retrieved for a Component by calling
 * {@link Component#getSession()}. As currently implemented, each Component
 * does not itself have a reference to the session that contains it. However,
 * the Page component at the root of the containment hierarchy does have a
 * reference to the Session that holds the Page. So
 * {@link Component#getSession()} traverses the component hierarchy to the root
 * Page and then calls {@link Page#getSession()}.
 * 
 * <li><b>Access via Thread Local </b>- In the odd case where neither a
 * RequestCycle nor a Component is available, the currently active Session for
 * the calling thread can be retrieved by calling the static method
 * Session.get(). This last form should only be used if the first two forms
 * cannot be used since thread local access can involve a potentially more
 * expensive hash map lookup.
 * 
 * <li><b>Locale </b>- A session has a Locale property to support localization.
 * The Locale for a session can be set by calling
 * {@link Session#setLocale(Locale)}. The Locale for a Session determines how
 * localized resources are found and loaded.
 * 
 * <li><b>Style </b>- Besides having an appearance based on locale, resources
 * can also have different looks in the same locale (a.k.a. "skins"). The style
 * for a session determines the look which is used within the appopriate locale.
 * The session style ("skin") can be set with the setStyle() method.
 * 
 * <li><b>Resource Loading </b>- Based on the Session locale and style,
 * searching for resources occurs in the following order (where sourcePath is
 * set via the ApplicationSettings object for the current Application, and style
 * and locale are Session properties):
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
 * 
 * <li><b>Session Properties </b>- Arbitrary objects can be attached to a
 * Session by installing a session factory on your Application class which
 * creates custom Session subclasses that have typesafe properties specific to
 * the application (see {@link Application} for details). To discourage
 * non-typesafe access to Session properties, no setProperty() or getProperty()
 * method is provided. In a clustered environment, you should take care to
 * 
 * 
 * <li><b>Class Resolver </b>- Sessions have a class resolver (
 * {@link IClassResolver}) implementation that is used to locate classes for
 * components such as pages.
 * 
 * <li><b>Page Factory </b>- A pluggable implementation of {@link IPageFactory}
 * is used to instantiate pages for the session.
 * 
 * <li><b>Removal </b>- Pages can be removed from the Session forcibly by
 * calling remove(Page) or removeAll(), although such an action should rarely be
 * necessary.
 * 
 * @author Jonathan Locke
 * @author Eelco Hillenius
 */
public abstract class Session implements Serializable
{
	/** Name of session attribute under which this session is stored */
	public static final String SESSION_ATTRIBUTE_NAME = "session";

	/** Separator for component paths. */
	private static final char COMPONENT_PATH_SEPERATOR = ':';

	/** Thread-local current session. */
	private static final ThreadLocal CURRENT = new ThreadLocal();

	/** Logging object */
	private static final Log log = LogFactory.getLog(Session.class);

	/** Number designating when a page was accessed */
	short accessSequenceNumber;

	/** Prefix for attributes holding page map entries */
	final String pageMapEntryAttributePrefix = "p:";

	/** Application that this is a session of. */
	private transient Application application;

	/**
	 * Cached instance of agent info which is typically designated by calling
	 * {@link RequestCycle#newClientInfo()}.
	 */
	private ClientInfo clientInfo;

	/** The converter instance. */
	private transient IConverter converter;

	/** True if session state has been changed */
	private transient boolean dirty = false;

	/** The locale to use when loading resources for this session. */
	private Locale locale;

	/** Factory for constructing Pages for this Session */
	private transient IPageFactory pageFactory;

	/** the session store of this session. */
	private transient ISessionStore sessionStore;

	/** Attribute prefix for page maps stored in the session */
	private final String pageMapAttributePrefix = "m:";

	/** Any special "skin" style to use when loading resources. */
	private String style;

	/**
	 * Visitor interface for visiting page maps
	 * 
	 * @author Jonathan Locke
	 */
	public static interface IPageMapVisitor
	{
		/**
		 * @param pageMap
		 *            The page map
		 */
		public void pageMap(final PageMap pageMap);
	}

	/**
	 * Get the session for the calling thread.
	 * 
	 * @return Session for calling thread
	 */
	public static Session get()
	{
		Session session = (Session)CURRENT.get();
		if (session == null)
		{
			throw new WicketRuntimeException("there is not session attached to current thread "
					+ Thread.currentThread().getName());
		}
		return session;
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
		if (session == null)
		{
			throw new IllegalArgumentException("Argument session must me not null");
		}
		CURRENT.set(session);
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
		setLocale(application.getApplicationSettings().getDefaultLocale());
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
	 * @return The authorization strategy for this session
	 */
	public final IAuthorizationStrategy getAuthorizationStrategy()
	{
		return application.getSecuritySettings().getAuthorizationStrategy();
	}

	/**
	 * @return The class resolver for this Session
	 */
	public final IClassResolver getClassResolver()
	{
		return application.getApplicationSettings().getClassResolver();
	}

	/**
	 * Gets the client info object for this session. This method lazily gets the
	 * new agent info object for this session. It uses any cached or set ({@link #setClientInfo(ClientInfo)})
	 * client info object or uses {@link RequestCycle#newClientInfo()} to get
	 * the info object based on the current request when no client info object
	 * was set yet, and then caches the returned object; we can expect the
	 * client to stay the same for the whole session, and implementations of
	 * {@link RequestCycle#newClientInfo()} might be relatively expensive.
	 * 
	 * 
	 * @return the client info object based on this request
	 */
	public ClientInfo getClientInfo()
	{
		if (clientInfo == null)
		{
			this.clientInfo = getRequestCycle().newClientInfo();
		}
		return clientInfo;
	}

	/**
	 * Gets the unique id for this session (or a constant defining this is
	 * constant. By default returns the hasCode of this object as a String.
	 * 
	 * @return the unique id for this session (or a constant defining this is
	 *         constant
	 */
	public String getId()
	{
		return getSessionStore().getId();
	}

	/**
	 * Get this session's locale.
	 * 
	 * @return This session's locale
	 */
	public Locale getLocale()
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
		Page page = getPage(pageMapName, Integer.parseInt(Strings.firstPathComponent(path,
				COMPONENT_PATH_SEPERATOR)));

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
			pageFactory = application.getSessionSettings().getPageFactory();
		}
		return pageFactory;
	}

	/**
	 * @param page
	 *            The page, or null if no page context is available
	 * @return The page factory for the page, or the default page factory if
	 *         page was null
	 */
	public final IPageFactory getPageFactory(final Page page)
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
		return (PageMap)getAttribute(attributeForPageMapName(pageMapName));
	}

	/**
	 * @return A list of all PageMaps in this session.
	 */
	public final List getPageMaps()
	{
		final List list = new ArrayList();
		for (final Iterator iterator = getAttributeNames().iterator(); iterator.hasNext();)
		{
			final String attribute = (String)iterator.next();
			if (attribute.startsWith(pageMapAttributePrefix))
			{
				list.add((PageMap)getAttribute(attribute));
			}
		}
		return list;
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL IT.
	 * 
	 * @return The currently active request cycle for this session
	 */
	public final RequestCycle getRequestCycle()
	{
		return RequestCycle.get();
	}

	/**
	 * @return Size of this session, including all the pagemaps it contains
	 */
	public int getSize()
	{
		int size = Objects.sizeof(this);
		for (Iterator iterator = getPageMaps().iterator(); iterator.hasNext();)
		{
			PageMap pageMap = (PageMap)iterator.next();
			size += pageMap.getSize();
		}
		return size;
	}

	/**
	 * Get the style (see {@link wicket.Session}).
	 * 
	 * @return Returns the style (see {@link wicket.Session})
	 */
	public final String getStyle()
	{
		return style;
	}

	/**
	 * Set the session for each PageMap
	 */
	public final void init()
	{
		visitPageMaps(new IPageMapVisitor()
		{
			public void pageMap(PageMap pageMap)
			{
				if (log.isDebugEnabled())
				{
					log.debug("updateSession(): Attaching session to PageMap " + pageMap);
				}
				pageMap.setSession(Session.this);
			}
		});
	}

	/**
	 * Invalidates this session.
	 */
	public void invalidate()
	{
		getSessionStore().invalidate();
	}

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
		setAttribute(attributeForPageMapName(name), pageMap);
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
		visitPageMaps(new IPageMapVisitor()
		{
			public void pageMap(PageMap pageMap)
			{
				pageMap.removeAll();
			}
		});
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
	 * Sets the client info object for this session. This will only work when
	 * {@link #getClientInfo()} is not overriden.
	 * 
	 * @param clientInfo
	 *            the client info object
	 */
	public final void setClientInfo(ClientInfo clientInfo)
	{
		this.clientInfo = clientInfo;
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
	 * Set the style (see {@link wicket.Session}).
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
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL IT.
	 * 
	 * The page will be 'touched' in the session. If it wasn't added yet to the
	 * pagemap, it will be added to the page map else it will set this page to
	 * the front.
	 * 
	 * If another page was removed because of this it will be cleaned up.
	 * 
	 * @param page
	 */
	public void touch(Page page)
	{
		// Touch the page in its pagemap.
		page.getPageMap().put(page);
	}

	/**
	 * @param visitor
	 *            The visitor to call at each Page in this PageMap.
	 */
	public final void visitPageMaps(final IPageMapVisitor visitor)
	{
		for (final Iterator iterator = getAttributeNames().iterator(); iterator.hasNext();)
		{
			final String attribute = (String)iterator.next();
			if (attribute.startsWith(pageMapAttributePrefix))
			{
				visitor.pageMap((PageMap)getAttribute(attribute));
			}
		}
	}

	/**
	 * Any detach logic for session subclasses.
	 */
	protected void detach()
	{
	}

	/**
	 * Marks session state as dirty
	 */
	protected final void dirty()
	{
		this.dirty = true;
	}

	/**
	 * Gets the session store.
	 * 
	 * @return the session store
	 */
	protected final ISessionStore getSessionStore()
	{
		if (sessionStore == null)
		{
			ISessionStoreFactory sessionStoreFactory = application.getSessionSettings()
					.getSessionStoreFactory();
			sessionStore = sessionStoreFactory.newSessionStore(this);

			// still null?
			if (sessionStore == null)
			{
				throw new IllegalStateException(sessionStoreFactory.getClass().getName()
						+ " did not produce a session store");
			}
		}
		return sessionStore;
	}

	/**
	 * Gets the attribute value with the given name
	 * 
	 * @param name
	 *            The name of the attribute to store
	 * @return The value of the attribute
	 */
	protected final Object getAttribute(final String name)
	{
		return getSessionStore().getAttribute(name);
	}

	/**
	 * @return List of attributes for this session
	 */
	protected final List getAttributeNames()
	{
		return getSessionStore().getAttributeNames();
	}

	/**
	 * @return Request cycle factory for this kind of session.
	 */
	protected abstract IRequestCycleFactory getRequestCycleFactory();

	/**
	 * Removes the attribute with the given name.
	 * 
	 * @param name
	 *            the name of the attribute to remove
	 */
	protected final void removeAttribute(String name)
	{
		getSessionStore().removeAttribute(name);
	}

	/**
	 * Adds or replaces the attribute with the given name and value.
	 * 
	 * @param name
	 *            the name of the attribute
	 * @param value
	 *            the value of the attribute
	 */
	protected final void setAttribute(String name, Object value)
	{
		// get the old value if any
		Object oldValue = getAttribute(name);

		// set the actual attribute
		getSessionStore().setAttribute(name, value);

		// Do some extra profiling/ debugging. This can be a great help
		// just for testing whether your webbapp will behave when using
		// session replication
		if (log.isDebugEnabled())
		{
			String valueTypeName = (value != null ? value.getClass().getName() : "null");
			int size;
			try
			{
				final ByteArrayOutputStream out = new ByteArrayOutputStream();
				new ObjectOutputStream(out).writeObject(value);
				log.debug("Stored attribute " + name + "{ " + valueTypeName + "} with size: "
						+ Bytes.bytes(out.size()));
			}
			catch (Exception e)
			{
				throw new WicketRuntimeException(
						"Internal error cloning object. Make sure all dependent objects implement Serializable. Class: "
								+ valueTypeName, e);
			}
		}
	}

	/**
	 * Updates the session, e.g. for replication purposes.
	 */
	protected void update()
	{
		// If state is dirty
		if (dirty)
		{
			if (log.isDebugEnabled())
			{
				log.debug("updateCluster(): Session is dirty.  Replicating.");
			}

			// State is no longer dirty
			this.dirty = false;

			// Set attribute.
			setAttribute(SESSION_ATTRIBUTE_NAME, this);
		}
		else
		{
			if (log.isDebugEnabled())
			{
				log.debug("updateCluster(): Session not dirty.");
			}
		}

		// Go through all entries, replicating any dirty pages
		for (final Iterator iterator = getAttributeNames().iterator(); iterator.hasNext();)
		{
			final String attribute = (String)iterator.next();
			if (attribute.startsWith(pageMapEntryAttributePrefix))
			{
				// Get next entry
				IPageMapEntry entry = (IPageMapEntry)getAttribute(attribute);

				// If entry is dirty
				if (entry.isDirty())
				{
					// Make it undirty
					entry.setDirty(false);

					// Set session attribute to replicate the page
					setAttribute(attribute, entry);
				}
			}
		}
	}

	/**
	 * Indicates an access to a given entry
	 * 
	 * @param entry
	 *            The entry
	 */
	final void access(IPageMapEntry entry)
	{
		accessSequenceNumber++;
		entry.setAccessSequenceNumber(accessSequenceNumber);
	}

	/**
	 * Gets the converter instance.
	 * 
	 * @return the converter
	 */
	final IConverter getConverter()
	{
		if (converter == null)
		{
			// Let the factory create a new converter
			converter = getApplication().getApplicationSettings().getConverterFactory()
					.newConverter(getLocale());
		}
		return converter;
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
	final Page getPage(final String pageMapName, final int id)
	{
		// This call will always mark the Page dirty
		PageMap pageMap = getPageMap(pageMapName);
		if (pageMap == null)
		{
			return null;
		}
		return pageMap.get(id);
	}

	/**
	 * @param pageMap
	 *            Page map to remove
	 */
	final void removePageMap(final PageMap pageMap)
	{
		removeAttribute(attributeForPageMapName(pageMap.getName()));
	}

	/**
	 * @param pageMapName
	 *            Name of page map
	 * @return Session attribute holding page map
	 */
	private final String attributeForPageMapName(final String pageMapName)
	{
		return pageMapAttributePrefix + pageMapName;
	}
}
