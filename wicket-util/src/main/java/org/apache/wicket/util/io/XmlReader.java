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
package org.apache.wicket.util.io;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.string.Strings;


/**
 * This is a simple XmlReader. Its only purpose is to determine the character encoding of the
 * input and apply it to all subsequent characters.
 * <p>
 * The encoding is taken from the first of these that is present:
 * <ol>
 * <li>the xml decl, e.g. {@code <?xml version="1.0" encoding="UTF-8"?>}, which has to be the
 * first line of the input. The xml decl string itself is removed from the output.</li>
 * <li>the first {@code <meta charset="UTF-8">} or
 * {@code <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">} within the first
 * 1024 bytes of the input, outside of comments, naming a charset the JVM supports. The meta tag
 * stays in the output.</li>
 * <li>the default encoding passed to the constructor.</li>
 * </ol>
 *
 * @author Juergen Donnerstag
 */
public final class XmlReader extends Reader
{
	/** Regex to find <?xml encoding ... ?> */
	private static final Pattern xmlDecl = Pattern.compile("[\\s\\n\\r]*<\\?xml(\\s+.*)?\\?>");

	/** Regex to find <?xml encoding ... ?> */
	private static final Pattern encodingPattern = Pattern.compile("\\s+encoding\\s*=\\s*([\"\'](.*?)[\"\']|(\\S*)).*\\?>");

	/** The number of bytes browsers prescan for a meta charset, see the HTML Living Standard */
	private static final int META_PRESCAN_LENGTH = 1024;

	/** Regex to find the charset in the content of <meta http-equiv="Content-Type"> */
	private static final Pattern contentCharsetPattern = Pattern.compile(
		"charset\\s*=\\s*[\"']?([^\\s;\"']+)", Pattern.CASE_INSENSITIVE);

	/** Null, if JVM default. Else from <?xml encoding="">, a meta charset or the default */
	private String encoding;

	/** The input stream to read the data from */
	private final InputStream inputStream;

	/** The reader which does the character encoding */
	private Reader reader;

	/**
	 * Construct.
	 * 
	 * @param inputStream
	 *            The InputStream to read the xml data from
	 * @param defaultEncoding
	 *            Default character encoding to use when not specified in XML declaration, specify
	 *            null to use JVM default
	 * @throws IOException
	 *             In case something went wrong while reading the data
	 */
	public XmlReader(final InputStream inputStream, final String defaultEncoding)
		throws IOException
	{
		Args.notNull(inputStream, "inputStream");

		if (!inputStream.markSupported())
		{
			this.inputStream = new BufferedInputStream(new BOMInputStream(inputStream));
		}
		else
		{
			this.inputStream = new BOMInputStream(inputStream);
		}
		encoding = defaultEncoding;

		init();
	}

	/**
	 * Return the encoding used while reading the markup file.
	 * 
	 * @return if null, then JVM default
	 */
	public final String getEncoding()
	{
		return encoding;
	}

	/**
	 * Reads and parses markup from a resource such as file.
	 * 
	 * @throws IOException
	 */
	public void init() throws IOException
	{
		// read ahead buffer required for the first line of the markup (encoding)
		final int readAheadSize = 80;
		inputStream.mark(META_PRESCAN_LENGTH);

		// read-ahead the input stream and check if it starts with <?xml..?>.
		String xmlDeclaration = getXmlDeclaration(inputStream, readAheadSize);
		if (!Strings.isEmpty(xmlDeclaration))
		{
			// If yes than determine the encoding from the xml decl
			encoding = determineEncoding(xmlDeclaration);
		}
		else
		{
			inputStream.reset();
			String metaCharset = findMetaCharset(readPrescan(inputStream));
			inputStream.reset();
			if (metaCharset != null)
			{
				encoding = metaCharset;
			}
		}

		if (encoding == null)
		{
			// Use JVM default
			reader = new InputStreamReader(inputStream);
		}
		else
		{
			// Use the encoding provided
			reader = new InputStreamReader(inputStream, encoding);
		}
	}

