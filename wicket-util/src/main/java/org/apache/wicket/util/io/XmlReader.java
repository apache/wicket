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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.string.Strings;


/**
 * This is a simple XmlReader. Its only purpose is to read the xml decl string from the input and
 * apply proper character encoding to all subsequent characters. The xml decl string itself is
 * removed from the output.
 * 
 * @author Juergen Donnerstag
 */
public final class XmlReader extends Reader
{
	/** Regex to find <?xml encoding ... ?> */
	private static final Pattern xmlDecl = Pattern.compile("[\\s\\n\\r]*<\\?xml(\\s+.*)?\\?>");

	/** Regex to find <?xml encoding ... ?> */
	private static final Pattern encodingPattern = Pattern.compile("\\s+encoding\\s*=\\s*([\"\'](.*?)[\"\']|(\\S*)).*\\?>");

	/** Null, if JVM default. Else from <?xml encoding=""> */
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
		inputStream.mark(readAheadSize);

		// read-ahead the input stream and check if it starts with <?xml..?>.
		String xmlDeclaration = getXmlDeclaration(inputStream, readAheadSize);
		if (!Strings.isEmpty(xmlDeclaration))
		{
			// If yes than determine the encoding from the xml decl
			encoding = determineEncoding(xmlDeclaration);
		}
		else
		{
			// If not, reset the input stream to the beginning of the file
			inputStream.reset();
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
	private final String determineEncoding(final CharSequence string)
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
	private final String getXmlDeclaration(final InputStream in, final int readAheadSize)
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
