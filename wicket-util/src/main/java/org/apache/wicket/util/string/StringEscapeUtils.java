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
package org.apache.wicket.util.string;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Locale;

/**
 * <p>
 * Escapes and unescapes <code>String</code>s for Java, Java Script, HTML, XML, and SQL.
 * </p>
 * 
 * <p>
 * #ThreadSafe#
 * </p>
 * 
 * @author Apache Software Foundation
 * @author Apache Jakarta Turbine
 * @author Purple Technology
 * @author <a href="mailto:alex@purpletech.com">Alexander Day Chaffee</a>
 * @author Antony Riley
 * @author Helge Tesgaard
 * @author <a href="sean@boohai.com">Sean Brown</a>
 * @author <a href="mailto:ggregory@seagullsw.com">Gary Gregory</a>
 * @author Phil Steitz
 * @author Pete Gieser
 * @since 2.0
 * @version $Id$
 */
// Copy from commons-lang ver. 2.6. Non-html/xml methods were removed
class StringEscapeUtils
{

	/**
	 * <p>
	 * <code>StringEscapeUtils</code> instances should NOT be constructed in standard programming.
	 * </p>
	 * 
	 * <p>
	 * Instead, the class should be used as:
	 * 
	 * <pre>
	 * StringEscapeUtils.escapeJava(&quot;foo&quot;);
	 * </pre>
	 * 
	 * </p>
	 * 
	 * <p>
	 * This constructor is public to permit tools that require a JavaBean instance to operate.
	 * </p>
	 */
	public StringEscapeUtils()
	{
		super();
	}

	/**
	 * <p>
	 * Returns an upper case hexadecimal <code>String</code> for the given character.
	 * </p>
	 * 
	 * @param ch
	 *            The character to convert.
	 * @return An upper case hexadecimal <code>String</code>
	 */
	private static String hex(char ch)
	{
		return Integer.toHexString(ch).toUpperCase(Locale.ROOT);
	}

	// HTML and XML
	// --------------------------------------------------------------------------
	/**
	 * <p>
	 * Escapes the characters in a <code>String</code> using HTML entities.
	 * </p>
	 * 
	 * <p>
	 * For example:
	 * </p>
	 * <p>
	 * <code>"bread" & "butter"</code>
	 * </p>
	 * becomes:
	 * <p>
	 * <code>&amp;quot;bread&amp;quot; &amp;amp; &amp;quot;butter&amp;quot;</code>.
	 * </p>
	 * 
	 * <p>
	 * Supports all known HTML 4.0 entities, including funky accents. Note that the commonly used
	 * apostrophe escape character (&amp;apos;) is not a legal entity and so is not supported).
	 * </p>
	 * 
	 * @param str
	 *            the <code>String</code> to escape, may be null
	 * @return a new escaped <code>String</code>, <code>null</code> if null string input
	 * 
	 * @see #unescapeHtml(String)
	 * @see <a href="http://hotwired.lycos.com/webmonkey/reference/special_characters/">ISO
	 *      Entities</a>
	 * @see <a href="http://www.w3.org/TR/REC-html32#latin1">HTML 3.2 Character Entities for ISO
	 *      Latin-1</a>
	 * @see <a href="http://www.w3.org/TR/REC-html40/sgml/entities.html">HTML 4.0 Character entity
	 *      references</a>
	 * @see <a href="http://www.w3.org/TR/html401/charset.html#h-5.3">HTML 4.01 Character
	 *      References</a>
	 * @see <a href="http://www.w3.org/TR/html401/charset.html#code-position">HTML 4.01 Code
	 *      positions</a>
	 */
	public static String escapeHtml(String str)
	{
		if (str == null)
		{
			return null;
		}
		try
		{
			StringWriter writer = new StringWriter((int)(str.length() * 1.5));
			escapeHtml(writer, str);
			return writer.toString();
		}
		catch (IOException ioe)
		{
			// should be impossible
			throw new RuntimeException(ioe);
		}
	}

