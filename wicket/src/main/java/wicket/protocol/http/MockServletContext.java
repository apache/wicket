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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.Application;
import wicket.WicketRuntimeException;
import wicket.util.value.ValueMap;

/**
 * Mock implementation of the servlet context for testing purposes. This
 * implementation supports all of the standard context methods except that
 * request dispatching just indicates what is being dispatched to, rather than
 * doing the actual dispatch.
 * <p>
 * The context can be configured with a path parameter that should point to an
 * absolute directory location that represents the place where the contents of
 * the WAR bundle are located. Setting this value allows all of the resource
 * location functionality to work as in a fully functioning web application.
 * This value is not set then not resource location functionality will work and
 * instead null will always be returned.
 * 
 * @author Chris Turner
 */
public class MockServletContext implements ServletContext
{
	private static final Log log = LogFactory.getLog(MockServletContext.class);

	private Application application;

	private Map<String, Object> attributes = new HashMap<String, Object>();

	private ValueMap initParameters = new ValueMap();

	/** Map of mime types */
	private final ValueMap mimeTypes = new ValueMap();

	private File webappRoot;

	/**
	 * Create the mock object. As part of the creation, the context ets the root
	 * directory where web application content is stored. This must be an
	 * ABSOLUTE directory relative to where the tests are being executed. For
	 * example: <code>System.getProperty("user.dir") +
	 * "/src/webapp"</code>
	 * 
	 * @param application
	 *            The application that this context is for
	 * @param path
	 *            The path to the root of the web application
	 */
	public MockServletContext(final Application application, final String path)
	{
		this.application = application;

		webappRoot = null;
		if (path != null)
		{
			webappRoot = new File(path);
			if (!webappRoot.exists() || !webappRoot.isDirectory())
			{
				log.warn("WARNING: The webapp root directory is invalid: " + path);
				webappRoot = null;
			}
		}

		mimeTypes.put("html", "text/html");
		mimeTypes.put("htm", "text/html");
		mimeTypes.put("css", "text/css");
		mimeTypes.put("xml", "text/xml");
		mimeTypes.put("js", "text/plain");
		mimeTypes.put("gif", "image/gif");
		mimeTypes.put("jpg", "image/jpeg");
		mimeTypes.put("png", "image/png");

		// Set ServletContext temp dir
		try
		{
			File tmpDir = File.createTempFile("wicket", null);
			tmpDir.delete();
			tmpDir.mkdir();
			setAttribute("javax.servlet.context.tempdir", tmpDir);
		}
		catch (IOException e)
		{
			throw new WicketRuntimeException(e);
		}
	}

	/**
	 * Add an init parameter.
	 * 
	 * @param name
	 *            The parameter name
	 * @param value
	 *            The parameter value
	 */
	public void addInitParameter(final String name, final String value)
	{
		initParameters.put(name, value);
	}

	// Configuration methods

	/**
	 * Add a new recognised mime type.
	 * 
	 * @param fileExtension
	 *            The file extension (e.g. "jpg")
	 * @param mimeType
	 *            The mime type (e.g. "image/jpeg")
	 */
	public void addMimeType(final String fileExtension, final String mimeType)
	{
		mimeTypes.put(fileExtension, mimeType);
	}

	/**
	 * Get an attribute with the given name.
	 * 
	 * @param name
	 *            The attribute name
	 * @return The value, or null
	 */
	public Object getAttribute(final String name)
	{
		return attributes.get(name);
	}

	/**
	 * Get all of the attribute names.
	 * 
	 * @return The attribute names
	 */
	public Enumeration<String> getAttributeNames()
	{
		return Collections.enumeration(attributes.keySet());
	}

	// ServletContext interface methods

	/**
	 * Get the context for the given URL path
	 * 
	 * @param name
	 *            The url path
	 * @return Always returns this
	 */
	public ServletContext getContext(String name)
	{
		return this;
	}

	/**
	 * Get the init parameter with the given name.
	 * 
	 * @param name
	 *            The name
	 * @return The parameter, or null if no such parameter
	 */
	public String getInitParameter(final String name)
	{
		return initParameters.getString(name);
	}

	/**
	 * Get the name of all of the init parameters.
	 * 
	 * @return The init parameter names
	 */
	public Enumeration<String> getInitParameterNames()
	{
		return Collections.enumeration(initParameters.keySet());
	}

	/**
	 * @return Always 2
	 */
	public int getMajorVersion()
	{
		return 2;
	}

	/**
	 * Get the mime type for the given file. Uses a hardcoded map of mime types
	 * set at initialisation time.
	 * 
	 * @param name
	 *            The name to get the mime type for
	 * @return The mime type
	 */
	public String getMimeType(final String name)
	{
		int index = name.lastIndexOf('.');
		if (index == -1 || index == (name.length() - 1))
		{
			return null;
		}
		else
		{
			return mimeTypes.getString(name.substring(index + 1));
		}
	}

	/**
	 * @return Always 3
	 */
	public int getMinorVersion()
	{
		return 3;
	}

	/**
	 * Wicket does not use the RequestDispatcher, so this implementation just
	 * returns a dummy value.
	 * 
	 * @param name
	 *            The name of the servlet or JSP
	 * @return The dispatcher
	 */
	public RequestDispatcher getNamedDispatcher(final String name)
	{
		return getRequestDispatcher(name);
	}

	/**
	 * Get the real file path of the given resource name.
	 * 
	 * @param name
	 *            The name
	 * @return The real path or null
	 */
	public String getRealPath(String name)
	{
		if (webappRoot == null)
		{
			return null;
		}

		if (name.startsWith("/"))
		{
			name = name.substring(1);
		}

		File f = new File(webappRoot, name);
		if (!f.exists())
		{
			return null;
		}
		else
		{
			return f.getPath();
		}
	}

