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

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;

import org.apache.wicket.util.lang.Args;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adapted from Spring's UriUtils, but defines instances for query string decoding versus URL path
 * component decoding.
 * <p/>
 * The difference is important because a space is encoded as a + in a query string, but this is a
 * valid value in a path component (and is therefore not decode back to a space).
 *
 * @author Thomas Heigl
 * @see org.springframework.web.util.UriUtils
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
	 * For example: http://org.acme/notthis/northis/oreventhis?buthis=isokay&amp;asis=thispart
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
	 */
	public String decode(final String s, final String enc)
	{
		Args.notNull(enc, "enc");

		try
		{
			return decode(s, Charset.forName(enc));
		}
		catch (IllegalCharsetNameException | UnsupportedCharsetException e)
		{
			throw new RuntimeException(new UnsupportedEncodingException(enc));
		}
	}

	/**
	 * @param source
	 *            string to decode
	 * @param charset
	 *            encoding to decode with
	 * @return decoded string
	 */
	public String decode(final String source, final Charset charset)
	{
		if (source == null || source.isEmpty())
		{
			return source;
		}

		Args.notNull(charset, "charset");

		final int length = source.length();
		final ByteArrayOutputStream bos = new ByteArrayOutputStream(length);
		boolean changed = false;
		for (int i = 0; i < length; i++)
		{
			final int ch = source.charAt(i);
			if (ch == '%')
			{
				if (i + 2 < length)
				{
					final char hex1 = source.charAt(i + 1);
					final char hex2 = source.charAt(i + 2);
					final int u = Character.digit(hex1, 16);
					final int l = Character.digit(hex2, 16);
					if (u != -1 && l != -1)
					{
						bos.write((char)((u << 4) + l));
						i += 2;
					}
					changed = true;
				}
				else
				{
					LOG.info(
						"Incomplete trailing escape (%) pattern in '{}'. The escape character (%) will be ignored.",
						source);
				}
			}
			else if (ch == '+')
			{
				if (decodePlus)
				{
					bos.write(' ');
					changed = true;
				}
				else
				{
					bos.write(ch);
				}
			}
			else
			{
				bos.write(ch);
			}
		}
		final String result = changed ? new String(bos.toByteArray(), charset) : source;
		// no trying to filter out bad escapes beforehand, just kill all null bytes here at the end,
		// that way none will come through
		return result.replace("\0", "NULL");
	}
}
