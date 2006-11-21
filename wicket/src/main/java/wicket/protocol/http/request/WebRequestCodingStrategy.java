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
package wicket.protocol.http.request;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.Application;
import wicket.Component;
import wicket.IPageMap;
import wicket.IRedirectListener;
import wicket.IRequestTarget;
import wicket.Page;
import wicket.PageMap;
import wicket.PageParameters;
import wicket.Request;
import wicket.RequestCycle;
import wicket.RequestListenerInterface;
import wicket.Session;
import wicket.WicketRuntimeException;
import wicket.protocol.http.WebRequest;
import wicket.protocol.http.WebRequestCycle;
import wicket.request.IRequestCodingStrategy;
import wicket.request.IRequestTargetMountsInfo;
import wicket.request.RequestParameters;
import wicket.request.target.coding.IRequestTargetUrlCodingStrategy;
import wicket.request.target.component.BookmarkableListenerInterfaceRequestTarget;
import wicket.request.target.component.IBookmarkablePageRequestTarget;
import wicket.request.target.component.IPageRequestTarget;
import wicket.request.target.component.listener.IListenerInterfaceRequestTarget;
import wicket.request.target.resource.ISharedResourceRequestTarget;
import wicket.util.string.AppendingStringBuffer;
import wicket.util.string.Strings;

/**
 * Request parameters factory implementation that uses http request parameters
 * and path info to construct the request parameters object.
 * 
 * @author Eelco Hillenius
 * @author Jonathan Locke
 */
public class WebRequestCodingStrategy implements IRequestCodingStrategy, IRequestTargetMountsInfo
{
	/** Name of interface target query parameter */
	public static final String NAME_SPACE = "wicket:";

	/** Name of interface target query parameter */
	public static final String INTERFACE_PARAMETER_NAME = NAME_SPACE + "interface";

	/** AJAX query parameter name */
	public static final String BEHAVIOR_ID_PARAMETER_NAME = NAME_SPACE + "behaviorId";

	/** Parameter name used all over the place */
	public static final String BOOKMARKABLE_PAGE_PARAMETER_NAME = NAME_SPACE + "bookmarkablePage";

	/** Pagemap parameter constant */
	public static final String PAGEMAP = NAME_SPACE + "pageMapName";

	/**
	 * Parameter name that tells decode to ignore this request if the
	 * page+version encoded in the url is not on top of the stack. The value of
	 * this parameter is not important, it simply has to be present to enable
	 * the behavior
	 */
	public static final String IGNORE_IF_NOT_ACTIVE_PARAMETER_NAME = NAME_SPACE
			+ "ignoreIfNotActive";

	/**
	 * Various settings used to configure this strategy
	 * 
	 * @author ivaynberg
	 */
	public static class Settings
	{
		/** whether or not mount paths are case sensitive */
		private boolean mountsCaseSensitive = true;

		/**
		 * Construct.
		 */
		public Settings()
		{
		}

		/**
		 * Sets mountsCaseSensitive.
		 * 
		 * @param mountsCaseSensitive
		 *            mountsCaseSensitive
		 */
		public void setMountsCaseSensitive(boolean mountsCaseSensitive)
		{
			this.mountsCaseSensitive = mountsCaseSensitive;
		}

		/**
		 * Gets caseSensitive.
		 * 
		 * @return caseSensitive
		 */
		public boolean areMountsCaseSensitive()
		{
			return mountsCaseSensitive;
		}
	}


	/** settings for the coding strategy */
	private final Settings settings;

	/** log. */
	private static final Log log = LogFactory.getLog(WebRequestCodingStrategy.class);

