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
package org.apache.wicket.protocol.http;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.CharArrayReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.IPageMap;
import org.apache.wicket.IRedirectListener;
import org.apache.wicket.IResourceListener;
import org.apache.wicket.Page;
import org.apache.wicket.RequestListenerInterface;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.IFormSubmitListener;
import org.apache.wicket.markup.html.form.IOnChangeListener;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.ILinkListener;
import org.apache.wicket.protocol.http.request.WebRequestCodingStrategy;
import org.apache.wicket.util.file.File;
import org.apache.wicket.util.io.IOUtils;
import org.apache.wicket.util.lang.Classes;
import org.apache.wicket.util.upload.FileUploadBase;
import org.apache.wicket.util.value.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Mock servlet request. Implements all of the methods from the standard HttpServletRequest class
 * plus helper methods to aid setting up a request.
 * 
 * @author Chris Turner
 */
public class MockHttpServletRequest implements HttpServletRequest
{
	/**
	 * A holder class for an uploaded file.
	 * 
	 * @author Frank Bille (billen)
	 */
	private class UploadedFile
	{
		private String fieldName;
		private File file;
		private String contentType;

		/**
		 * Construct.
		 * 
		 * @param fieldName
		 * @param file
		 * @param contentType
		 */
		public UploadedFile(String fieldName, File file, String contentType)
		{
			this.fieldName = fieldName;
			this.file = file;
			this.contentType = contentType;
		}

		/**
		 * @return The content type of the file. Mime type.
		 */
		public String getContentType()
		{
			return contentType;
		}

		/**
		 * @param contentType
		 *            The content type.
		 */
		public void setContentType(String contentType)
		{
			this.contentType = contentType;
		}

		/**
		 * @return The field name.
		 */
		public String getFieldName()
		{
			return fieldName;
		}

		/**
		 * @param fieldName
		 */
		public void setFieldName(String fieldName)
		{
			this.fieldName = fieldName;
		}

		/**
		 * @return The uploaded file.
		 */
		public File getFile()
		{
			return file;
		}

		/**
		 * @param file
		 */
		public void setFile(File file)
		{
			this.file = file;
		}
	}

	/** Logging object */
	private static final Logger log = LoggerFactory.getLogger(MockHttpServletRequest.class);

	/** The application */
	private final Application application;

	private final ValueMap attributes = new ValueMap();

	private String authType;

	private String characterEncoding;

	private final ServletContext context;

	private final List<Cookie> cookies = new ArrayList<Cookie>();

	private final ValueMap headers = new ValueMap();

	private String method;

	private final ValueMap parameters = new ValueMap();

	private String path;

	private final HttpSession session;

	private String url;

	private Map<String, UploadedFile> uploadedFiles;

	private boolean useMultiPartContentType;

	/**
	 * Create the request using the supplied session object. Note that in order for temporary
	 * sessions to work, the supplied session must be an instance of {@link MockHttpSession}
	 * 
	 * @param application
	 *            The application that this request is for
	 * @param session
	 *            The session object
	 * @param context
	 *            The current servlet context
	 */
	public MockHttpServletRequest(final Application application, final HttpSession session,
		final ServletContext context)
	{
		this.application = application;
		this.session = session;
		this.context = context;
		initialize();
	}

	/**
	 * Add a new cookie.
	 * 
	 * @param cookie
	 *            The cookie
	 */
	public void addCookie(final Cookie cookie)
	{
		cookies.add(cookie);
	}