	/**
	 * Determine the encoding from the xml decl.
	 * 
	 * @param string
	 *            The xmlDecl string
	 * @return The encoding. Null, if not found
	 */
	private String determineEncoding(final CharSequence string)
	{
		// Does the string match the <?xml .. ?> pattern
		final Matcher matcher = encodingPattern.matcher(string);
		if (!matcher.find())
		{
			// No
			return null;
		}

		// Extract the encoding
		String encoding = matcher.group(2);
		if ((encoding == null) || (encoding.length() == 0))
		{
			encoding = matcher.group(3);
		}

		if (encoding != null)
		{
			encoding = encoding.trim();
		}

		return encoding;
	}

	/**
	 * Read-ahead the input stream (markup file). If the first line contains &lt;?xml...?&gt;, than
	 * remember the xml decl for later to determine the encoding.
	 * <p>
	 * The xml decl will not be forwarded to the user.
	 * 
	 * @param in
	 *            The markup file
	 * @param readAheadSize
	 *            The read ahead buffer available to read the xml encoding information
	 * @return true, if &lt;?xml ..?&gt; has been found
	 * @throws IOException
	 */
	private String getXmlDeclaration(final InputStream in, final int readAheadSize)
		throws IOException
	{
		// Max one line
		final StringBuilder pushBack = new StringBuilder(readAheadSize);

		// The current char from the markup file
		int value;
		while ((value = in.read()) != -1)
		{
			pushBack.append((char)value);

			// Stop at the end of the first tag or end of line. If it is HTML
			// without newlines, stop after X bytes (= characters)
			if ((value == '>') || (value == '\n') || (value == '\r') ||
				(pushBack.length() >= (readAheadSize - 1)))
			{
				break;
			}
		}

		// Does the string match the <?xml .. ?> pattern
		final Matcher matcher = xmlDecl.matcher(pushBack);
		if (!matcher.matches())
		{
			// No
			return null;
		}

		// Save the whole <?xml ..> string for later
		return pushBack.toString().trim();
	}

	private static String readPrescan(final InputStream in) throws IOException
	{
		byte[] buffer = new byte[META_PRESCAN_LENGTH];
		int length = 0;
		int read;
		while (length < buffer.length &&
			(read = in.read(buffer, length, buffer.length - length)) != -1)
		{
			length += read;
		}
		// one char per byte keeps the ASCII tags readable whatever the real encoding is
		return new String(buffer, 0, length, StandardCharsets.ISO_8859_1);
	}

	/**
	 * Find the charset declared by the first {@code <meta>} tag that names a supported one.
	 * Comments are skipped, and a tag cut off by the end of the prescan is ignored.
	 *
	 * @param markup
	 *            The start of the markup
	 * @return The charset name as written in the markup. Null, if not found
	 */
	private static String findMetaCharset(final String markup)
	{
		int pos = 0;
		while ((pos = markup.indexOf('<', pos)) != -1)
		{
			if (markup.startsWith("<!--", pos))
			{
				int end = markup.indexOf("-->", pos + 4);
				if (end == -1)
				{
					return null;
				}
				pos = end + 3;
			}
			else if (pos + 1 < markup.length() && Character.isLetter(markup.charAt(pos + 1)))
			{
				int nameEnd = pos + 1;
				while (nameEnd < markup.length() && isTagNameChar(markup.charAt(nameEnd)))
				{
					nameEnd++;
				}
				String tagName = markup.substring(pos + 1, nameEnd);

				Map<String, String> attributes = new HashMap<>();
				pos = readAttributes(markup, nameEnd, attributes);
				if (pos == -1)
				{
					return null;
				}

				if ("meta".equalsIgnoreCase(tagName))
				{
					String charset = getCharset(attributes);
					if (charset != null)
					{
						return charset;
					}
				}
			}
			else
			{
				pos++;
			}
		}
		return null;
	}

