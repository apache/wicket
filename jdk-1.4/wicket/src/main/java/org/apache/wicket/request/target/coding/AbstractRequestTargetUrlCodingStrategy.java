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
package org.apache.wicket.request.target.coding;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.Application;
import org.apache.wicket.protocol.http.UnitTestSettings;
import org.apache.wicket.util.string.AppendingStringBuffer;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.value.ValueMap;


/**
 * Abstract class for mount encoders that uses paths and forward slashes.
 * 
 * @author Eelco Hillenius
 * @author Igor Vaynberg (ivaynberg)
 */
public abstract class AbstractRequestTargetUrlCodingStrategy
		implements
			IRequestTargetUrlCodingStrategy,
			IMountableRequestTargetUrlCodingStrategy
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
		checkMountPath(mountPath);
		this.mountPath = (mountPath.startsWith("/")) ? mountPath.substring(1) : mountPath;
	}

	/**
	 * @see org.apache.wicket.request.target.coding.IMountableRequestTargetUrlCodingStrategy#getMountPath()
	 */
	public final String getMountPath()
	{
		return mountPath;
	}

	/**
	 * Checks mount path is valid.
	 * 
	 * @param path
	 *            mount path
	 */
	private void checkMountPath(String path)
	{
		if (path == null)
		{
			throw new IllegalArgumentException("Mount path cannot be null");
		}
		if (path.startsWith("/resources/") || path.equals("/resources"))
		{
			throw new IllegalArgumentException("Mount path cannot start with '/resources'");
		}
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
			final Iterator entries;
			if (UnitTestSettings.getSortUrlParameters())
			{
				entries = new TreeMap(parameters).entrySet().iterator();
			}
			else
			{
				entries = parameters.entrySet().iterator();
			}
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
	 * Decodes parameters object from the provided url fragment
	 * 
	 * @param urlFragment
	 *            fragment of the url after the decoded path and before the
	 *            query string
	 * @param urlParameters
	 *            query string parameters
	 * @return Parameters created from the url fragment and query string
	 */
	protected ValueMap decodeParameters(String urlFragment, Map urlParameters)
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
