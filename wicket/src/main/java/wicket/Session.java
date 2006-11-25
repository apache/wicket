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

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.application.IClassResolver;
import wicket.authorization.IAuthorizationStrategy;
import wicket.feedback.FeedbackMessage;
import wicket.feedback.FeedbackMessages;
import wicket.request.ClientInfo;
import wicket.session.ISessionStore;
import wicket.util.convert.IConverter;
import wicket.util.lang.Bytes;
import wicket.util.lang.Objects;
import wicket.util.string.Strings;
import wicket.util.time.Duration;

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
 * method is provided. In a clustered environment, you should take care to call
 * the dirty() method when you change a property or youre own. This way the
 * session will be reset again in the http session so that the http session
 * knows the session is changed.
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
 * <li><b>Flash Messages</b>- Flash messages are messages that are stored in
 * session and are removed after they are displayed to the user. Session acts as
 * a store for these messages because they can last across requests.
 * 
 * @author Jonathan Locke
 * @author Eelco Hillenius
 * @author Igor Vaynberg (ivaynberg)
 */
public abstract class Session implements Serializable, IConverterLocator
{
	private static final long serialVersionUID = 1L;

	/** meta data key for missing body tags logging. */
	public static final MetaDataKey<PageMapAccessMetaData> PAGEMAP_ACCESS_MDK = new MetaDataKey<PageMapAccessMetaData>(
			PageMapAccessMetaData.class)
	{
		private static final long serialVersionUID = 1L;
	};

	/**
	 * meta data for recording map map access.
	 */
	public static final class PageMapAccessMetaData implements Serializable
	{
		private static final long serialVersionUID = 1L;

		Set<String> pageMapNames = new HashSet<String>(2);

		/**
		 * @param pagemap
		 *            the pagemap to add as used.
		 * @return the boolean if it was added (didn't already contain the
		 *         pagemap)
		 */
		public boolean add(IPageMap pagemap)
		{
			return pageMapNames.add(pagemap.getName());
		}
	}

	/** Name of session attribute under which this session is stored */
	public static final String SESSION_ATTRIBUTE_NAME = "session";

	/** Prefix for attributes holding page map entries */
	static final String pageMapEntryAttributePrefix = "p:";

	/** Thread-local current session. */
	private static final ThreadLocal<Session> current = new ThreadLocal<Session>();

	/** A store for dirty objects for one request */
	private static final ThreadLocal<List<Serializable>> dirtyObjects = new ThreadLocal<List<Serializable>>();

	/** Logging object */
	private static final Log log = LogFactory.getLog(Session.class);

	/** Attribute prefix for page maps stored in the session */
	private static final String pageMapAttributePrefix = "m:";

	/**
	 * Cached instance of agent info which is typically designated by calling
	 * {@link RequestCycle#newClientInfo()}.
	 */
	private ClientInfo clientInfo;

	/** The converter instance. */
	private transient IConverterLocator converterSupplier;

	/** True if session state has been changed */
	private transient boolean dirty = false;

	/** The locale to use when loading resources for this session. */
	private Locale locale;

	/** A number to generate names for auto create pagemaps */
	private int autoCreatePageMapCounter = 0;

	/** A linked list for last used pagemap queue */
	private LinkedList<IPageMap> usedPageMaps = new LinkedList<IPageMap>();

	/** Any special "skin" style to use when loading resources. */
	private String style;

	/** feedback messages */
	private FeedbackMessages feedbackMessages = new FeedbackMessages(
			new CopyOnWriteArrayList<FeedbackMessage>());

	private transient Map<IPageMap, Thread> pageMapsUsedInRequest;

	/** cached id because you can't access the id after session unbound */
	private String id = null;

	/**
	 * Temporary instance of the session store. Should be set on each request as
	 * it is not supposed to go in the session.
	 */
	private transient ISessionStore sessionStore;

