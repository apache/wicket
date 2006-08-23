/*
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
package wicket.protocol.http.portlet;

import javax.portlet.ActionResponse;
import javax.portlet.PortletResponse;
import javax.portlet.PortletURL;
import javax.portlet.RenderResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.Application;
import wicket.Component;
import wicket.IRedirectListener;
import wicket.IRequestTarget;
import wicket.IResourceListener;
import wicket.Page;
import wicket.PageMap;
import wicket.PageParameters;
import wicket.Request;
import wicket.RequestCycle;
import wicket.RequestListenerInterface;
import wicket.Session;
import wicket.WicketRuntimeException;
import wicket.protocol.http.request.AbstractWebRequestCodingStrategy;
import wicket.protocol.http.request.WebRequestCodingStrategy;
import wicket.request.RequestParameters;
import wicket.request.target.coding.IRequestTargetUrlCodingStrategy;
import wicket.request.target.component.IBookmarkablePageRequestTarget;
import wicket.request.target.component.IPageRequestTarget;
import wicket.request.target.component.listener.IListenerInterfaceRequestTarget;
import wicket.request.target.resource.ISharedResourceRequestTarget;
import wicket.util.string.AppendingStringBuffer;
import wicket.util.string.Strings;

/**
 * Request coding strategy implementation that uses PortletURL object to create
 * links.
 * 
 * Also stores response/render parameters as portlet render parameters.
 * 
 * @author Janne Hietam&auml;ki
 */

//TODO: this should not really implement IRequestTargetMounter

public final class PortletRequestCodingStrategy extends AbstractWebRequestCodingStrategy
{

    /** Name of component path query parameter */
    public static final String COMPONENT_PATH_PARAMETER_NAME = NAME_SPACE + "path";
	
    /** Name of version number query parameter */
    public static final String VERSION_PARAMETER_NAME = NAME_SPACE + "version";
    
	/** log. */
	private static final Log log = LogFactory.getLog(PortletRequestCodingStrategy.class);

	
	/** cached url prefix. */
	private CharSequence urlPrefix;
	
	/**
	 * Construct.
	 */
	public PortletRequestCodingStrategy()
	{
	}

