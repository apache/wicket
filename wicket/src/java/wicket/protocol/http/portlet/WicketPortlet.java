/*
 * $Id$
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
package wicket.protocol.http.portlet;

import java.io.IOException;
import java.util.Map;

import javax.portlet.GenericPortlet;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.UnavailableException;

/**
 * Base class for Wicket Portlets. Each WicketPortlet must be linked to a corresponding
 * WicketServlet using the "wicketApplicationName" init parameter. This parameter value
 * is the name of the corresponding WicketServlet as defined in web.xml. The WicketServlet
 * must be initialized with a {@link PortletApplication}. 
 * 
 * @author Ate Douma
 */
public class WicketPortlet extends GenericPortlet
{
	private static final String WICKET_APPLICATION_NAME = "wicketApplicationName";
	private PortletApplication portletApplication;
	
	/**
	 * Portlet initialization
	 * @see javax.portlet.GenericPortlet#init(javax.portlet.PortletConfig)
	 */
	public void init(PortletConfig config) throws PortletException
	{
		super.init(config);

		final String wicketApplicationName = getInitParameter(WICKET_APPLICATION_NAME);
		final Map portletApplications = (Map)config.getPortletContext().getAttribute(PortletApplication.PORTLET_APPLICATIONS);
		if ( portletApplications != null )
		{
			// Related WicketServlet must use <load-on-startup>1</load-on-startup> or something
			// to make sure its initialized before its Portlet
			portletApplication = (PortletApplication)portletApplications.get(wicketApplicationName);
		}
		if ( portletApplication == null )
		{
			throw new UnavailableException(WICKET_APPLICATION_NAME+" "+wicketApplicationName+"not found");
		}
	}

	protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException
	{
	}
}