	/**
	 * map of path mounts for mount encoders on paths.
	 * <p>
	 * mountsOnPath is sorted by longest paths first to improve resolution of
	 * possible path conflicts. <br />
	 * For example: <br/> we mount Page1 on /page and Page2 on /page/test <br />
	 * Page1 uses a parameters encoder that only encodes parameter values <br />
	 * now suppose we want to access Page1 with a single paramter param="test".
	 * we have a url collision since both pages can be access with /page/test
	 * <br />
	 * the sorting by longest path first guarantees that the iterator will
	 * return the mount /page/test before it returns mount /page therefore
	 * giving deterministic behavior to path resolution by always trying to
	 * match the longest possible path first.
	 * </p>
	 */
	private final MountsMap mountsOnPath;

	/** cached url prefix. */
	private CharSequence urlPrefix;

	/**
	 * Construct.
	 */
	public WebRequestCodingStrategy()
	{
		this(new Settings());
	}

	/**
	 * Construct.
	 * 
	 * @param settings
	 */
	public WebRequestCodingStrategy(Settings settings)
	{
		if (settings == null)
		{
			throw new IllegalArgumentException("Argument [[settings]] cannot be null");
		}
		this.settings = settings;
		mountsOnPath = new MountsMap(settings.areMountsCaseSensitive());
	}