	/**
	 * Add an uploaded file to the request. Use this to simulate a file that has been uploaded to a
	 * field.
	 * 
	 * @param fieldName
	 *            The fieldname of the upload field.
	 * @param file
	 *            The file to upload.
	 * @param contentType
	 *            The content type of the file. Must be a correct mimetype.
	 */
	public void addFile(String fieldName, File file, String contentType)
	{
		if (file == null)
		{
			throw new IllegalArgumentException("File must not be null");
		}

		if (file.exists() == false)
		{
			throw new IllegalArgumentException(
				"File does not exists. You must provide an existing file: " +
					file.getAbsolutePath());
		}

		if (file.isFile() == false)
		{
			throw new IllegalArgumentException(
				"You can only add a File, which is not a directory. Only files can be uploaded.");
		}

		if (uploadedFiles == null)
		{
			uploadedFiles = new HashMap<String, UploadedFile>();
		}

		UploadedFile uf = new UploadedFile(fieldName, file, contentType);

		uploadedFiles.put(fieldName, uf);
	}

	/**
	 * Add a header to the request.
	 * 
	 * @param name
	 *            The name of the header to add
	 * @param value
	 *            The value
	 */
	public void addHeader(String name, String value)
	{
		@SuppressWarnings("unchecked")
		List<String> list = (List<String>)headers.get(name);
		if (list == null)
		{
			list = new ArrayList<String>(1);
			headers.put(name, list);
		}
		list.add(value);
	}

	/**
	 * @param name
	 * @param date
	 */
	public void addDateHeader(String name, long date)
	{
		DateFormat df = DateFormat.getDateInstance(DateFormat.FULL);
		String dateString = df.format(new Date(date));
		addHeader(name, dateString);
	}

	/**
	 * Get an attribute.
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
	 * Get the names of all of the values.
	 * 
	 * @return The names
	 */
	public Enumeration<String> getAttributeNames()
	{
		return Collections.enumeration(attributes.keySet());
	}

	// HttpServletRequest methods

	/**
	 * Get the auth type.
	 * 
	 * @return The auth type
	 */
	public String getAuthType()
	{
		return authType;
	}

	/**
	 * Get the current character encoding.
	 * 
	 * @return The character encoding
	 */
	public String getCharacterEncoding()
	{
		return characterEncoding;
	}

	/**
	 * true will force Request generate multiPart ContentType and ContentLength
	 * 
	 * @param useMultiPartContentType
	 */
	public void setUseMultiPartContentType(boolean useMultiPartContentType)
	{
		this.useMultiPartContentType = useMultiPartContentType;
	}


	/**
	 * Return the length of the content. This is always -1 except if useMultiPartContentType set as
	 * true. Then the length will be the length of the generated request.
	 * 
	 * @return -1 if useMultiPartContentType is false. Else the length of the generated request.
	 */
	public int getContentLength()
	{
		if (useMultiPartContentType)
		{
			byte[] request = buildRequest();
			return request.length;
		}

		return -1;
	}

	/**
	 * If useMultiPartContentType set as true return the correct content-type.
	 * 
	 * @return The correct multipart content-type if useMultiPartContentType is true. Else null.
	 */
	public String getContentType()
	{
		if (useMultiPartContentType)
		{
			return FileUploadBase.MULTIPART_FORM_DATA + "; boundary=abcdefgABCDEFG";
		}

		return null;
	}

	/**
	 * Get the context path. For this mock implementation the name of the application is always
	 * returned.
	 * 
	 * @return The context path
	 */
	public String getContextPath()
	{
		return "/" + application.getName();
	}

	/**
	 * Get all of the cookies for this request.
	 * 
	 * @return The cookies
	 */
	public Cookie[] getCookies()
	{
		if (cookies.size() == 0)
		{
			return null;
		}
		Cookie[] result = new Cookie[cookies.size()];
		return cookies.toArray(result);
	}

	/**
	 * Get the given header as a date.
	 * 
	 * @param name
	 *            The header name
	 * @return The date, or -1 if header not found
	 * @throws IllegalArgumentException
	 *             If the header cannot be converted
	 */
	public long getDateHeader(final String name) throws IllegalArgumentException
	{
		String value = getHeader(name);
		if (value == null)
		{
			return -1;
		}

		DateFormat df = DateFormat.getDateInstance(DateFormat.FULL);
		try
		{
			return df.parse(value).getTime();
		}
		catch (ParseException e)
		{
			throw new IllegalArgumentException("Can't convert header to date " + name + ": " +
				value);
		}
	}

