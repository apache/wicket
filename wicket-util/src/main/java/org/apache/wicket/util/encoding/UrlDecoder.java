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
package org.apache.wicket.util.encoding;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import org.apache.wicket.util.string.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adapted from java.net.URLDecoder, but defines instances for query string decoding versus URL path
 * component decoding.
 * <p/>
 * The difference is important because a space is encoded as a + in a query string, but this is a
 * valid value in a path component (and is therefore not decode back to a space).
 * 
 * @author Doug Donohoe
 * @see java.net.URLDecoder
 * @see <a href="http://www.ietf.org/rfc/rfc2396.txt">RFC-2396</a>
 */
public class UrlDecoder
{
	private static final Logger LOG = LoggerFactory.getLogger(UrlDecoder.class);

	private final boolean decodePlus;

	/**
	 * Encoder used to decode name or value components of a query string.<br/>
	 * <br/>
	 * 
	 * For example: http://org.acme/notthis/northis/oreventhis?buthis=isokay&asis=thispart
	 */
	public static final UrlDecoder QUERY_INSTANCE = new UrlDecoder(true);

	/**
	 * Encoder used to decode components of a path.<br/>
	 * <br/>
	 * 
	 * For example: http://org.acme/foo/thispart/orthispart?butnot=thispart
	 */
	public static final UrlDecoder PATH_INSTANCE = new UrlDecoder(false);

	/**
	 * Create decoder
	 * 
	 * @param decodePlus
	 *            - whether to decode + to space
	 */
	private UrlDecoder(final boolean decodePlus)
	{
		this.decodePlus = decodePlus;
	}

	/**
	 * @param s
	 *            string to decode
	 * @param enc
	 *            encoding to decode with
	 * @return decoded string
	 * @see java.net.URLDecoder#decode(String, String)
	 */
	public String decode(final String s, final Charset enc)
	{
		return decode(s, enc.name());
	}

	/**
	 * @param s
	 *            string to decode
	 * @param enc
	 *            encoding to decode with
	 * @return decoded string
	 * @see java.net.URLDecoder#decode(String, String)
	 */
	public String decode(final String s, final String enc)
	{
		if (Strings.isEmpty(s))
		{
			return s;
		}

		int numChars = s.length();
		StringBuilder sb = new StringBuilder(numChars > 500 ? numChars / 2 : numChars);
		int i = 0;

		if (enc.length() == 0)
		{
			throw new RuntimeException(new UnsupportedEncodingException(
				"URLDecoder: empty string enc parameter"));
		}

		char c;
		byte[] bytes = null;
		while (i < numChars)
		{
			c = s.charAt(i);
			switch (c)
			{
				case '+' :
					sb.append(decodePlus ? ' ' : '+');
					i++;
					break;

				case '%' :
					/*
					 * Starting with this instance of %, process all consecutive substrings of the
					 * form %xy. Each substring %xy will yield a byte. Convert all consecutive bytes
					 * obtained this way to whatever character(s) they represent in the provided
					 * encoding.
					 */
					try
					{
						// (numChars-i)/3 is an upper bound for the number
						// of remaining bytes
						if (bytes == null)
						{
							bytes = new byte[(numChars - i) / 3];
						}
						int pos = 0;

						while (((i + 2) < numChars) && (c == '%'))
						{
							bytes[pos++] = (byte)Integer.parseInt(s.substring(i + 1, i + 3), 16);
							i += 3;
							if (i < numChars)
							{
								c = s.charAt(i);
							}
						}

						// A trailing, incomplete byte encoding such as
						// "%x" will cause an exception to be thrown
						if ((i < numChars) && (c == '%'))
						{
							LOG.info("Incomplete trailing escape (%) pattern in '%s'. The escape character (%) will be ignored.",
									s);
							i++;
							break;
						}

						try
						{
							sb.append(new String(bytes, 0, pos, enc));
						}
						catch (UnsupportedEncodingException e)
						{
							throw new RuntimeException(e);
						}
					}
					catch (NumberFormatException e)
					{
						LOG.info("Illegal hex characters in escape (%) pattern in '{}'. " +
								"The escape character (%) will be ignored. NumberFormatException: {} ",
							s, e.getMessage());
						i++;
					}
					break;

				default :
					sb.append(c);
					i++;
					break;
			}
		}

		// no trying to filter out bad escapes beforehand, just kill all null bytes here at the end,
		// that way none will come through
		return sb.toString().replace("\0", "NULL");
	}
}