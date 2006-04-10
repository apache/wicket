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
package wicket.protocol.http;

import javax.servlet.http.HttpSession;

import wicket.Application;
import wicket.Request;
import wicket.Session;
import wicket.session.ISessionStore;

/**
 * Session facade that works with {@link HttpSession}s.
 * 
 * @author Eelco Hillenius
 */
public final class HttpSessionFacade extends AbstractHttpSessionFacade
{
	private static final long serialVersionUID = 1L;

	/**
	 * @see wicket.protocol.http.AbstractHttpSessionFacade#onBind(wicket.Request,
	 *      wicket.Session)
	 */
	protected final void onBind(Request request, Session newSession)
	{
		WebRequest webRequest = toWebRequest(request);
		String sessionObjectAttribute = getSessionObjectAttribute(webRequest);
		ISessionStore store = getSessionStore(request);
		store.setAttribute(sessionObjectAttribute, newSession);
	}

	/**
	 * @see wicket.SessionFacade#lookup(wicket.Request)
	 */
	protected final Session lookup(Request request)
	{
		WebRequest webRequest = toWebRequest(request);
		String sessionObjectAttribute = getSessionObjectAttribute(webRequest);
		ISessionStore store = getSessionStore(request);
		return (Session)store.getAttribute(sessionObjectAttribute);
	}

	/**
	 * @see wicket.SessionFacade#newSessionStore(java.lang.String)
	 */
	protected final ISessionStore newSessionStore(String sessionId)
	{
		return new HttpSessionStore();
	}

	/**
	 * Gets the prefix for storing variables in the actual session (typically
	 * {@link HttpSession} for this application instance.
	 * 
	 * @param request
	 *            the request
	 * 
	 * @return the prefix for storing variables in the actual session
	 */
	protected final String getSessionAttributePrefix(final WebRequest request)
	{
		WebApplication application = (WebApplication)Application.get();
		return application.getSessionAttributePrefix(request);
	}

	/**
	 * Gets the attribute key for the session object.
	 * 
	 * @param webRequest
	 *            The web request
	 * @return The attribute key
	 */
	protected final String getSessionObjectAttribute(WebRequest webRequest)
	{
		String sessionObjectAttribute = getSessionAttributePrefix(webRequest)
				+ Session.SESSION_ATTRIBUTE_NAME;
		return sessionObjectAttribute;
	}
}
