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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.Page;
import wicket.RequestCycle;
import wicket.Response;
import wicket.WicketRuntimeException;
import wicket.protocol.http.WebRequestCycle;
import wicket.request.ClientInfo;

/**
 * 
 * Base RequestCycle implementation for portlets. There is two implementations
 * of this abstract class, PortletRenderRequestCycle for portlet render
 * requests, and PortletActionRequesCycle for portlet action requests.
 * 
 * @see PortletRenderRequestCycle
 * @see PortletActionRequestCycle
 * 
 * 
 * @author Janne Hietam&auml;ki
 * 
 */
public abstract class PortletRequestCycle extends RequestCycle
{
	/** Logging object */
	private static final Log log = LogFactory.getLog(WebRequestCycle.class);

	/**
	 * Constructor which simply passes arguments to superclass for storage
	 * there.
	 * 
	 * @param session
	 *            The session
	 * @param request
	 *            The request
	 * @param response
	 *            The response
	 */
	public PortletRequestCycle(final WicketPortletSession session,
			final WicketPortletRequest request, final Response response)
	{
		super(session, request, response);
	}

	/**
	 * @return Request as a PortletRequest
	 */
	public WicketPortletRequest getPortletRequest()
	{
		return (WicketPortletRequest)request;
	}

	/**
	 * @return Response as a PortletResponse
	 */
	public WicketPortletResponse getPortletResponse()
	{
		return (WicketPortletResponse)response;
	}

	/**
	 * @return Session as a PortletSession
	 */
	public WicketPortletSession getPortletSession()
	{
		return (WicketPortletSession)session;
	}

	/*
	 * @see wicket.RequestCycle#redirectTo(wicket.Page)
	 */
	public final void redirectTo(final Page page)
	{
		throw new WicketRuntimeException("Portlet can't do a redirect");
	}

	/**
	 * @see wicket.RequestCycle#newClientInfo()
	 */
	protected ClientInfo newClientInfo()
	{
		throw new WicketRuntimeException("ClientInfo not yet implemented");
	}
}