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
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.Application;
import wicket.Component;
import wicket.IRequestTarget;
import wicket.Page;
import wicket.PageMap;
import wicket.PageParameters;
import wicket.Request;
import wicket.RequestCycle;
import wicket.WicketRuntimeException;
import wicket.protocol.http.WebRequest;
import wicket.protocol.http.WebRequestCycle;
import wicket.request.IBookmarkablePageRequestTarget;
import wicket.request.IListenerInterfaceRequestTarget;
import wicket.request.IPageRequestTarget;
import wicket.request.IRequestCodingStrategy;
import wicket.request.ISharedResourceRequestTarget;
import wicket.request.RequestParameters;
import wicket.request.target.coding.IRequestTargetUrlCodingStrategy;
import wicket.util.lang.Classes;
import wicket.util.string.Strings;

/**
 * Request parameters factory implementation that uses http request parameters
 * and path info to construct the request parameters object.
 * 
 * @author Eelco Hillenius
 */
public class WebRequestCodingStrategy implements IRequestCodingStrategy
{
	/** Comparator implementation that sorts longest strings first */
	private static final Comparator lengthComparator = new Comparator()
	{
		public int compare(Object o1, Object o2)
		{
			// longer first
			if (o1 == o2)
			{
				return 0;
			}
			else if (o1 == null)
			{
				return 1;
			}
			else if (o2 == null)
			{
				return -1;
			}
			else
			{
				String lhs = (String)o1;
				String rhs = (String)o2;
				return 0 - lhs.compareTo(rhs);
			}
		}

	};

	/** log. */
	private static Log log = LogFactory.getLog(WebRequestCodingStrategy.class);

	/**
	 * map of path mounts for mount encoders on paths.
	 * <p>
	 * mountsOnPath is sorted by longest paths first to improve resolution of
	 * possible path conflicts. <br />
	 * For example: <br/> we mount Page1 on /page and Page2 on /page/test <br />
	 * Page1 uses a parameters encoder that only encodes parameter values <br />
	 * now suppose we want to access Page1 with a single paramter param="test".
	 * we have a url collission since both pages can be access with /page/test
	 * <br />
	 * the sorting by longest path first guarantees that the iterator will
	 * return the mount /page/test before it returns mount /page therefore
	 * giving deterministic behaviour to path resolution by always trying to
	 * match the longest possible path first.
	 * </p>
	 */
	private SortedMap/* <String,IRequestTargetUrlCodingStrategy> */mountsOnPath = new TreeMap(
			lengthComparator);

	/** cached url prefix. */
	private String urlPrefix;

	/**
	 * Construct.
	 */
	public WebRequestCodingStrategy()
	{
	}

	/**
	 * @see wicket.request.IRequestCodingStrategy#decode(wicket.Request)
	 */
	public final RequestParameters decode(Request request)
	{
		RequestParameters parameters = new RequestParameters();
		String pathInfo = getRequestPath(request);
		parameters.setPath(pathInfo);
		addPageParameters(request, parameters);
		addBookmarkablePageParameters(request, parameters);
		addResourceParameters(request, parameters);
		return parameters;
	}

	/**
	 * Encode the given request target. If a mount is found, that mounted url
	 * will be returned. Otherwise, one of the delegation methods will be
	 * called. In case you are using custom targets that are not part of the
	 * default target hierarchy, you need to override
	 * {@link #doEncode(RequestCycle, IRequestTarget)}, which will be called
	 * after the defaults have been tried. When that doesn't provide a url
	 * either, and exception will be thrown saying that encoding could not be
	 * done.
	 * 
	 * @see wicket.request.IRequestCodingStrategy#encode(wicket.RequestCycle,
	 *      wicket.IRequestTarget)
	 */
	public final String encode(RequestCycle requestCycle, IRequestTarget requestTarget)
	{
		// first check whether the target was mounted
		IRequestTargetUrlCodingStrategy encoder = getMountEncoder(requestTarget);
		if (encoder != null)
		{
			final StringBuffer prefix = new StringBuffer(urlPrefix(requestCycle));
			return urlPrefix(requestCycle) + pathForTarget(requestTarget);
		}

		// no mount found; go on with default processing
		if (requestTarget instanceof IBookmarkablePageRequestTarget)
		{
			return encode(requestCycle, (IBookmarkablePageRequestTarget)requestTarget);
		}
		else if (requestTarget instanceof IListenerInterfaceRequestTarget)
		{
			return encode(requestCycle, (IListenerInterfaceRequestTarget)requestTarget);
		}
		else if (requestTarget instanceof ISharedResourceRequestTarget)
		{
			return encode(requestCycle, (ISharedResourceRequestTarget)requestTarget);
		}

		// fallthough for non-default request targets
		String url = doEncode(requestCycle, requestTarget);
		if (url != null)
		{
			return url;
		}

		// this method was not able to produce a url; throw an exception
		throw new WicketRuntimeException("unable to encode " + requestTarget);
	}