	/**
	 * Wicket does not use the RequestDispatcher, so this implementation just
	 * returns a dummy value.
	 * 
	 * @param name
	 *            The name of the resource to get the dispatcher for
	 * @return The dispatcher
	 */
	public RequestDispatcher getRequestDispatcher(final String name)
	{
		return new RequestDispatcher()
		{
			public void forward(ServletRequest servletRequest, ServletResponse servletResponse)
					throws IOException
			{
				servletResponse.getWriter().write("FORWARD TO RESOURCE: " + name);
			}

			public void include(ServletRequest servletRequest, ServletResponse servletResponse)
					throws IOException
			{
				servletResponse.getWriter().write("INCLUDE OF RESOURCE: " + name);
			}
		};
	}

	/**
	 * Get the URL for a particular resource that is relative to the web app
	 * root directory.
	 * 
	 * @param name
	 *            The name of the resource to get
	 * @return The resource, or null if resource not found
	 * @throws MalformedURLException
	 *             If the URL is invalid
	 */
	public URL getResource(String name) throws MalformedURLException
	{
		if (webappRoot == null)
		{
			return null;
		}

		if (name.startsWith("/"))
		{
			name = name.substring(1);
		}

		File f = new File(webappRoot, name);
		if (!f.exists())
		{
			return null;
		}
		else
		{
			return f.toURI().toURL();
		}
	}

	/**
	 * Get an input stream for a particular resource that is relative to the web
	 * app root directory.
	 * 
	 * @param name
	 *            The name of the resource to get
	 * @return The input stream for the resource, or null of resource is not
	 *         found
	 */
	public InputStream getResourceAsStream(String name)
	{
		if (webappRoot == null)
		{
			return null;
		}

		if (name.startsWith("/"))
		{
			name = name.substring(1);
		}

		File f = new File(webappRoot, name);
		if (!f.exists())
		{
			return null;
		}
		else
		{
			try
			{
				return new FileInputStream(f);
			}
			catch (FileNotFoundException e)
			{
				e.printStackTrace();
				return null;
			}
		}
	}

	/**
	 * Get the resource paths starting from the web app root directory and then
	 * relative to the the given name.
	 * 
	 * @param name
	 *            The starting name
	 * @return The set of resource paths at this location
	 */
	public Set<String> getResourcePaths(String name)
	{
		if (webappRoot == null)
		{
			return new HashSet<String>();
		}

		if (name.startsWith("/"))
		{
			name = name.substring(1);
		}
		if (name.endsWith("/"))
		{
			name = name.substring(0, name.length() - 1);
		}
		String[] elements = null;
		if (name.trim().length() == 0)
		{
			elements = new String[0];
		}
		else
		{
			elements = name.split("/");
		}

		File current = webappRoot;
		for (String element : elements)
		{
			File[] files = current.listFiles();
			boolean match = false;
			for (File element0 : files)
			{
				if (element0.getName().equals(element) && element0.isDirectory())
				{
					current = element0;
					match = true;
					break;
				}
			}
			if (!match)
			{
				return null;
			}
		}

		File[] files = current.listFiles();
		Set<String> result = new HashSet<String>();
		int stripLength = webappRoot.getPath().length();
		for (File element : files)
		{
			String s = element.getPath().substring(stripLength).replace('\\', '/');
			if (element.isDirectory())
			{
				s = s + "/";
			}
			result.add(s);
		}
		return result;
	}

	/**
	 * Get the server info.
	 * 
	 * @return The server info
	 */
	public String getServerInfo()
	{
		return "Wicket Mock Test Environment v1.0";
	}

	/**
	 * NOT USED - Servlet Spec requires that this always returns null.
	 * 
	 * @param name
	 *            Not used
	 * @return null
	 * @throws ServletException
	 *             Not used
	 */
	public Servlet getServlet(String name) throws ServletException
	{
		return null;
	}

	/**
	 * Return the name of the servlet context.
	 * 
	 * @return The name
	 */
	public String getServletContextName()
	{
		return application.getName();
	}

	/**
	 * NOT USED - Servlet spec requires that this always returns null.
	 * 
	 * @return null
	 */
	public Enumeration getServletNames()
	{
		return null;
	}

	/**
	 * NOT USED - Servlet spec requires that this always returns null.
	 * 
	 * @return null
	 */
	public Enumeration getServlets()
	{
		return null;
	}

	/**
	 * As part of testing we always log to the console.
	 * 
	 * @param e
	 *            The exception to log
	 * @param msg
	 *            The message to log
	 */
	public void log(Exception e, String msg)
	{
		log.error(msg, e);
	}

	/**
	 * As part of testing we always log to the console.
	 * 
	 * @param msg
	 *            The message to log
	 */
	public void log(String msg)
	{
		log.info(msg);
	}

	/**
	 * As part of testing we always log to the console.
	 * 
	 * @param msg
	 *            The message to log
	 * @param cause
	 *            The cause exception
	 */
	public void log(String msg, Throwable cause)
	{
		log.error(msg, cause);
	}

	/**
	 * Remove an attribute with the given name.
	 * 
	 * @param name
	 *            The name
	 */
	public void removeAttribute(final String name)
	{
		attributes.remove(name);
	}

	/**
	 * Set an attribute.
	 * 
	 * @param name
	 *            The name of the attribute
	 * @param o
	 *            The value
	 */
	public void setAttribute(final String name, final Object o)
	{
		attributes.put(name, o);
	}
}