	/**
	 * Encode the given request target and one of the delegation methods will be
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

		if (requestTarget instanceof IBookmarkablePageRequestTarget)
		{
			return encodeRequest(requestCycle, (IBookmarkablePageRequestTarget)requestTarget);
		}
		else if (requestTarget instanceof ISharedResourceRequestTarget)
		{
			return encodeRequest(requestCycle, (ISharedResourceRequestTarget)requestTarget);
		}
		else if (requestTarget instanceof IListenerInterfaceRequestTarget)
		{
			return encodeRequest(requestCycle, (IListenerInterfaceRequestTarget)requestTarget);
		}
		else if (requestTarget instanceof IPageRequestTarget)
		{
			return encodeRequest(requestCycle, (IPageRequestTarget)requestTarget);
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
		final String requestString = request.getParameter(INTERFACE_PARAMETER_NAME);
		if (requestString != null)
		{

			// Set pagemap name
			final String pageMapName = request.getParameter(PAGEMAP);
			parameters.setPageMapName(pageMapName != null && pageMapName.length() > 0
					? PageMap.DEFAULT_NAME
							: pageMapName);

			final String interfaceName = request.getParameter(INTERFACE_PARAMETER_NAME);
			parameters.setInterfaceName(interfaceName != null && interfaceName.length() != 0
					? interfaceName
							: IRedirectListener.INTERFACE.getName());

			final String versionNumberString = request.getParameter(VERSION_PARAMETER_NAME);
			final int versionNumber = Strings.isEmpty(versionNumberString) ? 0 : Integer
					.parseInt(versionNumberString);
			parameters.setVersionNumber(versionNumber);

			final String componentPath = request.getParameter(COMPONENT_PATH_PARAMETER_NAME);
			parameters.setComponentPath(componentPath);
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
	protected CharSequence encodeRequest(RequestCycle requestCycle,
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
	protected CharSequence encodeRequest(RequestCycle requestCycle,
			IListenerInterfaceRequestTarget requestTarget)
	{
		PortletURL url;
		final RequestListenerInterface rli = requestTarget.getRequestListenerInterface();

		try
		{
			url = getActionURL(requestCycle);
		}
		catch (WicketRuntimeException e)
		{
			/*
			 * 
			 * TODO
			 * 
			 * This is a kludge to allow page to be set stateful by calling
			 * page.urlFor. If we could set the page stateful manually, this
			 * could be removed.
			 * 
			 * Exception is thrown when getActionURL is called when not in the
			 * portlet render phase.
			 * 
			 * @see doSetRenderParameters(RequestCycle requestCycle,
			 *      IPageRequestTarget requestTarget)
			 */
			return null;
		}

		if(IResourceListener.class.isAssignableFrom(rli.getMethod().getDeclaringClass()))
		{
			return encodeServletRequest(requestCycle,requestTarget);
		}
		
		// Get component and page for request target
		final Component component = requestTarget.getTarget();
		final Page page = component.getPage();
		// Add pagemap
		final PageMap pageMap = page.getPageMap();
		if (!pageMap.isDefault())
		{
			url.setParameter(PAGEMAP, pageMap.getName());
		}
		// Add path to component
		url.setParameter(COMPONENT_PATH_PARAMETER_NAME, component.getPath());

		// Add version
		final int versionNumber = component.getPage().getCurrentVersionNumber();
		if (!rli.getRecordsPageVersion())
		{
			url.setParameter(VERSION_PARAMETER_NAME, String.valueOf(Page.LATEST_VERSION));
		}
		else if (versionNumber > 0)
		{
			url.setParameter(VERSION_PARAMETER_NAME, String.valueOf(versionNumber));
		}

		// Add listener interface
		final String listenerName = rli.getName();
		url.setParameter(INTERFACE_PARAMETER_NAME, listenerName);

		return url.toString();
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
	protected CharSequence encodeServletRequest(RequestCycle requestCycle,
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
		final PageMap pageMap = page.getPageMap();
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
	protected CharSequence encodeRequest(RequestCycle requestCycle,
			IBookmarkablePageRequestTarget requestTarget)
	{
		final PortletURL url = getActionURL(requestCycle);

		// Get page Class
		final Class pageClass = requestTarget.getPageClass();
		final Application application = Application.get();

		// Get page parameters
		final PageParameters parameters = requestTarget.getPageParameters();
		url.setParameters(parameters);

		url.setParameter(WebRequestCodingStrategy.BOOKMARKABLE_PAGE_PARAMETER_NAME, pageClass
				.getName());

		return url.toString();
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
			parameters.setBookmarkablePageClass(requestString);
		}
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
	protected CharSequence encodeRequest(RequestCycle requestCycle, IPageRequestTarget requestTarget)
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


	/*
	 * Set the render parameters to the portlet so we can later resolve current
	 * page parameters
	 */
	/**
	 * @param requestCycle
	 * @param requestTarget
	 */
	public void setRenderParameters(PortletRequestCycle requestCycle, IRequestTarget requestTarget)
	{
		if (requestTarget instanceof IBookmarkablePageRequestTarget)
		{
			doSetRenderParameters(requestCycle, (IBookmarkablePageRequestTarget)requestTarget);
		}
		else if (requestTarget instanceof IListenerInterfaceRequestTarget)
		{
			doSetRenderParameters(requestCycle, (IListenerInterfaceRequestTarget)requestTarget);
		}
		else if (requestTarget instanceof IPageRequestTarget)
		{
			doSetRenderParameters(requestCycle, (IPageRequestTarget)requestTarget);
		}
		else
		{
			// TODO: ??
			log.warn("Unable to set RenderParameters, unsupported IRequestTarget "
					+ requestTarget.getClass().getName());
		}
	}

	private void doSetRenderParameters(PortletRequestCycle requestCycle,
			IBookmarkablePageRequestTarget requestTarget)
	{
		ActionResponse response = getActionResponse(requestCycle);
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
				final PageMap pageMap = currentPage.getPageMap();
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
		if (!application.getHomePage().equals(pageClass) || !"".equals(pageMapName))
		{
			response.setRenderParameter(PAGEMAP, pageMapName);
			response.setRenderParameter(WebRequestCodingStrategy.BOOKMARKABLE_PAGE_PARAMETER_NAME,
					pageClass.getName());
		}
		response.setRenderParameters(requestTarget.getPageParameters());
	}

	private void doSetRenderParameters(PortletRequestCycle requestCycle,
			IListenerInterfaceRequestTarget requestTarget)
	{
		ActionResponse response = getActionResponse(requestCycle);
		final RequestListenerInterface rli = requestTarget.getRequestListenerInterface();

		// Get component and page for request target
		final Component component = requestTarget.getTarget();
		final Page page = component.getPage();

		// Add pagemap
		final PageMap pageMap = page.getPageMap();
		if (!pageMap.isDefault())
		{
			response.setRenderParameter(PAGEMAP, pageMap.getName());
		}
		// Add path to component
		response.setRenderParameter(COMPONENT_PATH_PARAMETER_NAME, component.getPath());

		// Add version
		final int versionNumber = component.getPage().getCurrentVersionNumber();
		if (!rli.getRecordsPageVersion())
		{
			response
			.setRenderParameter(VERSION_PARAMETER_NAME, String.valueOf(Page.LATEST_VERSION));
		}
		else if (versionNumber > 0)
		{
			response.setRenderParameter(VERSION_PARAMETER_NAME, String.valueOf(versionNumber));
		}

		// Add listener interface
		final String listenerName = rli.getName();
		response.setRenderParameter(INTERFACE_PARAMETER_NAME, listenerName);
	}


	private void doSetRenderParameters(RequestCycle requestCycle, IPageRequestTarget requestTarget)
	{
		ActionResponse response = getActionResponse(requestCycle);

		final PortletPage page = (PortletPage)requestTarget.getPage();

		/*
		 * Kludge: call page.urlFor to make the page stateful.
		 * 
		 * @see encodeRequest(RequestCycle requestCycle,
		 *      IListenerInterfaceRequestTarget requestTarget)
		 */
		
		page.urlFor(requestTarget);

		// Touch the page to make sure it will be added to the PageMap
		requestCycle.getSession().touch(page);

		// Add pagemap
		final PageMap pageMap = page.getPageMap();

		if (!pageMap.isDefault())
		{
			response.setRenderParameter(PAGEMAP, pageMap.getName());
		}
		// Add path to component
		response.setRenderParameter(COMPONENT_PATH_PARAMETER_NAME, page.getPath());

		// Add version
		final int versionNumber = page.getCurrentVersionNumber();
		response.setRenderParameter(VERSION_PARAMETER_NAME, String.valueOf(versionNumber));
		response
		.setRenderParameter(INTERFACE_PARAMETER_NAME, IRedirectListener.INTERFACE.getName());
	}

	/**
	 * @param requestCycle
	 * @return the PortletURL instance for portlet action URL
	 */
	private PortletURL getActionURL(RequestCycle requestCycle)
	{
		PortletRequestCycle portletRequestCycle = (PortletRequestCycle)requestCycle;
		PortletResponse response = portletRequestCycle.getPortletResponse().getPortletResponse();

		if (!(response instanceof RenderResponse))
		{
			throw new WicketRuntimeException("Unable to render URL while not in RenderResponse");
		}

		RenderResponse resp = (RenderResponse)response;
		return resp.createActionURL();
	}


	/**
	 * @param requestCycle
	 * @return the PortletURL instance for portlet render URL
	 */
	private PortletURL getRenderURL(RequestCycle requestCycle)
	{
		PortletRequestCycle portletRequestCycle = (PortletRequestCycle)requestCycle;
		PortletResponse response = portletRequestCycle.getPortletResponse().getPortletResponse();
		if (!(response instanceof RenderResponse))
		{
			throw new WicketRuntimeException("Unable to render URL while not in RenderResponse");
		}

		RenderResponse resp = (RenderResponse)response;
		return resp.createRenderURL();
	}

	private ActionResponse getActionResponse(RequestCycle requestCycle)
	{
		PortletRequestCycle portletRequestCycle = (PortletRequestCycle)requestCycle;
		PortletResponse response = portletRequestCycle.getPortletResponse().getPortletResponse();
		if (!(response instanceof ActionResponse))
		{
			throw new WicketRuntimeException("Invalid state: ActionRquest not available");
		}
		return (ActionResponse)response;
	}


	// TODO: These should be removed, needs to be implemented because
	// IRequestCodingStrategy extends IRequestTargetMounter

	/*
	 * @see wicket.request.IRequestTargetMounter#pathForTarget(wicket.IRequestTarget)
	 */
	public CharSequence pathForTarget(IRequestTarget requestTarget)
	{
		return null;
	}

	/**
	 * @see wicket.request.IRequestTargetMounter#urlCodingStrategyForPath(java.lang.String)
	 */
	public final IRequestTargetUrlCodingStrategy urlCodingStrategyForPath(final String path)
	{
		return null;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see wicket.request.IRequestTargetMounter#mount(java.lang.String,
	 *      wicket.request.target.coding.IRequestTargetUrlCodingStrategy)
	 */
	public void mount(IRequestTargetUrlCodingStrategy urlCodingStrategy)
	{
		throw new WicketRuntimeException("Portlet can't do mounts");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see wicket.request.IRequestTargetMounter#unmount(java.lang.String)
	 */
	public void unmount(String path)
	{
		throw new WicketRuntimeException("Portlet can't do mounts");
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
			final WicketPortletRequest request = ((PortletRenderRequestCycle)requestCycle).getPortletRequest();
			if (request != null)
			{
				String contextPath = Application.get().getApplicationSettings().getContextPath();
				if (contextPath == null)
				{
					contextPath = ((PortletRenderRequestCycle)RequestCycle.get()).getPortletRequest().getPortletRequest().getContextPath();
					if (contextPath == null)
					{
						contextPath = "";
					}
				}
				if (!contextPath.equals("/"))
				{
					buffer.append(contextPath);
				}
			}
			urlPrefix = buffer;
		}
		return urlPrefix;
	}
}