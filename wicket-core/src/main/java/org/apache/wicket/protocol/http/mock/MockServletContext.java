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
package org.apache.wicket.protocol.http.mock;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterRegistration;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRegistration;
import jakarta.servlet.ServletRegistration.Dynamic;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.SessionCookieConfig;
import jakarta.servlet.SessionTrackingMode;
import jakarta.servlet.descriptor.JspConfigDescriptor;

import org.apache.wicket.Application;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.util.cookies.CookieUtils;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.value.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Mock implementation of the servlet context for testing purposes. This implementation supports all
 * of the standard context methods except that request dispatching just indicates what is being
 * dispatched to, rather than doing the actual dispatch.
 * <p>
 * The context can be configured with a path parameter that should point to an absolute directory
 * location that represents the place where the contents of the WAR bundle are located. Setting this
 * value allows all of the resource location functionality to work as in a fully functioning web
 * application. This value is not set then not resource location functionality will work and instead
 * null will always be returned.
 *
 * @author Chris Turner
 */
public class MockServletContext implements ServletContext
{
	private static final Logger log = LoggerFactory.getLogger(MockServletContext.class);

	private final Application application;

	private final ValueMap attributes = new ValueMap();

	private final ValueMap initParameters = new ValueMap();

	private final Map<String, ServletRegistration.Dynamic> servletRegistration = new HashMap<>();

	/** Map of mime types */
	private final ValueMap mimeTypes = new ValueMap();

	private File webappRoot;

	private final SessionCookieConfig sessionCookieConfig = new SessionCookieConfig()
	{
		private boolean secure;
		private String path;
		private String name = CookieUtils.DEFAULT_SESSIONID_COOKIE_NAME;
		private int maxAge;
		private boolean httpOnly;
		private String domain;
		private String comment;

		@Override
		public void setSecure(boolean secure)
		{
			this.secure = secure;
		}

		@Override
		public void setPath(String path)
		{
			this.path = path;
		}

		@Override
		public void setName(String name)
		{
			this.name = name;
		}

		@Override
		public void setMaxAge(int maxAge)
		{
			this.maxAge = maxAge;
		}

		@Override
		public void setHttpOnly(boolean httpOnly)
		{
			this.httpOnly = httpOnly;
		}

		@Override
		public void setDomain(String domain)
		{
			this.domain = domain;
		}

		@Override
		public void setComment(String comment)
		{
			this.comment = comment;
		}

		@Override
		public boolean isSecure()
		{
			return secure;
		}

		@Override
		public boolean isHttpOnly()
		{
			return httpOnly;
		}

		@Override
		public String getPath()
		{
			return path;
		}

		@Override
		public String getName()
		{
			return name;
		}

		@Override
		public int getMaxAge()
		{
			return maxAge;
		}

		@Override
		public String getDomain()
		{
			return domain;
		}

		@Override
		public String getComment()
		{
			return comment;
		}
	};

	private int sessionTimeout = 30; // in minutes
	private Charset requestCharacterEncoding = StandardCharsets.UTF_8;
	private Charset responseCharacterEncoding = StandardCharsets.UTF_8;

	/**
	 * Create the mock object. As part of the creation, the context sets the root directory where
	 * web application content is stored. This must be an ABSOLUTE directory relative to where the
	 * tests are being executed. For example: <code>System.getProperty("user.dir") +
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

		// the user app can configure specific work folder by setting -Dwicket.tester.work.folder JVM option,
		// otherwise assume we're running in maven or an eclipse project created by maven,
		// so the sessions directory will be created inside the target directory,
		// and will be cleaned up with a mvn clean

		String workFolder = System.getProperty("wicket.tester.work.folder", "target/work/");
		File file = new File(workFolder);
		try
		{
			file.mkdirs();
		}
		catch (SecurityException sx)
		{
			// not allowed to write so fallback to tmpdir
			String tmpDir = System.getProperty("java.io.tmpdir");
			file = new File(tmpDir);
		}

		attributes.put("jakarta.servlet.context.tempdir", file);

		mimeTypes.put("html", "text/html");
		mimeTypes.put("htm", "text/html");
		mimeTypes.put("css", "text/css");
		mimeTypes.put("xml", "text/xml");
		mimeTypes.put("js", "text/javascript");
		mimeTypes.put("gif", "image/gif");
		mimeTypes.put("jpg", "image/jpeg");
		mimeTypes.put("png", "image/png");
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
	 * Add a new recognized mime type.
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
	@Override
	public Object getAttribute(final String name)
	{
		return attributes.get(name);
	}

	/**
	 * Get all of the attribute names.
	 *
	 * @return The attribute names
	 */
	@Override
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
	@Override
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
	@Override
	public String getInitParameter(final String name)
	{
		return initParameters.getString(name);
	}

	/**
	 * Get the name of all of the init parameters.
	 *
	 * @return The init parameter names
	 */
	@Override
	public Enumeration<String> getInitParameterNames()
	{
		return Collections.enumeration(initParameters.keySet());
	}

