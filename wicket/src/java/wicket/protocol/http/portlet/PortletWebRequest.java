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

import java.util.Locale;
import java.util.Map;

import javax.portlet.PortletRequest;


import wicket.WicketRuntimeException;
import wicket.protocol.http.WebRequest;
import wicket.util.lang.Bytes;
import wicket.util.upload.FileUploadException;

/**
 * A Portlet specific WebRequest implementation wrapping a PortletRequest. 
 * 
 * @author Ate Douma
 */
public class PortletWebRequest extends WebRequest
{
	private final PortletRequest request;
	private final String servletPath;

	/**
	 * Constructor
	 * 
	 * @param request the PortletRequest
	 * @param servletPath the WicketServlet applicationPath init parameter
	 */
	public PortletWebRequest(PortletRequest request, String servletPath)
	{
		super();
		this.request = request;
		this.servletPath = servletPath;
	}

	/**
	 * @see wicket.protocol.http.WebRequest#getContextPath()
	 */
	public String getContextPath()
	{
		return request.getContextPath();
	}

	/**
	 * @return the (hardcoded) WicketServlet applicationPath init parameter
	 * @see wicket.protocol.http.WebRequest#getServletPath()
	 * @see PortletApplication#APPLICATION_PATH_PARAMETER
	 */
	public String getServletPath()
	{
		return servletPath;
	}

	/**
	 * @see wicket.protocol.http.WebRequest#getRelativeURL()
	 */
	public String getRelativeURL()
	{
		String url = getServletPath();
		// If url is non-empty it has to start with '/', which we should lose
		if (!url.equals(""))
		{
			// Remove leading '/'
			url = url.substring(1);
		}
		return url;
	}

	/**
	 * @see wicket.Request#getLocale()
	 */
	public Locale getLocale()
	{
		return request.getLocale();
	}

	/**
	 * @see wicket.Request#getParameter(java.lang.String)
	 */
	public String getParameter(String key)
	{
		return request.getParameter(key);
	}

	/**
	 * @see wicket.Request#getParameterMap()
	 */
	public Map getParameterMap()
	{
		return request.getParameterMap();
	}

	/**
	 * @see wicket.Request#getParameters(java.lang.String)
	 */
	public String[] getParameters(String key)
	{
		return request.getParameterValues(key);
	}

	/**
	 * @return always null as a PortletRequest doesn't have the concept of a request url.
	 * @see wicket.Request#getPath()
	 */
	public String getPath()
	{
		return null;
	}

	/**
	 * @see wicket.protocol.http.WebRequest#newMultipartWebRequest(wicket.util.lang.Bytes)
	 */
	public WebRequest newMultipartWebRequest(Bytes maxsize)
	{
		try
		{
			return new MultipartPortletWebRequest(request, servletPath, maxsize);
		}
		catch (FileUploadException e)
		{
			throw new WicketRuntimeException(e);
		}
	}
}