	/**
	 * Get the given header value.
	 * 
	 * @param name
	 *            The header name
	 * @return The header value or null
	 */
	public String getHeader(final String name)
	{
		@SuppressWarnings("unchecked")
		final List<String> l = (List<String>)headers.get(name);
		if (l == null || l.size() < 1)
		{
			return null;
		}
		else
		{
			return l.get(0);
		}
	}

	/**
	 * Get the names of all of the headers.
	 * 
	 * @return The header names
	 */
	public Enumeration<String> getHeaderNames()
	{
		return Collections.enumeration(headers.keySet());
	}

	/**
	 * Get enumeration of all header values with the given name.
	 * 
	 * @param name
	 *            The name
	 * @return The header values
	 */
	public Enumeration<String> getHeaders(final String name)
	{
		@SuppressWarnings("unchecked")
		List<String> list = (List<String>)headers.get(name);
		if (list == null)
		{
			list = new ArrayList<String>();
		}
		return Collections.enumeration(list);
	}

	/**
	 * Returns an input stream if there has been added some uploaded files. Use
	 * {@link #addFile(String, File, String)} to add some uploaded files.
	 * 
	 * @return The input stream
	 * @throws IOException
	 *             If an I/O related problem occurs
	 */
	public ServletInputStream getInputStream() throws IOException
	{
		byte[] request = buildRequest();

		// Ok lets make an input stream to return
		final ByteArrayInputStream bais = new ByteArrayInputStream(request);

		return new ServletInputStream()
		{
			@Override
			public int read()
			{
				return bais.read();
			}
		};
	}

	/**
	 * Get the given header as an int.
	 * 
	 * @param name
	 *            The header name
	 * @return The header value or -1 if header not found
	 * @throws NumberFormatException
	 *             If the header is not formatted correctly
	 */
	public int getIntHeader(final String name)
	{
		String value = getHeader(name);
		if (value == null)
		{
			return -1;
		}
		return Integer.valueOf(value).intValue();
	}

	/**
	 * Get the locale of the request. Attempts to decode the Accept-Language header and if not found
	 * returns the default locale of the JVM.
	 * 
	 * @return The locale
	 */
	public Locale getLocale()
	{
		final String header = getHeader("Accept-Language");
		if (header == null)
		{
			return Locale.getDefault();
		}

		final String[] firstLocale = header.split(",");
		if (firstLocale.length < 1)
		{
			return Locale.getDefault();
		}

		final String[] bits = firstLocale[0].split("-");
		if (bits.length < 1)
		{
			return Locale.getDefault();
		}

		final String language = bits[0].toLowerCase();
		if (bits.length > 1)
		{
			final String country = bits[1].toUpperCase();
			return new Locale(language, country);
		}
		else
		{
			return new Locale(language);
		}
	}

	/**
	 * Return all the accepted locales. This implementation always returns just one.
	 * 
	 * @return The locales
	 */
	public Enumeration<Locale> getLocales()
	{
		List<Locale> list = new ArrayList<Locale>(1);
		list.add(getLocale());
		return Collections.enumeration(list);
	}

	/**
	 * Get the method.
	 * 
	 * @return The method
	 */
	public String getMethod()
	{
		return method;
	}

	/**
	 * Get the request parameter with the given name.
	 * 
	 * @param name
	 *            The parameter name
	 * @return The parameter value, or null
	 */
	public String getParameter(final String name)
	{
		return parameters.getString(name);
	}

	/**
	 * Get the map of all of the parameters.
	 * 
	 * @return The parameters
	 */
	public Map<String, Object> getParameterMap()
	{
		return parameters;
	}

