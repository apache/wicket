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

/**
 * Adapted from Spring Framework's UriUtils class, but defines instances for query string encoding versus URL path
 * component encoding.
 * <p/>
 * The difference is important because a space is encoded as a + in a query string, but this is a
 * valid value in a path component (and is therefore not decode back to a space).
 *
 * @author Thomas Heigl
 * @see <a href="http://www.ietf.org/rfc/rfc2396.txt">RFC-2396</a>
 */
public class UrlEncoder
{

	enum Type {
		//@formatter:off
		QUERY {
			@Override
			public boolean isAllowed(int c) 
			{
				return isPchar(c) ||
						' ' == c || // encoding a space to a + is done in the encode() method
						'*' == c ||
						'/' == c || // to allow direct passing of URL in query
						',' == c ||
						':' == c || // allowed and used in wicket interface
						'@' == c ;
			}
		},
		PATH {
			@Override
			public boolean isAllowed(int c) 
			{
				return isPchar(c) ||
						'*' == c ||
						'&' == c ||
						'+' == c ||
						',' == c ||
						';' == c || // semicolon is used in ;jsessionid=
						'=' == c ||
						':' == c || // allowed and used in wicket interface
						'@' == c ;

			}
		},
		HEADER {
			@Override
			public boolean isAllowed(int c) 
			{
				return isPchar(c) ||
						'#' == c ||
						'&' == c ||
						'+' == c ||
						'^' == c ||
						'`' == c ||
						'|' ==c;
			}
		};
		//@formatter:on

		/**
		 * Indicates whether the given character is allowed in this URI component.
		 * @return {@code true} if the character is allowed; {@code false} otherwise
		 */
		public abstract boolean isAllowed(int c);

		/**
		 * Indicates whether the given character is in the {@code ALPHA} set.
		 * @see <a href="https://www.ietf.org/rfc/rfc3986.txt">RFC 3986, appendix A</a>
		 */
		protected boolean isAlpha(int c)
		{
			return (c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z');
		}

		/**
		 * Indicates whether the given character is in the {@code DIGIT} set.
		 * @see <a href="https://www.ietf.org/rfc/rfc3986.txt">RFC 3986, appendix A</a>
		 */
		protected boolean isDigit(int c)
		{
			return (c >= '0' && c <= '9');
		}

		/**
		 * Indicates whether the given character is in the {@code sub-delims} set.
		 * @see <a href="https://www.ietf.org/rfc/rfc3986.txt">RFC 3986, appendix A</a>
		 */
		protected boolean isSubDelimiter(int c)
		{
			return ('!' == c || '$' == c);
		}

		/**
		 * Indicates whether the given character is in the {@code unreserved} set.
		 * @see <a href="https://www.ietf.org/rfc/rfc3986.txt">RFC 3986, appendix A</a>
		 */
		protected boolean isUnreserved(int c)
		{
			return (isAlpha(c) || isDigit(c) || '-' == c || '.' == c || '_' == c || '~' == c);
		}

		/**
		 * Indicates whether the given character is in the {@code pchar} set.
		 * @see <a href="https://www.ietf.org/rfc/rfc3986.txt">RFC 3986, appendix A</a>
		 */
		protected boolean isPchar(int c)
		{
			return (isUnreserved(c) || isSubDelimiter(c));
		}
	}

	private final Type type;

	/**
	 * Encoder used to encode name or value components of a query string.<br/>
	 * <br/>
	 *
	 * For example: http://org.acme/notthis/northis/oreventhis?buthis=isokay&amp;asis=thispart
	 */
	public static final UrlEncoder QUERY_INSTANCE = new UrlEncoder(Type.QUERY);

	/**
	 * Encoder used to encode segments of a path.<br/>
	 * <br/>
	 *
	 * For example: http://org.acme/foo/thispart/orthispart?butnot=thispart
	 */
	public static final UrlEncoder PATH_INSTANCE = new UrlEncoder(Type.PATH);

	/**
	 * Encoder used to encode a header.
	 */
	public static final UrlEncoder HEADER_INSTANCE = new UrlEncoder(Type.HEADER);

	/**
	 * Allow subclass to call constructor.
	 *
	 * @param type
	 *            encoder type
	 */
	protected UrlEncoder(final Type type)
	{
		this.type = type;
	}

	/**
	 * @param s
	 *            string to encode
	 * @param charsetName
	 *            charset to use for encoding
	 * @return encoded string
	 */
	public String encode(final String s, final String charsetName)
	{
		Args.notNull(charsetName, "charsetName");

		try
		{
			return encode(s, Charset.forName(charsetName));
		}
		catch (IllegalCharsetNameException | UnsupportedCharsetException e)
		{
			throw new RuntimeException(new UnsupportedEncodingException(charsetName));
		}
	}

	/**
	 * @param unsafeInput
	 *            string to encode
	 * @param charset
	 *            encoding to use
	 * @return encoded string
	 */
	public String encode(final String unsafeInput, final Charset charset)
	{
		if (unsafeInput == null || unsafeInput.isEmpty())
		{
			return unsafeInput;
		}

		Args.notNull(charset, "charset");

		final byte[] bytes = unsafeInput.getBytes(charset);
		boolean original = true;
		for (final byte b : bytes)
		{
			if (!type.isAllowed(b) || b == ' ' || b == '\0')
			{
				original = false;
				break;
			}
		}
		if (original)
		{
			return unsafeInput;
		}

		final ByteArrayOutputStream bos = new ByteArrayOutputStream(bytes.length);
		for (final byte b : bytes)
		{
			if (type.isAllowed(b))
			{
				if (b == ' ')
				{
					bos.write('+');
				}
				else
				{
					bos.write(b);
				}
			}
			else
			{
				if (b == '\0')
				{
					bos.writeBytes("NULL".getBytes(charset));
				}
				else
				{
					bos.write('%');
					bos.write(Character.toUpperCase(Character.forDigit((b >> 4) & 0xF, 16)));
					bos.write(Character.toUpperCase(Character.forDigit(b & 0xF, 16)));
				}
			}
		}
		return bos.toString(charset);
	}

}
