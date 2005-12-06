/*
 * $Id$ $Revision$ $Date$
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
package wicket.examples.hellobrowser;

import wicket.Response;
import wicket.markup.html.pages.BrowserInfoPage;
import wicket.protocol.http.WebRequest;
import wicket.protocol.http.WebRequestCycle;
import wicket.protocol.http.WebSession;
import wicket.protocol.http.request.WebClientInfo;
import wicket.request.ClientInfo;
import wicket.request.PageRequestTarget;

/**
 * Custom request cycle that does a redirect to a javascript properties test
 * page to do some client snooping.
 * 
 * @author Eelco Hillenius
 */
public class HelloBrowserRequestCycle extends WebRequestCycle
{

	/**
	 * Construct.
	 * 
	 * @param session
	 *            the web session
	 * @param request
	 *            the web request
	 * @param response
	 *            the response
	 */
	public HelloBrowserRequestCycle(WebSession session, WebRequest request, Response response)
	{
		super(session, request, response);
	}

	/**
	 * Construct a new {@link WebClientInfo} object that does basic analizing
	 * based on the user-agent request header, and set the request target to a
	 * snoop page. Nice thing about this is that it is only done when needed.
	 * Danger is that other code might override setRequestTarget after this,
	 * resulting in the test not being done. Alternatively you might want to do
	 * this in checkAccess or something similar.
	 * 
	 * @see wicket.RequestCycle#newClientInfo()
	 */
	protected ClientInfo newClientInfo()
	{
		setRequestTarget(new PageRequestTarget(new BrowserInfoPage(getRequest().getURL())));
		return new WebClientInfo(this);
	}
}