	/**
	 * Get the names of all of the parameters.
	 * 
	 * @return The parameter names
	 */
	public Enumeration<String> getParameterNames()
	{
		return Collections.enumeration(parameters.keySet());
	}

	/**
	 * Get the values for the given parameter.
	 * 
	 * @param name
	 *            The name of the parameter
	 * @return The return values
	 */
	public String[] getParameterValues(final String name)
	{
		Object value = parameters.get(name);
		if (value == null)
		{
			return new String[0];
		}

		if (value instanceof String[])
		{
			return (String[])value;
		}
		else
		{
			String[] result = new String[1];
			result[0] = value.toString();
			return result;
		}
	}

	/**
	 * Get the path info.
	 * 
	 * @return The path info
	 */
	public String getPathInfo()
	{
		return path;
	}

	/**
	 * Always returns null.
	 * 
	 * @return null
	 */
	public String getPathTranslated()
	{
		return null;
	}

	/**
	 * Get the protocol.
	 * 
	 * @return Always HTTP/1.1
	 */
	public String getProtocol()
	{
		return "HTTP/1.1";
	}

	/**
	 * Get the query string part of the request.
	 * 
	 * @return The query string
	 */
	public String getQueryString()
	{
		if (parameters.size() == 0)
		{
			return null;
		}
		else
		{
			final StringBuffer buf = new StringBuffer();
			for (Iterator<String> iterator = parameters.keySet().iterator(); iterator.hasNext();)
			{
				final String name = iterator.next();
				final String value = parameters.getString(name);
				if (name != null)
					buf.append(WicketURLEncoder.QUERY_INSTANCE.encode(name));
				buf.append('=');
				if (value != null)
					buf.append(WicketURLEncoder.QUERY_INSTANCE.encode(value));
				if (iterator.hasNext())
				{
					buf.append('&');
				}
			}
			return buf.toString();
		}
	}

	/**
	 * This feature is not implemented at this time as we are not supporting binary servlet input.
	 * This functionality may be added in the future.
	 * 
	 * @return The reader
	 * @throws IOException
	 *             If an I/O related problem occurs
	 */
	public BufferedReader getReader() throws IOException
	{
		return new BufferedReader(new CharArrayReader(new char[0]));
	}

	/**
	 * Deprecated method - should not be used.
	 * 
	 * @param name
	 *            The name
	 * @return The path
	 * @deprecated Use ServletContext.getRealPath(String) instead.
	 */
	@Deprecated
	public String getRealPath(String name)
	{
		return context.getRealPath(name);
	}

	/**
	 * Get the remote address of the client.
	 * 
	 * @return Always 127.0.0.1
	 */
	public String getRemoteAddr()
	{
		return "127.0.0.1";
	}

	/**
	 * Get the remote host.
	 * 
	 * @return Always localhost
	 */
	public String getRemoteHost()
	{
		return "localhost";
	}

	/**
	 * Get the name of the remote user from the REMOTE_USER header.
	 * 
	 * @return The name of the remote user
	 */
	public String getRemoteUser()
	{
		return getHeader("REMOTE_USER");
	}

	/**
	 * Return a dummy dispatcher that just records that dispatch has occurred without actually doing
	 * anything.
	 * 
	 * @param name
	 *            The name to dispatch to
	 * @return The dispatcher
	 */
	public RequestDispatcher getRequestDispatcher(String name)
	{
		return context.getRequestDispatcher(name);
	}

	/**
	 * Get the requested session id. Always returns the id of the current session.
	 * 
	 * @return The session id
	 */
	public String getRequestedSessionId()
	{
		if (session instanceof MockHttpSession && ((MockHttpSession)session).isTemporary())
			return null;
		return session.getId();
	}

	/**
	 * Returns context path and servlet path concatenated, typically
	 * /applicationClassName/applicationClassName
	 * 
	 * @return The path value
	 * @see javax.servlet.http.HttpServletRequest#getRequestURI()
	 */
	public String getRequestURI()
	{
		if (url == null)
		{
			return getContextPath() + getServletPath();
		}
		return url;
	}