	/**
	 * <p>
	 * Escapes the characters in a <code>String</code> using HTML entities and writes them to a
	 * <code>Writer</code>.
	 * </p>
	 * 
	 * <p>
	 * For example:
	 * </p>
	 * <code>"bread" & "butter"</code>
	 * <p>
	 * becomes:
	 * </p>
	 * <code>&amp;quot;bread&amp;quot; &amp;amp; &amp;quot;butter&amp;quot;</code>.
	 * 
	 * <p>
	 * Supports all known HTML 4.0 entities, including funky accents. Note that the commonly used
	 * apostrophe escape character (&amp;apos;) is not a legal entity and so is not supported).
	 * </p>
	 * 
	 * @param writer
	 *            the writer receiving the escaped string, not null
	 * @param string
	 *            the <code>String</code> to escape, may be null
	 * @throws IllegalArgumentException
	 *             if the writer is null
	 * @throws IOException
	 *             when <code>Writer</code> passed throws the exception from calls to the
	 *             {@link Writer#write(int)} methods.
	 * 
	 * @see #escapeHtml(String)
	 * @see #unescapeHtml(String)
	 * @see <a href="http://hotwired.lycos.com/webmonkey/reference/special_characters/">ISO
	 *      Entities</a>
	 * @see <a href="http://www.w3.org/TR/REC-html32#latin1">HTML 3.2 Character Entities for ISO
	 *      Latin-1</a>
	 * @see <a href="http://www.w3.org/TR/REC-html40/sgml/entities.html">HTML 4.0 Character entity
	 *      references</a>
	 * @see <a href="http://www.w3.org/TR/html401/charset.html#h-5.3">HTML 4.01 Character
	 *      References</a>
	 * @see <a href="http://www.w3.org/TR/html401/charset.html#code-position">HTML 4.01 Code
	 *      positions</a>
	 */
	public static void escapeHtml(Writer writer, String string) throws IOException
	{
		if (writer == null)
		{
			throw new IllegalArgumentException("The Writer must not be null.");
		}
		if (string == null)
		{
			return;
		}
		Entities.HTML40.escape(writer, string);
	}

	// -----------------------------------------------------------------------
	/**
	 * <p>
	 * Unescapes a string containing entity escapes to a string containing the actual Unicode
	 * characters corresponding to the escapes. Supports HTML 4.0 entities.
	 * </p>
	 * 
	 * <p>
	 * For example, the string "&amp;lt;Fran&amp;ccedil;ais&amp;gt;" will become
	 * "&lt;Fran&ccedil;ais&gt;"
	 * </p>
	 * 
	 * <p>
	 * If an entity is unrecognized, it is left alone, and inserted verbatim into the result string.
	 * e.g. "&amp;gt;&amp;zzzz;x" will become "&gt;&amp;zzzz;x".
	 * </p>
	 * 
	 * @param str
	 *            the <code>String</code> to unescape, may be null
	 * @return a new unescaped <code>String</code>, <code>null</code> if null string input
	 * @see #escapeHtml(Writer, String)
	 */
	public static String unescapeHtml(String str)
	{
		if (str == null)
		{
			return null;
		}
		try
		{
			StringWriter writer = new StringWriter((int)(str.length() * 1.5));
			unescapeHtml(writer, str);
			return writer.toString();
		}
		catch (IOException ioe)
		{
			// should be impossible
			throw new RuntimeException(ioe);
		}
	}

	/**
	 * <p>
	 * Unescapes a string containing entity escapes to a string containing the actual Unicode
	 * characters corresponding to the escapes. Supports HTML 4.0 entities.
	 * </p>
	 * 
	 * <p>
	 * For example, the string "&amp;lt;Fran&amp;ccedil;ais&amp;gt;" will become
	 * "&lt;Fran&ccedil;ais&gt;"
	 * </p>
	 * 
	 * <p>
	 * If an entity is unrecognized, it is left alone, and inserted verbatim into the result string.
	 * e.g. "&amp;gt;&amp;zzzz;x" will become "&gt;&amp;zzzz;x".
	 * </p>
	 * 
	 * @param writer
	 *            the writer receiving the unescaped string, not null
	 * @param string
	 *            the <code>String</code> to unescape, may be null
	 * @throws IllegalArgumentException
	 *             if the writer is null
	 * @throws IOException
	 *             if an IOException occurs
	 * @see #escapeHtml(String)
	 */
	public static void unescapeHtml(Writer writer, String string) throws IOException
	{
		if (writer == null)
		{
			throw new IllegalArgumentException("The Writer must not be null.");
		}
		if (string == null)
		{
			return;
		}
		Entities.HTML40.unescape(writer, string);
	}

