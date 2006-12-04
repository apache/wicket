/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
 * 
 */
public class WicketPortletRequest extends Request
{
	/** The underlying request object. */
	PortletRequest req;

	/**
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

	/*
	 * @see wicket.Request#getLocale()
	 */
	public Locale getLocale()
	{
		return req.getLocale();
	}

	/*
	 * @see wicket.Request#getParameter(java.lang.String)
	 */
	public String getParameter(String key)
	{
		return req.getParameter(key);
	}

	/*
	 * @see wicket.Request#getParameterMap()
	 */
	public Map getParameterMap()
	{
		return req.getParameterMap();
	}

	/*
	 * @see wicket.Request#getParameters(java.lang.String)
	 */
	public String[] getParameters(String key)
	{
		return req.getParameterValues(key);
	}

	/**
	 * @return the underlying portlet request
	 */
	public PortletRequest getPortletRequest()
	{
		return req;
	}

	public String getPath()
	{
		throw new WicketRuntimeException("Path is not available in portlet request");
	}

	public String getRelativeURL()
	{
		throw new WicketRuntimeException("Relative URL is not available in portlet request");
	}

	public String getURL()
	{
		throw new WicketRuntimeException("URL is not available in portlet request");
	}
}