	/**
	 * Try to build a rough URL.
	 * 
	 * @return The url
	 */
	public StringBuffer getRequestURL()
	{
		final StringBuffer buf = new StringBuffer();
		buf.append("http://localhost");
		buf.append(getContextPath());
		if (getPathInfo() != null)
		{
			buf.append(getPathInfo());
		}

		final String query = getQueryString();
		if (query != null)
		{
			buf.append('?');
			buf.append(query);
		}
		return buf;
	}

	/**
	 * Get the scheme.
	 * 
	 * @return Always http
	 */
	public String getScheme()
	{
		return "http";
	}

	/**
	 * Get the server name.
	 * 
	 * @return Always localhost
	 */
	public String getServerName()
	{
		return "localhost";
	}

	/**
	 * Get the server port.
	 * 
	 * @return Always 80
	 */
	public int getServerPort()
	{
		return 80;
	}

	/**
	 * The servlet path may either be the application name or /. For test purposes we always return
	 * the servlet name.
	 * 
	 * @return The servlet path
	 */
	public String getServletPath()
	{
		return getContextPath();
	}

	/**
	 * Get the sessions.
	 * 
	 * @return The session
	 */
	public HttpSession getSession()
	{
		if (session instanceof MockHttpSession && ((MockHttpSession)session).isTemporary())
			return null;
		return session;
	}

	/**
	 * Get the session.
	 * 
	 * @param b
	 *            Ignored, there is always a session
	 * @return The session
	 */
	public HttpSession getSession(boolean b)
	{
		if (b && session instanceof MockHttpSession)
			((MockHttpSession)session).setTemporary(false);
		return getSession();
	}

	/**
	 * Get the user principal.
	 * 
	 * @return A user principal
	 */
	public Principal getUserPrincipal()
	{
		final String user = getRemoteUser();
		if (user == null)
		{
			return null;
		}
		else
		{
			return new Principal()
			{
				public String getName()
				{
					return user;
				}
			};
		}
	}

	/**
	 * @return True if there has been added files to this request using
	 *         {@link #addFile(String, File, String)}
	 */
	public boolean hasUploadedFiles()
	{
		return uploadedFiles != null;
	}

	/**
	 * Reset the request back to a default state.
	 */
	public void initialize()
	{
		authType = null;
		method = "post";
		cookies.clear();
		setDefaultHeaders();
		path = null;
		url = null;
		characterEncoding = "UTF-8";
		parameters.clear();
		attributes.clear();
	}

	/**
	 * Check whether session id is from a cookie. Always returns true.
	 * 
	 * @return Always true
	 */
	public boolean isRequestedSessionIdFromCookie()
	{
		return true;
	}

	/**
	 * Check whether session id is from a url rewrite. Always returns false.
	 * 
	 * @return Always false
	 */
	public boolean isRequestedSessionIdFromUrl()
	{
		return false;
	}

	/**
	 * Check whether session id is from a url rewrite. Always returns false.
	 * 
	 * @return Always false
	 */
	public boolean isRequestedSessionIdFromURL()
	{
		return false;
	}

	/**
	 * Check whether the session id is valid.
	 * 
	 * @return Always true
	 */
	public boolean isRequestedSessionIdValid()
	{
		return true;
	}

	/**
	 * Always returns false.
	 * 
	 * @return Always false
	 */
	public boolean isSecure()
	{
		return false;
	}

	/**
	 * NOT IMPLEMENTED.
	 * 
	 * @param name
	 *            The role name
	 * @return Always false
	 */
	public boolean isUserInRole(String name)
	{
		return false;
	}

	/**
	 * Remove the given attribute.
	 * 
	 * @param name
	 *            The name of the attribute
	 */
	public void removeAttribute(final String name)
	{
		attributes.remove(name);
	}