	/** Application level meta data. */
	private MetaDataEntry[] metaData;

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
		public void pageMap(final IPageMap pageMap);
	}

	/**
	 * Get the session for the calling thread.
	 * 
	 * @return Session for calling thread, or null if not set
	 */
	public static Session get()
	{
		return current.get();
	}

	/**
	 * Get the session for the calling thread.
	 * 
	 * @param forceBind
	 *            Whether to force binding this session to the application's
	 *            {@link ISessionStore session store}. A Wicket application can
	 *            operate in a session-less mode as long as stateless pages are
	 *            used. Session objects will then be created for each request,
	 *            but they will only live for that request. You can recognize
	 *            temporary sessions by calling {@link #isTemporary()} which
	 *            basically checks whether the session's id is null. Hence,
	 *            temporary sessions have no session id. If this argument is
	 *            true, the session will be bound (made not-temporary) if it was
	 *            not bound yet. It is useful for cases where you want to be
	 *            absolutely sure this session object will be available in next
	 *            requests.
	 * 
	 * @return Session for calling thread
	 */
	public static Session get(boolean forceBind)
	{
		Session session = get();
		if (forceBind)
		{
			session.bind();
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
			throw new IllegalArgumentException("Argument session can not be null");
		}
		current.set(session);
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL IT.
	 * <p>
	 * Clears the session for calling thread.
	 * 
	 */
	public static void unset()
	{
		current.set(null);
	}

	/**
	 * Constructor.
	 * 
	 * @param application
	 *            The application that this is a session of
	 */
	protected Session(final Application application)
	{
		// Set locale to default locale
		setLocale(application.getApplicationSettings().getDefaultLocale());
	}

	/**
	 * Force binding this session to the application's
	 * {@link ISessionStore session store}. A Wicket application can operate in
	 * a session-less mode as long as stateless pages are used. Session objects
	 * will be then created for each request, but they will only live for that
	 * request. You can recognize temporary sessions by calling
	 * {@link #isTemporary()} which basically checks whether the session's id is
	 * null. Hence, temporary sessions have no session id.
	 * <p>
	 * By calling this method, the session will be bound (made not-temporary) if
	 * it was not bound yet. It is useful for cases where you want to be
	 * absolutely sure this session object will be available in next requests.
	 * </p>
	 */
	public final void bind()
	{
		ISessionStore store = getSessionStore();
		Request request = RequestCycle.get().getRequest();
		if (store.getSessionId(request, false) == null)
		{
			// explicitly create a session
			this.id = store.getSessionId(request, true);
			// bind it
			store.bind(request, this);
		}
	}

	/**
	 * Whether this session is temporary. A Wicket application can operate in a
	 * session-less mode as long as stateless pages are used. If this session
	 * object is temporary, it will not be available on a next request.
	 * 
	 * @return Whether this session is temporary (which is the same as it's id
	 *         being null)
	 */
	public final boolean isTemporary()
	{
		return getId() == null;
	}

	/**
	 * Removes all pages from the session. Although this method should rarely be
	 * needed, it is available (possibly for security reasons).
	 */
	public final void clear()
	{
		visitPageMaps(new IPageMapVisitor()
		{
			public void pageMap(IPageMap pageMap)
			{
				pageMap.clear();
			}
		});
	}

	/**
	 * Get the application that is currently working with this session.
	 * 
	 * @return Returns the application.
	 */
	public Application getApplication()
	{
		return Application.get();
	}

	/**
	 * @return The authorization strategy for this session
	 */
	public IAuthorizationStrategy getAuthorizationStrategy()
	{
		return getApplication().getSecuritySettings().getAuthorizationStrategy();
	}

	/**
	 * @return The class resolver for this Session
	 */
	public final IClassResolver getClassResolver()
	{
		return getApplication().getApplicationSettings().getClassResolver();
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
	 * @return the client info object based on this request
	 */
	public ClientInfo getClientInfo()
	{
		if (clientInfo == null)
		{
			this.clientInfo = RequestCycle.get().newClientInfo();
		}
		return clientInfo;
	}

	/**
	 * @return The default page map
	 */
	public final IPageMap getDefaultPageMap()
	{
		return pageMapForName(PageMap.DEFAULT_NAME, true);
	}

	/**
	 * Gets the unique id for this session from the underlying SessionStore. May
	 * be null if a concrete session is not yet created.
	 * 
	 * @return The unique id for this session or null if it is a temporary
	 *         session
	 */
	public final String getId()
	{
		if (id == null)
		{
			id = getSessionStore().getSessionId(RequestCycle.get().getRequest(), false);

			// we have one?
			if (id != null)
			{
				dirty();
			}
		}
		return id;
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
	 * Gets metadata for this session using the given key.
	 * 
	 * @param key
	 *            The key for the data
	 * @param <T>
	 * @return The metadata
	 * @see MetaDataKey
	 */
	public final <T extends Serializable> T getMetaData(final MetaDataKey<T> key)
	{
		return key.get(metaData);
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

		// Get page map by name, creating the default page map automatically
		IPageMap pageMap = pageMapForName(pageMapName, Objects.equal(PageMap.DEFAULT_NAME,
				pageMapName));
		if (pageMap != null)
		{
			synchronized (usedPageMaps) // get a lock so be sure that only one
			// is made
			{
				if (pageMapsUsedInRequest == null)
				{
					// TODO!! this is not synchronized.. it should be (on
					// session?)
					pageMapsUsedInRequest = new HashMap<IPageMap, Thread>(3);
				}
			}
			synchronized (pageMapsUsedInRequest)
			{
				long startTime = System.currentTimeMillis();

				// TODO For now only use the setting. Might be extended with
				// something overridable on request/ page/ request target level
				// later
				Duration timeout = Application.get().getRequestCycleSettings().getTimeout();

				// Get page entry for id and version
				Thread t = pageMapsUsedInRequest.get(pageMap);
				while (t != null && t != Thread.currentThread())
				{
					try
					{
						pageMapsUsedInRequest.wait(timeout.getMilliseconds());
					}
					catch (InterruptedException ex)
					{
						throw new WicketRuntimeException(ex);
					}
					t = pageMapsUsedInRequest.get(pageMap);
					if (t != null && t != Thread.currentThread()
							&& (startTime + timeout.getMilliseconds()) < System.currentTimeMillis())
					{
						// if it is still not the right thread..
						// This either points to long running code (a report
						// page?) or a deadlock or such
						throw new WicketRuntimeException("After " + timeout + " the Pagemap "
								+ pageMapName + " is still locked by: " + t
								+ ", giving up trying to get the page for path: " + path);
					}
				}
				pageMapsUsedInRequest.put(pageMap, Thread.currentThread());
				final String id = Strings.firstPathComponent(path, Component.PATH_SEPARATOR);
				Page page = pageMap.get(Integer.parseInt(id), versionNumber);
				if (page == null)
				{
					pageMapsUsedInRequest.remove(pageMap);
					pageMapsUsedInRequest.notifyAll();
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
		return getApplication().getSessionSettings().getPageFactory();
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
	 * Gets a page map for the given name, automatically creating it if need be.
	 * 
	 * @param pageMapName
	 *            Name of page map, or null for default page map
	 * @param autoCreate
	 *            True if the page map should be automatically created if it
	 *            does not exist
	 * @return PageMap for name
	 */
	public final IPageMap pageMapForName(String pageMapName, final boolean autoCreate)
	{
		IPageMap pageMap = (IPageMap)getAttribute(attributeForPageMapName(pageMapName));
		if (pageMap == null && autoCreate)
		{
			pageMap = newPageMap(pageMapName);
		}
		return pageMap;
	}

	/**
	 * Automatically creates a page map, giving it a session unique name.
	 * 
	 * @return Created PageMap
	 */
	public synchronized final IPageMap createAutoPageMap()
	{
		return newPageMap(createAutoPageMapName());
	}

	/**
	 * With this call you can create a pagemap name but not create the pagemap
	 * itself already. It will give the first pagemap name where it couldn't
	 * find a current pagemap for.
	 * 
	 * It will return the same name if you call it 2 times in a row.
	 * 
	 * @return The created pagemap name
	 */
	public synchronized final String createAutoPageMapName()
	{
		String name = "wicket-" + autoCreatePageMapCounter;
		IPageMap pm = pageMapForName(name, false);
		while (pm != null)
		{
			autoCreatePageMapCounter++;
			name = "wicket-" + autoCreatePageMapCounter;
			pm = pageMapForName(name, false);
		}
		return name;
	}

	/**
	 * @return A list of all PageMaps in this session.
	 */
	public final List<IPageMap> getPageMaps()
	{
		final List<IPageMap> list = new ArrayList<IPageMap>();
		for (Object element : getAttributeNames())
		{
			final String attribute = (String)element;
			if (attribute.startsWith(pageMapAttributePrefix))
			{
				list.add((IPageMap)getAttribute(attribute));
			}
		}
		return list;
	}

	/**
	 * @return Size of this session, including all the pagemaps it contains
	 */
	public final long getSizeInBytes()
	{
		long size = Objects.sizeof(this);
		for (IPageMap pageMap : getPageMaps())
		{
			size += pageMap.getSizeInBytes();
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
		// Set session on each page map
		visitPageMaps(new IPageMapVisitor()
		{
			public void pageMap(IPageMap pageMap)
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
		getSessionStore().invalidate(RequestCycle.get().getRequest());
	}

	/**
	 * Creates a new page map with a given name
	 * 
	 * @param name
	 *            The name for the new page map
	 * @return The newly created page map
	 */
	public final IPageMap newPageMap(final String name)
	{
		// Check that session doesn't have too many page maps already
		final int maxPageMaps = getApplication().getSessionSettings().getMaxPageMaps();
		if (usedPageMaps.size() >= maxPageMaps)
		{
			IPageMap pm = usedPageMaps.getFirst();
			pm.remove();
		}

		// Create new page map
		final IPageMap pageMap = getSessionStore().createPageMap(name, this);
		setAttribute(attributeForPageMapName(name), pageMap);
		dirty();
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
	 * @param pageMap
	 *            Page map to remove
	 */
	public final void removePageMap(final IPageMap pageMap)
	{
		PageMapAccessMetaData pagemapMetaData = getMetaData(PAGEMAP_ACCESS_MDK);
		if (pagemapMetaData != null)
			pagemapMetaData.pageMapNames.remove(pageMap.getName());
		usedPageMaps.remove(pageMap);
		removeAttribute(attributeForPageMapName(pageMap.getName()));
		dirty();
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL IT.
	 * <p>
	 * Sets the client info object for this session. This will only work when
	 * {@link #getClientInfo()} is not overriden.
	 * 
	 * @param clientInfo
	 *            the client info object
	 */
	public final void setClientInfo(ClientInfo clientInfo)
	{
		this.clientInfo = clientInfo;
		dirty();
	}

	/**
	 * Set the locale for this session.
	 * 
	 * @param locale
	 *            New locale
	 */
	public final void setLocale(final Locale locale)
	{
		if (locale == null)
		{
			throw new IllegalArgumentException("Parameter 'locale' must not be null");
		}
		this.locale = locale;
		dirty();
	}

	/**
	 * Sets the metadata for this session using the given key. If the metadata
	 * object is not of the correct type for the metadata key, an
	 * IllegalArgumentException will be thrown. For information on creating
	 * MetaDataKeys, see {@link MetaDataKey}.
	 * 
	 * @param <M>
	 *            Meta data type
	 * 
	 * @param key
	 *            The singleton key for the metadata
	 * @param object
	 *            The metadata object
	 * @throws IllegalArgumentException
	 * @see MetaDataKey
	 */
	public final <M extends Serializable> void setMetaData(final MetaDataKey<M> key, final M object)
	{
		metaData = key.set(metaData, object);
		dirty();
	}

	/**
	 * Set the style (see {@link wicket.Session}).
	 * 
	 * @param style
	 *            The style to set.
	 * @return the Session object
	 */
	public final Session setStyle(final String style)
	{
		this.style = style;
		dirty();
		return this;
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL IT.
	 * <p>
	 * The page will be 'touched' in the session. If it wasn't added yet to the
	 * pagemap, it will be added to the page map else it will set this page to
	 * the front.
	 * 
	 * If another page was removed because of this it will be cleaned up.
	 * 
	 * @param page
	 */
	public final void touch(Page page)
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
		for (Object element : getAttributeNames())
		{
			final String attribute = (String)element;
			if (attribute.startsWith(pageMapAttributePrefix))
			{
				visitor.pageMap((PageMap)getAttribute(attribute));
			}
		}
	}

	/**
	 * Registers an informational feedback message for this session
	 * 
	 * @param message
	 *            The feedback message
	 */
	public final void info(final Serializable message)
	{
		addFeedbackMessage(message, FeedbackMessage.INFO);
	}

	/**
	 * Registers a warning feedback message for this session
	 * 
	 * @param message
	 *            The feedback message
	 */
	public final void warn(final Serializable message)
	{
		addFeedbackMessage(message, FeedbackMessage.WARNING);
	}

	/**
	 * Registers an error feedback message for this session
	 * 
	 * @param message
	 *            The feedback message
	 */
	public final void error(final Serializable message)
	{
		addFeedbackMessage(message, FeedbackMessage.ERROR);
	}

	/**
	 * Gets feedback messages stored in session
	 * 
	 * @return unmodifiable list of feedback messages
	 */
	public final FeedbackMessages getFeedbackMessages()
	{
		return feedbackMessages;
	}

	/**
	 * Gets the converter instance. This method returns the cached converter for
	 * the current locale. Whenever the locale is changed, the cached value is
	 * cleared and the converter will be recreated for the new locale on a next
	 * request.
	 * 
	 * @param type
	 *            TODO
	 * 
	 * @return the converter
	 */
	public final IConverter getConverter(Class type)
	{
		if (converterSupplier == null)
		{
			// Let the factory create a new converter
			converterSupplier = getApplication().getApplicationSettings()
					.getConverterLocatorFactory().newConverterLocator();
		}
		return converterSupplier.getConverter(type);
	}

	/**
	 * Adds a feedback message to the list of messages
	 * 
	 * @param message
	 * @param level
	 * 
	 */
	private void addFeedbackMessage(Serializable message, int level)
	{
		getFeedbackMessages().add(null, message, level);
		dirty();
	}

	/**
	 * Any detach logic for session subclasses. This is called on the end of
	 * handling a request, when the RequestCycle is about to be detached from
	 * the current thread.
	 */
	protected void detach()
	{
	}

	/**
	 * Marks session state as dirty.
	 */
	protected void dirty()
	{
		this.dirty = true;
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
		RequestCycle cycle = RequestCycle.get();
		if (cycle != null)
		{
			return getSessionStore().getAttribute(cycle.getRequest(), name);
		}
		return null;
	}

	/**
	 * @return List of attributes for this session
	 */
	protected final List<String> getAttributeNames()
	{
		RequestCycle cycle = RequestCycle.get();
		if (cycle != null)
		{
			return getSessionStore().getAttributeNames(cycle.getRequest());
		}
		return null;
	}

	/**
	 * @return Request cycle factory for this kind of session.
	 */
	protected abstract IRequestCycleFactory getRequestCycleFactory();

	/**
	 * Gets the session store.
	 * 
	 * @return the session store
	 */
	protected final ISessionStore getSessionStore()
	{
		if (sessionStore == null)
		{
			sessionStore = getApplication().getSessionStore();
		}
		return sessionStore;
	}

	/**
	 * Removes the attribute with the given name.
	 * 
	 * @param name
	 *            the name of the attribute to remove
	 */
	protected final void removeAttribute(String name)
	{
		RequestCycle cycle = RequestCycle.get();
		if (cycle != null)
		{
			getSessionStore().removeAttribute(cycle.getRequest(), name);
		}
	}

	/**
	 * Adds or replaces the attribute with the given name and value.
	 * 
	 * @param name
	 *            The name of the attribute
	 * @param value
	 *            The value of the attribute
	 */
	protected final void setAttribute(String name, Object value)
	{
		RequestCycle cycle = RequestCycle.get();
		if (cycle == null)
		{
			throw new WicketRuntimeException("Can not set the attribute. No RequestCycle available");
		}

		ISessionStore store = getSessionStore();
		Request request = cycle.getRequest();

		// extra check on session binding event
		if (value == this)
		{
			Object current = store.getAttribute(request, name);
			if (current == null)
			{
				String id = store.getSessionId(request, false);
				if (id != null)
				{
					// this is a new instance. wherever it came from, bind the
					// session now
					store.bind(request, (Session)value);
				}
			}
		}
		String valueTypeName = (value != null ? value.getClass().getName() : "null");
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

		// Set the actual attribute
		store.setAttribute(request, name, value);
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
				log.debug("update: Session is dirty.  Replicating.");
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
				log.debug("update: Session not dirty.");
			}
		}

		// Go through all dirty entries, replicating any dirty objects
		List dirtyObjects = Session.dirtyObjects.get();
		if (dirtyObjects != null)
		{
			for (final Iterator iterator = dirtyObjects.iterator(); iterator.hasNext();)
			{
				String attribute = null;
				Object object = iterator.next();
				if (object instanceof Page)
				{
					final Page page = (Page)object;
					if (page.isPageStateless())
					{
						// check, can it be that stateless pages where added to
						// the session?
						// and should be removed now?
						continue;
					}
					attribute = page.getPageMap().attributeForId(page.getNumericId());
					if (getAttribute(attribute) == null)
					{
						// page removed by another thread. don't add it again.
						continue;
					}
					object = page.getPageMapEntry();
				}
				else if (object instanceof IPageMap)
				{
					attribute = attributeForPageMapName(((IPageMap)object).getName());
				}

				setAttribute(attribute, object);
			}
			Session.dirtyObjects.set(null);
		}
	}

	/**
	 * Removes any rendered feedback messages as well as compacts memory. This
	 * method is usually called at the end of the request cycle processing.
	 */
	final void cleanupFeedbackMessages()
	{
		int size = feedbackMessages.size();
		feedbackMessages.clearRendered();
		// mark the session as dirty when the feedback messages have been
		// altered.
		if (size != feedbackMessages.size())
		{
			dirty();
		}
	}

	/**
	 * @param page
	 *            The page to add to dirty objects list
	 */
	void dirtyPage(final Page page)
	{
		List<Serializable> dirtyObjects = getDirtyObjectsList();
		if (!dirtyObjects.contains(page))
		{
			dirtyObjects.add(page);
		}
	}

	/**
	 * INTERNAL API. The request cycle when detached will call this.
	 * 
	 */
	final void requestDetached()
	{
		if (pageMapsUsedInRequest != null)
		{
			synchronized (pageMapsUsedInRequest)
			{
				Thread t = Thread.currentThread();
				Iterator<Map.Entry<IPageMap, Thread>> it = pageMapsUsedInRequest.entrySet()
						.iterator();
				while (it.hasNext())
				{
					Entry<IPageMap, Thread> entry = it.next();
					if (entry.getValue() == t)
					{
						it.remove();
					}
				}
				pageMapsUsedInRequest.notifyAll();
			}
		}
	}

	/**
	 * @param map
	 *            The page map to add to dirty objects list
	 */
	void dirtyPageMap(final IPageMap map)
	{
		if (!map.isDefault())
		{
			usedPageMaps.remove(map);
			usedPageMaps.addLast(map);
		}
		List<Serializable> dirtyObjects = getDirtyObjectsList();
		if (!dirtyObjects.contains(map))
		{
			dirtyObjects.add(map);
		}
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

	/**
	 * @return The current thread dirty objects list
	 */
	List<Serializable> getDirtyObjectsList()
	{
		List<Serializable> list = dirtyObjects.get();
		if (list == null)
		{
			list = new ArrayList<Serializable>(4);
			dirtyObjects.set(list);
		}
		return list;
	}
}
