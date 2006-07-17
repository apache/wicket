/*
 * $Id: WebRequestCodingStrategy.java,v 1.24 2006/02/15 02:00:30 jonathanlocke
 * Exp $ $Revision$ $Date: 2006-04-25 15:03:59 +0200 (Di, 25 Apr 2006) $
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
package wicket.protocol.http.request;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.Application;
import wicket.IRequestTarget;
import wicket.PageParameters;
import wicket.Request;
import wicket.RequestCycle;
import wicket.WicketRuntimeException;
import wicket.request.IRequestCodingStrategy;
import wicket.request.RequestParameters;
import wicket.request.target.coding.IRequestTargetUrlCodingStrategy;
import wicket.util.crypt.ICrypt;
import wicket.util.string.AppendingStringBuffer;
import wicket.util.string.Strings;

/**
 * This is a request coding strategy which encrypts the URL and hence makes it
 * impossible for users to guess what is in the url and rebuild it manually. It
 * uses the CryptFactory registered with the application to encode and decode
 * the URL. Hence, the coding algorithm must be a two-way one (reversable).
 * Because the algrithm is reversible, URLs which were bookmarkable before will
 * remain bookmarkable.
 * <p>
 * To register the request coding strategy to need to do the following:
 * 
 * <pre>
 * protected IRequestCycleProcessor newRequestCycleProcessor()
 * {
 * 	return new CompoundRequestCycleProcessor(new CryptedUrlWebRequestCodingStrategy(
 * 			new WebRequestCodingStrategy()), null, null, null, null);
 * }
 * </pre>
 * 
 * @author Juergen Donnerstag
 */
public class CryptedUrlWebRequestCodingStrategy implements IRequestCodingStrategy
{
	/** log. */
	private static final Log log = LogFactory.getLog(CryptedUrlWebRequestCodingStrategy.class);

	/** The default request coding strategy most of the methods are delegated to */
	private final IRequestCodingStrategy defaultStrategy;

	/**
	 * Construct.
	 * 
	 * @param defaultStrategy
	 *            The default strategy most requests are forwarded to
	 */
	public CryptedUrlWebRequestCodingStrategy(final IRequestCodingStrategy defaultStrategy)
	{
		this.defaultStrategy = defaultStrategy;
	}

	/**
	 * Decode the querystring of the URL
	 * 
	 * @see wicket.request.IRequestCodingStrategy#decode(wicket.Request)
	 */
	public RequestParameters decode(final Request request)
	{
		String url = request.decodeURL(request.getURL());
		String decodedQueryParams = decodeURL(url);
		if (decodedQueryParams != null)
		{
			// The difficulty now is that this.defaultStrategy.decode(request)
			// doesn't know the just decoded url which is why must create
			// a fake Request for.
			Request fakeRequest = new DecodedUrlRequest(request, url, decodedQueryParams);
			return this.defaultStrategy.decode(fakeRequest);
		}

		return this.defaultStrategy.decode(request);
	}

	/**
	 * Encode the querystring of the URL
	 * 
	 * @see wicket.request.IRequestCodingStrategy#encode(wicket.RequestCycle,
	 *      wicket.IRequestTarget)
	 */
	public CharSequence encode(final RequestCycle requestCycle, final IRequestTarget requestTarget)
	{
		CharSequence url = this.defaultStrategy.encode(requestCycle, requestTarget);
		url = encodeURL(url);
		return url;
	}

	/**
	 * @see wicket.request.IRequestTargetMounter#mount(
	 *      wicket.request.target.coding.IRequestTargetUrlCodingStrategy)
	 */
	public void mount(IRequestTargetUrlCodingStrategy urlCodingStrategy)
	{
		this.defaultStrategy.mount(urlCodingStrategy);
	}

	/**
	 * @see wicket.request.IRequestTargetMounter#unmount(java.lang.String)
	 */
	public void unmount(String path)
	{
		this.defaultStrategy.unmount(path);
	}

	/**
	 * @see wicket.request.IRequestTargetMounter#urlCodingStrategyForPath(java.lang.String)
	 */
	public IRequestTargetUrlCodingStrategy urlCodingStrategyForPath(String path)
	{
		return this.defaultStrategy.urlCodingStrategyForPath(path);
	}

	/**
	 * @see wicket.request.IRequestTargetMounter#pathForTarget(wicket.IRequestTarget)
	 */
	public CharSequence pathForTarget(IRequestTarget requestTarget)
	{
		return this.defaultStrategy.pathForTarget(requestTarget);
	}