	/**
	 * Set the given attribute.
	 * 
	 * @param name
	 *            The attribute name
	 * @param o
	 *            The value to set
	 */
	public void setAttribute(final String name, final Object o)
	{
		attributes.put(name, o);
	}

	/**
	 * Set the auth type.
	 * 
	 * @param authType
	 *            The auth type
	 */
	public void setAuthType(final String authType)
	{
		this.authType = authType;
	}

	/**
	 * Set the character encoding.
	 * 
	 * @param encoding
	 *            The character encoding
	 * @throws UnsupportedEncodingException
	 *             If encoding not supported
	 */
	public void setCharacterEncoding(final String encoding) throws UnsupportedEncodingException
	{
		characterEncoding = encoding;
	}

	/**
	 * Set the cookies.
	 * 
	 * @param theCookies
	 *            The cookies
	 */
	public void setCookies(final Cookie[] theCookies)
	{
		cookies.clear();
		for (int i = 0; i < theCookies.length; i++)
		{
			cookies.add(theCookies[i]);
		}
	}

	/**
	 * Set the method.
	 * 
	 * @param method
	 *            The method
	 */
	public void setMethod(final String method)
	{
		this.method = method;
	}

	/**
	 * Set a parameter.
	 * 
	 * @param name
	 *            The name
	 * @param value
	 *            The value
	 */
	public void setParameter(final String name, final String value)
	{
		parameters.put(name, value);
	}

	/**
	 * Sets a map of parameters.
	 * 
	 * @param parameters
	 *            the parameters to set
	 */
	public void setParameters(final Map<String, String[]> parameters)
	{
		this.parameters.putAll(parameters);
	}

	/**
	 * Set the path that this request is supposed to be serving. The path is relative to the web
	 * application root and should start with a / character
	 * 
	 * @param path
	 */
	public void setPath(final String path)
	{
		this.path = path;
	}

	/**
	 * Set the complete url for this request. The url will be analyzed.
	 * 
	 * @param url
	 */
	public void setURL(String url)
	{
		if (url.startsWith("http://"))
		{
			int index = url.indexOf("/", 7);
			url = url.substring(index);
		}
		this.url = url;
		if (url.startsWith(getContextPath()))
		{
			url = url.substring(getContextPath().length());
		}
		if (url.startsWith(getServletPath()))
		{
			url = url.substring(getServletPath().length());
		}

		int index = url.indexOf("?");
		if (index == -1)
		{
			path = url;
		}
		else
		{
			path = url.substring(0, index);

			String queryString = url.substring(index + 1);
			RequestUtils.decodeParameters(queryString, parameters);
		}
	}

	/**
	 * Initialize the request parameters to point to the given bookmarkable page.
	 * 
	 * @param page
	 *            The page to point to
	 * @param params
	 *            Additional parameters
	 */
	public void setRequestToBookmarkablePage(final Page page, final Map<String, Object> params)
	{
		parameters.putAll(params);
		parameters.put(WebRequestCodingStrategy.BOOKMARKABLE_PAGE_PARAMETER_NAME, page.getClass()
			.getName());
	}

