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
package wicket.protocol.http;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import wicket.util.string.IStringIterator;
import wicket.util.string.StringList;
import wicket.util.string.Strings;
import wicket.util.value.ValueMap;

/**
 * It extends WebRequest and decodes URLs encoded by WebResponseCrawlerSave. 
 * Wicket's default URLs for static resources like Pages and Images
 * is like myApp?bookmarkable=wicket.test.MyPage. Many users however prefer
 * URLs like myApp/test/mypage. This is what we try to do here. Dynamically
 * generated URLs at runtime refering to listeners etc
 * 
 * @author Juergen Donnerstag
 */
public class WebRequestCrawlerSave extends WebRequest
{
	/** URL querystring decoded */
	private final String queryString;
	
	/** URL query parameters decoded */
	private ValueMap parameters;

	/** decoded url path */
	private String path;
	
	/**
	 * Constructor.
	 * 
	 * @param request
	 *            The oiginal request information
	 */
	public WebRequestCrawlerSave(final HttpServletRequest request)
	{
		super(request);

		String servletPath = request.getServletPath();
		queryString = request.getQueryString();
		String contextPath = request.getContextPath();
		this.path = request.getPathInfo();
		
		if (this.path == null)
		{
		    return;
		}
		
		if (this.path.startsWith("/") && this.path.endsWith(".wic"))
		{
		    String path = this.path.substring(1, this.path.length() - 4);
			
			path = Strings.replaceAll(path, "/", ".");
			this.parameters = new ValueMap();
			this.parameters.put("bookmarkablePage", path);
			
			this.path = null;
		}
		
		if (queryString != null)
		{
		    if (parameters == null)
		    {
		        parameters = new ValueMap();
		    }
		    
			// extract parameter key/value pairs from the query string
		    this.parameters.putAll(analyzeQueryString(this.queryString));
		}
		
		// If available, add POST parameters as well. 
		// The parameters from HttpRequest 
		final Map params = super.getParameterMap();
		if ((params != null) && !params.isEmpty())
		{
		    // For all parameters (POST + URL query string)
		    final Iterator iter = params.entrySet().iterator();
		    while (iter.hasNext())
		    {
		        final Map.Entry entry = (Map.Entry)iter.next();
	            // add key/value to our parameter map
	            this.parameters.put(entry.getKey(), entry.getValue());
		    }
		}
	}

	/**
	 * In case the query string has been shortened prior to encryption,
	 * than rebuild (lengthen) the query string now.
	 * 
	 * @param queryString The URL's query string
	 * @return The lengthened query string
	 */
	private String rebuildUrl(String queryString)
	{
	    queryString = Strings.replaceAll(queryString, "1=", "component=");
	    queryString = Strings.replaceAll(queryString, "2=", "version=");
	    queryString = Strings.replaceAll(queryString, "4=", "interface=IRedirectListener");
	    queryString = Strings.replaceAll(queryString, "3=", "interface=");
	    queryString = Strings.replaceAll(queryString, "5=", "bookmarkablePage=");
	    
	    return queryString;
	}

	/**
	 * Extract key/value pairs from query string
	 * 
	 * @param queryString The query string
	 * @return A map of query string parameter keys and values
	 */
	private ValueMap analyzeQueryString(final String queryString)
	{
		final ValueMap params = new ValueMap();

		// Get a list of strings separated by the delimiter
		final StringList pairs = StringList.tokenize(queryString, "&");
	    
		// Go through each string in the list
		for (IStringIterator iterator = pairs.iterator(); iterator.hasNext();)
		{
			// Get the next key value pair
			final String pair = iterator.next();

			// separate key and value
			final int pos = pair.indexOf("=");
			if (pos < 0)
			{
			    // Parameter without value
				params.put(pair, null);
			}
			else
			{
			    final String key = pair.substring(0, pos);
			    final String value = pair.substring(pos + 1);
			    
				params.put(key, value);
			}
		}
		
		return params;
	}
	
	/**
	 * Gets the request parameter with the given key.
	 * 
	 * @param key
	 *            Parameter name
	 * @return Parameter value
	 */
	public String getParameter(final String key)
	{
	    if (parameters != null)
	    {
	        return this.parameters.getString(key);
	    }
	    
	    return super.getParameter(key);
	}

	/**
	 * Gets the request parameters.
	 * 
	 * @return Map of parameters
	 */
	public Map getParameterMap()
	{
	    if (parameters != null)
	    {
	        return Collections.unmodifiableMap(parameters);
	    }
        return Collections.unmodifiableMap(super.getParameterMap());
	}

	/**
	 * Gets the request parameters with the given key.
	 * 
	 * @param key
	 *            Parameter name
	 * @return Parameter values
	 */
	public String[] getParameters(final String key)
	{
	    if (parameters != null)
	    {
	        return (String[])parameters.keySet().toArray();
	    }
        return super.getParameters(key);
	    
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return super.toString();
	}

	/**
	 * Gets the path info if any.
	 * 
	 * @return Any servlet path info
	 */
	public String getPath()
	{
		return this.path;
	}
}
