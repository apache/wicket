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
package wicket.protocol.http.request;

import wicket.Request;
import wicket.request.compound.IRequestParametersFactory;
import wicket.request.compound.RequestParameters;
import wicket.util.string.Strings;

/**
 * Request parameters factory implementation that uses http request parameters
 * and path info to construct the request parameters object.
 * 
 * @author Eelco Hillenius
 */
public class WebRequestParametersFactory implements IRequestParametersFactory
{

	/**
	 * Construct.
	 */
	public WebRequestParametersFactory()
	{
	}

	/**
	 * @see wicket.request.compound.IRequestParametersFactory#newParameters(wicket.Request)
	 */
	public RequestParameters newParameters(Request request)
	{
		RequestParameters parameters = new RequestParameters();
		String pathInfo = request.getPath();
		parameters.setPathInfo(pathInfo);

		addPageParameters(request, parameters);

		addBookmarkablePageParameters(request, parameters);

		addResourceParameters(request, parameters);

		return parameters;
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
	 * parameters).
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
	 * Adds (shared) resource related parameters (resource key).
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
}
