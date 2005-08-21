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

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;

import wicket.ApplicationSettings;
import wicket.WicketRuntimeException;
import wicket.protocol.http.WebApplication;
import wicket.protocol.http.WicketServlet;

/**
 * Base Application class to be used for WicketPortlets providing portlet specific initialization and cleanup.
 * <p>
 * Because Portlets don't operate within a Servlet context (formally), getting access to its WicketServlet and
 * its relative resources requires an {@link #APPLICATION_PATH_PARAMETER "applicationPath"} initialization parameter
 * with the mapped servletpath of the WicketServlet.
 * </p>
 * 
 * @author Ate Douma
 */
public class PortletApplication extends WebApplication
{
	/**
	 * Required WicketServlet initialization parameter name with its mapped servlet path.
	 */
	public static final String APPLICATION_PATH_PARAMETER = "applicationPath";
	
	/**
	 * Reserved Web and Portlet Application context attribute key containing a map of
	 * WicketServlet names and PortletApplication instances. 
	 */
	public static final String PORTLET_APPLICATIONS = "wicket.portletApplications";
	
	private String applicationPath;
	
	/*
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL IT.
	 * 
	 */
	protected void internalInit()
	{
		super.internalInit();
		final WicketServlet servlet = getWicketServlet();
		applicationPath = servlet.getInitParameter(APPLICATION_PATH_PARAMETER);
		if ( applicationPath == null )
		{
			throw new WicketRuntimeException("Required init parameter "+APPLICATION_PATH_PARAMETER+" undefined.");
		}
		final ServletContext context = servlet.getServletContext();
		synchronized (PortletApplication.class)
		{
			Map portletApplications = (Map)context.getAttribute(PORTLET_APPLICATIONS);
			if ( portletApplications == null )
			{
				portletApplications = new HashMap();
				context.setAttribute(PORTLET_APPLICATIONS, portletApplications);
			}
			portletApplications.put(servlet.getServletName(), this);
		}
		// enforce required RenderStrategy for PortletApplications
		getSettings().setRenderStrategy(ApplicationSettings.REDIRECT_TO_RENDER);
	}	

	/*
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL IT.
	 * 
	 */
	protected void internalDestroy()
	{
		final WicketServlet servlet = getWicketServlet();
		final ServletContext context = servlet.getServletContext();
		synchronized (PortletApplication.class)
		{
			Map portletApplications = (Map)context.getAttribute(PORTLET_APPLICATIONS);
			if ( portletApplications != null )
			{
				portletApplications.remove(servlet.getServletName());
				if ( portletApplications.size() == 0)
				{
					context.removeAttribute(PORTLET_APPLICATIONS);
				}
			}
		}
		super.internalDestroy();
	}	
}
