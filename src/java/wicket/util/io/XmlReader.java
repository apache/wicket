/*
 * $Id$
 * $Revision$ $Date$
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.util.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import wicket.util.string.AppendingStringBuffer;

/**
 * This is a simple XmlReader. Its only purpose is to read the xml decl string
 * from the input and apply proper character encoding to all subsequent
 * characters. The xml decl string itself is removed from the output.
 * 
 * @author Juergen Donnerstag
 */
public final class XmlReader extends Reader
{
	/** Regex to find <?xml encoding ... ?> */
	private static final Pattern encodingPattern = Pattern.compile(
			"[\\s\\n\\r]*<\\?xml\\s+(.*\\s)?encoding\\s*=\\s*([\"\'](.*?)[\"\']|(\\S]*)).*\\?>");
	
	/** Null, if JVM default. Else from <?xml encoding=""> */
	private String encoding;

	/** Null or if found in the markup, the whole <?xml ...?> string */
	private String xmlDeclarationString;

	/** The input stream to read the data from */
	private InputStream inputStream;

	/** The reader which does the character encoding */
	private Reader reader;

	/**
	 * Construct.
	 * 
	 * @param inputStream The InputStream to read the xml data from
	 * @param defaultEncoding Apply 'null' for JVM default
	 * @throws IOException In case something went wrong while reading the data
	 */
	public XmlReader(final InputStream inputStream, final String defaultEncoding)
			throws IOException
	{
		// The xml parser does not have a parent filter
		super();

		this.inputStream = inputStream;
		this.encoding = defaultEncoding;

		if (inputStream == null)
		{
			throw new IllegalArgumentException("Parameter 'inputStream' must not be null");
		}

		init();
	}

	/**
	 * Return the encoding used while reading the markup file.
	 * 
	 * @return if null, than JVM default
	 */
	public String getEncoding()
	{
		return encoding;
	}

	/**
	 * Return the XML declaration string, in case if found in the markup.
	 * 
	 * @return Null, if not found.
	 */
	public String getXmlDeclaration()
	{
		return this.xmlDeclarationString;
	}

	/**
	 * Reads and parses markup from a resource such as file.
	 * 
	 * @throws IOException
	 */
	public void init() throws IOException
	{
		if (!this.inputStream.markSupported())
		{
			throw new IOException("The InputStream must support mark/reset");
		}

		// read ahead buffer required for the first line of the markup
		// (encoding)
		final int readAheadSize = 80;
		this.inputStream.mark(readAheadSize);

		// read-ahead the input stream if it starts with <?xml
		// encoding=".."?>. If yes, set this.encoding.
		// If no, return the whole line. determineEncoding will read-ahead
		// at max the very first line of the markup.
		final String encoding = determineEncoding(this.inputStream, readAheadSize);
		if (encoding != null)
		{
			this.encoding = encoding;
		}

		// Depending on the encoding determined from the markup-file, read
		// the rest either with specific encoding or JVM default
		if (this.encoding == null)
		{
			// Use JVM default
			this.inputStream.reset();
			this.reader = new BufferedReader(new InputStreamReader(this.inputStream));
		}
		else
		{
			// Use the encoding as specified in <?xml encoding=".." ?>
			// Don't re-read <?xml ..> again.
			// Ignore ALL characters preceding <?xml>
			this.reader = new BufferedReader(new InputStreamReader(this.inputStream, this.encoding));
		}
	}

	/**
	 * Read-ahead the input stream (markup file). If it starts with &lt;?xml
	 * encoding=".." ?&gt;, than set this.encoding and return null. If not,
	 * return all characters read so far. determineEncoding will read-ahead at
	 * max. the very first line of the markup.
	 * 
	 * @param in
	 *            The markup file
	 * @param readAheadSize
	 *            The read ahead buffer available to read the xml encoding
	 *            information
	 * @return Null, if &lt;?xml ..?&gt; has been found; else all characters
	 *         read ahead
	 * @throws IOException
	 */
	private final String determineEncoding(final InputStream in, final int readAheadSize)
			throws IOException
	{
		// Max one line
		final AppendingStringBuffer pushBack = new AppendingStringBuffer(readAheadSize);

		// The current char from the markup file
		int value;
		while ((value = in.read()) != -1)
		{
			pushBack.append((char)value);

			// Stop at the end of the first tag or end of line. If it is HTML
			// without newlines, stop after X bytes (= characters)
			if ((value == '>') || (value == '\n') || (value == '\r')
					|| (pushBack.length() >= (readAheadSize - 1)))
			{
				break;
			}
		}

		// Does the string match the <?xml .. ?> pattern
		final Matcher matcher = encodingPattern.matcher(pushBack);
		if (!matcher.matches())
		{
			// No
			return null;
		}

		// Save the whole <?xml ..> string for later
		this.xmlDeclarationString = pushBack.toString().trim();

		// Extract the encoding
		String encoding = matcher.group(3);
		if ((encoding == null) || (encoding.length() == 0))
		{
			encoding = matcher.group(4);
		}

		return encoding;
	}

	/**
	 * @see java.io.Reader#close()
	 */
	public void close() throws IOException
	{
		this.reader.close();
		this.inputStream.close();
	}

	/**
	 * @see java.io.Reader#read(char[], int, int)
	 */
	public int read(char[] buf, int from, int to) throws IOException
	{
		return this.reader.read(buf, from, to);
	}

	/**
	 * @return The markup to be parsed
	 */
	public String toString()
	{
		return this.inputStream.toString() + " (" + this.encoding + ")";
	}
}