	/**
	 * @see wicket.request.IRequestCodingStrategy#decode(wicket.Request)
	 */
	public final RequestParameters decode(final Request request)
	{
		final RequestParameters parameters = new RequestParameters();
		final String pathInfo = getRequestPath(request);
		parameters.setPath(pathInfo);
		parameters.setPageMapName(request.getParameter(PAGEMAP));
		addInterfaceParameters(request, parameters);
		addBookmarkablePageParameters(request, parameters);
		addResourceParameters(request, parameters);
		parameters.setBehaviorId(request.getParameter(BEHAVIOR_ID_PARAMETER_NAME));
		if (request.getParameter(IGNORE_IF_NOT_ACTIVE_PARAMETER_NAME) != null)
		{
			parameters.setOnlyProcessIfPathActive(true);
		}

		Map map = request.getParameterMap();
		Iterator iterator = map.keySet().iterator();
		while (iterator.hasNext())
		{
			String key = (String)iterator.next();
			if (key.startsWith(NAME_SPACE))
			{
				iterator.remove();
			}
		}
		parameters.setParameters(map);
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
	public final CharSequence encode(final RequestCycle requestCycle,
			final IRequestTarget requestTarget)
	{
		// first check whether the target was mounted
		CharSequence path = pathForTarget(requestTarget);
		if (path != null)
		{
			CharSequence prefix = urlPrefix(requestCycle);
			// special check if the prefix ends on '/' because a mount always
			// starts with '/'
			if (prefix.charAt(prefix.length() - 1) == '/')
				prefix = prefix.subSequence(0, prefix.length() - 1);
			final AppendingStringBuffer buffer = new AppendingStringBuffer(prefix.length()
					+ path.length());
			buffer.append(prefix);
			buffer.append(path);
			return requestCycle.getOriginalResponse().encodeURL(buffer);
		}

		// no mount found; go on with default processing
		if (requestTarget instanceof IBookmarkablePageRequestTarget)
		{
			return encode(requestCycle, (IBookmarkablePageRequestTarget)requestTarget);
		}
		else if (requestTarget instanceof ISharedResourceRequestTarget)
		{
			return encode(requestCycle, (ISharedResourceRequestTarget)requestTarget);
		}
		else if (requestTarget instanceof IListenerInterfaceRequestTarget)
		{
			return encode(requestCycle, (IListenerInterfaceRequestTarget)requestTarget);
		}
		else if (requestTarget instanceof IPageRequestTarget)
		{
			return encode(requestCycle, (IPageRequestTarget)requestTarget);
		}

		// fallthough for non-default request targets
		String url = doEncode(requestCycle, requestTarget);
		if (url != null)
		{
			return url;
		}
		// Just return null intead of throwing an exception. So that it can be
		// handled better
		return null;
	}

	/**
	 * @see wicket.request.IRequestTargetMountsInfo#listMounts()
	 */
	public IRequestTargetUrlCodingStrategy[] listMounts()
	{
		return (IRequestTargetUrlCodingStrategy[])mountsOnPath.strategies().toArray(
				new IRequestTargetUrlCodingStrategy[mountsOnPath.size()]);
	}

	/**
	 * @see wicket.request.IRequestTargetMounter#urlCodingStrategyForPath(java.lang.String)
	 */
	public final IRequestTargetUrlCodingStrategy urlCodingStrategyForPath(String path)
	{
		if (path == null)
		{
			return (IRequestTargetUrlCodingStrategy)mountsOnPath.strategyForMount(null);
		}
		else if (!path.equals("/")) // ignore root paths.. is this the right
		// path?
		{
			IRequestTargetUrlCodingStrategy strategy = mountsOnPath.strategyForPath(path);
			if (strategy != null)
			{
				return strategy;
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
		if (path.equals("/"))
		{
			throw new IllegalArgumentException(
					"The mount path '/' is reserved for the application home page");
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

		if (mountsOnPath.strategyForMount(path) != null)
		{
			throw new WicketRuntimeException(path + " is already mounted for "
					+ mountsOnPath.strategyForMount(path));
		}
		mountsOnPath.mount(path, encoder);
	}

	/**
	 * @see wicket.request.IRequestCodingStrategy#pathForTarget(wicket.IRequestTarget)
	 */
	public final CharSequence pathForTarget(IRequestTarget requestTarget)
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
	 * @see wicket.request.IRequestCodingStrategy#targetForRequest(wicket.request.RequestParameters)
	 */
	public final IRequestTarget targetForRequest(RequestParameters requestParameters)
	{
		IRequestTargetUrlCodingStrategy encoder = urlCodingStrategyForPath(requestParameters
				.getPath());
		return (encoder != null) ? encoder.decode(requestParameters) : null;
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

		mountsOnPath.unmount(path);
	}

	/**
	 * Adds bookmarkable page related parameters (page alias and optionally page
	 * parameters). Any bookmarkable page alias mount will override this method;
	 * hence if a mount is found, this method will not be called.
	 * 
	 * If you override this method to behave different then also
	 * {@link #encode(RequestCycle, IBookmarkablePageRequestTarget)} should be
	 * overridden to by in sync with that behaviour.
	 * 
	 * @param request
	 *            the incoming request
	 * @param parameters
	 *            the parameters object to set the found values on
	 */
	protected void addBookmarkablePageParameters(final Request request,
			final RequestParameters parameters)
	{
		final String requestString = request
				.getParameter(WebRequestCodingStrategy.BOOKMARKABLE_PAGE_PARAMETER_NAME);
		if (requestString != null)
		{
			final String[] components = Strings.split(requestString, Component.PATH_SEPARATOR);
			if (components.length != 2)
			{
				throw new WicketRuntimeException("Invalid bookmarkablePage parameter: "
						+ requestString + ", expected: 'pageMapName:pageClassName'");
			}

			// Extract any pagemap name
			final String pageMapName = components[0];
			parameters.setPageMapName(pageMapName.length() == 0
					? PageMap.DEFAULT_NAME
					: pageMapName);

			// Extract bookmarkable page class name
			final String pageClassName = components[1];
			parameters.setBookmarkablePageClass(pageClassName);
		}
	}

	/**
	 * Adds page related parameters (path and pagemap and optionally version and
	 * interface).
	 * 
	 * If you override this method to behave different then also
	 * {@link #encode(RequestCycle, IListenerInterfaceRequestTarget)} should be
	 * overridden to by in sync with that behaviour.
	 * 
	 * @param request
	 *            the incoming request
	 * @param parameters
	 *            the parameters object to set the found values on
	 */
	protected void addInterfaceParameters(final Request request, final RequestParameters parameters)
	{
		// Format of interface target parameter is
		// <page-map-name>:<path>:<version>:<interface>
		final String requestString = request.getParameter(INTERFACE_PARAMETER_NAME);
		if (requestString != null)
		{
			// Split into array of strings
			String[] pathComponents = Strings.split(requestString, Component.PATH_SEPARATOR);

			// There must be at least 4 components
			if (pathComponents.length < 4)
			{
				throw new WicketRuntimeException("Internal error parsing "
						+ INTERFACE_PARAMETER_NAME + " = " + requestString);
			}

			// Set pagemap name
			final String pageMapName = pathComponents[0];
			parameters.setPageMapName(pageMapName.length() == 0
					? PageMap.DEFAULT_NAME
					: pageMapName);

			// Extract interface name after last colon
			final String interfaceName = pathComponents[pathComponents.length - 1];
			parameters.setInterfaceName(interfaceName.length() != 0
					? interfaceName
					: IRedirectListener.INTERFACE.getName());

			// Extract version
			final String versionNumberString = pathComponents[pathComponents.length - 2];
			final int versionNumber = Strings.isEmpty(versionNumberString) ? 0 : Integer
					.parseInt(versionNumberString);
			parameters.setVersionNumber(versionNumber);

			// Component path is everything after pageMapName and before version
			final int start = pageMapName.length() + 1;
			final int end = requestString.length() - interfaceName.length()
					- versionNumberString.length() - 2;
			final String componentPath = requestString.substring(start, end);
			parameters.setComponentPath(componentPath);
		}
	}

	/**
	 * Adds (shared) resource related parameters (resource key). Any shared
	 * resource key mount will override this method; hence if a mount is found,
	 * this method will not be called.
	 * 
	 * If you override this method to behave different then also
	 * {@link #encode(RequestCycle, ISharedResourceRequestTarget)} should be
	 * overridden to by in sync with that behaviour.
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
			int ix = "/resources/".length();
			if (pathInfo.length() > ix)
			{
				StringBuffer path = new StringBuffer(pathInfo.substring(ix));
				int ixSemiColon = path.indexOf(";");
				// strip off any jsession id
				if (ixSemiColon != -1)
				{
					int ixEnd = path.indexOf("?");
					if (ixEnd == -1)
					{
						ixEnd = path.length();
					}
					path.delete(ixSemiColon, ixEnd);
				}
				parameters.setResourceKey(path.toString());
			}
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
	 * If you override this method to behave different then also
	 * {@link #addBookmarkablePageParameters(Request, RequestParameters)} should
	 * be overridden to by in sync with that behaviour.
	 * 
	 * @param requestCycle
	 *            the current request cycle
	 * @param requestTarget
	 *            the target to encode
	 * @return the encoded url
	 */
	protected CharSequence encode(RequestCycle requestCycle,
			IBookmarkablePageRequestTarget requestTarget)
	{
		// Begin encoding URL
		final AppendingStringBuffer url = new AppendingStringBuffer(64);
		url.append(urlPrefix(requestCycle));

		// Get page Class
		final Class pageClass = requestTarget.getPageClass();
		final Application application = Application.get();

		// Find pagemap name
		String pageMapName = requestTarget.getPageMapName();
		if (pageMapName == null)
		{
			IRequestTarget currentTarget = requestCycle.getRequestTarget();
			if (currentTarget instanceof IPageRequestTarget)
			{
				Page currentPage = ((IPageRequestTarget)currentTarget).getPage();
				final IPageMap pageMap = currentPage.getPageMap();
				if (pageMap.isDefault())
				{
					pageMapName = "";
				}
				else
				{
					pageMapName = pageMap.getName();
				}
			}
			else
			{
				pageMapName = "";
			}
		}

		boolean firstParameter = true;
		if (!application.getHomePage().equals(pageClass) || !"".equals(pageMapName)
				|| requestTarget instanceof BookmarkableListenerInterfaceRequestTarget)
		{
			firstParameter = false;
			url.append('?');
			url.append(WebRequestCodingStrategy.BOOKMARKABLE_PAGE_PARAMETER_NAME);
			url.append('=');


			// Add <page-map-name>:<bookmarkable-page-class>
			String pageClassName = pageClass.getName();

			/*
			 * Encode the url so it is correct even for class names containing
			 * non ASCII characters, like ä, æ, ø, å etc.
			 * 
			 * The reason for this is that when redirecting to these
			 * bookmarkable pages, we need to have the url encoded correctly
			 * because we can't rely on the browser to interpret the unencoded
			 * url correctly.
			 */
			try
			{
				pageClassName = URLEncoder.encode(pageClassName, "UTF-8");
			}
			catch (UnsupportedEncodingException e)
			{
				throw new RuntimeException(e);
			}
			url.append(pageMapName + Component.PATH_SEPARATOR + pageClassName);
		}

		// Is it a bookmarkable interface listener?
		if (requestTarget instanceof BookmarkableListenerInterfaceRequestTarget)
		{
			BookmarkableListenerInterfaceRequestTarget listenerTarget = (BookmarkableListenerInterfaceRequestTarget)requestTarget;
			if (firstParameter == true)
			{
				url.append("?");
			}
			else
			{
				url.append("&");
			}
			firstParameter = false;
			url.append(INTERFACE_PARAMETER_NAME);
			url.append("=");
			url.append(Component.PATH_SEPARATOR);
			url.append(listenerTarget.getComponentPath());
			url.append(Component.PATH_SEPARATOR);
			url.append(Component.PATH_SEPARATOR);
			url.append(listenerTarget.getInterfaceName());
		}

		// Get page parameters
		final PageParameters parameters = requestTarget.getPageParameters();
		if (parameters != null)
		{
			Iterator it = parameters.keySet().iterator();
			while (it.hasNext())
			{
				final String key = (String)it.next();
				final String value = parameters.getString(key);
				if (value != null)
				{
					String escapedValue = value;
					try
					{
						escapedValue = URLEncoder.encode(escapedValue, application
								.getRequestCycleSettings().getResponseRequestEncoding());
					}
					catch (UnsupportedEncodingException ex)
					{
						log.error(ex.getMessage(), ex);
					}
					if (!firstParameter)
					{
						url.append('&');
					}
					else
					{
						firstParameter = false;
						url.append('?');
					}
					url.append(key);
					url.append('=');
					url.append(escapedValue);
				}
			}
		}
		return requestCycle.getOriginalResponse().encodeURL(url);
	}

	/**
	 * Encode a shared resource target.
	 * 
	 * If you override this method to behave different then also
	 * {@link #addResourceParameters(Request, RequestParameters)} should be
	 * overridden to by in sync with that behaviour.
	 * 
	 * @param requestCycle
	 *            the current request cycle
	 * @param requestTarget
	 *            the target to encode
	 * @return the encoded url
	 */
	protected CharSequence encode(RequestCycle requestCycle,
			ISharedResourceRequestTarget requestTarget)
	{
		final CharSequence prefix = urlPrefix(requestCycle);
		final String sharedResourceKey = requestTarget.getResourceKey();
		if ((sharedResourceKey == null) || (sharedResourceKey.trim().length() == 0))
		{
			return prefix;
		}
		else
		{
			final AppendingStringBuffer buffer = new AppendingStringBuffer(sharedResourceKey
					.length()
					+ prefix.length() + 11);
			buffer.append(prefix);
			if ((buffer.length() > 0) && buffer.charAt(buffer.length() - 1) == '/')
			{
				buffer.append("resources/");
			}
			else
			{
				buffer.append("/resources/");
			}
			buffer.append(sharedResourceKey);
			Map map = requestTarget.getRequestParameters().getParameters();
			if (map != null && map.size() > 0)
			{
				buffer.append('?');
				Iterator it = map.entrySet().iterator();
				while (it.hasNext())
				{
					Map.Entry entry = (Entry)it.next();
					buffer.append(entry.getKey());
					buffer.append('=');
					buffer.append(entry.getValue());
					buffer.append('&');
				}
				buffer.setLength(buffer.length() - 1);
			}
			return requestCycle.getOriginalResponse().encodeURL(buffer);
		}
	}

	/**
	 * Encode a listener interface target.
	 * 
	 * If you override this method to behave different then also
	 * {@link #addInterfaceParameters(Request, RequestParameters)} should be
	 * overridden to by in sync with that behaviour.
	 * 
	 * @param requestCycle
	 *            the current request cycle
	 * @param requestTarget
	 *            the target to encode
	 * @return the encoded url
	 */
	protected CharSequence encode(RequestCycle requestCycle,
			IListenerInterfaceRequestTarget requestTarget)
	{
		final RequestListenerInterface rli = requestTarget.getRequestListenerInterface();

		// Start string buffer for url
		final AppendingStringBuffer url = new AppendingStringBuffer(64);
		url.append(urlPrefix(requestCycle));
		url.append('?');
		url.append(INTERFACE_PARAMETER_NAME);
		url.append('=');

		// Get component and page for request target
		final Component component = requestTarget.getTarget();
		final Page page = component.getPage();

		// Add pagemap
		final IPageMap pageMap = page.getPageMap();
		if (!pageMap.isDefault())
		{
			url.append(pageMap.getName());
		}
		url.append(Component.PATH_SEPARATOR);

		// Add path to component
		url.append(component.getPath());
		url.append(Component.PATH_SEPARATOR);

		// Add version
		final int versionNumber = component.getPage().getCurrentVersionNumber();
		if (!rli.getRecordsPageVersion())
		{
			url.append(Page.LATEST_VERSION);
		}
		else if (versionNumber > 0)
		{
			url.append(versionNumber);
		}
		url.append(Component.PATH_SEPARATOR);

		// Add listener interface
		final String listenerName = rli.getName();
		if (!IRedirectListener.INTERFACE.getName().equals(listenerName))
		{
			url.append(listenerName);
		}

		return requestCycle.getOriginalResponse().encodeURL(url);
	}

	/**
	 * Encode a page target.
	 * 
	 * @param requestCycle
	 *            the current request cycle
	 * @param requestTarget
	 *            the target to encode
	 * @return the encoded url
	 */
	protected CharSequence encode(RequestCycle requestCycle, IPageRequestTarget requestTarget)
	{
		// Get the page we want a url from:
		Page page = requestTarget.getPage();

		// A url to a page is the IRedirectListener interface:
		CharSequence urlRedirect = page.urlFor(IRedirectListener.INTERFACE);

		// Touch the page once because it could be that it did go from stateless
		// to statefull or it was a internally made page where just a url must
		// be made for (frames)
		Session.get().touch(page);
		return urlRedirect;
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
		// TODO Post 1.2: Performance: Optimize algorithm if possible and/ or
		// cache lookup results
		for (Iterator i = mountsOnPath.strategies().iterator(); i.hasNext();)
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
	protected final CharSequence urlPrefix(final RequestCycle requestCycle)
	{
		if (urlPrefix == null)
		{
			final AppendingStringBuffer buffer = new AppendingStringBuffer();
			final WebRequest request = ((WebRequestCycle)requestCycle).getWebRequest();
			if (request != null)
			{
				String contextPath = Application.get().getApplicationSettings().getContextPath();
				if (contextPath == null)
				{
					contextPath = ((WebRequestCycle)RequestCycle.get()).getWebRequest()
							.getContextPath();
					if (contextPath == null)
					{
						contextPath = "";
					}
				}
				if (!contextPath.equals("/"))
				{
					buffer.append(contextPath);
				}
				String path = request.getServletPath();
				if (path != null && !path.equals(""))
				{
					if (!buffer.endsWith("/") && !path.startsWith("/"))
						buffer.append("/");
					buffer.append(path);
				}
			}
			// special check, if everything is empty then we need to define '/'
			// as urlPrefix
			// else all urls get relative this is bad for mounts.
			// Except for mounts who have to do a special check else mounts get:
			// //mount
			// see encode(RequestCycle,IRequestTarget) when a mount is found.
			if (buffer.length() == 0)
			{
				urlPrefix = "/";
			}
			else
			{
				urlPrefix = buffer;
			}
		}
		return urlPrefix;
	}

	/**
	 * Map used to store mount paths and their corresponding url coding
	 * strategies.
	 * 
	 * @author ivaynberg
	 */
	private static class MountsMap
	{
		private static final long serialVersionUID = 1L;

		/** case sensitive flag */
		private final boolean caseSensitiveMounts;

		/** backing map */
		private final TreeMap map;

		/**
		 * Constructor
		 * 
		 * @param caseSensitiveMounts
		 *            whether or not keys of this map are case-sensitive
		 */
		public MountsMap(boolean caseSensitiveMounts)
		{
			map = new TreeMap(LENGTH_COMPARATOR);
			this.caseSensitiveMounts = caseSensitiveMounts;
		}

		/**
		 * Checks if the specified path matches any mount, and if so returns the
		 * coding strategy for that mount. Returns null if the path doesnt match
		 * any mounts.
		 * 
		 * NOTE: path here is not the mount - it is the full url path
		 * 
		 * @param path
		 *            non-null url path
		 * @return coding strategy or null
		 */
		public IRequestTargetUrlCodingStrategy strategyForPath(String path)
		{
			if (path == null)
			{
				throw new IllegalArgumentException("Argument [[path]] cannot be null");
			}
			if (caseSensitiveMounts == false)
			{
				path = path.toLowerCase();
			}
			for (final Iterator it = map.entrySet().iterator(); it.hasNext();)
			{
				final Map.Entry entry = (Entry)it.next();
				final String key = (String)entry.getKey();
				if (path.startsWith(key))
				{
					return (IRequestTargetUrlCodingStrategy)entry.getValue();
				}
			}
			return null;
		}


		/**
		 * @return number of mounts in the map
		 */
		public int size()
		{
			return map.size();
		}

		/**
		 * @return collection of coding strategies associated with every mount
		 */
		public Collection strategies()
		{
			return map.values();
		}


		/**
		 * Removes mount from the map
		 * 
		 * @param mount
		 */
		public void unmount(String mount)
		{
			if (caseSensitiveMounts == false && mount != null)
			{
				mount = mount.toLowerCase();
			}

			map.remove(mount);
		}


		/**
		 * Gets the coding strategy for the specified mount path
		 * 
		 * @param mount
		 *            mount paht
		 * @return associated coding strategy or null if none
		 */
		public IRequestTargetUrlCodingStrategy strategyForMount(String mount)
		{
			if (caseSensitiveMounts == false && mount != null)
			{
				mount = mount.toLowerCase();
			}

			return (IRequestTargetUrlCodingStrategy)map.get(mount);
		}

		/**
		 * Associates a mount with a coding strategy
		 * 
		 * @param mount
		 * @param encoder
		 * @return previous coding strategy associated with the mount, or null
		 *         if none
		 */
		public IRequestTargetUrlCodingStrategy mount(String mount,
				IRequestTargetUrlCodingStrategy encoder)
		{
			if (caseSensitiveMounts == false && mount != null)
			{
				mount = mount.toLowerCase();
			}
			return (IRequestTargetUrlCodingStrategy)map.put(mount, encoder);
		}


		/** Comparator implementation that sorts longest strings first */
		private static final Comparator LENGTH_COMPARATOR = new Comparator()
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
					final String lhs = (String)o1;
					final String rhs = (String)o2;
					return rhs.compareTo(lhs);
				}
			}
		};

	}
}
