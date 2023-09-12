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
package org.apache.wicket.resource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Locale;

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.io.IOUtils;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
import org.apache.wicket.util.string.Strings;

/**
 * Utilities for resources.
 * 
 * @author Jeremy Thomerson
 */
public class ResourceUtil
{

	/**
	 * Used to denote {@code null} in encoded strings.
	 */
	private static final String NULL_VALUE = "null";

	/**
	 * Reads resource reference attributes (style, locale, variation) encoded in the given string.
	 * 
	 * @param encodedAttributes
	 * 			the string containing the resource attributes
	 * @return the encoded attributes
	 * 
	 * @see ResourceReference.UrlAttributes
	 */
	public static ResourceReference.UrlAttributes decodeResourceReferenceAttributes(String encodedAttributes)
	{
		String[] decodeAttributes = decodeStringParts(encodedAttributes);

		Locale locale = decodeAttributes.length > 0 ? parseLocale(decodeAttributes[0]) : null;
		String style = decodeAttributes.length > 1 ? decodeAttributes[1] : null;
		String variation = decodeAttributes.length > 2 ? decodeAttributes[2] : null;

		return new ResourceReference.UrlAttributes(locale, style, variation);
	}
	
	/**
	 * Reads resource reference attributes (style, locale, variation) encoded in the given URL. 
	 * 
	 * @param url
	 * 			the url containing the resource attributes
	 * @return the encoded attributes
	 * 
	 * @see ResourceReference.UrlAttributes
	 */
	public static ResourceReference.UrlAttributes decodeResourceReferenceAttributes(Url url)
	{
		Args.notNull(url, "url");
	
		if (url.getQueryParameters().size() > 0)
		{
			Url.QueryParameter param = url.getQueryParameters().get(0);
			if (Strings.isEmpty(param.getValue()))
			{
				return decodeResourceReferenceAttributes(param.getName());
			}
		}
		return new ResourceReference.UrlAttributes(null, null, null);
	}

	/**
	 * Encodes the given resource reference attributes returning the corresponding textual representation.
	 * 
	 * @param attributes
	 * 		the resource reference attributes to encode
	 * @return the textual representation for the given attributes
	 * 
	 * @see ResourceReference.UrlAttributes
	 */
	public static String encodeResourceReferenceAttributes(ResourceReference.UrlAttributes attributes)
	{
		if (attributes == null ||
				(attributes.getLocale() == null && attributes.getStyle() == null && attributes.getVariation() == null))
			{
				return null;
			}
			else
			{
				StringBuilder buffer = new StringBuilder(32);
				encodeStringPart(attributes.getLocale() == null ? null : attributes.getLocale().toString(), buffer);
				encodeStringPart(attributes.getStyle(), buffer);
				encodeStringPart(attributes.getVariation(), buffer);
				return buffer.toString();
			}
	}

	/**
	 * Encodes the attributes of the given resource reference in the specified url.
	 * 
	 * @param url
	 * 			the resource reference attributes to encode
	 * @param reference
	 * 
	 * @see ResourceReference.UrlAttributes
	 * @see Url
	 */
	public static void encodeResourceReferenceAttributes(Url url, ResourceReference reference)
	{
		Args.notNull(url, "url");
		Args.notNull(reference, "reference");

		String encoded = encodeResourceReferenceAttributes(reference.getUrlAttributes());
		if (!Strings.isEmpty(encoded))
		{
			url.getQueryParameters().add(new Url.QueryParameter(encoded, ""));
		}
	}

	/**
	 * Parses the string representation of a {@link java.util.Locale} (for example 'en_GB').
	 * 
	 * @param locale
	 * 		the string representation of a {@link java.util.Locale}
	 * @return the corresponding {@link java.util.Locale} instance
	 */
	public static Locale parseLocale(String locale)
	{
		if (Strings.isEmpty(locale))
		{
			return null;
		}
		else
		{
			String parts[] = locale.toLowerCase(Locale.ROOT).split("_", 3);
			if (parts.length == 1)
			{
				return new Locale(parts[0]);
			}
			else if (parts.length == 2)
			{
				return new Locale(parts[0], parts[1]);
			}
			else if (parts.length == 3)
			{
				return new Locale(parts[0], parts[1], parts[2]);
			}
			else
			{
				return null;
			}
		}
	}

	/**
	 * read string with platform default encoding from resource stream
	 * 
	 * @param resourceStream
	 * @return string read from resource stream
	 * 
	 * @see #readString(org.apache.wicket.util.resource.IResourceStream, java.nio.charset.Charset)
	 */
	public static String readString(IResourceStream resourceStream)
	{
		return readString(resourceStream, null);
	}

	/**
	 * read string with specified encoding from resource stream
	 * 
	 * @param resourceStream
	 *            string source
	 * @param charset
	 *            charset for the string encoding (use <code>null</code> for platform default)
	 * @return string read from resource stream
	 */
	public static String readString(IResourceStream resourceStream, Charset charset)
	{
		try
		{
			InputStream stream = resourceStream.getInputStream();

			try
			{
				byte[] bytes = IOUtils.toByteArray(stream);

				if (charset == null)
                                {
					charset = Charset.defaultCharset();
                                }

				return new String(bytes, charset.name());
			}
			finally
			{
				resourceStream.close();
			}
		}
		catch (IOException e)
		{
			throw new WicketRuntimeException("failed to read string from " + resourceStream, e);
		}
		catch (ResourceStreamNotFoundException e)
		{
			throw new WicketRuntimeException("failed to locate stream from " + resourceStream, e);
		}
	}

	/**
	 * Encode the {@code part} in the format <string length encoded in base ten ASCII>~<string data>.
	 *
	 * If the {@code part} is {@code null} the special value {@link #NULL_VALUE} is used;
	 *
	 * @param part
	 *     The string to encode.
	 * @param buffer
	 *     The buffer into which the {@code part} is encoded.
	 * @return The {@code buffer} for chaining.
	 */
	static StringBuilder encodeStringPart(String part, StringBuilder buffer)
	{
		if (part == null) {
			return buffer.append(NULL_VALUE);
		}

		int length = part.length();
		return buffer.append(length).append('~').append(part);
	}

	/**
	 * Decodes the {@code encoded} parts of a string decoded by {@link #encodeStringPart(String)}.
	 *
	 * @param encoded
	 * @return An array containing the parts of {@code encoded}.
	 *     The array can contain {@code null} but is itself never {@code null}
	 */
	static String[] decodeStringParts(String encoded)
	{
		ArrayList<String> result = new ArrayList<>();

		StringBuilder lengthString = new StringBuilder();
		char[] chars = encoded.toCharArray();
		for (int i = 0; i < chars.length; i++)
		{
			boolean isAtStartOfPart = lengthString.length() == 0;
			if (isAtStartOfPart && chars.length >= i + NULL_VALUE.length() &&
					String.valueOf(chars, i, NULL_VALUE.length()).equals(NULL_VALUE)) {
				result.add(null);
				i += NULL_VALUE.length() - 1;
				continue;
			}

			char c = chars[i];
			if (c >= '0' && c <= '9') {
				lengthString.append(c);
			} else {
				if (isAtStartOfPart || c != '~') { // Not a (valid) resource attribute string
					return new String[0];
				}

				int length = Integer.parseInt(lengthString.toString());
				lengthString.setLength(0); // reset the length buffer
				result.add(String.valueOf(chars, i + 1, length));
				i += length;
			}
		}

		return result.toArray(String[]::new);
	}

	private ResourceUtil()
	{
		// no-op
	}
}