	// -----------------------------------------------------------------------
	/**
	 * <p>
	 * Escapes the characters in a <code>String</code> using XML entities.
	 * </p>
	 * 
	 * <p>
	 * For example: <tt>"bread" & "butter"</tt> =>
	 * <tt>&amp;quot;bread&amp;quot; &amp;amp; &amp;quot;butter&amp;quot;</tt>.
	 * </p>
	 * 
	 * <p>
	 * Supports only the five basic XML entities (gt, lt, quot, amp, apos). Does not support DTDs or
	 * external entities.
	 * </p>
	 * 
	 * <p>
	 * Note that unicode characters greater than 0x7f are currently escaped to their numerical \\u
	 * equivalent. This may change in future releases.
	 * </p>
	 * 
	 * @param writer
	 *            the writer receiving the unescaped string, not null
	 * @param str
	 *            the <code>String</code> to escape, may be null
	 * @throws IllegalArgumentException
	 *             if the writer is null
	 * @throws IOException
	 *             if there is a problem writing
	 * @see #unescapeXml(java.lang.String)
	 */
	public static void escapeXml(Writer writer, String str) throws IOException
	{
		if (writer == null)
		{
			throw new IllegalArgumentException("The Writer must not be null.");
		}
		if (str == null)
		{
			return;
		}
		Entities.XML.escape(writer, str);
	}

	/**
	 * <p>
	 * Escapes the characters in a <code>String</code> using XML entities.
	 * </p>
	 * 
	 * <p>
	 * For example: <tt>"bread" & "butter"</tt> =>
	 * <tt>&amp;quot;bread&amp;quot; &amp;amp; &amp;quot;butter&amp;quot;</tt>.
	 * </p>
	 * 
	 * <p>
	 * Supports only the five basic XML entities (gt, lt, quot, amp, apos). Does not support DTDs or
	 * external entities.
	 * </p>
	 * 
	 * <p>
	 * Note that unicode characters greater than 0x7f are currently escaped to their numerical \\u
	 * equivalent. This may change in future releases.
	 * </p>
	 * 
	 * @param str
	 *            the <code>String</code> to escape, may be null
	 * @return a new escaped <code>String</code>, <code>null</code> if null string input
	 * @see #unescapeXml(java.lang.String)
	 */
	public static String escapeXml(String str)
	{
		if (str == null)
		{
			return null;
		}
		return Entities.XML.escape(str);
	}

	// -----------------------------------------------------------------------
	/**
	 * <p>
	 * Unescapes a string containing XML entity escapes to a string containing the actual Unicode
	 * characters corresponding to the escapes.
	 * </p>
	 * 
	 * <p>
	 * Supports only the five basic XML entities (gt, lt, quot, amp, apos). Does not support DTDs or
	 * external entities.
	 * </p>
	 * 
	 * <p>
	 * Note that numerical \\u unicode codes are unescaped to their respective unicode characters.
	 * This may change in future releases.
	 * </p>
	 * 
	 * @param writer
	 *            the writer receiving the unescaped string, not null
	 * @param str
	 *            the <code>String</code> to unescape, may be null
	 * @throws IllegalArgumentException
	 *             if the writer is null
	 * @throws IOException
	 *             if there is a problem writing
	 * @see #escapeXml(String)
	 */
	public static void unescapeXml(Writer writer, String str) throws IOException
	{
		if (writer == null)
		{
			throw new IllegalArgumentException("The Writer must not be null.");
		}
		if (str == null)
		{
			return;
		}
		Entities.XML.unescape(writer, str);
	}

	/**
	 * <p>
	 * Unescapes a string containing XML entity escapes to a string containing the actual Unicode
	 * characters corresponding to the escapes.
	 * </p>
	 * 
	 * <p>
	 * Supports only the five basic XML entities (gt, lt, quot, amp, apos). Does not support DTDs or
	 * external entities.
	 * </p>
	 * 
	 * <p>
	 * Note that numerical \\u unicode codes are unescaped to their respective unicode characters.
	 * This may change in future releases.
	 * </p>
	 * 
	 * @param str
	 *            the <code>String</code> to unescape, may be null
	 * @return a new unescaped <code>String</code>, <code>null</code> if null string input
	 * @see #escapeXml(String)
	 */
	public static String unescapeXml(String str)
	{
		if (str == null)
		{
			return null;
		}
		return Entities.XML.unescape(str);
	}
}
