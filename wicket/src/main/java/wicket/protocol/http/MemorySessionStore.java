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
package wicket.protocol.http;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.AccessStackPageMap;
import wicket.IPageMap;
import wicket.Request;
import wicket.Session;
import wicket.session.ISessionStore;
import wicket.util.concurrent.ConcurrentHashMap;

/**
 * Session store that keeps attributes in memory instead of putting them in the
 * {@link HttpSession}. This should be a good match when you use <a
 * href="www.terracotta.org">Terracotta</a> to manage a cluster.
 * 
 * @author eelcohillenius
 */
public class MemorySessionStore extends AbstractHttpSessionStore
{
	/** Log. */
	private static final Log log = LogFactory.getLog(MemorySessionStore.class);

	/**
	 * Map of session ids to store objects.
	 */
	private final Map sessionIdToStore = new ConcurrentHashMap();

	/**
	 * Construct.
	 */
	public MemorySessionStore()
	{
	}

	/**
	 * @see wicket.session.ISessionStore#createPageMap(java.lang.String,
	 *      wicket.Session)
	 */
	public IPageMap createPageMap(String name, Session session)
	{
		return new AccessStackPageMap(name, session);
	}

	/**
	 * @see ISessionStore#getAttribute(Request, String)
	 */
	public Object getAttribute(Request request, String name)
	{
		Map store = getStore(request);
		return store.get(name);
	}

	/**
	 * @see ISessionStore#getAttributeNames(Request)
	 */
	public List getAttributeNames(Request request)
	{
		Map store = getStore(request);
		return new ArrayList(store.keySet());
	}

	/**
	 * @return The number of sessions.
	 */
	public int getNumberOfSessions()
	{
		return sessionIdToStore.size();
	}

	/**
	 * Gets the internal store (for integration purposes).
	 * 
	 * @return The internal store
	 */
	public Map getSessionIdToStore()
	{
		return sessionIdToStore;
	}

	/**
	 * @see wicket.protocol.http.AbstractHttpSessionStore#lookup(wicket.Request)
	 */
	public Session lookup(Request request)
	{
		Map store = getStoreUnsafe(request);
		if (store != null)
		{
			return (Session)store.get(Session.SESSION_ATTRIBUTE_NAME);
		}
		return null;
	}

	/**
	 * @see ISessionStore#removeAttribute(Request, String)
	 */
	public void removeAttribute(Request request, String name)
	{
		Map store = getStore(request);
		store.remove(name);
	}

	/**
	 * @see ISessionStore#setAttribute(Request, String, Object)
	 */
	public void setAttribute(Request request, String name, Object value)
	{
		Map store = getStore(request);
		store.put(name, (Serializable)value);
	}

	/**
	 * Gets the store for the session of the provided request, returning null
	 * and log a warning when the store was not found.
	 * 
	 * @param request
	 * 
	 * @return The store
	 */
	private final Map getStore(Request request)
	{
		String sessionId = getSessionId(request, true);
		Map store = (Map)sessionIdToStore.get(sessionId);
		if (store == null)
		{
			log.warn("no store found for session with id " + sessionId + " (request=" + request
					+ ")");
			// return a dummy
			return new HashMap();
		}
		return store;
	}

	/**
	 * Gets the store for the session of the provided request, returning null
	 * when the store was not found.
	 * 
	 * @param request
	 * 
	 * @return The store
	 */
	private final Map getStoreUnsafe(Request request)
	{
		String sessionId = getSessionId(request, true);
		return (Map)sessionIdToStore.get(sessionId);
	}

	/**
	 * @see AbstractHttpSessionStore#onBind(Request, Session)
	 */
	protected void onBind(Request request, Session newSession)
	{
		String sessionId = getSessionId(request, true);
		sessionIdToStore.put(sessionId, new HashMap());
		log.info("new session " + sessionId + " bound to session store");
	}

	/**
	 * @see AbstractHttpSessionStore#onUnbind(String)
	 */
	protected void onUnbind(String sessionId)
	{
		Map store = (Map)sessionIdToStore.remove(sessionId);
		log.info("session " + sessionId + " unbound from session store; cleaning up "
				+ store.size() + " entries");
	}
}