	/**
	 * @see wicket.request.IRequestTargetMounter#urlCodingStrategyForPath(java.lang.String)
	 */
	public final IRequestTargetUrlCodingStrategy urlCodingStrategyForPath(String path)
	{
		if (path == null)
		{
			return (IRequestTargetUrlCodingStrategy)mountsOnPath.get(null);
		}

		Iterator it = mountsOnPath.entrySet().iterator();
		while (it.hasNext())
		{
			Map.Entry entry = (Entry)it.next();
			String key = (String)entry.getKey();
			if (path.startsWith(key))
			{
				return (IRequestTargetUrlCodingStrategy)entry.getValue();
			}
		}
		return null;
	}

	/**
	 * @see wicket.request.IRequestTargetMounter#mount(java.lang.String,
	 *      wicket.request.target.coding.IRequestTargetUrlCodingStrategy)
	 */
	public final void mount(String path, IRequestTargetUrlCodingStrategy encoder)
	{
		if (path == null)
		{
			throw new IllegalArgumentException("Argument path must be not-null");
		}

		if (encoder == null)
		{
			throw new IllegalArgumentException("Argument encoder must be not-null");
		}

		// sanity check
		if (!path.startsWith("/"))
		{
			path = "/" + path;
		}

		if (mountsOnPath.containsKey(path))
		{
			throw new WicketRuntimeException(path + " is already mounted for "
					+ mountsOnPath.get(path));
		}
		mountsOnPath.put(path, encoder);
	}

	/**
	 * @see wicket.request.IRequestCodingStrategy#pathForTarget(wicket.IRequestTarget)
	 */
	public final String pathForTarget(IRequestTarget requestTarget)
	{
		// first check whether the target was mounted
		IRequestTargetUrlCodingStrategy encoder = getMountEncoder(requestTarget);
		if (encoder != null)
		{
			return encoder.encode(requestTarget);
		}
		return null;
	}

	/**
	 * @see wicket.request.IRequestCodingStrategy#targetForPath(java.lang.String)
	 */
	public final IRequestTarget targetForPath(String path)
	{
		IRequestTargetUrlCodingStrategy encoder = urlCodingStrategyForPath(path);
		return (encoder != null) ? encoder.decode(path) : null;
	}

