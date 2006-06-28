/*
 * $Id: BookmarkablePageRequestTargetUrlCodingStrategy.java,v 1.1 2005/12/10
 * 21:28:56 eelco12 Exp $ $Revision$ $Date: 2006-04-02 14:09:16 -0700
 * (Sun, 02 Apr 2006) $
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
package wicket.request.target.coding;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.Application;
import wicket.util.string.AppendingStringBuffer;
import wicket.util.string.Strings;
import wicket.util.value.ValueMap;

/**
 * Abstract class for mount encoders that uses paths and forward slashes.
 * 
 * @author Eelco Hillenius
 * @author Igor Vaynberg (ivaynberg)
 */
public abstract class AbstractRequestTargetUrlCodingStrategy
		implements
			IRequestTargetUrlCodingStrategy
{
	/** log. */
	private static final Log log = LogFactory.getLog(AbstractRequestTargetUrlCodingStrategy.class);


	/** mounted path. */
	private final String mountPath;

	/**
	 * Construct.
	 * 
	 * @param mountPath
	 *            the mount path
	 */
	public AbstractRequestTargetUrlCodingStrategy(final String mountPath)
	{
		if (mountPath == null)
		{
			throw new IllegalArgumentException("Argument mountPath must be not null");
		}

		this.mountPath = mountPath;
	}

	/**
	 * Gets path.
	 * 
	 * @return path
	 */
	public final String getMountPath()
	{
		return mountPath;
	}

	/**
	 * Decodes parameters object from the provided url fragment
	 * 
	 * @param urlFragment
	 *            fragment of the url after the decoded path and before the
	 *            query string
	 * @param urlParameters
	 *            query string parameters
	 * @return Parameters created from the url fragment and query string
	 */
	protected ValueMap decodeParameters(String urlFragment,
			Map<String, ? extends Object> urlParameters)
	{
		// Hack off any leading slash
		if (urlFragment.startsWith("/"))
		{
			urlFragment = urlFragment.substring(1);
		}

		if (urlFragment.length() == 0)
		{
			return new ValueMap();
		}

		// Split into pairs
		final String[] pairs = urlFragment.split("/");

		// If we don't have an even number of pairs
		if (pairs.length % 2 != 0)
		{
			// give up
			throw new IllegalStateException("URL fragment has unmatched key/value " + "pair: "
					+ urlFragment);
		}

		// Loop through pairs

		ValueMap parameters = new ValueMap();
		for (int i = 0; i < pairs.length; i += 2)
		{
			String value = pairs[i + 1];
			value = urlDecode(value);
			parameters.add(pairs[i], value);
		}


		if (urlParameters != null)
		{
			parameters.putAll(urlParameters);
		}

		return parameters;
	}

	/**
	 * Returns a decoded value of the given value
	 * 
	 * @param value
	 * @return Decodes the value
	 */
	protected String urlDecode(String value)
	{
		try
		{
			value = URLDecoder.decode(value, Application.get().getRequestCycleSettings()
					.getResponseRequestEncoding());
		}
		catch (UnsupportedEncodingException ex)
		{
			log.error("error decoding parameter", ex);
		}
		return value;
	}

	/**
	 * Encodes Map into a url fragment and append that to the provided url
	 * buffer.
	 * 
	 * @param url
	 *            url so far
	 * 
	 * @param parameters
	 *            Map object to be encoded
	 */
	protected void appendParameters(AppendingStringBuffer url, Map parameters)
	{
		if (parameters != null && parameters.size() > 0)
		{
			Iterator entries = parameters.entrySet().iterator();
			while (entries.hasNext())
			{
				Map.Entry entry = (Entry)entries.next();
				if (entry.getValue() != null)
				{
					String escapedValue = urlEncode(entry.getValue().toString());
					if (!Strings.isEmpty(escapedValue))
					{
						url.append("/").append(entry.getKey()).append("/").append(escapedValue);
					}
				}
			}
		}
	}

	/**
	 * Url encodes a string
	 * 
	 * @param string
	 *            string to be encoded
	 * @return encoded string
	 */
	protected String urlEncode(String string)
	{
		try
		{
			return URLEncoder.encode(string, Application.get().getRequestCycleSettings()
					.getResponseRequestEncoding());
		}
		catch (UnsupportedEncodingException e)
		{
			log.error(e.getMessage(), e);
			return string;
		}

	}
}