	@Override
	public boolean setInitParameter(String name, String value)
	{
		return false;
	}

	/**
	 * Get the mime type for the given file. Uses a hardcoded map of mime types set at
	 * Initialization time.
	 *
	 * @param name
	 *            The name to get the mime type for
	 * @return The mime type
	 */
	@Override
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

	@Override
	public int getMajorVersion()
	{
		return 3;
	}

	@Override
	public int getMinorVersion()
	{
		return 0;
	}

	@Override
	public int getEffectiveMajorVersion()
	{
		return 3;
	}

	@Override
	public int getEffectiveMinorVersion()
	{
		return 0;
	}

	/**
	 * Wicket does not use the RequestDispatcher, so this implementation just returns a dummy value.
	 *
	 * @param name
	 *            The name of the servlet or JSP
	 * @return The dispatcher
	 */
	@Override
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
	@Override
	public String getRealPath(String name)
	{
		try {
			URL url = getResource(name);
			if (url != null) {
				// WICKET-6755 do not use url.getFile() as it does not properly decode the path
				return new File(url.toURI()).getAbsolutePath();
			}
		} catch (IOException | URISyntaxException e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}

	/**
	 * Wicket does not use the RequestDispatcher, so this implementation just returns a dummy value.
	 *
	 * @param name
	 *            The name of the resource to get the dispatcher for
	 * @return The dispatcher
	 */
	@Override
	public RequestDispatcher getRequestDispatcher(final String name)
	{
		return new RequestDispatcher()
		{
			@Override
			public void forward(ServletRequest servletRequest, ServletResponse servletResponse)
				throws IOException
			{
				servletResponse.getWriter().write("FORWARD TO RESOURCE: " + name);
			}

			@Override
			public void include(ServletRequest servletRequest, ServletResponse servletResponse)
				throws IOException
			{
				servletResponse.getWriter().write("INCLUDE OF RESOURCE: " + name);
			}
		};
	}

	/**
	 * Get the URL for a particular resource that is relative to the web app root directory.
	 *
	 * @param name
	 *            The name of the resource to get
	 * @return The resource, or null if resource not found
	 * @throws MalformedURLException
	 *             If the URL is invalid
	 */
	@Override
	public URL getResource(String name) throws MalformedURLException
	{
		if (name.startsWith("/"))
		{
			name = name.substring(1);
		}

		if (webappRoot != null)
		{
			File f = new File(webappRoot, name);
			if (f.exists())
			{
				return f.toURI().toURL();
			}
		}

		return getClass().getClassLoader().getResource("META-INF/resources/" + name);
	}

	/**
	 * Get an input stream for a particular resource that is relative to the web app root directory.
	 *
	 * @param name
	 *            The name of the resource to get
	 * @return The input stream for the resource, or null of resource is not found
	 */
	@Override
	public InputStream getResourceAsStream(String name)
	{
		try {
			URL url = getResource(name);
			if (url != null) {
				return url.openStream();
			}
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}

	/**
	 * Get the resource paths starting from the web app root directory and then relative to the the
	 * given name.
	 *
	 * @param name
	 *            The starting name
	 * @return The set of resource paths at this location
	 */
	@Override
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
			elements = Strings.split(name, '/');
		}

		File current = webappRoot;
		for (String element : elements)
		{
			File[] files = current.listFiles();
			boolean match = false;
			if (files != null)
			{
				for (File file : files)
				{
					if (file.getName().equals(element) && file.isDirectory())
					{
						current = file;
						match = true;
						break;
					}
				}
			}
			if (!match)
			{
				return null;
			}
		}

		File[] files = current.listFiles();
		Set<String> result = new HashSet<>();
		if (files != null)
		{
			int stripLength = webappRoot.getPath().length();
			for (File file : files)
			{
				String s = file.getPath().substring(stripLength).replace('\\', '/');
				if (file.isDirectory())
				{
					s = s + "/";
				}
				result.add(s);
			}
		}
		return result;
	}

	/**
	 * Get the server info.
	 *
	 * @return The server info
	 */
	@Override
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
	@Override
	public Servlet getServlet(String name) throws ServletException
	{
		return null;
	}

	/**
	 * Return the name of the servlet context.
	 *
	 * @return The name
	 */
	@Override
	public String getServletContextName()
	{
		return application.getName();
	}

	@Override
	public ServletRegistration.Dynamic addServlet(String servletName, String className)
	{
		try
		{
			return addServlet(servletName, Class.forName(className).asSubclass(Servlet.class));
		}
		catch (ClassNotFoundException e)
		{
			throw new WicketRuntimeException(e);
		}
	}

	@Override
	public ServletRegistration.Dynamic addServlet(String servletName, Servlet servlet)
	{
		Dynamic mockRegistration = (Dynamic)Proxy.newProxyInstance(Dynamic.class.getClassLoader(),
			new Class<?>[]{Dynamic.class}, new MockedServletRegistationHandler(servletName));

		servletRegistration.put(servletName, mockRegistration);

		return mockRegistration;
	}

