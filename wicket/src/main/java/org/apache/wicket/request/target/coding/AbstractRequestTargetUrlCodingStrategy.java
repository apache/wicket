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

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.wicket.protocol.http.WicketURLDecoder;
import org.apache.wicket.protocol.http.WicketURLEncoder;
import org.apache.wicket.protocol.http.servlet.AbortWithWebErrorCodeException;
import org.apache.wicket.util.string.AppendingStringBuffer;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.value.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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
	private static final Logger log = LoggerFactory.getLogger(AbstractRequestTargetUrlCodingStrategy.class);

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
			throw new IllegalArgumentException("Mount path cannot be null or empty");
		}
		this.mountPath = mountPath.startsWith("/") ? mountPath.substring(1) : mountPath;
		if (this.mountPath.startsWith("resources/") || this.mountPath.equals("resources"))
		{
			throw new IllegalArgumentException("Mount path cannot be under '/resources'");
		}
	}

	/**
	 * @see org.apache.wicket.request.target.coding.IMountableRequestTargetUrlCodingStrategy#getMountPath()
	 */
	public final String getMountPath()
	{
		return mountPath;
	}

	/**
	 * Encodes Map into a url fragment and append that to the provided url buffer.
	 * 
	 * @param url
	 *            url so far
	 * 
	 * @param parameters
	 *            Map object to be encoded
	 */
	protected void appendParameters(AppendingStringBuffer url, Map<?,?> parameters)
	{
		if (parameters != null && parameters.size() > 0)
		{
			for (Entry<?, ?> entry1 : parameters.entrySet())
			{
				Object value = ((Entry<?, ?>) entry1).getValue();
				if (value != null)
				{
					if (value instanceof String[])
					{
						String[] values = (String[]) value;
						for (String value1 : values)
						{
							appendValue(url, ((Entry<?, ?>) entry1).getKey().toString(), value1);
						}
					} else
					{
						appendValue(url, ((Entry<?, ?>) entry1).getKey().toString(), value.toString());
					}
				}
			}
		}
	}

	private void appendValue(AppendingStringBuffer url, String key, String value)
	{
		String escapedValue = urlEncodePathComponent(value);
		if (!Strings.isEmpty(escapedValue))
		{
			if (!url.endsWith("/"))
			{
				url.append("/");
			}
			url.append(key).append("/").append(escapedValue).append("/");
		}
	}

	/**
	 * Decodes parameters object from the provided url fragment
	 * 
	 * @param urlFragment
	 *            fragment of the url after the decoded path and before the query string
	 * @param urlParameters
	 *            query string parameters
	 * @return Parameters created from the url fragment and query string
	 */
	protected ValueMap decodeParameters(String urlFragment, Map<String,Object> urlParameters)
	{
		// Hack off any leading slash
		if (urlFragment.startsWith("/"))
		{
			urlFragment = urlFragment.substring(1);
		}
		// Hack off any trailing slash
		if (urlFragment.length() > 0 && urlFragment.endsWith("/"))
		{
			urlFragment = urlFragment.substring(0, urlFragment.length() - 1);
		}

		if (urlFragment.length() == 0)
		{
			return new ValueMap(urlParameters != null ? urlParameters : Collections.EMPTY_MAP);
		}

		// Split into pairs
		final String[] pairs = urlFragment.split("/");

		// If we don't have an even number of pairs
		if (pairs.length % 2 != 0)
		{
			log.warn("URL fragment has unmatched key/value pairs, responding with 404. Fragment: " +
				urlFragment);
			throw new AbortWithWebErrorCodeException(404);
		}

		// Loop through pairs

		ValueMap parameters = new ValueMap();
		for (int i = 0; i < pairs.length; i += 2)
		{
			String value = pairs[i + 1];
			value = urlDecodePathComponent(value);
			parameters.add(pairs[i], value);
		}


		if (urlParameters != null)
		{
			parameters.putAll(urlParameters);
		}

		return parameters;
	}

	/**
	 * Url encodes a string that is mean for a URL path (e.g., between slashes)
	 * 
	 * @param string
	 *            string to be encoded
	 * @return encoded string
	 */
	protected String urlEncodePathComponent(String string)
	{
		return WicketURLEncoder.PATH_INSTANCE.encode(string);
	}

	/**
	 * Returns a decoded value of the given value (taken from a URL path section)
	 * 
	 * @param value
	 * @return Decodes the value
	 */
	protected String urlDecodePathComponent(String value)
	{
		return WicketURLDecoder.PATH_INSTANCE.decode(value);
	}

	/**
	 * Url encodes a string mean for a URL query string
	 * 
	 * @param string
	 *            string to be encoded
	 * @return encoded string
	 */
	protected String urlEncodeQueryComponent(String string)
	{
		return WicketURLEncoder.QUERY_INSTANCE.encode(string);
	}

	/**
	 * Returns a decoded value of the given value (taken from a URL query string)
	 * 
	 * @param value
	 * @return Decodes the value
	 */
	protected String urlDecodeQueryComponent(String value)
	{
		return WicketURLDecoder.QUERY_INSTANCE.decode(value);
	}

	/**
	 * @deprecated Use urlEncodePathComponent or urlEncodeQueryComponent instead
	 */
	@Deprecated
	protected String urlDecode(String value)
	{
		return urlDecodePathComponent(value);
	}

	/**
	 * @deprecated Use urlEncodePathComponent or urlEncodeQueryComponent instead
	 */
	@Deprecated
	protected String urlEncode(String string)
	{
		return urlEncodePathComponent(string);
	}

	/**
	 * Does given path match this mount? We match /mount/point or /mount/point/with/extra/path, but
	 * not /mount/pointXXX.
	 * 
	 * @param path
	 * @param caseSensitive
	 *            whether the strategy should treat <code>path</code> argument with case sensitivity
	 *            or not
	 * @return true if matches, false otherwise
	 */
	public boolean matches(String path, boolean caseSensitive)
	{
		if (Strings.startsWith(path, mountPath, caseSensitive))
		{
			/*
			 * We need to match /mount/point or /mount/point/with/extra/path, but not
			 * /mount/pointXXX
			 */
			String remainder = path.substring(mountPath.length());
			if (remainder.length() == 0 || remainder.startsWith("/"))
			{
				return true;
			}
		}
		return false;
	}
}
