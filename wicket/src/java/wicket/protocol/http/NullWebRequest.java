/*
 * $Id$
 * $Revision$ $Date$
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
package wicket.protocol.http;

import java.util.Collections;
import java.util.Map;

/**
 * A placeholder WebRequest where all methods do nothing. This class is not
 * accessible outside this package because it is intended to be used as a
 * singleton. To gain access to a NullWebRequest instance, just access the
 * static final field WebRequest.NULL.
 * 
 * @see WebRequest#NULL
 * @author Jonathan Locke
 */
class NullWebRequest extends WebRequest
{
	/**
	 * Private constructor to force use of static factory method.
	 */
	NullWebRequest()
	{
		super(null);
	}

	/**
	 * @see wicket.protocol.http.WebRequest#getContextPath()
	 */
	public String getContextPath()
	{
		return "[No context path]";
	}

	/**
	 * @see wicket.Request#getParameter(java.lang.String)
	 */
	public String getParameter(String key)
	{
		return null;
	}

	/**
	 * @see wicket.Request#getParameterMap()
	 */
	public Map getParameterMap()
	{
		return Collections.EMPTY_MAP;
	}

	/**
	 * @see wicket.Request#getParameters(java.lang.String)
	 */
	public String[] getParameters(String key)
	{
		return null;
	}

	/**
	 * @see wicket.protocol.http.WebRequest#getPathInfo()
	 */
	public String getPathInfo()
	{
		return "[No path info]";
	}

	/**
	 * @see wicket.protocol.http.WebRequest#getServletPath()
	 */
	public String getServletPath()
	{
		return "[No servlet path]";
	}

	/**
	 * @see wicket.Request#getURL()
	 */
	public String getURL()
	{
		return "[No request URL]";
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return "[NullWebRequest]";
	}
}
