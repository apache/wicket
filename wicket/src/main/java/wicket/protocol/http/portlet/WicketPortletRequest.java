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

import java.util.Locale;
import java.util.Map;

import javax.portlet.PortletRequest;

import wicket.Request;
import wicket.WicketRuntimeException;

/**
 * A Request implementation that uses PortletRequest
 * 
 * @author Janne Hietam&auml;ki
 */
public class WicketPortletRequest extends Request
{
	/** The underlying request object. */
	PortletRequest req;

	/**
	 * Construct.
	 * 
	 * @param req
	 */
	public WicketPortletRequest(PortletRequest req)
	{
		this.req = req;
	}

	/**
	 * @return the context path
	 */
	public String getContextPath()
	{
		return req.getContextPath();
	}

	/**
	 * @see wicket.Request#getLocale()
	 */
	@Override
	public Locale getLocale()
	{
		return req.getLocale();
	}

	/**
	 * @see wicket.Request#getParameter(java.lang.String)
	 */
	@Override
	public String getParameter(String key)
	{
		return req.getParameter(key);
	}

	/**
	 * @see wicket.Request#getParameterMap()
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Map<String, Object> getParameterMap()
	{
		return (Map<String, Object>)req.getParameterMap();
	}

	/**
	 * @see wicket.Request#getParameters(java.lang.String)
	 */
	@Override
	public String[] getParameters(String key)
	{
		return req.getParameterValues(key);
	}

	/**
	 * @see wicket.Request#getPath()
	 */
	@Override
	public String getPath()
	{
		return null;
	}

	/**
	 * @return the underlying portlet request
	 */
	public PortletRequest getPortletRequest()
	{
		return req;
	}

	/**
	 * @see wicket.Request#getRelativeURL()
	 */
	@Override
	public String getRelativeURL()
	{
		throw new WicketRuntimeException("Relative URL is not available in portlet request");
	}

	/**
	 * @see wicket.Request#getURL()
	 */
	@Override
	public String getURL()
	{
		throw new WicketRuntimeException("URL is not available in portlet request");
	}
}
