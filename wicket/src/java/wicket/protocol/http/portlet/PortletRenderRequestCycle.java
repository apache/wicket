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

import wicket.Response;
import wicket.request.IRequestCycleProcessor;

/**
 * A portlet RequestCycle implementation for portlet RenderRequest.
 * 
 * @author Janne Hietam&auml;ki
 * 
 */
public class PortletRenderRequestCycle extends PortletRequestCycle
{
	/** Logging object */
	private static final Log log = LogFactory.getLog(PortletRenderRequestCycle.class);

	/**
	 * @param session
	 * @param request
	 * @param response
	 */
	public PortletRenderRequestCycle(WicketPortletSession session, WicketPortletRequest request,
			Response response)
	{
		super(session, request, response);
	}

	/**
	 * Returns the PortletApplication default render request cycle processor.
	 * {@link PortletApplication#getRequestCycleProcessor()}.
	 * 
	 * @see wicket.RequestCycle#getProcessor()
	 */

	public final IRequestCycleProcessor getProcessor()
	{
		PortletApplication application = (PortletApplication)getApplication();
		return application.getRenderRequestCycleProcessor();
	}
}