	/**
	 * @see wicket.request.IRequestTargetMounter#targetForRequest(wicket.request.RequestParameters)
	 */
	public IRequestTarget targetForRequest(RequestParameters requestParameters)
	{
		return this.defaultStrategy.targetForRequest(requestParameters);
	}

	/**
	 * Returns the given url encoded.
	 * 
	 * @param url
	 *            The URL to encode
	 * @return The encoded url
	 */
	protected CharSequence encodeURL(final CharSequence url)
	{
		// Get the crypt implementation from the application
		ICrypt urlCrypt = Application.get().getSecuritySettings().getCryptFactory().newCrypt();
		if (urlCrypt != null)
		{
			// The url must have a query string, otherwise keep the url
			// unchanged
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
					String encryptedQueryString = urlCrypt.encryptUrlSafe(queryString);

					try
					{
						encryptedQueryString = URLEncoder.encode(encryptedQueryString, Application
								.get().getRequestCycleSettings().getResponseRequestEncoding());
					}
					catch (UnsupportedEncodingException ex)
					{
						throw new WicketRuntimeException(ex);
					}

					// build the new complete url
					return new AppendingStringBuffer(urlPrefix).append("?x=").append(
							encryptedQueryString);
				}
			}
		}

		// we didn't change anything
		return url;
	}

	/**
	 * Decode the "x" parameter of the querystring
	 * 
	 * @param url
	 *            The encoded URL
	 * @return The decoded 'x' parameter of the querystring
	 */
	protected String decodeURL(final String url)
	{
		int startIndex = url.indexOf("?x=");
		if (startIndex != -1)
		{
			startIndex = startIndex + 3;
			final int endIndex = url.indexOf("&", startIndex);
			String secureParam;
			if (endIndex == -1)
			{
				secureParam = url.substring(startIndex);
			}
			else
			{
				secureParam = url.substring(startIndex, endIndex);
			}

			try
			{
				secureParam = URLDecoder.decode(secureParam, Application.get()
						.getRequestCycleSettings().getResponseRequestEncoding());
			}
			catch (UnsupportedEncodingException ex)
			{
				throw new WicketRuntimeException(ex);
			}

			// Get the crypt implementation from the application
			final ICrypt urlCrypt = Application.get().getSecuritySettings().getCryptFactory()
					.newCrypt();

			// Decrypt the query string
			String queryString = urlCrypt.decryptUrlSafe(secureParam);

			// The querystring might have been shortened (length reduced).
			// In that case, lengthen the query string again.
			queryString = rebuildUrl(queryString);
			return queryString;
		}
		return null;
	}

	/**
	 * Try to shorten the querystring without loosing information. Note:
	 * WebRequestWithCryptedUrl must implement exactly the opposite logic.
	 * 
	 * @param queryString
	 *            The original query string
	 * @return The shortened querystring
	 */
	protected CharSequence shortenUrl(CharSequence queryString)
	{
		queryString = Strings.replaceAll(queryString,
				WebRequestCodingStrategy.BEHAVIOR_ID_PARAMETER_NAME + "=", "1-");
		queryString = Strings.replaceAll(queryString,
				WebRequestCodingStrategy.INTERFACE_PARAMETER_NAME + "=IRedirectListener", "2-");
		queryString = Strings.replaceAll(queryString,
				WebRequestCodingStrategy.INTERFACE_PARAMETER_NAME + "=IFormSubmitListener", "3-");
		queryString = Strings.replaceAll(queryString,
				WebRequestCodingStrategy.INTERFACE_PARAMETER_NAME + "=IOnChangeListener", "4-");
		queryString = Strings.replaceAll(queryString,
				WebRequestCodingStrategy.INTERFACE_PARAMETER_NAME + "=ILinkListener", "5-");
		queryString = Strings.replaceAll(queryString,
				WebRequestCodingStrategy.INTERFACE_PARAMETER_NAME + "=", "6-");
		queryString = Strings.replaceAll(queryString,
				WebRequestCodingStrategy.BOOKMARKABLE_PAGE_PARAMETER_NAME + "=", "7-");

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

	/**
	 * In case the query string has been shortened prior to encryption, than
	 * rebuild (lengthen) the query string now. Note: This implementation must
	 * exactly match the reverse one implemented in WebResponseWithCryptedUrl.
	 * 
	 * @param queryString
	 *            The URL's query string
	 * @return The lengthened query string
	 */
	protected String rebuildUrl(CharSequence queryString)
	{
		queryString = Strings.replaceAll(queryString, "1-",
				WebRequestCodingStrategy.BEHAVIOR_ID_PARAMETER_NAME + "=");
		queryString = Strings.replaceAll(queryString, "2-",
				WebRequestCodingStrategy.INTERFACE_PARAMETER_NAME + "=IRedirectListener");
		queryString = Strings.replaceAll(queryString, "3-",
				WebRequestCodingStrategy.INTERFACE_PARAMETER_NAME + "=IFormSubmitListener");
		queryString = Strings.replaceAll(queryString, "4-",
				WebRequestCodingStrategy.INTERFACE_PARAMETER_NAME + "=IOnChangeListener");
		queryString = Strings.replaceAll(queryString, "5-",
				WebRequestCodingStrategy.INTERFACE_PARAMETER_NAME + "=ILinkListener");
		queryString = Strings.replaceAll(queryString, "6-",
				WebRequestCodingStrategy.INTERFACE_PARAMETER_NAME + "=");
		queryString = Strings.replaceAll(queryString, "7-",
				WebRequestCodingStrategy.BOOKMARKABLE_PAGE_PARAMETER_NAME + "=");

		return queryString.toString();
	}

	/**
	 * IRequestCodingStrategy.decode(Request) requires a Request parameter and
	 * not a URL. Hence, based on the original URL and the decoded 'x' parameter
	 * a new Request object must be created to serve the default coding strategy
	 * as input for analyzing the URL.
	 */
	private static class DecodedUrlRequest extends Request
	{
		/** The original request */
		private final Request request;

		/** The new URL with the 'x' param decoded */
		private final String url;

		/**
		 * The new parameter map with the 'x' param removed and the 'new' one
		 * included
		 */
		private final Map parameterMap;

		/** The start index to the new relative URL */
		private final int startRelativeUrl;

		/**
		 * Construct.
		 * 
		 * @param request
		 * @param url
		 * @param encodedParamReplacement
		 */
		@SuppressWarnings("unchecked")
		public DecodedUrlRequest(final Request request, final String url,
				final String encodedParamReplacement)
		{
			this.request = request;

			// Create a copy of the original parameter map
			this.parameterMap = this.request.getParameterMap();

			// Remove the 'x' parameter which contains ALL the encoded params
			this.parameterMap.remove("x");

			// Add ALL of the params from the decoded 'x' param
			PageParameters params = new PageParameters(encodedParamReplacement, "&");
			this.parameterMap.putAll(params);

			// Rebuild the URL with the 'x' param removed
			int pos1 = url.indexOf("?x=");
			if (pos1 == -1)
			{
				throw new WicketRuntimeException("Programming error: we should come here");
			}
			int pos2 = url.indexOf("&");

			AppendingStringBuffer urlBuf = new AppendingStringBuffer(url.length()
					+ encodedParamReplacement.length());
			urlBuf.append(url.subSequence(0, pos1 + 1));
			urlBuf.append(encodedParamReplacement);
			if (pos2 != -1)
			{
				urlBuf.append(url.substring(pos2));
			}
			this.url = urlBuf.toString();

			// Determine the index for the relative path.
			this.startRelativeUrl = url.indexOf(request.getRelativeURL());
		}

		/**
		 * Delegate to the original request
		 * 
		 * @see wicket.Request#getLocale()
		 */
		@Override
		public Locale getLocale()
		{
			return this.request.getLocale();
		}

		/**
		 * @see wicket.Request#getParameter(java.lang.String)
		 */
		@Override
		public String getParameter(final String key)
		{
			if (key == null)
			{
				return null;
			}
			return (String)this.parameterMap.get(key);
		}

		/**
		 * @see wicket.Request#getParameterMap()
		 */
		@Override
		public Map<String,?> getParameterMap()
		{
			return this.parameterMap;
		}

		/**
		 * @see wicket.Request#getParameters(java.lang.String)
		 */
		@Override
		public String[] getParameters(final String key)
		{
			if (key == null)
			{
				return null;
			}
			return (String[])this.parameterMap.get(key);
		}

		/**
		 * @see wicket.Request#getPath()
		 */
		@Override
		public String getPath()
		{
			// Hasn't changed. We only encoded the querystring
			return this.request.getPath();
		}

		/**
		 * @see wicket.Request#getRelativeURL()
		 */
		@Override
		public String getRelativeURL()
		{
			return this.url.substring(this.startRelativeUrl);
		}

		/**
		 * @see wicket.Request#getURL()
		 */
		@Override
		public String getURL()
		{
			return this.url;
		}
	}
}
