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
import wicket.session.ISessionStoreFactory;

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
	public abstract String getId(Request request);

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
		String sessionId = getId(request);
		ISessionStore sessionStore = (ISessionStore)sessionIdToSessionStore.get(sessionId);
		if (sessionStore == null)
		{
			ISessionStoreFactory sessionStoreFactory = Application.get().getSessionSettings()
					.getSessionStoreFactory();
			sessionStore = sessionStoreFactory.newSessionStore();
			if (sessionStore == null)
			{
				throw new IllegalStateException("factory " + sessionStoreFactory
						+ " did not produce a session store instance");
			}
			sessionIdToSessionStore.put(sessionId, sessionStore);
		}
		return sessionStore;
	}

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
}
