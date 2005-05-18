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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.Session;
import wicket.util.crypt.ICrypt;
import wicket.util.string.Strings;

/**
 * It extends WebResponse and encodes (encrypt) wicket's URL 
 * query string. Thus it hides the details from the user.
 *  
 * @author Juergen Donnerstag
 */
public class WebResponseWithCryptedUrl extends WebResponse
{
    /** Logger */
    private static Log log = LogFactory.getLog(WebResponseWithCryptedUrl.class);

	/**
	 * Constructor.
	 * 
	 * @param httpServletResponse
	 *            The servlet response object
	 * @throws IOException
	 */
	public WebResponseWithCryptedUrl(final HttpServletResponse httpServletResponse) throws IOException
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
	public String encodeURL(String url)
	{
	    // Get the crypt implementation from the application
		ICrypt urlCrypt = Session.get().getApplication().newCrypt();
		if (urlCrypt != null)
		{
		    // The url must have a query string, otherwise keep the url unchanged
		    final int pos = url.indexOf('?');
		    if (pos > 0)
		    {
		        // The url's path
			    String urlPrefix = url.substring(0, pos);
			    
			    // Extract the querystring 
			    String queryString = url.substring(pos + 1);
			    
			    // if the querystring starts with a parameter like 
			    // "secure=", than don#t change the querystring as it 
			    // has been encoded already
			    if (!queryString.startsWith("secure="))
			    {
			        // The length of the encrypted string depends on the
			        // length of the original querystring. Let's try to
			        // make the querystring shorter first without loosing
			        // information.
				    queryString = shortenUrl(queryString);
				    
				    // encrypt the query string
					
					final String encryptedQueryString = urlCrypt.encrypt(queryString);
					
					// build the new complete url
					final String encryptedUrl = urlPrefix + "?secure=" + escapeUrl(encryptedQueryString);
					return encryptedUrl;
			    }
		    }
		}
		
		// we didn't change anything
		return url;
	}
	
	/**
	 * Escape invalid URL characters 
	 * 
	 * @param queryString The orginal querystring
	 * @return url The querystring with invalid characters escaped
	 */
	private String escapeUrl(String queryString)
	{
	    queryString = Strings.replaceAll(queryString, " ", "%20");
	    queryString = Strings.replaceAll(queryString, "%", "%26");
	    queryString = Strings.replaceAll(queryString, "=", "%3D");
	    queryString = Strings.replaceAll(queryString, "/", "%2F");
	    queryString = Strings.replaceAll(queryString, "+", "%2B");
	    
	    return queryString;
	}
	
	/**
	 * Try to shorten the querystring without loosing information
	 * 
	 * @param queryString The original query string
	 * @return The shortened querystring
	 */
	private String shortenUrl(String queryString)
	{
	    queryString = Strings.replaceAll(queryString, "component=", "1=");
	    queryString = Strings.replaceAll(queryString, "version=", "2=");
	    queryString = Strings.replaceAll(queryString, "interface=IRedirectListener", "4=");
	    queryString = Strings.replaceAll(queryString, "interface=", "3=");
	    queryString = Strings.replaceAll(queryString, "bookmarkablePage=", "5=");
	    
	    // For debugging only: determine possibilities to further shorten
	    // the query string
	    if (log.isInfoEnabled())
	    {
	        // Every word with at least 3 letters
	        Pattern words = Pattern.compile("\\w\\w\\w+");
	        Matcher matcher = words.matcher(queryString);
	        while (matcher.find())
	        {
	            String word = queryString.substring(matcher.start(), matcher.end());
	            log.info("URL pattern NOT shortened: '" + word + "' - '" + queryString + "'");
	        }
	    }
	    
	    return queryString;
	}
}
