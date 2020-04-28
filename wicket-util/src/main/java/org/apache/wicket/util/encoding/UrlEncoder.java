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

import java.io.CharArrayWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.BitSet;

import org.apache.wicket.util.lang.Args;

/**
 * Adapted from java.net.URLEncoder, but defines instances for query string encoding versus URL path
 * component encoding.
 * <p/>
 * The difference is important because a space is encoded as a + in a query string, but this is a
 * valid value in a path component (and is therefore not decode back to a space).
 * 
 * @author Doug Donohoe
 * @see java.net.URLEncoder
 * @see <a href="http://www.ietf.org/rfc/rfc2396.txt">RFC-2396</a>
 */
public class UrlEncoder
{
	/**
	 * encoder types
	 */
	public enum Type {
		QUERY,
		PATH,
		HEADER
	}

	/**
	 * List of what not to encode, i.e. characters (e.g. A-Z) and other allowed signs (e.g. !)
	 * that are allowed but don't have a special meaning.
	 */
	protected BitSet dontNeedEncoding;

	// used in decoding
	protected static final int caseDiff = ('a' - 'A');

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
		/*
		 * This note from java.net.URLEncoder ==================================
		 * 
		 * The list of characters that are not encoded has been determined as follows:
		 * 
		 * RFC 2396 states: ----- Data characters that are allowed in a URI but do not have a
		 * reserved purpose are called unreserved. These include upper and lower case letters,
		 * decimal digits, and a limited set of punctuation marks and symbols.
		 * 
		 * unreserved = alphanum | mark
		 * 
		 * mark = "-" | "_" | "." | "!" | "~" | "*" | "'" | "(" | ")"
		 * 
		 * Unreserved characters can be escaped without changing the semantics of the URI, but this
		 * should not be done unless the URI is being used in a context that does not allow the
		 * unescaped character to appear. -----
		 * 
		 * It appears that both Netscape and Internet Explorer escape all special characters from
		 * this list with the exception of "-", "_", ".", "*". While it is not clear why they are
		 * escaping the other characters, perhaps it is safest to assume that there might be
		 * contexts in which the others are unsafe if not escaped. Therefore, we will use the same
		 * list. It is also noteworthy that this is consistent with O'Reilly's
		 * "HTML: The Definitive Guide" (page 164).
		 * 
		 * As a last note, Intenet Explorer does not encode the "@" character which is clearly not
		 * unreserved according to the RFC. We are being consistent with the RFC in this matter, as
		 * is Netscape.
		 * 
		 * This bit added by Doug Donohoe ================================== RFC 3986 (2005) updates
		 * this (http://tools.ietf.org/html/rfc3986):
		 * 
		 * unreserved = ALPHA / DIGIT / "-" / "." / "_" / "~"
		 * 
		 * pct-encoded = "%" HEXDIG HEXDIG
		 * 
		 * reserved = gen-delims / sub-delims
		 * 
		 * gen-delims = ":" / "/" / "?" / "#" / "[" / "]" / "@"
		 * 
		 * sub-delims = "!" / "$" / "&" / "'" / "(" / ")" / "*" / "+" / "," / ";" / "=" // -- PATH
		 * COMPONENT -- //
		 * 
		 * path = (see RFC for all variations) path-abempty =( "/" segment ) segment =pchar pchar =
		 * unreserved / pct-encoded / sub-delims / ":" / "@" // -- QUERY COMPONENT -- //
		 * 
		 * query =( pchar / "/" / "?" )
		 */

		// unreserved
		dontNeedEncoding = new BitSet(256);
		int i;
		for (i = 'a'; i <= 'z'; i++)
		{
			dontNeedEncoding.set(i);
		}
		for (i = 'A'; i <= 'Z'; i++)
		{
			dontNeedEncoding.set(i);
		}
		for (i = '0'; i <= '9'; i++)
		{
			dontNeedEncoding.set(i);
		}
		dontNeedEncoding.set('-');
		dontNeedEncoding.set('.');
		dontNeedEncoding.set('_');
		// tilde encoded by java.net.URLEncoder version, but RFC is clear on this
		dontNeedEncoding.set('~');

		// sub-delims
		dontNeedEncoding.set('!');
		dontNeedEncoding.set('$');

