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

import java.security.Principal;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import javax.portlet.PortalContext;
import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;
import javax.portlet.WindowState;

import wicket.Component;
import wicket.protocol.http.MockHttpServletRequest;

/**
 * Mock implementation of PortletRequest
 * 
 * @author Janne Hietam&auml;ki (jannehietamaki)
 */
public class MockPortletRequest implements PortletRequest
{
	MockHttpServletRequest req;
	PortletMode portletMode=PortletMode.VIEW;
	WindowState windowState=WindowState.NORMAL;
	MockPortletApplication application;
	MockPortletSession portletSession;
	
	/**
	 * Construct.
	 * @param application
	 * @param portletSession
	 * @param req
	 */
	public MockPortletRequest(MockPortletApplication application, MockPortletSession portletSession, MockHttpServletRequest req)
	{		
		this.req=req;
		this.portletSession=portletSession;
		this.application=application;
	}

	public Object getAttribute(String key)
	{
		return req.getAttribute(key);
	}

	public Enumeration getAttributeNames()
	{
		return req.getAttributeNames();
	}

	public String getAuthType()
	{
		return req.getAuthType();
	}

	public String getContextPath()
	{
		return req.getContextPath();
	}

	public Locale getLocale()
	{
		return req.getLocale();
	}

	public Enumeration getLocales()
	{
		return req.getLocales();
	}

	public String getParameter(String key)
	{
		return req.getParameter(key);
	}

	public Map getParameterMap()
	{
		return req.getParameterMap();
	}

	public Enumeration getParameterNames()
	{
		return req.getParameterNames();
	}

	public String[] getParameterValues(String key)
	{
		return req.getParameterValues(key);
	}

	public PortalContext getPortalContext()
	{
		return null;
	}

	public PortletMode getPortletMode()
	{
		return portletMode;
	}

	public PortletSession getPortletSession()
	{
		return portletSession;
	}

	public PortletSession getPortletSession(boolean create)
	{
		return getPortletSession();
	}

	public PortletPreferences getPreferences()
	{
		// TODO
		return null;
	}

	public Enumeration getProperties(String key)
	{
		return new Vector().elements();
	}

	public String getProperty(String key)
	{
		return null;
	}

	public Enumeration getPropertyNames()
	{
		return null;
	}

	public String getRemoteUser()
	{
		return req.getRemoteUser();
	}

	public String getRequestedSessionId()
	{
		return req.getRequestedSessionId();
	}

	public String getResponseContentType()
	{
		//  TODO
		return null;
	}

	public Enumeration getResponseContentTypes()
	{
		return new Vector().elements();
	}

	public String getScheme()
	{
		return req.getScheme();
	}

	public String getServerName()
	{
		return req.getServerName();
	}

	public int getServerPort()
	{
		return req.getServerPort();
	}

	public Principal getUserPrincipal()
	{
		return req.getUserPrincipal();
	}

	public WindowState getWindowState()
	{
		return windowState;
	}

	public boolean isPortletModeAllowed(PortletMode mode)
	{
		return true;
	}

	public boolean isRequestedSessionIdValid()
	{
		return req.isRequestedSessionIdValid();
	}

	public boolean isSecure()
	{
		return req.isSecure();
	}

	public boolean isUserInRole(String role)
	{
		return req.isUserInRole(role);
	}

	public boolean isWindowStateAllowed(WindowState state)
	{
		return true;
	}

	public void removeAttribute(String key)
	{
		req.removeAttribute(key);
	}

	public void setAttribute(String key, Object value)
	{
		req.setAttribute(key,value);
	}

	/**
	 * 
	 */
	public void initialize()
	{
		req.initialize();
	}

	/**
	 * @param parameters
	 */
	@SuppressWarnings("unchecked")
	public void setParameters(Map parameters)
	{
		req.setParameters(parameters);
	}

	/**
	 * Initialise the request parameters from the given redirect string that
	 * redirects back to a particular component for display.
	 * 
	 * @param redirect
	 *            The redirect string to display from
	 */
	public void setRequestToRedirectString(final String redirect)
	{
		req.setRequestToRedirectString(redirect);
	}

	/**
	 * @param component
	 */
	public void setRequestToComponent(Component component)
	{
		req.setRequestToComponent(component);
	}

}
