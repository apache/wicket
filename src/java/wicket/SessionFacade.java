/*
 * $Id: org.eclipse.jdt.ui.prefs 5004 2006-03-17 20:47:08 -0800 (Fri, 17 Mar
 * 2006) eelco12 $ $Revision: 5004 $ $Date: 2006-03-17 20:47:08 -0800 (Fri, 17
 * Mar 2006) $
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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import wicket.session.ISessionStore;

/**
 * The session facade hides where where the session and session store come from.
 * The main reason for the existance of this class is to enable having a session
 * object that is stored in the session, and a session store object that is NOT
 * stored in the session. It is not a class typically used by framework clients.
 * 
 * @author Eelco Hillenius
 */
public abstract class SessionFacade
{
	/**
	 * Map of session ids to session stores.
	 */
	private Map sessionIdToSessionStore = Collections.synchronizedMap(new HashMap());

	/**
	 * Adds the provided new session to this facade using the provided request.
	 * 
	 * @param request
	 *            The request that triggered making a new sesion
	 * @param newSession
	 *            The new session
	 */
	public abstract void bind(Request request, Session newSession);

	/**
	 * Get the session id for the provided request.
	 * 
	 * @param request
	 *            The request
	 * @return The session id for the provided request
	 */
	public abstract String getSessionId(Request request);

	/**
	 * Retrieves the session for the provided request from this facade.
	 * 
	 * @param request
	 *            The request
	 * @return The session for the provided request.
	 */
	public final Session getSession(Request request)
	{
		if (request == null)
		{
			throw new IllegalArgumentException("request must be not null");
		}

		Session session = lookup(request);

		return session;
	}

	/**
	 * Gets the session store for the given request.
	 * 
	 * @param request
	 *            The request
	 * @return An instance of {@link ISessionStore}
	 */
	public final ISessionStore getSessionStore(Request request)
	{
		String sessionId = getSessionId(request);
		return getSessionStore(sessionId);
	}

	/**
	 * Creates a new instance of {@link ISessionStore} for the session with the
	 * provided id.
	 * 
	 * @param sessionId
	 *            The id of the session to create a new session store for
	 * @return A new session store instance
	 */
	protected abstract ISessionStore newSessionStore(String sessionId);

	/**
	 * Retrieves the session for the provided request from this facade.
	 * 
	 * @param request
	 *            The current request
	 * @return The session for the provided request. The contract is to never
	 *         return null. If it is somehow not possible to retrieve a session
	 *         object for the provided request, implementations should throw an
	 *         {@link IllegalArgumentException}
	 */
	protected abstract Session lookup(Request request);

	/**
	 * Unbinds the session with the provided session id.
	 * <p>
	 * <strong>It is the full responsibility of subclasses of the session facade
	 * to call the unbind method.
	 * </p>
	 * 
	 * @param applicationKey
	 *            The unique key of the application within this web application
	 * @param sessionId
	 *            The id of the session to be unbinded
	 */
	protected final void unbind(String applicationKey, String sessionId)
	{
		onUnbind(applicationKey, sessionId);
		ISessionStore sessionStore = getSessionStore(sessionId);
		sessionStore.destroy();
	}

	/**
	 * Template method that is called when the session with the provided session
	 * id is unbinded.
	 * 
	 * @param applicationKey
	 *            The unique key of the application within this web application
	 * @param sessionId
	 *            The id of the session to be unbinded
	 */
	protected void onUnbind(String applicationKey, String sessionId)
	{
	}

	/**
	 * Gets the session store for the session with the given session id.
	 * 
	 * @param sessionId
	 *            The id of the session
	 * @return The session store
	 */
	private final ISessionStore getSessionStore(String sessionId)
	{
		ISessionStore sessionStore = (ISessionStore)sessionIdToSessionStore.get(sessionId);
		if (sessionStore == null)
		{
			sessionStore = newSessionStore(sessionId);
			if (sessionStore == null)
			{
				throw new IllegalStateException("session facade " + getClass().getName()
						+ " did not produce a session store instance");
			}
			sessionIdToSessionStore.put(sessionId, sessionStore);
		}
		return sessionStore;
	}
}