	/**
	 * Initialize the request parameters to point to the given component.
	 * 
	 * @param component
	 *            The component
	 */
	public void setRequestToComponent(final Component component)
	{
		final IPageMap pageMap = component.getPage().getPageMap();
		final String pageMapName = pageMap.isDefault() ? "" : pageMap.getName();
		if (component instanceof BookmarkablePageLink)
		{
			final Class<? extends Page> clazz = ((BookmarkablePageLink<?>)component).getPageClass();
			parameters.put(WebRequestCodingStrategy.BOOKMARKABLE_PAGE_PARAMETER_NAME, pageMapName +
				':' + clazz.getName());
		}
		else
		{
			int version = component.getPage().getCurrentVersionNumber();
			Class<?> clazz = null;
			if (component instanceof IRedirectListener)
			{
				clazz = IRedirectListener.class;
			}
			else if (component instanceof IResourceListener)
			{
				clazz = IResourceListener.class;
			}
			else if (component instanceof IFormSubmitListener)
			{
				clazz = IFormSubmitListener.class;
			}
			else if (component instanceof ILinkListener)
			{
				clazz = ILinkListener.class;
			}
			else if (component instanceof IOnChangeListener)
			{
				clazz = IOnChangeListener.class;
			}
			else
			{
				throw new IllegalArgumentException(
					"The component class doesn't seem to implement any of the known *Listener interfaces: " +
						component.getClass());
			}

			// manually create the url using default strategy and format
			parameters.put(WebRequestCodingStrategy.INTERFACE_PARAMETER_NAME, pageMapName + ':' +
				component.getPath() + ':' + (version == 0 ? "" : "" + version) + ':' +
				Classes.simpleName(clazz) + "::");

			// see if we can replace our manual listener url with a properly generated one...

			try
			{
				RequestListenerInterface rli = (RequestListenerInterface)clazz.getField("INTERFACE")
					.get(clazz);

				String auto = component.getRequestCycle().urlFor(component, rli).toString();
				int idx = auto.indexOf(WebRequestCodingStrategy.INTERFACE_PARAMETER_NAME);
				if (idx >= 0)
				{
					auto = auto.substring(idx +
						WebRequestCodingStrategy.INTERFACE_PARAMETER_NAME.length() + 1);
				}
				else
				{
					// additional check for crypted strategy
					idx = auto.indexOf("x=6*");
					if (idx >= 0)
					{
						auto = auto.substring(idx + 4);
					}
				}

				idx = auto.indexOf("&");
				if (idx >= 0)
				{
					auto = auto.substring(0, idx);
				}

				parameters.put(WebRequestCodingStrategy.INTERFACE_PARAMETER_NAME, auto);

			}
			catch (Exception e)
			{
				// noop
			}


			if (component.isStateless() && component.getPage().isBookmarkable())
			{
				parameters.put(WebRequestCodingStrategy.BOOKMARKABLE_PAGE_PARAMETER_NAME,
					pageMapName + ':' + component.getPage().getClass().getName());
			}
		}
	}

	/**
	 * Initialize the request parameters to point to the given form component. The additional map
	 * should contain mappings between individual components that appear in the form and the string
	 * value that should be submitted for each of these components.
	 * 
	 * @param form
	 *            The for to send the request to
	 * @param values
	 *            The values for each of the form components
	 */
	public void setRequestToFormComponent(final Form<?> form, final Map<String, Object> values)
	{
		setRequestToComponent(form);

		final Map<String, Object> valuesApplied = new HashMap<String, Object>();
		form.visitChildren(FormComponent.class, new Component.IVisitor<FormComponent<?>>()
		{
			public Object component(final FormComponent<?> component)
			{
				String value = (String)values.get(component);
				if (value != null)
				{
					parameters.put(component.getInputName(), values.get(component));
					valuesApplied.put(component.getId(), component);
				}
				return CONTINUE_TRAVERSAL;
			}
		});

		if (values.size() != valuesApplied.size())
		{
			Map<String, Object> diff = new HashMap<String, Object>();
			diff.putAll(values);

			Iterator<String> iter = valuesApplied.keySet().iterator();
			while (iter.hasNext())
			{
				diff.remove(iter.next());
			}

			log.error("Parameter mismatch: didn't find all components referenced in parameter 'values': " +
				diff.keySet());
		}
	}

