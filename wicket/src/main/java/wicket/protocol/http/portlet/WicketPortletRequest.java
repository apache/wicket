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

import java.util.HashMap;
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
	private final PortletRequest request;

	/**
	 * Construct.
	 * 
	 * @param request
	 */
	public WicketPortletRequest(PortletRequest request)
	{
		this.request = request;
	}

	/**
	 * @return the context path
	 */
	public String getContextPath()
	{
		return request.getContextPath();
	}

	/**
	 * @see wicket.Request#getLocale()
	 */
	@Override
	public Locale getLocale()
	{
		return request.getLocale();
	}

	/**
	 * @see wicket.Request#getParameter(java.lang.String)
	 */
	@Override
	public String getParameter(String key)
	{
		return request.getParameter(key);
	}

	/**
	 * @see wicket.Request#getParameterMap()
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Map<String, Object> getParameterMap()
	{
		return new HashMap<String, Object>(request.getParameterMap());
	}

	/**
	 * @see wicket.Request#getParameters(java.lang.String)
	 */
	@Override
	public String[] getParameters(String key)
	{
		return request.getParameterValues(key);
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
		return request;
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
