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

import java.io.IOException;
import java.util.Iterator;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.util.string.Strings;
import wicket.util.value.ValueMap;

/**
 * EXPERIMENTAL ONLY
 * <p>
 * It extends WebResponse and encodes wicket's URL to make them crawler save.
 *  
 * @author Juergen Donnerstag
 */
public class WebResponseCrawlerSave extends WebResponse
{
    /** Logger */
    private static Log log = LogFactory.getLog(WebResponseCrawlerSave.class);

	/**
	 * Constructor.
	 * 
	 * @param httpServletResponse
	 *            The servlet response object
	 * @throws IOException
	 */
	public WebResponseCrawlerSave(final HttpServletResponse httpServletResponse) throws IOException
	{
		super(httpServletResponse);
	}

	/**
	 * Returns the given url encoded.
	 * 
	 * @param url
	 *            The URL to encode
	 * @return The encoded url
	 */
	public final String encodeURL(final String url)
	{
	    // The url must have a query string, otherwise keep the url unchanged
	    final int pos = url.indexOf('?');
	    if (pos > 0)
	    {
	        // The url's path
		    final String urlPrefix = url.substring(0, pos);
		    
		    // Extract the querystring 
		    String queryString = url.substring(pos + 1);

		    // The length of the encrypted string depends on the
	        // length of the original querystring. Let's try to
	        // make the querystring shorter first without loosing
	        // information.
		    queryString = encodeQueryString(queryString);

			// build the new complete url
			final String newUrl = urlPrefix + queryString;
			return newUrl;
	    }
		
		// we didn't change anything
		return url;
	}
	
	/**
	 * Try to shorten the querystring without loosing information
	 * 
	 * @param queryString The original query string
	 * @return The shortened querystring
	 */
	private String encodeQueryString(String queryString)
	{
	    final ValueMap param = new ValueMap(queryString, "&");
	    
	    final String bookmarkablePage = param.getString("bookmarkablePage");
	    if (bookmarkablePage != null)
	    {
	        String url = "/" + Strings.replaceAll(bookmarkablePage, ".", "/") + ".wic";
	        
	        param.remove("bookmarkablePage");
	        if (param.size() > 0)
	        {
	            char separator = '?';
				for (final Iterator iterator = param.keySet().iterator(); iterator.hasNext();)
				{
					final String key = (String)iterator.next();
					url += separator + key + "=" + param.getString(key);
					separator = '&';
				}
	        }
	        return url;
	    }
	    
	    return "?" + queryString;
	}
}