	/**
	 * Initialize the request parameters from the given redirect string that redirects back to a
	 * particular component for display.
	 * 
	 * @param redirect
	 *            The redirect string to display from
	 */
	public void setRequestToRedirectString(final String redirect)
	{
		parameters.clear();

		int queryStringPos = redirect.indexOf('?');

		// Decode the parameters
		if (queryStringPos != -1)
		{
			final String queryString = redirect.substring(queryStringPos + 1);
			RequestUtils.decodeParameters(queryString, parameters);
		}

		// We need to absolutize the redirect URL as we are not as smart as a web-browser
		// (WICKET-702)
		url = getContextPath() + getServletPath() + "/" + redirect;

		// Remove occurrences of ".." from the path
		url = RequestUtils.removeDoubleDots(url);
		log.info("Redirecting to " + url);
	}

	/**
	 * Helper method to create some default headers for the request
	 */
	private void setDefaultHeaders()
	{
		headers.clear();
		addHeader("Accept", "text/xml,application/xml,application/xhtml+xml,"
			+ "text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5");
		addHeader("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.7");
		Locale l = Locale.getDefault();
		addHeader("Accept-Language", l.getLanguage().toLowerCase() + "-" +
			l.getCountry().toLowerCase() + "," + l.getLanguage().toLowerCase() + ";q=0.5");
		addHeader("User-Agent",
			"Mozilla/5.0 (Windows; U; Windows NT 5.0; en-US; rv:1.7) Gecko/20040707 Firefox/0.9.2");
	}

	private static final String crlf = "\r\n";
	private static final String boundary = "--abcdefgABCDEFG";

	private void newAttachment(OutputStream out) throws IOException
	{
		out.write(boundary.getBytes());
		out.write(crlf.getBytes());
		out.write("Content-Disposition: form-data".getBytes());
	}

	/**
	 * Build the request based on the uploaded files and the parameters.
	 * 
	 * @return The request as a string.
	 */
	private byte[] buildRequest()
	{
		try
		{
			// Build up the input stream based on the files and parameters
			ByteArrayOutputStream out = new ByteArrayOutputStream();

			// Add parameters
			for (Iterator<String> iterator = parameters.keySet().iterator(); iterator.hasNext();)
			{
				final String name = iterator.next();
				newAttachment(out);
				out.write("; name=\"".getBytes());
				out.write(name.getBytes());
				out.write("\"".getBytes());
				out.write(crlf.getBytes());
				out.write(crlf.getBytes());
				out.write(parameters.get(name).toString().getBytes());
				out.write(crlf.getBytes());
			}

			// Add files
			if (uploadedFiles != null)
			{
				for (Iterator<String> iterator = uploadedFiles.keySet().iterator(); iterator.hasNext();)
				{
					String fieldName = iterator.next();

					UploadedFile uf = uploadedFiles.get(fieldName);

					newAttachment(out);
					out.write("; name=\"".getBytes());
					out.write(fieldName.getBytes());
					out.write("\"; filename=\"".getBytes());
					out.write(uf.getFile().getName().getBytes());
					out.write("\"".getBytes());
					out.write(crlf.getBytes());
					out.write("Content-Type: ".getBytes());
					out.write(uf.getContentType().getBytes());
					out.write(crlf.getBytes());
					out.write(crlf.getBytes());

					// Load the file and put it into the the inputstream
					FileInputStream fis = new FileInputStream(uf.getFile());
					IOUtils.copy(fis, out);
					fis.close();
					out.write(crlf.getBytes());
				}
			}

			out.write(boundary.getBytes());
			out.write("--".getBytes());
			out.write(crlf.getBytes());
			return out.toByteArray();
		}
		catch (IOException e)
		{
			// NOTE: IllegalStateException(Throwable) only exists since Java 1.5
			throw new WicketRuntimeException(e);
		}
	}

	/**
	 * @return local address
	 */
	public String getLocalAddr()
	{
		return "127.0.0.1";
	}

	/**
	 * @return local host name
	 */
	public String getLocalName()
	{
		return "127.0.0.1";
	}

	/**
	 * @return local port
	 */
	public int getLocalPort()
	{
		return 80;
	}

	/**
	 * @return remote port
	 */
	public int getRemotePort()
	{
		return 80;
	}
}
