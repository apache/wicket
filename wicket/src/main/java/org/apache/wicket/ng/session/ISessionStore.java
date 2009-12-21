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
package org.apache.wicket.ng.session;

import java.io.Serializable;
import java.util.Set;

import javax.servlet.http.HttpSession;

import org.apache.wicket.Request;
import org.apache.wicket.Session;


/**
 * The actual store that is used by {@link org.apache.wicket.ng.Session} to store its attributes.
 * <p>
 * This class is intended for internal framework use.
 * 
 * @author Eelco Hillenius
 * @author Johan Compagner
 * @author Matej Knopp
 */
public interface ISessionStore
{
	/**
	 * Gets the attribute value with the given name
	 * 
	 * @param request
	 *            the current request
	 * @param name
	 *            The name of the attribute to store
	 * @return The value of the attribute
	 */
	Serializable getAttribute(Request request, final String name);

	/**
	 * @param request
	 *            the current request
	 * 
	 * @return List of attributes for this session
	 */
	Set<String> getAttributeNames(Request request);

	/**
	 * Adds or replaces the attribute with the given name and value.
	 * 
	 * @param request
	 *            the current request
	 * @param name
	 *            the name of the attribute
	 * @param value
	 *            the value of the attribute
	 */
	void setAttribute(Request request, String name, Serializable value);

	/**
	 * Removes the attribute with the given name.
	 * 
	 * @param request
	 *            the current request
	 * @param name
	 *            the name of the attribute to remove
	 */
	void removeAttribute(Request request, String name);

	/**
	 * Invalidates the session.
	 * 
	 * @param request
	 *            the current request
	 */
	void invalidate(Request request);

	/**
	 * Get the session id for the provided request. If create is false and the creation of the
	 * actual session is deferred, this method should return null to reflect it doesn't have one.
	 * 
	 * @param request
	 *            The request
	 * @param create
	 *            Whether to create an actual session (typically an instance of {@link HttpSession})
	 *            when not already done so
	 * @return The session id for the provided request, possibly null if create is false and the
	 *         creation of the actual session was deferred
	 */
	String getSessionId(Request request, boolean create);

	/**
	 * Retrieves the session for the provided request from this facade.
	 * <p>
	 * This method should return null if it is not bound yet, so that Wicket can recognize that it
	 * should create a session and call {@link #bind(Request, Session)} right after that.
	 * </p>
	 * 
	 * @param request
	 *            The current request
	 * @return The session for the provided request or null if the session was not bound
	 */
	Session lookup(Request request);

	/**
	 * Adds the provided new session to this facade using the provided request.
	 * 
	 * @param request
	 *            The request that triggered making a new session
	 * @param newSession
	 *            The new session
	 */
	void bind(Request request, Session newSession);

	/**
	 * Called when the WebApplication is destroyed.
	 */
	void destroy();

	/**
	 * Listener invoked when session is unbound.
	 * 
	 * @author Matej Knopp
	 */
	public interface UnboundListener
	{
		/**
		 * Informs the listener that session with specifid id has been unbound.
		 * 
		 * @param sessionId
		 */
		public void sessionUnbound(String sessionId);
	};

	/**
	 * Registers listener invoked when session is unbound.
	 * 
	 * @param listener
	 */
	public void registerUnboundListener(UnboundListener listener);

	/**
	 * Unregisters listener invoked when session is unbound.
	 * 
	 * @param listener
	 */
	public void unregisterUnboundListener(UnboundListener listener);
}
