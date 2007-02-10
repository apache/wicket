/*
 * $Id$ $Revision$
 * $Date$
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
package wicket.markup.html;

import wicket.Resource;
import wicket.Response;
import wicket.protocol.http.WebResponse;

/**
 * Base class for web resources. See the base class {@link wicket.Resource} for
 * details on resources in general, including how they can be shared in an
 * application.
 * 
 * @author Jonathan Locke
 */
public abstract class WebResource extends Resource
{
	
	/**
	 * Construct.
	 */
	public WebResource()
	{
		super();
	}
	
	/**
	 * @see wicket.Resource#configureResponse(wicket.Response)
	 */
	protected final void configureResponse(final Response response)
	{
		if (response instanceof WebResponse)
		{
			setHeaders((WebResponse)response);
		}
	}

	/**
	 * Subclasses can override this to set there headers when the resource is being served.
	 * By default 2 headers will be set if the Resource is cacheable
	 * <pre>
	 * response.setDateHeader("Expires", System.currentTimeMillis() + (3600 * 1000));
	 * response.setHeader("Cache-Control", "max-age=" + 3600);
	 * </pre>
	 * So if a resource wants to control this or doesn't want to set this info it should 
	 * override this method and don't call super.
	 * 
	 * @param response The WebResponse where set(Date)Header can be called on.
	 */
	protected void setHeaders(WebResponse response)
	{
		if (isCacheable())
		{
			// If time is set also set cache headers.
			response.setDateHeader("Expires", System.currentTimeMillis() + (3600 * 1000));
			response.setHeader("Cache-Control","max-age=" + 3600);
		}
	}
}
