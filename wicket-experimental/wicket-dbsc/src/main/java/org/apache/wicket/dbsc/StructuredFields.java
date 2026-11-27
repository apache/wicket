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
package org.apache.wicket.dbsc;

import java.util.ArrayList;
import java.util.List;

/**
 * The small part of RFC 9651 structured fields the DBSC headers need: writing and reading a
 * string item, and finding a token in a list by one of its parameters.
 */
final class StructuredFields
{
	private StructuredFields()
	{
	}

	/**
	 * Serializes a string item.
	 *
	 * @param value
	 *            the value, printable ASCII only
	 * @return the value in double quotes, with {@code "} and {@code \} escaped
	 * @throws IllegalArgumentException
	 *             if the value holds a character a string item cannot carry
	 */
	static String string(String value)
	{
		StringBuilder serialized = new StringBuilder(value.length() + 2).append('"');
		for (int i = 0; i < value.length(); i++)
		{
			char c = value.charAt(i);
			if (c < 0x20 || c > 0x7e)
			{
				throw new IllegalArgumentException(
					"A structured field string cannot hold character " + (int)c);
			}
			if (c == '"' || c == '\\')
			{
				serialized.append('\\');
			}
			serialized.append(c);
		}
		return serialized.append('"').toString();
	}

	/**
	 * Parses the string item a header value starts with and drops its parameters.
	 * <p>
	 * A value that does not start with a double quote is taken as a bare string up to its first
	 * parameter.
	 *
	 * @param value
	 *            the header value, may be {@code null}
	 * @return the unescaped string, or {@code null} if the value is {@code null}, empty or not a
	 *         well-formed string
	 */
	static String parseString(String value)
	{
		if (value == null)
		{
			return null;
		}
		String trimmed = value.trim();
		if (trimmed.isEmpty())
		{
			return null;
		}
		if (trimmed.charAt(0) != '"')
		{
			int parameters = trimmed.indexOf(';');
			String bare = (parameters < 0 ? trimmed : trimmed.substring(0, parameters)).trim();
			return bare.isEmpty() ? null : bare;
		}
		StringBuilder parsed = new StringBuilder(trimmed.length());
		for (int i = 1; i < trimmed.length(); i++)
		{
			char c = trimmed.charAt(i);
			if (c == '"')
			{
				return parsed.toString();
			}
			if (c == '\\')
			{
				if (++i == trimmed.length())
				{
					return null;
				}
				c = trimmed.charAt(i);
				if (c != '"' && c != '\\')
				{
					return null;
				}
			}
			else if (c < 0x20 || c > 0x7e)
			{
				return null;
			}
			parsed.append(c);
		}
		return null;
	}

	/**
	 * Finds the member of a list of tokens that carries a parameter with a given string value, as
	 * {@code Secure-Session-Skipped} names the DBSC session it reports on.
	 *
	 * @param lists
	 *            the field lines of the list
	 * @param parameter
	 *            the name of the parameter
	 * @param value
	 *            the value the parameter has to have
	 * @return the token of the first such member, or {@code null}
	 */
	static String tokenFor(List<String> lists, String parameter, String value)
	{
		for (String list : lists)
		{
			for (String member : splitOutsideStrings(list, ','))
			{
				List<String> parts = splitOutsideStrings(member, ';');
				String token = parts.get(0).trim();
				if (token.isEmpty() || token.charAt(0) == '"')
				{
					continue;
				}
				for (String part : parts.subList(1, parts.size()))
				{
					int separator = part.indexOf('=');
					if (separator > 0 && part.substring(0, separator).trim().equals(parameter) &&
						value.equals(parseString(part.substring(separator + 1))))
					{
						return token;
					}
				}
			}
		}
		return null;
	}

	private static List<String> splitOutsideStrings(String value, char separator)
	{
		List<String> parts = new ArrayList<>();
		boolean inString = false;
		int start = 0;
		for (int i = 0; i < value.length(); i++)
		{
			char c = value.charAt(i);
			if (inString && c == '\\')
			{
				i++;
			}
			else if (c == '"')
			{
				inString = !inString;
			}
			else if (!inString && c == separator)
			{
				parts.add(value.substring(start, i));
				start = i + 1;
			}
		}
		parts.add(value.substring(start));
		return parts;
	}
}