		// encoding type-specific
		switch (type)
		{
			case QUERY :
				// this code consistent with java.net.URLEncoder version#
				
				// encoding a space to a + is done in the encode() method
				dontNeedEncoding.set(' ');
				
				// sub-delims continued
				dontNeedEncoding.set('*');
				dontNeedEncoding.set('/'); // to allow direct passing of URL in query
				dontNeedEncoding.set(',');
				// "'" doesn't need encoding, but it will make it easier to use in in JavaScript  
				// "(" and ")" don't need encoding, but we'll be conservative

				dontNeedEncoding.set(':'); // allowed and used in wicket interface
				dontNeedEncoding.set('@');

				/*
				 * the below encoding of a ? is disabled because it interferes in portlet
				 * environments. as far as i can tell it will not interfere with the ability to pass
				 * around urls in the query string. however, should it cause problems we can
				 * re-enable it as portlet environments are not high priority. we can also add a
				 * switch somewhere to enable/disable this on applicaiton level. (WICKET-4019)
				 */
				// dontNeedEncoding.set('?'); // to allow direct passing of URL in query
				break;

			case PATH :
				// this added to deal with encoding a PATH segment
				
				// sub-delims continued
				dontNeedEncoding.set('*');
				dontNeedEncoding.set('&');
				dontNeedEncoding.set('+');
				// "'" doesn't need encoding, but it will make it easier to use in in JavaScript  
				// "(" and ")" don't need encoding, but we'll be conservative
				dontNeedEncoding.set(',');
				dontNeedEncoding.set(';'); // semicolon is used in ;jsessionid=
				dontNeedEncoding.set('=');
				
				dontNeedEncoding.set(':'); // allowed and used in wicket interface
				dontNeedEncoding.set('@');

				break;
				
			// this added to deal with encoding a PATH component
			case HEADER :
				// this added to deal with encoding of header
				
				// ' ' is encoded

				// sub-delims continued
				dontNeedEncoding.set('#');
				dontNeedEncoding.set('&');
				dontNeedEncoding.set('+');
				
				dontNeedEncoding.set('^');
				dontNeedEncoding.set('`');
				dontNeedEncoding.set('|');
				break;
		}
	}

	/**
	 * @param s
	 *            string to encode
	 * @param charsetName
	 *            charset to use for encoding
	 * @return encoded string
	 * @see java.net.URLEncoder#encode(String, String)
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
	 * @see java.net.URLEncoder#encode(String, String)
	 */
	public String encode(final String unsafeInput, final Charset charset)
	{
		final String s = unsafeInput.replace("\0", "NULL");
		StringBuilder out = new StringBuilder(s.length());
		CharArrayWriter charArrayWriter = new CharArrayWriter();

		Args.notNull(charset, "charset");

		for (int i = 0; i < s.length();)
		{
			int c = s.charAt(i);

			// System.out.println("Examining character: " + c);
			if (dontNeedEncoding.get(c))
			{
				if (c == ' ')
				{
					c = '+';
				}
				// System.out.println("Storing: " + c);
				out.append((char)c);
				i++;
			}
			else
			{
				// convert to external encoding before hex conversion
				do
				{
					charArrayWriter.write(c);
					/*
					 * If this character represents the start of a Unicode surrogate pair, then pass
					 * in two characters. It's not clear what should be done if a bytes reserved in
					 * the surrogate pairs range occurs outside of a legal surrogate pair. For now,
					 * just treat it as if it were any other character.
					 */
					if ((c >= 0xD800) && (c <= 0xDBFF))
					{
						/*
						 * System.out.println(Integer.toHexString(c) + " is high surrogate");
						 */
						if ((i + 1) < s.length())
						{
							int d = s.charAt(i + 1);
							/*
							 * System.out.println("\tExamining " + Integer.toHexString(d));
							 */
							if ((d >= 0xDC00) && (d <= 0xDFFF))
							{
								/*
								 * System.out.println("\t" + Integer.toHexString(d) + " is low
								 * surrogate");
								 */
								charArrayWriter.write(d);
								i++;
							}
						}
					}
					i++;
				}
				while ((i < s.length()) && !dontNeedEncoding.get((c = s.charAt(i))));

				charArrayWriter.flush();
				String str = new String(charArrayWriter.toCharArray());
				byte[] ba = str.getBytes(charset);
				for (byte b : ba)
				{
					out.append('%');
					char ch = Character.forDigit((b >> 4) & 0xF, 16);
					// converting to use uppercase letter as part of
					// the hex value if ch is a letter.
					if (Character.isLetter(ch))
					{
						ch -= caseDiff;
					}
					out.append(ch);
					ch = Character.forDigit(b & 0xF, 16);
					if (Character.isLetter(ch))
					{
						ch -= caseDiff;
					}
					out.append(ch);
				}
				charArrayWriter.reset();
			}
		}

		return out.toString();
	}
}