	/**
	 * @see wicket.request.IRequestCodingStrategy#unmount(java.lang.String)
	 */
	public final void unmount(String path)
	{
		if (path == null)
		{
			throw new IllegalArgumentException("Argument path must be not-null");
		}

		// sanity check
		if (!path.startsWith("/"))
		{
			path = "/" + path;
		}

		mountsOnPath.remove(path);
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
		String pageClass = request.getParameter(PageParameters.BOOKMARKABLE_PAGE);
		parameters.setBookmarkablePageClass(pageClass);
		parameters.setParameters(request.getParameterMap());
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
			parameters.setBehaviourId(request.getParameter("behaviourId"));
		}
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
	 * In case you are using custom targets that are not part of the default
	 * target hierarchy, you need to override this method, which will be called
	 * after the defaults have been tried. When this doesn't provide a url
	 * either (returns null), an exception will be thrown by the encode method
	 * saying that encoding could not be done.
	 * 
	 * @param requestCycle
	 *            the current request cycle (for efficient access)
	 * 
	 * @param requestTarget
	 *            the request target
	 * @return the url to the provided target
	 */
	protected String doEncode(RequestCycle requestCycle, IRequestTarget requestTarget)
	{
		return null;
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
	protected final String encode(RequestCycle requestCycle,
			IBookmarkablePageRequestTarget requestTarget)
	{
		final Class pageClass = requestTarget.getPageClass();
		final Class homePageClass = requestCycle.getApplication().getRequiredPageSettings().getHomePage();

		// TODO fix homepage class

		final PageParameters parameters = requestTarget.getPageParameters();
		final StringBuffer url = new StringBuffer(urlPrefix(requestCycle));
		url.append("?bookmarkablePage=");
		url.append(pageClass.getName());

		String pageMapName = requestTarget.getPageMapName();
		if (pageMapName == null)
		{
			IRequestTarget currentTarget = requestCycle.getRequestTarget();
			if (currentTarget instanceof IPageRequestTarget)
			{
				Page currentPage = ((IPageRequestTarget)currentTarget).getPage();
				final PageMap pageMap = currentPage.getPageMap();
				if (!pageMap.isDefault())
				{
					url.append("&pagemap=");
					url.append(pageMap.getName());
				}
			}
		}
		else
		{
			url.append("&pagemap=");
			url.append(pageMapName);
		}

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
					url.append('&');
					url.append(key);
					url.append('=');
					url.append(escapedValue);
				}
			}
		}

		return requestCycle.getResponse().encodeURL(url.toString());
	}

	/**
	 * Encode a listener interface target.
	 * 
	 * @param requestCycle
	 *            the current request cycle
	 * @param requestTarget
	 *            the target to encode
	 * @return the encoded url
	 */
	protected final String encode(RequestCycle requestCycle,
			IListenerInterfaceRequestTarget requestTarget)
	{
		final StringBuffer url = new StringBuffer(urlPrefix(requestCycle));
		url.append("?path=");
		Component component = requestTarget.getTarget();
		url.append(component.getPath());
		Page currentPage = component.getPage();
		final PageMap pageMap = currentPage.getPageMap();
		if (!pageMap.isDefault())
		{
			url.append("&pagemap=");
			url.append(pageMap.getName());
		}
		int versionNumber = component.getPage().getCurrentVersionNumber();
		if (versionNumber > 0)
		{
			url.append("&version=");
			url.append(versionNumber);
		}

		String listenerName = Classes.name(requestTarget.getListenerMethod().getDeclaringClass());
		if (!"IRedirectListener".equals(listenerName))
		{
			url.append("&interface=");
			url.append(listenerName);
		}

		return requestCycle.getResponse().encodeURL(url.toString());

	}

	/**
	 * Encode a shared resource target.
	 * 
	 * @param requestCycle
	 *            the current request cycle
	 * @param requestTarget
	 *            the target to encode
	 * @return the encoded url
	 */
	protected final String encode(RequestCycle requestCycle,
			ISharedResourceRequestTarget requestTarget)
	{
		String prefix = urlPrefix(requestCycle).toString();
		String resourceKey = requestTarget.getResourceKey();
		if ((resourceKey == null) || (resourceKey.trim().length() == 0))
		{
			return prefix;
		}
		else
		{
			if (prefix.endsWith("/") || resourceKey.startsWith("/"))
			{
				return prefix + resourceKey;
			}

			return prefix + "/" + resourceKey;
		}
	}

	/**
	 * Gets the mount encoder for the given request target if any.
	 * 
	 * @param requestTarget
	 *            the request target to match
	 * @return the mount encoder if any
	 */
	protected IRequestTargetUrlCodingStrategy getMountEncoder(IRequestTarget requestTarget)
	{
		// TODO optimize algoritm if possible and/ or cache lookup results
		for (Iterator i = mountsOnPath.values().iterator(); i.hasNext();)
		{
			IRequestTargetUrlCodingStrategy encoder = (IRequestTargetUrlCodingStrategy)i.next();
			if (encoder.matches(requestTarget))
			{
				return encoder;
			}
		}

		return null;
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
	 * Gets prefix.
	 * 
	 * @param requestCycle
	 *            the request cycle
	 * 
	 * @return prefix
	 */
	protected final String urlPrefix(RequestCycle requestCycle)
	{
		if (urlPrefix == null)
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
			urlPrefix = buffer.toString();
		}
		return urlPrefix;
	}
}
