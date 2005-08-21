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

import wicket.Session;
import wicket.protocol.http.servlet.ServletWebRequest;
import wicket.util.crypt.ICrypt;
import wicket.util.string.IStringIterator;
import wicket.util.string.StringList;
import wicket.util.string.Strings;
import wicket.util.value.ValueMap;

/**
 * it extends WebRequest and decodes URLs encoded by 
 * WebResponseWithCryptedUrl. One reason for obfuscating the URL's query string
 * might be, that you don't want the details to be visible to the user to
 * play around with.
 * 
 * @author Juergen Donnerstag
 */
public class WebRequestWithCryptedUrl extends ServletWebRequest
{
	/** URL querystring decoded */
	private final String queryString;
	
	/** URL query parameters decoded */
	private final ValueMap parameters;

	/** decoded url path */
	private String path;
	
	/**
	 * Constructor.
	 * 
	 * @param request
	 *            The oiginal request information
	 */
	public WebRequestWithCryptedUrl(final HttpServletRequest request)
	{
		super(request);

		// Encoded query string have only a single parameter named "x"
		final String secureParam = request.getParameter("x");
		if ((secureParam != null) && (secureParam.length() > 0))
		{
			// Get the crypt implementation from the application
			ICrypt urlCrypt = Session.get().getApplication().newCrypt();
		    // Decrypt the query string
			final String queryString = urlCrypt.decrypt(secureParam);
			
			// The querystring might have been shortened (length reduced).
			// In that case, lengthen the query string again. 
			this.queryString = rebuildUrl(queryString);
			
			// extract parameter key/value pairs from the query string
		    this.parameters = analyzeQueryString(this.queryString);
		}
		else
		{
		    // If "x" parameter does not exist, we assume the query string
		    // is not encoded.
		    // Note: You might want to throw an exception, if you don't want 
		    // the automatic fallback.
			this.queryString = null;
		    this.parameters = new ValueMap();
		}
		
		// If available, add POST parameters as well. They are not encrypted.
		// The parameters from HttpRequest 
		final Map params = super.getParameterMap();
		if ((params != null) && !params.isEmpty())
		{
		    // For all parameters (POST + URL query string)
		    final Iterator iter = params.entrySet().iterator();
		    while (iter.hasNext())
		    {
		        final Map.Entry entry = (Map.Entry)iter.next();
		        
		        // Ignore the "x" parameter
		        if (!"x".equalsIgnoreCase((String)entry.getKey()))
		        {
		            // add key/value to our parameter map
		            this.parameters.put(entry.getKey(), entry.getValue());
		        }
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
	    queryString = Strings.replaceAll(queryString, "1=", "path=");
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
		return this.parameters.getString(key);
	}

	/**
	 * Gets the request parameters.
	 * 
	 * @return Map of parameters
	 */
	public Map getParameterMap()
	{
		return Collections.unmodifiableMap(parameters);
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
		return (String[])parameters.keySet().toArray();
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return super.toString();
	}
}
