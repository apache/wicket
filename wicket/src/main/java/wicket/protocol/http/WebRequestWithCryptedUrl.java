/*
 * $Id: WebRequestWithCryptedUrl.java 4508 2006-02-16 15:14:18 -0800 (Thu, 16
 * Feb 2006) jonathanlocke $ $Revision$ $Date: 2006-02-16 15:14:18 -0800
 * (Thu, 16 Feb 2006) $
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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import wicket.Application;
import wicket.WicketRuntimeException;
import wicket.protocol.http.request.WebRequestCodingStrategy;
import wicket.protocol.http.servlet.ServletWebRequest;
import wicket.util.crypt.ICrypt;
import wicket.util.string.IStringIterator;
import wicket.util.string.StringList;
import wicket.util.string.Strings;
import wicket.util.value.ValueMap;

/**
 * It extends WebRequest and decodes URLs encoded by WebResponseWithCryptedUrl.
 * One reason for obfuscating the URL's query string might be, that you don't
 * want the details to be visible to the user to play around with.
 * 
 * @author Juergen Donnerstag
 * 
 * @deprecated since 1.2 Please see {@link wicket.protocol.http.request.CryptedUrlwebRequestCodingStrategy}
 */
public class WebRequestWithCryptedUrl extends ServletWebRequest
{
	/** URL querystring decoded */
	private final String queryString;

	/** URL query parameters decoded */
	private final ValueMap parameters;

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
			ICrypt urlCrypt = Application.get().getSecuritySettings().getCryptFactory().newCrypt();

			// Decrypt the query string
			final String queryString = urlCrypt.decryptUrlSafe(secureParam);

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
		final Enumeration paramNames = request.getParameterNames();

		// For all parameters (POST + URL query string)
		while (paramNames.hasMoreElements())
		{
			String paramName = (String)paramNames.nextElement();

			// Ignore the "x" parameter
			if (!"x".equalsIgnoreCase(paramName))
			{
				String[] values = request.getParameterValues(paramName);
				// add key/value to our parameter map
				this.parameters.put(paramName, values);
			}
		}
	}

	/**
	 * @see wicket.Request#decodeURL(java.lang.String)
	 */
	public String decodeURL(final String url)
	{
		int startIndex = url.indexOf("x=");
		if (startIndex != -1)
		{
			startIndex = startIndex + 2;
			final String secureParam;
			final int endIndex = url.indexOf("&", startIndex);
			try
			{
				if (endIndex == -1)
				{
					secureParam = URLDecoder.decode(url.substring(startIndex), Application.get()
							.getRequestCycleSettings().getResponseRequestEncoding());
				}
				else
				{
					secureParam = URLDecoder.decode(url.substring(startIndex, endIndex),
							Application.get().getRequestCycleSettings()
									.getResponseRequestEncoding());
				}
			}
			catch (UnsupportedEncodingException ex)
			{
				// should never happen
				throw new WicketRuntimeException(ex);
			}

			// Get the crypt implementation from the application
			final ICrypt urlCrypt = Application.get().getSecuritySettings().getCryptFactory()
					.newCrypt();

			// Decrypt the query string
			final String queryString = urlCrypt.decryptUrlSafe(secureParam);

			// The querystring might have been shortened (length reduced).
			// In that case, lengthen the query string again.
			return rebuildUrl(queryString);
		}
		return url;
	}

	/**
	 * In case the query string has been shortened prior to encryption, than
	 * rebuild (lengthen) the query string now. Note: This implementation must
	 * exactly match the reverse one implemented in WebResponseWithCryptedUrl.
	 * 
	 * @param queryString
	 *            The URL's query string
	 * @return The lengthened query string
	 */
	private String rebuildUrl(CharSequence queryString)
	{
		queryString = Strings.replaceAll(queryString, "1=",
				WebRequestCodingStrategy.BEHAVIOR_ID_PARAMETER_NAME + "=");
		queryString = Strings.replaceAll(queryString, "2=",
				WebRequestCodingStrategy.INTERFACE_PARAMETER_NAME + "=IRedirectListener");
		queryString = Strings.replaceAll(queryString, "3=",
				WebRequestCodingStrategy.INTERFACE_PARAMETER_NAME + "=IFormSubmitListener");
		queryString = Strings.replaceAll(queryString, "4=",
				WebRequestCodingStrategy.INTERFACE_PARAMETER_NAME + "=IOnChangeListener");
		queryString = Strings.replaceAll(queryString, "5=",
				WebRequestCodingStrategy.INTERFACE_PARAMETER_NAME + "=ILinkListener");
		queryString = Strings.replaceAll(queryString, "6=",
				WebRequestCodingStrategy.INTERFACE_PARAMETER_NAME + "=");
		queryString = Strings.replaceAll(queryString, "7=",
				WebRequestCodingStrategy.BOOKMARKABLE_PAGE_PARAMETER_NAME + "=");

		return queryString.toString();
	}

	/**
	 * Extract key/value pairs from query string
	 * 
	 * @param queryString
	 *            The query string
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
				String[] prevValue = (String[])params.get(pair);
				if (prevValue != null)
				{
					String[] newValue = new String[prevValue.length + 1];
					System.arraycopy(prevValue, 0, newValue, 0, prevValue.length);
					newValue[prevValue.length] = "";
					params.put(pair, newValue);
				}
				else
				{
					// Parameter without value
					params.put(pair, new String[] { "" });
				}
			}
			else
			{
				final String key = pair.substring(0, pos);
				final String value = pair.substring(pos + 1);
				String[] prevValue = (String[])params.get(key);
				if (prevValue != null)
				{
					String[] newValue = new String[prevValue.length + 1];
					System.arraycopy(prevValue, 0, newValue, 0, prevValue.length);
					newValue[prevValue.length] = value;
					params.put(key, newValue);
				}
				else
				{
					// Parameter without value
					params.put(key, new String[] { value });
				}
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
		String[] value = (String[])this.parameters.get(key);
		if (value != null)
		{
			return value[0];
		}
		return null;
	}

	/**
	 * Gets the request parameters.
	 * 
	 * @return Map of parameters
	 */
	public Map getParameterMap()
	{
		Map map = new HashMap(parameters.size(), 1);
		Iterator it = parameters.keySet().iterator();
		while (it.hasNext())
		{
			String param = (String)it.next();
			map.put(param, getParameter(param));
		}
		return map;
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
		return (String[])this.parameters.get(key);
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return super.toString();
	}
}
