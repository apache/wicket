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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.wicket.application.IClassResolver;
import org.apache.wicket.authorization.IAuthorizationStrategy;
import org.apache.wicket.core.request.ClientInfo;
import org.apache.wicket.core.util.lang.WicketObjects;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.event.IEventSink;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.feedback.FeedbackMessages;
import org.apache.wicket.page.IPageManager;
import org.apache.wicket.page.PageAccessSynchronizer;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.session.ISessionStore;
import org.apache.wicket.settings.IApplicationSettings;
import org.apache.wicket.util.IProvider;
import org.apache.wicket.util.LazyInitializer;
import org.apache.wicket.util.io.IClusterable;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.lang.Objects;
import org.apache.wicket.util.tester.BaseWicketTester;
import org.apache.wicket.util.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Holds information about a user session, including some fixed number of most recent pages (and all
 * their nested component information).
 * <ul>
 * <li><b>Access</b> - the Session can be retrieved either by {@link Component#getSession()}
 * or by directly calling the static method Session.get(). All classes which extend directly or indirectly
 * {@link org.apache.wicket.markup.html.WebMarkupContainer} can also use its convenience method
 * {@link org.apache.wicket.markup.html.WebMarkupContainer#getWebSession()}
 * 
 * <li><b>Locale</b> - A session has a Locale property to support localization. The Locale for a
 * session can be set by calling {@link Session#setLocale(Locale)}. The Locale for a Session
 * determines how localized resources are found and loaded.
 * 
 * <li><b>Style</b> - Besides having an appearance based on locale, resources can also have
 * different looks in the same locale (a.k.a. "skins"). The style for a session determines the look
 * which is used within the appropriate locale. The session style ("skin") can be set with the
 * setStyle() method.
 * 
 * <li><b>Resource Loading</b> - Based on the Session locale and style, searching for resources
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
 * <li><b>Session Properties</b> - Arbitrary objects can be attached to a Session by installing a
 * session factory on your Application class which creates custom Session subclasses that have
 * typesafe properties specific to the application (see {@link Application} for details). To
 * discourage non-typesafe access to Session properties, no setProperty() or getProperty() method is
 * provided. In a clustered environment, you should take care to call the dirty() method when you
 * change a property on your own. This way the session will be reset again in the http session so
 * that the http session knows the session is changed.
 * 
 * <li><b>Class Resolver</b> - Sessions have a class resolver ( {@link IClassResolver})
 * implementation that is used to locate classes for components such as pages.
 * 
 * <li><b>Page Factory</b> - A pluggable implementation of {@link IPageFactory} is used to
 * instantiate pages for the session.
 * 
 * <li><b>Removal</b> - Pages can be removed from the Session forcibly by calling clear(),
 * although such an action should rarely be necessary.
 * 
 * <li><b>Flash Messages</b> - Flash messages are messages that are stored in session and are removed
 * after they are displayed to the user. Session acts as a store for these messages because they can
 * last across requests.
 * 
 * @author Jonathan Locke
 * @author Eelco Hillenius
 * @author Igor Vaynberg (ivaynberg)
 */
public abstract class Session implements IClusterable, IEventSink
{
	private static final long serialVersionUID = 1L;

	/** Logging object */
	private static final Logger log = LoggerFactory.getLogger(Session.class);

	/** Name of session attribute under which this session is stored */
	public static final String SESSION_ATTRIBUTE_NAME = "session";

	/** a sequence used for whenever something session-specific needs a unique value */
	private final AtomicInteger sequence = new AtomicInteger(1);

	/** a sequence used for generating page IDs */
	private final AtomicInteger pageId = new AtomicInteger(0);

	/** synchronize page's access by session */
	private final IProvider<PageAccessSynchronizer> pageAccessSynchronizer;

	/**
	 * Checks existence of a <code>Session</code> associated with the current thread.
	 * 
	 * @return {@code true} if {@link Session#get()} can return the instance of session,
	 *         {@code false} otherwise
	 */
	public static boolean exists()
	{
		Session session = ThreadContext.getSession();

		if (session == null)
		{
			// no session is available via ThreadContext, so lookup in session store
			RequestCycle requestCycle = RequestCycle.get();
			if (requestCycle != null)
			{
				session = Application.get().getSessionStore().lookup(requestCycle.getRequest());
				if (session != null)
				{
					ThreadContext.setSession(session);
				}
			}
		}
		return session != null;
	}

	/**
	 * Returns session associated to current thread. Always returns a session during a request
	 * cycle, even though the session might be temporary
	 * 
	 * @return session.
	 */
	public static Session get()
	{
		Session session = ThreadContext.getSession();
		if (session != null)
		{
			return session;
		}
		else
		{
			return Application.get().fetchCreateAndSetSession(RequestCycle.get());
		}
	}

	/**
	 * Cached instance of agent info which is typically designated by calling
	 * {@link Session#getClientInfo()}.
	 */
	protected ClientInfo clientInfo;

	/** True if session state has been changed */
	private transient volatile boolean dirty = false;

	/** feedback messages */
	private final FeedbackMessages feedbackMessages = new FeedbackMessages();

	/** cached id because you can't access the id after session unbound */
	private String id = null;

	/** The locale to use when loading resources for this session. */
	private final AtomicReference<Locale> locale;

	/** Application level meta data. */
	private MetaDataEntry<?>[] metaData;

	/** True, if session has been invalidated */
	private transient volatile boolean sessionInvalidated = false;

	/**
	 * Temporary instance of the session store. Should be set on each request as it is not supposed
	 * to go in the session.
	 */
	private transient ISessionStore sessionStore;

	/** Any special "skin" style to use when loading resources. */
	private final AtomicReference<String> style = new AtomicReference<String>();

	/**
	 * Holds attributes for sessions that are still temporary/ not bound to a session store. Only
	 * used when {@link #isTemporary()} is true.
	 * <p>
	 * Note: this doesn't have to be synchronized, as the only time when this map is used is when a
	 * session is temporary, in which case it won't be shared between requests (it's a per request
	 * instance).
	 * </p>
	 */
	private transient Map<String, Serializable> temporarySessionAttributes;

	/**
	 * Constructor. Note that {@link RequestCycle} is not available until this constructor returns.
	 * 
	 * @param request
	 *            The current request
	 */
	public Session(Request request)
	{
		Locale locale = request.getLocale();
		if (locale == null)
		{
			throw new IllegalStateException(
				"Request#getLocale() cannot return null, request has to have a locale set on it");
		}
		this.locale = new AtomicReference<Locale>(locale);

		pageAccessSynchronizer = new PageAccessSynchronizerProvider();
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
				for (Map.Entry<String, Serializable> entry : temporarySessionAttributes.entrySet())
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
	 * 
	 * @deprecated see
	 *             {@link IApplicationSettings#setFeedbackMessageCleanupFilter(org.apache.wicket.feedback.IFeedbackMessageFilter)}
	 *             for cleanup during testing see {@link BaseWicketTester#cleanupFeedbackMessages()}
	 */
	@Deprecated
	public final void cleanupFeedbackMessages()
	{
		throw new UnsupportedOperationException("Deprecated, see the javadoc");
	}

	/**
	 * Removes all pages from the session. Although this method should rarely be needed, it is
	 * available (possibly for security reasons).
	 */
	public final void clear()
	{
		if (isTemporary() == false)
		{
			getPageManager().sessionExpired(getId());
		}
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
	 * Registers an fatal feedback message for this session
	 * 
	 * @param message
	 *            The feedback message
	 */
	public final void fatal(final Serializable message)
	{
		addFeedbackMessage(message, FeedbackMessage.FATAL);
	}

	/**
	 * Registers an debug feedback message for this session
	 * 
	 * @param message
	 *            The feedback message
	 */
	public final void debug(final Serializable message)
	{
		addFeedbackMessage(message, FeedbackMessage.DEBUG);
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
	 * client info object.
	 * 
	 * @return the client info object based on this request
	 */
	public abstract ClientInfo getClientInfo();

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
	 * Gets the unique id for this session from the underlying SessionStore. May be
	 * <code>null</code> if a concrete session is not yet created.
	 * 
	 * @return The unique id for this session or null if it is a temporary session
	 */
	public final String getId()
	{
		if (id == null)
		{
			updateId();

			// we have one?
			if (id != null)
			{
				dirty();
			}
		}
		return id;
	}

	private void updateId()
	{
		RequestCycle requestCycle = RequestCycle.get();
		if (requestCycle != null)
		{
			id = getSessionStore().getSessionId(requestCycle.getRequest(), false);
		}
	}

	/**
	 * Get this session's locale.
	 * 
	 * @return This session's locale
	 */
	public Locale getLocale()
	{
		return locale.get();
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
	public synchronized final <M extends Serializable> M getMetaData(final MetaDataKey<M> key)
	{
		return key.get(metaData);
	}

	/**
	 * When a regular request on certain page with certain version is being processed, we don't
	 * allow ajax requests to same page and version.
	 *
	 * @param lockedRequestCycle
	 * @return whether current request is valid or should be discarded
	 * @deprecated Not used since Wicket 1.5.0
	 */
	@Deprecated
	protected boolean isCurrentRequestValid(RequestCycle lockedRequestCycle)
	{
		return true;
	}

	/**
	 * @return The page factory for this session
	 */
	public IPageFactory getPageFactory()
	{
		return getApplication().getPageFactory();
	}

	/**
	 * @return Size of this session
	 */
	public final long getSizeInBytes()
	{
		return WicketObjects.sizeof(this);
	}

	/**
	 * Get the style (see {@link org.apache.wicket.Session}).
	 * 
	 * @return Returns the style (see {@link org.apache.wicket.Session})
	 */
	public final String getStyle()
	{
		return style.get();
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
	 * Registers an success feedback message for this session
	 * 
	 * @param message
	 *            The feedback message
	 */
	public final void success(final Serializable message)
	{
		addFeedbackMessage(message, FeedbackMessage.SUCCESS);
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
	 * Invalidate and remove session store and page manager
	 */
	private void destroy()
	{
		if (sessionStore != null)
		{
			sessionStore.invalidate(RequestCycle.get().getRequest());
			sessionStore = null;
		}
	}

	/**
	 * Invalidates this session immediately. Calling this method will remove all Wicket components
	 * from this session, which means that you will no longer be able to work with them.
	 */
	public void invalidateNow()
	{
		invalidate();
		destroy();
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
		destroy();
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
		Args.notNull(locale, "locale");

		if (!Objects.equal(getLocale(), locale))
		{
			this.locale.set(locale);
			dirty();
		}
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
		if (!Objects.equal(getStyle(), style))
		{
			this.style.set(style);
			dirty();
		}
		return this;
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
	 * Any detach logic for session subclasses. This is called on the end of handling a request,
	 * when the RequestCycle is about to be detached from the current thread.
	 */
	public void detach()
	{
		detachFeedback();

		if (sessionInvalidated)
		{
			invalidateNow();
		}
		else
		{
			// WICKET-5103 container might have changed id
			updateId();
		}
	}

	private void detachFeedback()
	{
		final int removed = feedbackMessages.clear(getApplication().getApplicationSettings()
			.getFeedbackMessageCleanupFilter());

		if (removed != 0)
		{
			dirty();
		}

		feedbackMessages.detach();
	}

	/**
	 * NOT PART OF PUBLIC API, DO NOT CALL
	 * 
	 * Detaches internal state of {@link Session}
	 */
	public void internalDetach()
	{
		if (dirty)
		{
			Request request = RequestCycle.get().getRequest();
			getSessionStore().flushSession(request, this);
		}
		dirty = false;
	}

	/**
	 * Marks session state as dirty so that it will be (re)stored in the ISessionStore
	 * at the end of the request.
	 * <strong>Note</strong>: binds the session if it is temporary
	 */
	public final void dirty()
	{
		dirty(true);
	}

	/**
	 * Marks session state as dirty so that it will be re-stored in the ISessionStore
	 * at the end of the request.
	 *
	 * @param forced
	 *          A flag indicating whether the session should be marked as dirty even
	 *          when it is temporary. If {@code true} the Session will be bound.
	 */
	public final void dirty(boolean forced)
	{
		if (isTemporary())
		{
			if (forced)
			{
				dirty = true;
			}
		}
		else
		{
			dirty = true;
		}
	}

	/**
	 * Gets the attribute value with the given name
	 * 
	 * @param name
	 *            The name of the attribute to store
	 * @return The value of the attribute
	 */
	public final Serializable getAttribute(final String name)
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
	public final List<String> getAttributeNames()
	{
		if (!isTemporary())
		{
			RequestCycle cycle = RequestCycle.get();
			if (cycle != null)
			{
				return Collections.unmodifiableList(getSessionStore().getAttributeNames(
					cycle.getRequest()));
			}
		}
		else
		{
			if (temporarySessionAttributes != null)
			{
				return Collections.unmodifiableList(new ArrayList<String>(
					temporarySessionAttributes.keySet()));
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
	public final void removeAttribute(String name)
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
	public final void setAttribute(String name, Serializable value)
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
				temporarySessionAttributes = new HashMap<String, Serializable>(3);
			}
			temporarySessionAttributes.put(name, value);
		}
	}

	/**
	 * Retrieves the next available session-unique value
	 * 
	 * @return session-unique value
	 */
	public int nextSequenceValue()
	{
		dirty(false);
		return sequence.getAndIncrement();
	}

	/**
	 * 
	 * @return the next page id
	 */
	public int nextPageId()
	{
		dirty(false);
		return pageId.getAndIncrement();
	}

	/**
	 * Returns the {@link IPageManager} instance.
	 * 
	 * @return {@link IPageManager} instance.
	 */
	public final IPageManager getPageManager()
	{
		IPageManager pageManager = Application.get().internalGetPageManager();
		return pageAccessSynchronizer.get().adapt(pageManager);
	}

	/** {@inheritDoc} */
	@Override
	public void onEvent(IEvent<?> event)
	{
	}

	/**
	 * A callback method that is executed when the user session is invalidated
	 * either by explicit call to {@link org.apache.wicket.Session#invalidate()}
	 * or due to HttpSession expiration.
	 *
	 * <p>In case of session expiration this method is called in a non-worker thread, i.e.
	 * there are no thread locals exported for the Application, RequestCycle and Session.
	 * The Session is the current instance. The Application can be found by using
	 * {@link Application#get(String)}. There is no way to get a reference to a RequestCycle</p>
	 */
	public void onInvalidate()
	{
	}

	private static final class PageAccessSynchronizerProvider extends
		LazyInitializer<PageAccessSynchronizer>
	{
		private static final long serialVersionUID = 1L;

		@Override
		protected PageAccessSynchronizer createInstance()
		{
			final Duration timeout;
			if (Application.exists())
			{
				timeout = Application.get().getRequestCycleSettings().getTimeout();
			}
			else
			{
				timeout = Duration.minutes(1);
				log.warn(
					"PageAccessSynchronizer created outside of application thread, using default timeout: {}",
					timeout);
			}
			return new PageAccessSynchronizer(timeout);
		}
	}

}
