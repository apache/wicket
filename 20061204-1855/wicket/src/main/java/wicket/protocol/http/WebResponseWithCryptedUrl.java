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
package wicket.protocol.http;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.Application;
import wicket.protocol.http.request.WebRequestCodingStrategy;
import wicket.util.crypt.ICrypt;
import wicket.util.string.AppendingStringBuffer;
import wicket.util.string.Strings;

/**
 * It extends WebResponse and encodes (encrypt) wicket's URL 
 * query string. Thus it hides the details from the user.
 *  
 * @author Juergen Donnerstag
 * 
 * @deprecated since 1.2 Please see {@link wicket.protocol.http.request.CryptedUrlwebRequestCodingStrategy}
 */
public class WebResponseWithCryptedUrl extends WebResponse
{
    /** Logger */
    private static final Log log = LogFactory.getLog(WebResponseWithCryptedUrl.class);

	/**
	 * Constructor.
	 * 
	 * @param httpServletResponse
	 *            The servlet response object
	 */
	public WebResponseWithCryptedUrl(final HttpServletResponse httpServletResponse)
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
	public CharSequence encodeURL(CharSequence url)
	{
	    // Get the crypt implementation from the application
		ICrypt urlCrypt = Application.get().getSecuritySettings().getCryptFactory().newCrypt();
		if (urlCrypt != null)
		{
		    // The url must have a query string, otherwise keep the url unchanged
		    final int pos = url.toString().indexOf('?');
		    if (pos > 0)
		    {
		        // The url's path
		    	CharSequence urlPrefix = url.subSequence(0, pos);

			    // Extract the querystring 
		    	String queryString = url.subSequence(pos + 1, url.length()).toString();

			    // if the querystring starts with a parameter like 
			    // "x=", than don't change the querystring as it 
			    // has been encoded already
			    if (!queryString.startsWith("x="))
			    {
			        // The length of the encrypted string depends on the
			        // length of the original querystring. Let's try to
			        // make the querystring shorter first without loosing
			        // information.
				    queryString = shortenUrl(queryString).toString();

				    // encrypt the query string
					final String encryptedQueryString = urlCrypt.encryptUrlSafe(queryString);

					// build the new complete url
					return new AppendingStringBuffer(urlPrefix).append("?x=").append(encryptedQueryString);
			    }
		    }
		}

		// we didn't change anything
		return url;
	}

	/**
	 * Try to shorten the querystring without loosing information.
	 * Note: WebRequestWithCryptedUrl must implement exactly
	 * the opposite logic.
	 * 
	 * @param queryString The original query string
	 * @return The shortened querystring
	 */
	protected CharSequence shortenUrl(CharSequence queryString)
	{
	    queryString = Strings.replaceAll(queryString, WebRequestCodingStrategy.BEHAVIOR_ID_PARAMETER_NAME + "=", "1=");
	    queryString = Strings.replaceAll(queryString, WebRequestCodingStrategy.INTERFACE_PARAMETER_NAME + "=IRedirectListener", "2=");
	    queryString = Strings.replaceAll(queryString, WebRequestCodingStrategy.INTERFACE_PARAMETER_NAME + "=IFormSubmitListener", "3=");
	    queryString = Strings.replaceAll(queryString, WebRequestCodingStrategy.INTERFACE_PARAMETER_NAME + "=IOnChangeListener", "4=");
	    queryString = Strings.replaceAll(queryString, WebRequestCodingStrategy.INTERFACE_PARAMETER_NAME + "=ILinkListener", "5=");
	    queryString = Strings.replaceAll(queryString, WebRequestCodingStrategy.INTERFACE_PARAMETER_NAME + "=", "6=");
	    queryString = Strings.replaceAll(queryString, WebRequestCodingStrategy.BOOKMARKABLE_PAGE_PARAMETER_NAME+"=", "7=");

	    // For debugging only: determine possibilities to further shorten
	    // the query string
	    if (log.isInfoEnabled())
	    {
	        // Every word with at least 3 letters
	        Pattern words = Pattern.compile("\\w\\w\\w+");
	        Matcher matcher = words.matcher(queryString);
	        while (matcher.find())
	        {
	            CharSequence word = queryString.subSequence(matcher.start(), matcher.end());
	            log.info("URL pattern NOT shortened: '" + word + "' - '" + queryString + "'");
	        }
	    }

	    return queryString;
	}
}