	@Override
	public ServletRegistration.Dynamic addServlet(String servletName, Class<? extends Servlet> servletClass)
	{
		try
		{
			return addServlet(servletName, servletClass.getDeclaredConstructor().newInstance());
		}
		catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e)
		{
			throw new WicketRuntimeException(e);
		}
	}

	@Override
	public Dynamic addJspFile(String s, String s1)
	{
		return null;
	}

	@Override
	public <T extends Servlet> T createServlet(Class<T> clazz) throws ServletException
	{
		try
		{
			return clazz.getDeclaredConstructor().newInstance();
		}
		catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e)
		{
			throw new WicketRuntimeException(e);
		}
	}

	@Override
	public ServletRegistration getServletRegistration(String servletName)
	{
		return servletRegistration.get(servletName);
	}

	@Override
	public Map<String, ? extends ServletRegistration> getServletRegistrations()
	{
		return servletRegistration;
	}

	@Override
	public FilterRegistration.Dynamic addFilter(String filterName, String className)
	{
		return null;
	}

	@Override
	public FilterRegistration.Dynamic addFilter(String filterName, Filter filter)
	{
		return null;
	}

	@Override
	public FilterRegistration.Dynamic addFilter(String filterName, Class<? extends Filter> filterClass)
	{
		return null;
	}

	@Override
	public <T extends Filter> T createFilter(Class<T> clazz) throws ServletException
	{
		return null;
	}

	@Override
	public FilterRegistration getFilterRegistration(String filterName)
	{
		return null;
	}

	@Override
	public Map<String, ? extends FilterRegistration> getFilterRegistrations()
	{
		return null;
	}

	@Override
	public SessionCookieConfig getSessionCookieConfig()
	{
		return sessionCookieConfig;
	}

	@Override
	public void setSessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes)
	{
	}

	@Override
	public Set<SessionTrackingMode> getDefaultSessionTrackingModes()
	{
		return EnumSet.of(SessionTrackingMode.COOKIE);
	}

	@Override
	public Set<SessionTrackingMode> getEffectiveSessionTrackingModes()
	{
		return getDefaultSessionTrackingModes();
	}

	@Override
	public void addListener(String className)
	{
	}

	@Override
	public <T extends EventListener> void addListener(T t)
	{
	}

	@Override
	public void addListener(Class<? extends EventListener> listenerClass)
	{
	}

	@Override
	public <T extends EventListener> T createListener(Class<T> clazz) throws ServletException
	{
		return null;
	}

	@Override
	public JspConfigDescriptor getJspConfigDescriptor()
	{
		return null;
	}

	@Override
	public ClassLoader getClassLoader()
	{
		return null;
	}

	@Override
	public void declareRoles(String... roleNames)
	{
	}

	@Override
	public String getVirtualServerName()
	{
		return "WicketTester 8.x";
	}

	@Override
	public int getSessionTimeout()
	{
		return sessionTimeout;
	}

	@Override
	public void setSessionTimeout(int sessionTimeout)
	{
		this.sessionTimeout = sessionTimeout;
	}

	@Override
	public String getRequestCharacterEncoding()
	{
		return requestCharacterEncoding.name();
	}

	@Override
	public void setRequestCharacterEncoding(String requestCharacterEncoding)
	{
		this.requestCharacterEncoding = Charset.forName(requestCharacterEncoding);
	}

	@Override
	public String getResponseCharacterEncoding()
	{
		return responseCharacterEncoding.name();
	}

	@Override
	public void setResponseCharacterEncoding(String responseCharacterEncoding)
	{
		this.responseCharacterEncoding = Charset.forName(responseCharacterEncoding);
	}

	/**
	 * NOT USED - Servlet spec requires that this always returns null.
	 *
	 * @return null
	 */
	@Override
	public Enumeration<String> getServletNames()
	{
		return null;
	}

	/**
	 * NOT USED - Servlet spec requires that this always returns null.
	 *
	 * @return null
	 */
	@Override
	public Enumeration<Servlet> getServlets()
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
	@Override
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
	@Override
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
	@Override
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
	@Override
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
	@Override
	public void setAttribute(final String name, final Object o)
	{
		attributes.put(name, o);
	}

	/**
	 * @return context path
	 */
	@Override
	public String getContextPath()
	{
		return "";
	}


	/**
	 * Invocation handler for proxy interface of {@link jakarta.servlet.ServletRegistration.Dynamic}.
	 * This class intercepts invocation for method {@link jakarta.servlet.ServletRegistration.Dynamic#getMappings}
	 * and returns the servlet name.
	 *
	 * @author andrea del bene
	 *
	 */
	class MockedServletRegistationHandler implements InvocationHandler
	{

		private final Collection<String> servletName;

		public MockedServletRegistationHandler(String servletName)
		{
			this.servletName = Arrays.asList(servletName);
		}

		@Override
		public Object invoke(Object object, Method method, Object[] args) throws Throwable
		{
			if (method.getName().equals("getMappings"))
			{
				return servletName;
			}

			return null;
		}
	}
}
