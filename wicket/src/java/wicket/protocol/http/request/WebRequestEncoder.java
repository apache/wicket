/*
 * $Id$
 * $Revision$ $Date$
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
package wicket.protocol.http.request;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.Application;
import wicket.IRequestTarget;
import wicket.PageParameters;
import wicket.Request;
import wicket.RequestCycle;
import wicket.WicketRuntimeException;
import wicket.protocol.http.WebRequest;
import wicket.protocol.http.WebRequestCycle;
import wicket.request.IPageClassRequestTarget;
import wicket.request.IRequestEncoder;
import wicket.request.RequestParameters;
import wicket.util.string.Strings;

/**
 * Request parameters factory implementation that uses http request parameters
 * and path info to construct the request parameters object.
 * 
 * @author Eelco Hillenius
 */
public class WebRequestEncoder implements IRequestEncoder
{
	/** log. */
	private Log log = LogFactory.getLog(WebRequestEncoder.class);

	//TODO this is not right yet: get rid of it and make it more general
	// based on request targets
	/**
	 * map of path mounts for bookmarkable pages.
	 */
	private Map/* <String,String> */bookmarkablePageMounts = new HashMap();

	/**
	 * map of path mounts for shared resources.
	 */
	private Map/* <String,String> */sharedResourceMounts = new HashMap();

	/**
	 * Construct.
	 */
	public WebRequestEncoder()
	{
	}

	/**
	 * Encode the given request target. If a mount is found, that mounted url
	 * will be returned. Otherwise, one of the delegation methods will be
	 * called.
	 * 
	 * @see wicket.request.IRequestEncoder#encode(wicket.RequestCycle,
	 *      wicket.IRequestTarget)
	 */
	public String encode(RequestCycle requestCycle, IRequestTarget requestTarget)
	{
		// TODO implement mounting in a more generic way, and check mounts first
		// TODO handle more options
		return encode(requestCycle, (IPageClassRequestTarget)requestTarget);
	}

	/**
	 * Encode a page class target.
	 * 
	 * @param requestCycle
	 *            the current request cycle
	 * @param requestTarget
	 *            the target to encode
	 * @return the encoded url
	 */
	protected String encode(RequestCycle requestCycle, IPageClassRequestTarget requestTarget)
	{
		final Class pageClass = requestTarget.getPageClass();
		final PageParameters parameters = requestTarget.getPageParameters();
		final StringBuffer buffer = urlPrefix(requestCycle);
		buffer.append("?bookmarkablePage=");
		String pageReference = requestCycle.getApplication().getPages().aliasForClass(pageClass);
		if (pageReference == null)
		{
			pageReference = pageClass.getName();
		}
		buffer.append(pageReference);
		if (parameters != null)
		{
			for (final Iterator iterator = parameters.keySet().iterator(); iterator.hasNext();)
			{
				final String key = (String)iterator.next();
				final String value = parameters.getString(key);
				if (value != null)
				{
					String escapedValue = value;
					try
					{
						escapedValue = URLEncoder.encode(escapedValue, Application.get()
								.getSettings().getResponseRequestEncoding());
					}
					catch (UnsupportedEncodingException ex)
					{
						log.error(ex.getMessage(), ex);
					}
					buffer.append('&');
					buffer.append(key);
					buffer.append('=');
					buffer.append(escapedValue);
				}
			}
		}
		return requestCycle.getResponse().encodeURL(buffer.toString());
	}

	/**
	 * @see wicket.request.IRequestEncoder#decode(wicket.Request)
	 */
	public final RequestParameters decode(Request request)
	{
		RequestParameters parameters = new RequestParameters();
		String pathInfo = getRequestPath(request);
		parameters.setPathInfo(pathInfo);

		addPageParameters(request, parameters);

		String alias = getBookmarkablePageAlias(pathInfo);
		if (alias != null)
		{
			parameters.setBookmarkablePageAlias(alias);
		}
		else
		{
			addBookmarkablePageParameters(request, parameters);
		}

		String key = getSharedResourceKey(pathInfo);
		if (key != null)
		{
			parameters.setResourceKey(key);
		}
		else
		{
			addResourceParameters(request, parameters);
		}

		return parameters;
	}

	/**
	 * Mounts a bookmarkable page alias to the given path.
	 * 
	 * @param path
	 *            the path to mount the bookmarkable page alias on
	 * @param bookmarkablePageAlias
	 *            the bookmarkable page alias
	 */
	public final void mountBookmarkablePage(String path, String bookmarkablePageAlias)
	{
		if (sharedResourceMounts.containsKey(path))
		{
			throw new WicketRuntimeException(path + " is already mounted as a shared resource");
		}
		bookmarkablePageMounts.put(path, bookmarkablePageAlias);
	}

