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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.wicket.application.IClassResolver;
import org.apache.wicket.authorization.IAuthorizationStrategy;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.feedback.FeedbackMessages;
import org.apache.wicket.protocol.http.IgnoreAjaxRequestException;
import org.apache.wicket.request.ClientInfo;
import org.apache.wicket.session.ISessionStore;
import org.apache.wicket.util.lang.Objects;
import org.apache.wicket.util.string.AppendingStringBuffer;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Holds information about a user session, including some fixed number of most recent pages (and all
 * their nested component information).
 * <ul>
 * <li><b>Access via RequestCycle </b>- The Session for a {@link RequestCycle} can be retrieved by
 * calling {@link RequestCycle#getSession()}.
 * 
 * <li><b>Access via Component </b>- If a RequestCycle object is not available, the Session can be
 * retrieved for a Component by calling {@link Component#getSession()}. As currently implemented,
 * each Component does not itself have a reference to the session that contains it. However, the
 * Page component at the root of the containment hierarchy does have a reference to the Session that
 * holds the Page. So {@link Component#getSession()} traverses the component hierarchy to the root
 * Page and then calls {@link Page#getSession()}.
 * 
 * <li><b>Access via Thread Local </b>- In the odd case where neither a RequestCycle nor a Component
 * is available, the currently active Session for the calling thread can be retrieved by calling the
 * static method Session.get(). This last form should only be used if the first two forms cannot be
 * used since thread local access can involve a potentially more expensive hash map lookup.
 * 
 * <li><b>Locale </b>- A session has a Locale property to support localization. The Locale for a
 * session can be set by calling {@link Session#setLocale(Locale)}. The Locale for a Session
 * determines how localized resources are found and loaded.
 * 
 * <li><b>Style </b>- Besides having an appearance based on locale, resources can also have
 * different looks in the same locale (a.k.a. "skins"). The style for a session determines the look
 * which is used within the appropriate locale. The session style ("skin") can be set with the
 * setStyle() method.
 * 
 * <li><b>Resource Loading </b>- Based on the Session locale and style, searching for resources
 * occurs in the following order (where sourcePath is set via the ApplicationSettings object for the
 * current Application, and style and locale are Session properties):
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
 * <li><b>Session Properties </b>- Arbitrary objects can be attached to a Session by installing a
 * session factory on your Application class which creates custom Session subclasses that have
 * typesafe properties specific to the application (see {@link Application} for details). To
 * discourage non-typesafe access to Session properties, no setProperty() or getProperty() method is
 * provided. In a clustered environment, you should take care to call the dirty() method when you
 * change a property on your own. This way the session will be reset again in the http session so
 * that the http session knows the session is changed.
 * 
 * <li><b>Class Resolver </b>- Sessions have a class resolver ( {@link IClassResolver})
 * implementation that is used to locate classes for components such as pages.
 * 
 * <li><b>Page Factory </b>- A pluggable implementation of {@link IPageFactory} is used to
 * instantiate pages for the session.
 * 
 * <li><b>Removal </b>- Pages can be removed from the Session forcibly by calling remove(Page) or
 * removeAll(), although such an action should rarely be necessary.
 * 
 * <li><b>Flash Messages</b>- Flash messages are messages that are stored in session and are removed
 * after they are displayed to the user. Session acts as a store for these messages because they can
 * last across requests.
 * 
 * @author Jonathan Locke
 * @author Eelco Hillenius
 * @author Igor Vaynberg (ivaynberg)
 */
public abstract class Session implements IClusterable
{
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
	 * meta data for recording map map access.
	 */
	public static final class PageMapAccessMetaData implements IClusterable
	{
		private static final long serialVersionUID = 1L;

		Set<String> pageMapNames = new HashSet<String>(2);

		/**
		 * @param pagemap
		 *            the pagemap to add as used.
		 * @return the boolean if it was added (didn't already contain the pagemap)
		 */
		public boolean add(IPageMap pagemap)
		{
			return pageMapNames.add(pagemap.getName());
		}
	}

	/** a sequence used for whenever something session-specific needs a unique value */
	private int sequence = 1;

	/** meta data key for missing body tags logging. */
	public static final MetaDataKey<PageMapAccessMetaData> PAGEMAP_ACCESS_MDK = new MetaDataKey<PageMapAccessMetaData>()
	{
		private static final long serialVersionUID = 1L;
	};

	/** Name of session attribute under which this session is stored */
	public static final String SESSION_ATTRIBUTE_NAME = "session";

	/** Thread-local current session. */
	private static final ThreadLocal<Session> current = new ThreadLocal<Session>();

	/** A store for dirty objects for one request */
	private static final ThreadLocal<List<IClusterable>> dirtyObjects = new ThreadLocal<List<IClusterable>>();

	/** Logging object */
	private static final Logger log = LoggerFactory.getLogger(Session.class);

	/** Attribute prefix for page maps stored in the session */
	private static final String pageMapAttributePrefix = "m:";

	private static final long serialVersionUID = 1L;

	/** A store for touched pages for one request */
	private static final ThreadLocal<List<Page>> touchedPages = new ThreadLocal<List<Page>>();

	/** Prefix for attributes holding page map entries */
	static final String pageMapEntryAttributePrefix = "p:";

	/** */
	private int pageIdCounter = 0;

	/**
	 * Checks if the <code>Session</code> threadlocal is set in this thread
	 * 
	 * @return true if {@link Session#get()} can return the instance of session, false otherwise
	 */
	public static boolean exists()
	{
		return current.get() != null;
	}

	/**
	 * Locate the session for the client of this request in the {@link ISessionStore} or create a
	 * new one and attach it when none could be located and sets it as the current instance for this
	 * thread. Typically, clients never touch this method, but rather use {@link Session#get()},
	 * which does the locating implicitly when not yet set as a thread local.
	 * 
	 * @return The session for the client of this request or a new, unbound
	 */
	public static final Session findOrCreate()
	{
		RequestCycle requestCycle = RequestCycle.get();
		if (requestCycle == null)
		{
			throw new IllegalStateException(
				"you can only locate or create sessions in the context of a request cycle");
		}
		Response response = requestCycle.getResponse();
		Request request = requestCycle.getRequest();
		return findOrCreate(request, response);
	}

	/**
	 * @param response
	 * @param request
	 * @return The Session that is found in the current request or created if not.
	 */
	public static Session findOrCreate(Request request, Response response)
	{
		Application application = Application.get();
		ISessionStore sessionStore = application.getSessionStore();
		Session session = sessionStore.lookup(request);

		if (session == null)
		{
			// Create session using session factory
			session = application.newSession(request, response);

			dirtyObjects.set(null);
			touchedPages.set(null);
		}

		// set thread local
		set(session);

		return session;
	}

	/**
	 * Get the session for the calling thread.
	 * 
	 * @return Session for calling thread
	 */
	public static Session get()
	{
		Session session = current.get();
		if (session == null)
		{
			session = findOrCreate();
		}
		return session;
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL IT.
	 * <p>
	 * Sets session for calling thread. Also triggers {@link #attach()} being called.
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

		try
		{
			// execute any attach logic now
			session.attach();
		}
		catch (RuntimeException e)
		{
			current.set(null);
		}
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

	/** A number to generate names for auto create pagemaps */
	private int autoCreatePageMapCounter = 0;

	/**
	 * Cached instance of agent info which is typically designated by calling
	 * {@link RequestCycle#newClientInfo()}.
	 */
	private ClientInfo clientInfo;

	/** True if session state has been changed */
	private transient boolean dirty = false;

	/** feedback messages */
	private final FeedbackMessages feedbackMessages = new FeedbackMessages();

	/** cached id because you can't access the id after session unbound */
	private String id = null;

	/** The locale to use when loading resources for this session. */
	private Locale locale;

	/** Application level meta data. */
	private MetaDataEntry<?>[] metaData;

	/**
	 * We need to know both thread that keeps the pagemap lock and the RequestCycle
	 */
	private static class PageMapsUsedInRequestEntry
	{
		Thread thread;
		RequestCycle requestCycle;
	};

	private transient Map<IPageMap, PageMapsUsedInRequestEntry> pageMapsUsedInRequest;

	/** True, if session has been invalidated */
	private transient boolean sessionInvalidated = false;

	/**
	 * Temporary instance of the session store. Should be set on each request as it is not supposed
	 * to go in the session.
	 */
	private transient ISessionStore sessionStore;

	/** Any special "skin" style to use when loading resources. */
	private String style;

	/**
	 * Holds attributes for sessions that are still temporary/ not bound to a session store. Only
	 * used when {@link #isTemporary()} is true.
	 * <p>
	 * Note: this doesn't have to be synchronized, as the only time when this map is used is when a
	 * session is temporary, in which case it won't be shared between requests (it's a per request
	 * instance).
	 * </p>
	 */
	private transient Map<String, Object> temporarySessionAttributes;

	/** A linked list for last used pagemap names queue */
	private final LinkedList<String> usedPageMapNames = new LinkedList<String>();

	/**
	 * Constructor. Note that {@link RequestCycle} is not available until this constructor returns.
	 * 
	 * @param request
	 *            The current request
	 */
	public Session(Request request)
	{
		locale = request.getLocale();
		if (locale == null)
		{
			throw new IllegalStateException(
				"Request#getLocale() cannot return null, request has to have a locale set on it");
		}
	}

	/**
	 * Constructor. Note that {@link RequestCycle} is not available until this constructor returns.
	 * 
	 * @deprecated Use #Session(Request)
	 * 
	 * @param application
	 *            The application that this is a session of
	 * @param request
	 *            The current request
	 */
	@Deprecated
	protected Session(Application application, Request request)
	{
		this(request);
	}

	/**
	 * Force binding this session to the application's {@link ISessionStore session store} if not
	 * already done so.
	 * <p>
	 * A Wicket application can operate in a session-less mode as long as stateless pages are used.
	 * Session objects will be then created for each request, but they will only live for that
	 * request. You can recognize temporary sessions by calling {@link #isTemporary()} which
	 * basically checks whether the session's id is null. Hence, temporary sessions have no session
	 * id.
	 * </p>
	 * <p>
	 * By calling this method, the session will be bound (made not-temporary) if it was not bound
	 * yet. It is useful for cases where you want to be absolutely sure this session object will be
	 * available in next requests. If the session was already bound (
	 * {@link ISessionStore#lookup(Request) returns a session}), this call will be a noop.
	 * </p>
	 */
	public final void bind()
	{
		// If there is no request cycle then this is not a normal request but for example a last
		// modified call.
		if (RequestCycle.get() == null)
		{
			return;
		}

		ISessionStore store = getSessionStore();
		Request request = RequestCycle.get().getRequest();
		if (store.lookup(request) == null)
		{
			// explicitly create a session
			id = store.getSessionId(request, true);
			// bind it
			store.bind(request, this);

			if (temporarySessionAttributes != null)
			{
				for (Entry<String, Object> entry : temporarySessionAttributes.entrySet())
				{
					store.setAttribute(request, String.valueOf(entry.getKey()), entry.getValue());
				}
				temporarySessionAttributes = null;
			}
		}
	}

	/**
	 * Cleans up all rendered feedback messages and any unrendered, dangling feedback messages there
	 * may be left after that.
	 */
	public abstract void cleanupFeedbackMessages();


	/**
	 * Removes all pages from the session. Although this method should rarely be needed, it is
	 * available (possibly for security reasons).
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
	 * Automatically creates a page map, giving it a session unique name.
	 * 
	 * @return Created PageMap
	 */
	public final IPageMap createAutoPageMap()
	{
		return newPageMap(createAutoPageMapName());
	}

	protected int currentCreateAutoPageMapCounter()
	{
		return autoCreatePageMapCounter;
	}

	protected void incrementCreateAutoPageMapCounter()
	{
		++autoCreatePageMapCounter;
	}

	/**
	 * With this call you can create a pagemap name but not create the pagemap itself already. It
	 * will give the first pagemap name where it couldn't find a current pagemap for.
	 * 
	 * It will return the same name if you call it 2 times in a row.
	 * 
	 * @return The created pagemap name
	 */
	public synchronized final String createAutoPageMapName()
	{
		String name = getAutoPageMapNamePrefix() + currentCreateAutoPageMapCounter() +
			getAutoPageMapNameSuffix();
		IPageMap pm = pageMapForName(name, false);
		while (pm != null)
		{
			incrementCreateAutoPageMapCounter();
			name = getAutoPageMapNamePrefix() + currentCreateAutoPageMapCounter() +
				getAutoPageMapNameSuffix();
			pm = pageMapForName(name, false);
		}
		return name;
	}

	/**
	 * @return The prefixed string default "wicket-".
	 */
	protected String getAutoPageMapNamePrefix()
	{
		return "wicket-";
	}

	/**
	 * @return The suffix default an empty string.
	 */
	protected String getAutoPageMapNameSuffix()
	{
		return "";
	}

	/**
	 * Registers an error feedback message for this session
	 * 
	 * @param message
	 *            The feedback message
	 */
	public final void error(final String message)
	{
		addFeedbackMessage(message, FeedbackMessage.ERROR);
	}

	/**
	 * Get the application that is currently working with this session.
	 * 
	 * @return Returns the application.
	 */
	public final Application getApplication()
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
	 * Gets the client info object for this session. This method lazily gets the new agent info
	 * object for this session. It uses any cached or set ({@link #setClientInfo(ClientInfo)})
	 * client info object or uses {@link RequestCycle#newClientInfo()} to get the info object based
	 * on the current request when no client info object was set yet, and then caches the returned
	 * object; we can expect the client to stay the same for the whole session, and implementations
	 * of {@link RequestCycle#newClientInfo()} might be relatively expensive.
	 * 
	 * @return the client info object based on this request
	 */
	public ClientInfo getClientInfo()
	{
		if (clientInfo == null)
		{
			clientInfo = RequestCycle.get().newClientInfo();
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
	 * Gets feedback messages stored in session
	 * 
	 * @return unmodifiable list of feedback messages
	 */
	public final FeedbackMessages getFeedbackMessages()
	{
		return feedbackMessages;
	}

	/**
	 * Gets the unique id for this session from the underlying SessionStore. May be null if a
	 * concrete session is not yet created.
	 * 
	 * @return The unique id for this session or null if it is a temporary session
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
	 * @param <M>
	 *            The type of the metadata.
	 * @return The metadata
	 * @see MetaDataKey
	 */
	public final synchronized <M extends Serializable> M getMetaData(final MetaDataKey<M> key)
	{
		return key.get(metaData);
	}

	/**
	 * When a regular request on certain page with certain version is being processed, we don't
	 * allow ajax requests to same page and version.
	 * 
	 * @param lockedRequestCycle
	 * @return whether current request is valid or should be discarded
	 */
	protected boolean isCurrentRequestValid(RequestCycle lockedRequestCycle)
	{
		return true;
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL IT.
	 * 
	 * Returns the page with given id and versionNumber. It keeps asking pageMaps for given page
	 * until it finds one that contains it.
	 * 
	 * @param pageId
	 * @param versionNumber
	 * @return The page of that pageid and version, null if not found
	 */
	public final Page getPage(final int pageId, final int versionNumber)
	{
		if (Application.get().getSessionSettings().isPageIdUniquePerSession() == false)
		{
			throw new IllegalStateException(
				"To call this method ISessionSettings.setPageIdUniquePerSession must be set to true");
		}

		List<IPageMap> pageMaps = getPageMaps();

		for (IPageMap pageMap : pageMaps)
		{
			if (pageMap.containsPage(pageId, versionNumber))
			{
				return getPage(pageMap.getName(), "" + pageId, versionNumber);
			}
		}

		return null;
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL IT.
	 * 
	 * Get the page for the given path.
	 * 
	 * FIXME javadoc - where does it look? where does it get the page from?
	 * 
	 * @param pageMapName
	 *            The name of the page map where the page is
	 * @param componentPath
	 *            Component path
	 * @param versionNumber
	 *            The version of the page required
	 * @return The page based on the first path component (the page id), or null if the requested
	 *         version of the page cannot be found.
	 */
	public final Page getPage(final String pageMapName, final String componentPath,
		final int versionNumber)
	{
		if (log.isDebugEnabled())
		{
			log.debug("Getting page [path = " + componentPath + ", versionNumber = " +
				versionNumber + "]");
		}

		// Get page map by name, creating the default page map automatically
		IPageMap pageMap = pageMapForName(pageMapName, pageMapName == PageMap.DEFAULT_NAME);
		if (pageMap != null)
		{
			synchronized (usedPageMapNames) // get a lock so be sure that only one
			// is made
			{
				if (pageMapsUsedInRequest == null)
				{
					pageMapsUsedInRequest = new HashMap<IPageMap, PageMapsUsedInRequestEntry>(3);
				}
			}
			synchronized (pageMapsUsedInRequest)
			{
				long startTime = System.currentTimeMillis();

				// TODO For now only use the setting. Might be extended with
				// something overridable on request/ page/ request target level
				// later
				Duration timeout = Application.get().getRequestCycleSettings().getTimeout();

				PageMapsUsedInRequestEntry entry = pageMapsUsedInRequest.get(pageMap);

				// Get page entry for id and version
				Thread t = entry != null ? entry.thread : null;
				while (t != null && t != Thread.currentThread())
				{
					if (isCurrentRequestValid(entry.requestCycle) == false)
					{
						// we need to ignore this request. That's because it is
						// an ajax request
						// while regular page request is being processed
						throw new IgnoreAjaxRequestException();
					}

					try
					{
						pageMapsUsedInRequest.wait(timeout.getMilliseconds());
					}
					catch (InterruptedException ex)
					{
						throw new WicketRuntimeException(ex);
					}

					entry = pageMapsUsedInRequest.get(pageMap);
					t = entry != null ? entry.thread : null;

					if (t != null && t != Thread.currentThread() &&
						(startTime + timeout.getMilliseconds()) < System.currentTimeMillis())
					{
						AppendingStringBuffer asb = new AppendingStringBuffer(100);
						asb.append("After " + timeout + " the Pagemap " + pageMapName +
							" is still locked by: " + t +
							", giving up trying to get the page for path: " + componentPath);
						// if it is still not the right thread..
						// This either points to long running code (a report
						// page?) or a deadlock or such
						WicketRuntimeException ex = new WicketRuntimeException(asb.toString());
						ex.setStackTrace(t.getStackTrace());
						throw ex;
					}
				}

				PageMapsUsedInRequestEntry newEntry = new PageMapsUsedInRequestEntry();
				newEntry.thread = Thread.currentThread();
				newEntry.requestCycle = RequestCycle.get();
				pageMapsUsedInRequest.put(pageMap, newEntry);
				final String id = Strings.firstPathComponent(componentPath,
					Component.PATH_SEPARATOR);
				Page page = pageMap.get(Integer.parseInt(id), versionNumber);
				if (page == null)
				{
					pageMapsUsedInRequest.remove(pageMap);
					pageMapsUsedInRequest.notifyAll();
				}
				else
				{
					// attach the page now.
					page.onPageAttached();
					touch(page);
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
	 * @return A list of all PageMaps in this session.
	 */
	public final List<IPageMap> getPageMaps()
	{
		final List<IPageMap> list = new ArrayList<IPageMap>();
		for (String attribute : getAttributeNames())
		{
			if (attribute.startsWith(pageMapAttributePrefix))
			{
				list.add((IPageMap)getAttribute(attribute));
			}
		}

		// there is a small chance another thread removes the pagemap while we are iterating the
		// attributes and we end up with null in our list
		Iterator<IPageMap> maps = list.iterator();
		while (maps.hasNext())
		{
			if (maps.next() == null)
			{
				maps.remove();
			}
		}

		Collections.sort(list, new LruComparator());
		return list;
	}

	/**
	 * Sorting page maps respecting the least recently used sequence.
	 */
	private class LruComparator implements Comparator<IPageMap>
	{
		public int compare(IPageMap pg1, IPageMap pg2)
		{
			Integer pg1Index = usedPageMapNames.indexOf(pg1.getName());
			Integer pg2Index = usedPageMapNames.indexOf(pg2.getName());
			return pg1Index.compareTo(pg2Index);
		}
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
	 * Get the style (see {@link org.apache.wicket.Session}).
	 * 
	 * @return Returns the style (see {@link org.apache.wicket.Session})
	 */
	public final String getStyle()
	{
		return style;
	}

	/**
	 * Registers an informational feedback message for this session
	 * 
	 * @param message
	 *            The feedback message
	 */
	public final void info(final String message)
	{
		addFeedbackMessage(message, FeedbackMessage.INFO);
	}

	/**
	 * Invalidates this session at the end of the current request. If you need to invalidate the
	 * session immediately, you can do this by calling invalidateNow(), however this will remove all
	 * Wicket components from this session, which means that you will no longer be able to work with
	 * them.
	 */
	public void invalidate()
	{
		sessionInvalidated = true;
	}

	/**
	 * Invalidates this session immediately. Calling this method will remove all Wicket components
	 * from this session, which means that you will no longer be able to work with them.
	 */
	public void invalidateNow()
	{
		sessionInvalidated = true; // set this for isSessionInvalidated
		getSessionStore().invalidate(RequestCycle.get().getRequest());
	}


	/**
	 * Replaces the underlying (Web)Session, invalidating the current one and creating a new one. By
	 * calling {@link ISessionStore#invalidate(Request)} and {@link #bind()}
	 * <p>
	 * Call upon login to protect against session fixation.
	 * 
	 * @see "http://www.owasp.org/index.php/Session_Fixation"
	 */
	public void replaceSession()
	{
		getSessionStore().invalidate(RequestCycle.get().getRequest());
		bind();
	}

	/**
	 * Whether the session is invalid now, or will be invalidated by the end of the request. Clients
	 * should rarely need to use this method if ever.
	 * 
	 * @return Whether the session is invalid when the current request is done
	 * 
	 * @see #invalidate()
	 * @see #invalidateNow()
	 */
	public final boolean isSessionInvalidated()
	{
		return sessionInvalidated;
	}

	/**
	 * Whether this session is temporary. A Wicket application can operate in a session-less mode as
	 * long as stateless pages are used. If this session object is temporary, it will not be
	 * available on a next request.
	 * 
	 * @return Whether this session is temporary (which is the same as it's id being null)
	 */
	public final boolean isTemporary()
	{
		return getId() == null;
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
		// Check that session doesn't have too many page maps already, if so, evict
		final int maxPageMaps = getApplication().getSessionSettings().getMaxPageMaps();
		synchronized (usedPageMapNames)
		{
			List<IPageMap> usedPageMaps = getPageMaps();
			int excessPagemaps = (usedPageMaps.size() + 1) - maxPageMaps;/*
																		 * plus 1 meaning the new
																		 * one we are about to add
																		 */
			if (excessPagemaps > 0)
			{
				for (int i = 0; i < excessPagemaps; i++)
				{
					usedPageMaps.get(i).remove();
				}
			}
		}

		// Create new page map
		final IPageMap pageMap = getSessionStore().createPageMap(name);
		setAttribute(attributeForPageMapName(name), pageMap);
		// marking it as the most recently used
		dirtyPageMap(pageMap);
		dirty();
		return pageMap;
	}

	/**
	 * Gets a page map for the given name, automatically creating it if need be.
	 * 
	 * @param pageMapName
	 *            Name of page map, or null for default page map
	 * @param autoCreate
	 *            True if the page map should be automatically created if it does not exist
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
	 * @param pageMap
	 *            Page map to remove
	 */
	public final void removePageMap(final IPageMap pageMap)
	{
		PageMapAccessMetaData pagemapMetaData = getMetaData(PAGEMAP_ACCESS_MDK);
		if (pagemapMetaData != null)
		{
			pagemapMetaData.pageMapNames.remove(pageMap.getName());
		}

		synchronized (usedPageMapNames)
		{
			usedPageMapNames.remove(pageMap.getName());
		}

		// the page map also needs to be removed from the dirty objects list or
		// the requestDetached method will end up adding it back into session
		getDirtyObjectsList().remove(pageMap);

		removeAttribute(attributeForPageMapName(pageMap.getName()));
		dirty();
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
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL IT.
	 * <p>
	 * Sets the client info object for this session. This will only work when
	 * {@link #getClientInfo()} is not overridden.
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
	public void setLocale(final Locale locale)
	{
		if (locale == null)
		{
			throw new IllegalArgumentException("Argument 'locale' must not be null");
		}
		if (!Objects.equal(this.locale, locale))
		{
			dirty();
		}
		this.locale = locale;
	}

	/**
	 * Sets the metadata for this session using the given key. If the metadata object is not of the
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
	public final synchronized void setMetaData(final MetaDataKey<?> key, final Serializable object)
	{
		metaData = key.set(metaData, object);
		dirty();
	}

	/**
	 * Set the style (see {@link org.apache.wicket.Session}).
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
	 * The page will be 'touched' in the session. If it wasn't added yet to the pagemap, it will be
	 * added to the page map else it will set this page to the front.
	 * 
	 * If another page was removed because of this it will be cleaned up.
	 * 
	 * @param page
	 */
	public final void touch(Page page)
	{
		// store it in a list, so that the pages are really pushed
		// to the pagemap when the session does it update/detaches.
		// all the pages are then detached
		List<Page> lst = touchedPages.get();
		if (lst == null)
		{
			lst = new ArrayList<Page>();
			touchedPages.set(lst);
			lst.add(page);
		}
		else if (!lst.contains(page))
		{
			lst.add(page);
		}
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL IT.
	 * <p>
	 * This method will remove a page that was previously added via touch()
	 * 
	 * @param page
	 */
	public final void untouch(Page page)
	{
		List<Page> lst = touchedPages.get();
		if (lst != null)
		{
			lst.remove(page);
		}
	}

	/**
	 * @param visitor
	 *            The visitor to call at each Page in this PageMap.
	 */
	public final void visitPageMaps(final IPageMapVisitor visitor)
	{
		for (final Iterator<String> iterator = getAttributeNames().iterator(); iterator.hasNext();)
		{
			final String attribute = iterator.next();
			if (attribute.startsWith(pageMapAttributePrefix))
			{
				visitor.pageMap((IPageMap)getAttribute(attribute));
			}
		}
	}

	/**
	 * Registers a warning feedback message for this session
	 * 
	 * @param message
	 *            The feedback message
	 */
	public final void warn(final String message)
	{
		addFeedbackMessage(message, FeedbackMessage.WARNING);
	}

	/**
	 * Adds a feedback message to the list of messages
	 * 
	 * @param message
	 * @param level
	 * 
	 */
	private void addFeedbackMessage(String message, int level)
	{
		getFeedbackMessages().add(null, message, level);
		dirty();
	}

	/**
	 * @param pageMapName
	 *            Name of page map
	 * @return Session attribute holding page map
	 */
	private static final String attributeForPageMapName(final String pageMapName)
	{
		return pageMapAttributePrefix + pageMapName;
	}

	/**
	 * Any attach logic for session subclasses. Called when a session is set for the thread.
	 * 
	 * @deprecated will not be available in 1.5+
	 */
	@Deprecated
	protected void attach()
	{
	}

	/**
	 * Any detach logic for session subclasses. This is called on the end of handling a request,
	 * when the RequestCycle is about to be detached from the current thread.
	 */
	protected void detach()
	{
		// remove the session id in case a container like tomcat tries to be smart by doing
		// session fixation protection by changing the session id. this will simply be re-read
		// from the underlying httpsession when needed.
		id = null;
		if (sessionInvalidated)
		{
			invalidateNow();
		}
	}

	/**
	 * Marks session state as dirty so that it will be flushed at the end of the request.
	 */
	public final void dirty()
	{
		dirty = true;
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
		if (!isTemporary())
		{
			RequestCycle cycle = RequestCycle.get();
			if (cycle != null)
			{
				return getSessionStore().getAttribute(cycle.getRequest(), name);
			}
		}
		else
		{
			if (temporarySessionAttributes != null)
			{
				return temporarySessionAttributes.get(name);
			}
		}
		return null;
	}

	/**
	 * @return List of attributes for this session
	 */
	protected final List<String> getAttributeNames()
	{
		if (!isTemporary())
		{
			RequestCycle cycle = RequestCycle.get();
			if (cycle != null)
			{
				return getSessionStore().getAttributeNames(cycle.getRequest());
			}
		}
		else
		{
			if (temporarySessionAttributes != null)
			{
				return new ArrayList<String>(temporarySessionAttributes.keySet());
			}
		}
		return Collections.emptyList();
	}

	/**
	 * Gets the session store.
	 * 
	 * @return the session store
	 */
	protected ISessionStore getSessionStore()
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
		if (!isTemporary())
		{
			RequestCycle cycle = RequestCycle.get();
			if (cycle != null)
			{
				getSessionStore().removeAttribute(cycle.getRequest(), name);
			}
		}
		else
		{
			if (temporarySessionAttributes != null)
			{
				temporarySessionAttributes.remove(name);
			}
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
		if (!isTemporary())
		{
			RequestCycle cycle = RequestCycle.get();
			if (cycle == null)
			{
				throw new IllegalStateException(
					"Cannot set the attribute: no RequestCycle available.  If you get this error when using WicketTester.startPage(Page), make sure to call WicketTester.createRequestCycle() beforehand.");
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
						// this is a new instance. wherever it came from, bind
						// the session now
						store.bind(request, (Session)value);
					}
				}
			}

			// Set the actual attribute
			store.setAttribute(request, name, value);
		}
		else
		{
			// we don't have to synchronize, as it is impossible a temporary
			// session instance gets shared across threads
			if (temporarySessionAttributes == null)
			{
				temporarySessionAttributes = new HashMap<String, Object>(3);
			}
			temporarySessionAttributes.put(name, value);
		}
	}

	/**
	 * NOT TO BE CALLED BY FRAMEWORK USERS.
	 * 
	 * @deprecated obsolete method (was meant for internal book keeping really). Clients should
	 *             override {@link #detach()} instead.
	 */
	@Deprecated
	protected final void update()
	{
		throw new UnsupportedOperationException();
	}


	/**
	 * @param page
	 *            The page to add to dirty objects list
	 */
	void dirtyPage(final Page page)
	{
		List<IClusterable> dirtyObjects = getDirtyObjectsList();
		if (!dirtyObjects.contains(page))
		{
			dirtyObjects.add(page);
		}
	}

	/**
	 * @param map
	 *            The page map to add to dirty objects list
	 */
	void dirtyPageMap(final IPageMap map)
	{
		// see WICKET-3108 - removed page maps should not be added to dirtyObjects
		if (!getPageMaps().contains(map))
		{
			return;
		}

		synchronized (usedPageMapNames)
		{
			usedPageMapNames.remove(map.getName());
			usedPageMapNames.addLast(map.getName());
		}

		List<IClusterable> dirtyObjects = getDirtyObjectsList();
		if (!dirtyObjects.contains(map))
		{
			dirtyObjects.add(map);
		}
	}

	/**
	 * @return The current thread dirty objects list
	 */
	List<IClusterable> getDirtyObjectsList()
	{
		List<IClusterable> list = dirtyObjects.get();
		if (list == null)
		{
			list = new ArrayList<IClusterable>(4);
			dirtyObjects.set(list);
		}
		return list;
	}

	// TODO remove after deprecation release

	/**
	 * INTERNAL API. The request cycle when detached will call this.
	 * 
	 * FIXME javadoc - does what?
	 */
	final void requestDetached()
	{
		List<Page> touchedPages = Session.touchedPages.get();
		Session.touchedPages.set(null);
		if (touchedPages != null)
		{
			for (int i = 0; i < touchedPages.size(); i++)
			{
				try
				{
					Page page = touchedPages.get(i);
					// page must be detached before it gets stored
					page.detach();
					page.getPageMap().put(page);
				}
				catch (Throwable t)
				{
					// catch runtime and errors so that the next page is not detached/serialized and
					// the pagemap released. (see below)
					log.error("Exception when detaching/serializing page", t);
				}
				dirty = true;
			}
		}

		try
		{
			// If state is dirty
			if (dirty)
			{
				// State is no longer dirty
				dirty = false;

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

			List<IClusterable> dirtyObjects = Session.dirtyObjects.get();
			Session.dirtyObjects.set(null);

			Map<String, Object> tempMap = new HashMap<String, Object>();

			// Go through all dirty entries, replicating any dirty objects
			if (dirtyObjects != null)
			{
				for (final Iterator<IClusterable> iterator = dirtyObjects.iterator(); iterator.hasNext();)
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

					// we might override some attributes, so we use a temporary map
					// and then just copy the last values to real sesssion
					tempMap.put(attribute, object);
				}
			}

			// in case we have dirty attributes, set them to session
			if (tempMap.isEmpty() == false)
			{
				for (Entry<String, Object> entry : tempMap.entrySet())
				{
					setAttribute(entry.getKey(), entry.getValue());
				}
			}
		}
		finally
		{

			if (pageMapsUsedInRequest != null)
			{
				synchronized (pageMapsUsedInRequest)
				{
					Thread t = Thread.currentThread();
					Iterator<Entry<IPageMap, PageMapsUsedInRequestEntry>> it = pageMapsUsedInRequest.entrySet()
						.iterator();
					while (it.hasNext())
					{
						Entry<IPageMap, PageMapsUsedInRequestEntry> entry = it.next();
						if ((entry.getValue()).thread == t)
						{
							it.remove();
						}
					}
					pageMapsUsedInRequest.notifyAll();
				}
			}
		}
	}

	/**
	 * 
	 * @return the next page id
	 */
	synchronized protected int nextPageId()
	{
		return pageIdCounter++;
	}

	/**
	 * Retrieves the next available session-unique value
	 * 
	 * @return session-unique value
	 */
	public synchronized int nextSequenceValue()
	{
		return sequence++;
	}
}
