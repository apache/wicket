/*
 * $Id$ $Revision:
 * 1.1 $ $Date$
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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.http.HttpSession;

import wicket.Application;
import wicket.Session;

/**
 * Session subclass for HTTP protocol which holds an HttpSession object and
 * provides access to that object via getHttpSession(). A method which abstracts
 * session invalidation is also provided via invalidate().
 * 
 * @author Jonathan Locke
 */
public class WebSession extends Session
{
	/** Serial Version ID */
	private static final long serialVersionUID = -7738551549126761943L;

	/** The underlying HttpSession object */
	private transient javax.servlet.http.HttpSession httpSession;

	/** The attribute in the HttpSession where this WebSession object is stored */
	private transient String sessionAttributeName;

	/**
	 * Constructor
	 * 
	 * @param application
	 *            The application
	 */
	protected WebSession(final Application application)
	{
		super(application);
	}

	/**
	 * @return The underlying HttpSession object
	 */
	public HttpSession getHttpSession()
	{
		return httpSession;
	}

	/**
	 * Invalidates this session
	 */
	public void invalidate()
	{
		try
		{
			httpSession.invalidate();
		}
		catch (IllegalStateException e)
		{
			// Ignore
		}
	}

	/**
	 * @see Session#getAttribute(String)
	 */
	protected Object getAttribute(final String name)
	{
		return httpSession.getAttribute(sessionAttributeName + "-" + name);
	}

	/**
	 * @see Session#getAttributeNames()
	 */
	protected List getAttributeNames()
	{
		final List list = new ArrayList();
		final Enumeration names = httpSession.getAttributeNames();
		final String prefix = sessionAttributeName + "-";
		while (names.hasMoreElements())
		{
			final String name = (String)names.nextElement();
			if (name.startsWith(prefix))
			{
				list.add(name.substring(prefix.length()));
			}
		}
		return list;
	}

	/**
	 * @see wicket.Session#removeAttribute(java.lang.String)
	 */
	protected void removeAttribute(final String name)
	{
		httpSession.removeAttribute(sessionAttributeName + "-" + name);
	}

	/**
	 * @see Session#setAttribute(String, Object)
	 */
	protected void setAttribute(final String name, final Object object)
	{
		httpSession.setAttribute(sessionAttributeName + "-" + name, object);
	}

	/**
	 * Initializes this session for a request
	 * 
	 * @param httpSession
	 *            The http session to attach
	 * @param sessionAttributeName
	 *            The session attribute name
	 */
	final void init(final HttpSession httpSession, final String sessionAttributeName)
	{
		// Set session attribute name
		this.sessionAttributeName = sessionAttributeName;

		// Attach / reattach http servlet session
		this.httpSession = httpSession;
		
		// Set the current session to the session we just retrieved
		set(this);
	}
}