	private static boolean isTagNameChar(final char c)
	{
		return !Character.isWhitespace(c) && c != '>' && c != '/';
	}

	/**
	 * Read the attributes of a tag into the map, keeping the first value of a repeated name.
	 *
	 * @param markup
	 *            The markup
	 * @param pos
	 *            The position right after the tag name
	 * @param attributes
	 *            The map to fill, keyed by lower case attribute name
	 * @return The position after the closing {@code >}, or -1 if the tag is not closed
	 */
	private static int readAttributes(final String markup, int pos,
		final Map<String, String> attributes)
	{
		final int length = markup.length();
		while (true)
		{
			while (pos < length &&
				(Character.isWhitespace(markup.charAt(pos)) || markup.charAt(pos) == '/'))
			{
				pos++;
			}
			if (pos >= length)
			{
				return -1;
			}
			if (markup.charAt(pos) == '>')
			{
				return pos + 1;
			}

			int nameStart = pos;
			while (pos < length && isAttributeNameChar(markup.charAt(pos)))
			{
				pos++;
			}
			if (pos == nameStart)
			{
				pos++;
				continue;
			}
			String name = markup.substring(nameStart, pos).toLowerCase(Locale.ROOT);

			while (pos < length && Character.isWhitespace(markup.charAt(pos)))
			{
				pos++;
			}
			String value = "";
			if (pos < length && markup.charAt(pos) == '=')
			{
				pos++;
				while (pos < length && Character.isWhitespace(markup.charAt(pos)))
				{
					pos++;
				}
				if (pos >= length)
				{
					return -1;
				}
				char quote = markup.charAt(pos);
				if (quote == '"' || quote == '\'')
				{
					int end = markup.indexOf(quote, pos + 1);
					if (end == -1)
					{
						return -1;
					}
					value = markup.substring(pos + 1, end);
					pos = end + 1;
				}
				else
				{
					int valueStart = pos;
					while (pos < length && !Character.isWhitespace(markup.charAt(pos)) &&
						markup.charAt(pos) != '>')
					{
						pos++;
					}
					value = markup.substring(valueStart, pos);
				}
			}
			attributes.putIfAbsent(name, value);
		}
	}

	private static boolean isAttributeNameChar(final char c)
	{
		return !Character.isWhitespace(c) && c != '=' && c != '>' && c != '/';
	}

	/**
	 * @param attributes
	 *            The attributes of a meta tag
	 * @return The supported charset it declares, or null
	 */
	private static String getCharset(final Map<String, String> attributes)
	{
		String charset = attributes.get("charset");
		if (charset == null && "content-type".equalsIgnoreCase(attributes.get("http-equiv")))
		{
			String content = attributes.get("content");
			if (content != null)
			{
				Matcher matcher = contentCharsetPattern.matcher(content);
				if (matcher.find())
				{
					charset = matcher.group(1);
				}
			}
		}
		if (charset == null)
		{
			return null;
		}
		charset = charset.trim();
		return isSupported(charset) ? charset : null;
	}

	private static boolean isSupported(final String charset)
	{
		try
		{
			return Charset.isSupported(charset);
		}
		catch (IllegalCharsetNameException e)
		{
			return false;
		}
	}

	/**
	 * @see java.io.Reader#close()
	 */
	@Override
	public void close() throws IOException
	{
		try
		{
			reader.close();
		}
		finally
		{
			inputStream.close();
		}
	}

	/**
	 * @see java.io.Reader#read(char[], int, int)
	 */
	@Override
	public int read(final char[] buf, final int from, final int to) throws IOException
	{
		return reader.read(buf, from, to);
	}

	/**
	 * @return The markup to be parsed
	 */
	@Override
	public String toString()
	{
		return inputStream.toString() + " (" + encoding + ")";
	}
}