	/**
	 * Unmounts a bookmarkable page alias.
	 * 
	 * @param path
	 *            the path of the bookmarkable page to unmount
	 */
	public final void unmountBookmarkablePage(String path)
	{
		bookmarkablePageMounts.remove(path);
	}

	/**
	 * Gets the bookmarkable page alias that was mounted at the provided path if
	 * any.
	 * 
	 * @param path
	 *            the path
	 * @return the mounted bookmarkable page alias or null if no alias was
	 *         mounted on this path
	 */
	public String getBookmarkablePageAlias(String path)
	{
		return (String)bookmarkablePageMounts.get(path);
	}

	/**
	 * Mounts a shared resource key to the given path.
	 * 
	 * @param path
	 *            the path to mount the bookmarkable page alias on
	 * @param sharedResourceKey
	 *            the shared resource key
	 */
	public final void mountSharedResourceKey(String path, String sharedResourceKey)
	{
		if (sharedResourceMounts.containsKey(path))
		{
			throw new WicketRuntimeException(path + " is already mounted as a shared resource");
		}
		sharedResourceMounts.put(path, sharedResourceKey);
	}

	/**
	 * Unmounts a bookmarkable page alias.
	 * 
	 * @param path
	 *            the path of the shared resource key to unmount
	 */
	public final void unmountSharedResourceKey(String path)
	{
		sharedResourceMounts.remove(path);
	}

	/**
	 * Gets the shared resource key that was mounted at the provided path if
	 * any.
	 * 
	 * @param path
	 *            the path
	 * @return the mounted shared resource key or null if no resource key was
	 *         mounted on this path
	 */
	public String getSharedResourceKey(String path)
	{
		return (String)bookmarkablePageMounts.get(path);
	}

	/**
	 * Gets the request info path. This is an overridable method in order to
	 * provide users with a means to implement e.g. a path encryption scheme.
	 * This method by default returns {@link Request#getPath()}.
	 * 
	 * @param request
	 *            the request
	 * @return the path info object, possibly processed
	 */
	protected String getRequestPath(Request request)
	{
		return request.getPath();
	}

	/**
	 * Adds page related parameters (path and optionally pagemap, version and
	 * interface).
	 * 
	 * @param request
	 *            the incomming request
	 * @param parameters
	 *            the parameters object to set the found values on
	 */
	protected void addPageParameters(Request request, RequestParameters parameters)
	{
		String componentPath = request.getParameter("path");
		if (componentPath != null)
		{
			parameters.setComponentPath(componentPath);
			parameters.setPageMapName(request.getParameter("pagemap"));
			final String versionNumberString = request.getParameter("version");
			final int versionNumber = Strings.isEmpty(versionNumberString) ? 0 : Integer
					.parseInt(versionNumberString);
			parameters.setVersionNumber(versionNumber);
			String interfaceName = request.getParameter("interface");
			if (interfaceName == null)
			{
				interfaceName = "IRedirectListener";
			}
			parameters.setInterfaceName(interfaceName);
		}
	}

	/**
	 * Adds bookmarkable page related parameters (page alias and optionally page
	 * parameters). Any bookmarkable page alias mount will override this method;
	 * hence if a mount is found, this method will not be called.
	 * 
	 * @param request
	 *            the incomming request
	 * @param parameters
	 *            the parameters object to set the found values on
	 */
	protected void addBookmarkablePageParameters(Request request, RequestParameters parameters)
	{
		parameters.setBookmarkablePageAlias(request.getParameter("bookmarkablePage"));
		parameters.setParameters(request.getParameterMap());
	}

	/**
	 * Adds (shared) resource related parameters (resource key). Any shared
	 * resource key mount will override this method; hence if a mount is found,
	 * this method will not be called.
	 * 
	 * @param request
	 *            the incomming request
	 * @param parameters
	 *            the parameters object to set the found values on
	 */
	protected void addResourceParameters(Request request, RequestParameters parameters)
	{
		String pathInfo = request.getPath();
		if (pathInfo != null && pathInfo.startsWith("/resources/"))
		{
			parameters.setResourceKey(pathInfo.substring("/resources/".length()));
		}
	}

	/**
	 * Creates a prefix for a url.
	 * 
	 * @param requestCycle
	 *            the current request cycle
	 * 
	 * @return Prefix for URLs including the context path and servlet path.
	 */
	protected StringBuffer urlPrefix(RequestCycle requestCycle)
	{
		final StringBuffer buffer = new StringBuffer();
		final WebRequest request = ((WebRequestCycle)requestCycle).getWebRequest();
		if (request != null)
		{
			final String contextPath = request.getContextPath();
			buffer.append(contextPath);
			String path = request.getServletPath();
			if (path == null || "".equals(path))
			{
				path = "/";
			}
			buffer.append(path);
		}

		return buffer;
	}
}
