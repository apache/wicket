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

import java.util.Iterator;

import javax.servlet.http.HttpServletResponse;

import wicket.protocol.http.request.WebRequestCodingStrategy;
import wicket.util.string.AppendingStringBuffer;
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
	/**
	 * Constructor.
	 * 
	 * @param httpServletResponse
	 *            The servlet response object
	 */
	public WebResponseCrawlerSave(final HttpServletResponse httpServletResponse)
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
	public final CharSequence encodeURL(final CharSequence url)
	{
	    // The url must have a query string, otherwise keep the url unchanged
		String stringUrl = url.toString();
	    final int pos = stringUrl.indexOf('?');
	    if (pos > 0)
	    {
	        // The url's path
		    final String urlPrefix = stringUrl.substring(0, pos);
		    
		    // Extract the querystring 
		    CharSequence queryString = stringUrl.substring(pos + 1);

		    // The length of the encrypted string depends on the
	        // length of the original querystring. Let's try to
	        // make the querystring shorter first without loosing
	        // information.
		    queryString = encodeQueryString(queryString);

			// build the new complete url
			return new AppendingStringBuffer(urlPrefix).append(queryString);
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
	private CharSequence encodeQueryString(CharSequence queryString)
	{
	    final ValueMap param = new ValueMap(queryString.toString(), "&");
	    
	    final String bookmarkablePage = param.getString(WebRequestCodingStrategy.BOOKMARKABLE_PAGE_PARAMETER_NAME);
	    if (bookmarkablePage != null)
	    {
	        AppendingStringBuffer url = new AppendingStringBuffer("/");
	        url.append(Strings.replaceAll(bookmarkablePage, ".", "/"));
	        url.append(".wic");
	        
	        param.remove(WebRequestCodingStrategy.BOOKMARKABLE_PAGE_PARAMETER_NAME);
	        if (param.size() > 0)
	        {
	            char separator = '?';
				for (final Iterator iterator = param.keySet().iterator(); iterator.hasNext();)
				{
					final String key = (String)iterator.next();
					url.append(separator);
					url.append(key);
					url.append("=");
					url.append(param.getCharSequence(key));
					separator = '&';
				}
	        }
	        return url;
	    }
	    
	    return new AppendingStringBuffer("?").append(queryString);
	}
}
