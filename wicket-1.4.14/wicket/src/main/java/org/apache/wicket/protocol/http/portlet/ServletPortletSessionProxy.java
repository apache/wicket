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
package org.apache.wicket.protocol.http.portlet;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.NoSuchElementException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Proxy for a Servlet HttpSession to attach to a PortletSession, providing only access to
 * PORTLET_SCOPE session attributes and hiding the APPLICATION_SCOPE attributes from the Servlet. <br/>
 * This Proxy can be used to isolate two instances of the same Portlet dispatching to Servlets so
 * they don't overwrite or read each others session attributes. <br/>
 * Caveat: APPLICATION_SCOPE sessions attributes cannot be used anymore (directly) for inter-portlet
 * communication, or when using Servlets directly which also need to "attach" to the PORTLET_SCOPE
 * session attributes.<br/>
 * The {@link org.apache.portals.bridges.util.PortletWindowUtils} class can help out with that
 * though. <br/>
 * Note: copied and adapted from the Apache Portal Bridges Common project
 * 
 * @author <a href="mailto:ate@douma.nu">Ate Douma</a>
 * @version $Id$
 * 
 */
public class ServletPortletSessionProxy implements InvocationHandler
{
	HttpSession servletSession;
	String portletWindowPrefix;

	@SuppressWarnings("unchecked")
	public static HttpSession createProxy(HttpServletRequest request, String portletWindowId)
	{
		String portletWindowNamespace = "javax.portlet.p." + portletWindowId;
		HttpSession servletSession = request.getSession();
		HashSet interfaces = new HashSet();
		interfaces.add(HttpSession.class);
		Class current = servletSession.getClass();
		while (current != null)
		{
			try
			{
				Class[] currentInterfaces = current.getInterfaces();
				for (int i = 0; i < currentInterfaces.length; i++)
				{
					interfaces.add(currentInterfaces[i]);
				}
				current = current.getSuperclass();
			}
			catch (Exception e)
			{
				current = null;
			}
		}
		Object proxy = Proxy.newProxyInstance(servletSession.getClass().getClassLoader(),
			(Class[])interfaces.toArray(new Class[interfaces.size()]),
			new ServletPortletSessionProxy(request.getSession(), portletWindowNamespace));
		return (HttpSession)proxy;
	}

	private ServletPortletSessionProxy(HttpSession servletSession, String portletWindowPrefix)
	{
		this.servletSession = servletSession;
		this.portletWindowPrefix = portletWindowPrefix;
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method,
	 *      java.lang.Object[])
	 */
	@SuppressWarnings("unchecked")
	public Object invoke(Object proxy, Method m, Object[] args) throws Throwable
	{
		Object retval = null;
		if (("getAttribute".equals(m.getName()) || "getValue".equals(m.getName())) &&
			args.length == 1 && args[0] instanceof String)
		{
			retval = servletSession.getAttribute(portletWindowPrefix + (String)args[0]);
		}
		else if (("setAttribute".equals(m.getName()) || "putValue".equals(m.getName())) &&
			args.length == 2 && args[0] instanceof String)
		{
			servletSession.setAttribute(portletWindowPrefix + (String)args[0], args[1]);
		}
		else if (("removeAttribute".equals(m.getName()) || "removeValue".equals(m.getName())) &&
			args.length == 1 && args[0] instanceof String)
		{
			servletSession.removeAttribute(portletWindowPrefix + (String)args[0]);
		}
		else if ("getAttributeNames".equals(m.getName()) && args == null)
		{
			retval = new NamespacedNamesEnumeration(servletSession.getAttributeNames(),
				portletWindowPrefix);
		}
		else if ("getValueNames".equals(m.getName()) && args == null)
		{
			ArrayList list = new ArrayList();
			Enumeration e = new NamespacedNamesEnumeration(servletSession.getAttributeNames(),
				portletWindowPrefix);
			while (e.hasMoreElements())
			{
				list.add(e.nextElement());
			}
			retval = list.toArray(new String[list.size()]);
		}
		else
		{
			retval = m.invoke(servletSession, args);
		}
		return retval;
	}

	@SuppressWarnings("unchecked")
	private static class NamespacedNamesEnumeration implements Enumeration
	{
		private final Enumeration namesEnumeration;
		private final String namespace;

		private String nextName;
		private boolean done;

		public NamespacedNamesEnumeration(Enumeration namesEnumeration, String namespace)
		{
			this.namesEnumeration = namesEnumeration;
			this.namespace = namespace;
			hasMoreElements();
		}

		public boolean hasMoreElements()
		{
			if (!done)
			{
				if (nextName == null)
				{
					while (namesEnumeration.hasMoreElements())
					{
						String name = (String)namesEnumeration.nextElement();
						if (name.startsWith(namespace))
						{
							nextName = name.substring(namespace.length());
							break;
						}
					}
					done = nextName == null;
				}
			}
			return !done;
		}

		public Object nextElement()
		{
			if (done)
			{
				throw new NoSuchElementException();
			}
			String name = nextName;
			nextName = null;
			return name;
		}
	}
}
