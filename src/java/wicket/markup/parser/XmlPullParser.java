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
package wicket.markup.parser;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.markup.MarkupElement;
import wicket.util.io.Streams;
import wicket.util.parse.metapattern.parsers.TagNameParser;
import wicket.util.parse.metapattern.parsers.VariableAssignmentParser;
import wicket.util.resource.IResourceStream;
import wicket.util.resource.ResourceStreamNotFoundException;
import wicket.util.resource.StringResourceStream;

/**
 * A fairly shallow markup pull or streaming parser. Parses a markup string of a
 * given type of markup (for example, html, xml, vxml or wml) into Tag and
 * RawMarkup tokens. IMarkupFilters may be used to handle markup specifics like
 * identifying Wicket components or HTML which is not 100% xml compliant.
 *
 * @author Jonathan Locke
 */
public final class XmlPullParser implements IXmlPullParser
{
	/** Regex to find <?xml encoding ... ?> */
	private static final Pattern encodingPattern = Pattern
			.compile("[\\s\\n\\r]*<\\?xml\\s+(.*\\s)?encoding\\s*=\\s*([\"\'](.*?)[\"\']|(\\S]*)).*\\?>");

	/** Logging */
	private static final Log log = LogFactory.getLog(XmlPullParser.class);

	/** current column number. */
	private int columnNumber = 1;

	/** Null, if JVM default. Else from <?xml encoding=""> */
	private String encoding;

	/** Null or if found in the markup, the whole <?xml ...?> string */
	private String xmlDeclarationString;
	
	/** Input to parse. */
	private String input;

	/** Position in parse. */
	private int inputPosition;

	/** Last place we counted lines from. */
	private int lastLineCountIndex;

	/** Current line and column numbers during parse. */
	private int lineNumber = 1;

	private int positionMarker;

	/** True to strip out HTML comments. */
	private boolean stripComments;

	/**
	 * Construct.
	 */
	public XmlPullParser()
	{
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
	 * Return the XML declaration string, in case if found in the
	 * markup.
	 * 
	 * @return Null, if not found.
	 */
	public String getXmlDeclaration()
	{
	    return this.xmlDeclarationString;
	}
	
	/**
	 * Get the character sequence from the position marker to toPos.
	 *
	 * @param toPos
	 *            index of first character not included
	 * @return Raw markup (a string) in between these two positions.
	 */
	public final CharSequence getInputFromPositionMarker(int toPos)
	{
		if (toPos < 0)
		{
			toPos = this.input.length();
		}
		return this.input.subSequence(this.positionMarker, toPos);
	}

	/**
	 * Get the character sequence in between both positions.
	 *
	 * @param fromPos
	 *            first index
	 * @param toPos
	 *            second index
	 * @return the string (raw markup) in between both positions
	 */
	public final CharSequence getInputSubsequence(final int fromPos, final int toPos)
	{
		return this.input.subSequence(fromPos, toPos);
	}

	/**
	 * As the xml parser will always be the last element on the chain, it will
	 * always return null.
	 *
	 * @return always null.
	 */
	public final IMarkupFilter getParent()
	{
		return null;
	}

	/**
	 * Gets the next tag from the input string.
	 *
	 * @return The extracted tag (will always be of type XmlTag).
	 * @throws ParseException
	 */
	public final MarkupElement nextTag() throws ParseException
	{
		// Index of open bracket
		int openBracketIndex = input.indexOf('<', this.inputPosition);

		// While we can find an open tag, parse the tag
		if (openBracketIndex != -1)
		{
			// Determine line number
			countLinesTo(input, openBracketIndex);

			// Get index of closing tag and advance past the tag
			int closeBracketIndex = input.indexOf('>', openBracketIndex);

			if (closeBracketIndex == -1)
			{
				throw new ParseException("No matching close bracket at position "
						+ openBracketIndex, this.inputPosition);
			}

			// Get the tagtext between open and close brackets
			String tagText = input.substring(openBracketIndex + 1, closeBracketIndex);

			// Handle comments
			if (tagText.startsWith("!--"))
			{
				// Skip ahead to -->
				this.inputPosition = input.indexOf("-->", openBracketIndex + 4) + 3;

				if (this.inputPosition == -1)
				{
					throw new ParseException("Unclosed comment beginning at " + openBracketIndex,
							openBracketIndex);
				}

				return nextTag();
			}
			
			// CDATA sections might contain "<" which is not part of an XML tag.
			// Make sure escaped "<" are treated right
			final String startText = (tagText.length() <= 8 ? tagText : tagText.substring(0, 8));
			if (startText.toUpperCase().equals("![CDATA["))
			{

				// Get index of closing tag and advance past the tag
				closeBracketIndex = findCloseBracket(input, '>', openBracketIndex);

				if (closeBracketIndex == -1)
				{
					throw new ParseException("No matching close bracket at position "
							+ openBracketIndex, this.inputPosition);
				}

				// Get the tagtext between open and close brackets
				tagText = input.substring(openBracketIndex + 1, closeBracketIndex);
			    
			}
			
			{
				// Type of tag
				XmlTag.Type type = XmlTag.OPEN;

				// If the tag ends in '/', it's a "simple" tag like <foo/>
				if (tagText.endsWith("/"))
				{
					type = XmlTag.OPEN_CLOSE;
					tagText = tagText.substring(0, tagText.length() - 1);
				}
				else if (tagText.startsWith("/"))
				{
					// The tag text starts with a '/', it's a simple close tag
					type = XmlTag.CLOSE;
					tagText = tagText.substring(1);
				}

				// We don't deeply parse tags like DOCTYPE that start with !
				// or XML document definitions that start with ?
				if (tagText.startsWith("!") || tagText.startsWith("?"))
				{
					// Move to position after the tag
					this.inputPosition = closeBracketIndex + 1;

					// Return next tag
					return nextTag();
				}
				else
				{
					// Parse remaining tag text, obtaining a tag object or null
					// if it's invalid
					final XmlTag tag = parseTagText(tagText);

					if (tag != null)
					{
						// Populate tag fields
						tag.type = type;
						tag.pos = openBracketIndex;
						tag.length = (closeBracketIndex + 1) - openBracketIndex;
						tag.text = input.substring(openBracketIndex, closeBracketIndex + 1);
						tag.lineNumber = lineNumber;
						tag.columnNumber = columnNumber;

						// Move to position after the tag
						this.inputPosition = closeBracketIndex + 1;

						// Return the tag we found!
						return tag;
					}
					else
					{
						throw new ParseException("Malformed tag (line " + lineNumber + ", column "
								+ columnNumber + ")", openBracketIndex);
					}
				}
			}
		}

		// There is no next matching tag
		return null;
	}

	/**
	 * Find the char but ignore any text within ".." and '..'
	 * 
	 * @param input The markup string
	 * @param ch The character to search
	 * @param startIndex Start index
	 * @return -1 if not found, else the index
	 */
	private int findCloseBracket(final String input, final char ch, int startIndex)
	{
	    char quote = 0;
	    
	    for(; startIndex < input.length(); startIndex++)
	    {
	        char charAt = input.charAt(startIndex);
	        if (quote != 0)
	        {
	            if (quote == charAt)
	            {
	                quote = 0;
	            }
	        }
	        else if ((charAt == '"') || (charAt == '\''))
	        {
	            quote = charAt;
	        }
	        else if (charAt == ch)
	        {
	            return startIndex;
	        }
	    }
		
	    return -1;
	}

	/**
	 * Parse the given string.
	 * <p>
	 * Note: xml character encoding is NOT applied. It is assumed the input
	 * provided does have the correct encoding already.
	 *
	 * @param string
	 *            The input string
	 * @throws IOException
	 *             Error while reading the resource
	 * @throws ResourceStreamNotFoundException
	 *             Resource not found
	 */
	public void parse(final CharSequence string) throws IOException, ResourceStreamNotFoundException
	{
		parse(new StringResourceStream(string));
	}

	/**
	 * Reads and parses markup from a resource such as file.
	 *
	 * @param resource
	 *            The resource to read and parse
	 * @throws IOException
	 * @throws ResourceStreamNotFoundException
	 */
	public void parse(final IResourceStream resource) throws IOException, ResourceStreamNotFoundException
	{
		// reset: Must come from markup
		this.encoding = null;

		try
		{
			final BufferedInputStream bin = new BufferedInputStream(resource.getInputStream(), 4000);
			if (!bin.markSupported())
			{
				throw new IOException("BufferedInputStream does not support mark/reset");
			}

			// read ahead buffer required for the first line of the markup
			// (encoding)
			final int readAheadSize = 80;
			bin.mark(readAheadSize);

			// read-ahead the input stream if it starts with <?xml
			// encoding=".."?>. If yes, set this.encoding.
			// If no, return the whole line. determineEncoding will read-ahead
			// at max. the very first line of the markup.
			this.encoding = determineEncoding(bin, readAheadSize);

			// Depending on the encoding determined from the markup-file, read
			// the rest either with specific encoding or JVM default
			final String markup;
			if (this.encoding == null)
			{
				// Use JVM default
				bin.reset();
				markup = Streams.readString(bin);
			}
			else
			{
				// Use the encoding as specified in <?xml encoding=".." ?>
				// Don't re-read <?xml ..> again
			    // Ignore ALL characters preceding <?xml>
				markup = Streams.readString(bin, encoding);
			}

			setInput(markup);
		}
		finally
		{
			resource.close();
		}
	}

	/**
	 * Remember the current position in markup
	 */
	public final void setPositionMarker()
	{
		this.positionMarker = this.inputPosition;
	}

	/**
	 * Set whether to strip components.
	 *
	 * @param stripComments
	 *            whether to strip components.
	 */
	public void setStripComments(boolean stripComments)
	{
		this.stripComments = stripComments;
	}

	/**
	 * @return The markup to be parsed
	 */
	public String toString()
	{
		return this.input;
	}

	/**
	 * Counts lines between indices.
	 *
	 * @param string
	 *            String
	 * @param end
	 *            End index
	 */
	private void countLinesTo(final String string, final int end)
	{
		for (int i = lastLineCountIndex; i < end; i++)
		{
			if (string.charAt(i) == '\n')
			{
				columnNumber = 1;
				lineNumber++;
			}
			else
			{
				columnNumber++;
			}
		}

		lastLineCountIndex = end;
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
		StringBuffer pushBack = new StringBuffer(readAheadSize);

		int value;
		while ((value = in.read()) != -1)
		{
			pushBack.append((char)value);

			// Stop at end of the first tag or end of line. If it is HTML
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
	 * Parses the text between tags. For example, "a href=foo.html".
	 *
	 * @param tagText
	 *            The text between tags
	 * @return A new Tag object or null if the tag is invalid
	 * @throws ParseException
	 */
	private XmlTag parseTagText(final String tagText) throws ParseException
	{
		// Get the length of the tagtext
		final int tagTextLength = tagText.length();

		// If we match tagname pattern
		final TagNameParser tagnameParser = new TagNameParser(tagText);

		if (tagnameParser.matcher().lookingAt())
		{
			final XmlTag tag = new XmlTag();

			// Extract the tag from the pattern matcher
			tag.name = tagnameParser.getName();
			tag.namespace = tagnameParser.getNamespace();

			// Are we at the end? Then there are no attributes, so we just
			// return the tag
			int pos = tagnameParser.matcher().end(0);
			if (pos == tagTextLength)
			{
				return tag;
			}

			// Extract attributes
			final VariableAssignmentParser attributeParser = new VariableAssignmentParser(tagText);

			while (attributeParser.matcher().find(pos))
			{
				// Get key and value using attribute pattern
				String value = attributeParser.getValue();

				// In case like <html xmlns:wicket> will the value be null
				if (value == null)
				{
					value = "";
				}

				// Set new position to end of attribute
				pos = attributeParser.matcher().end(0);

				// Chop off double quotes or single quotes
				if (value.startsWith("\"") || value.startsWith("\'"))
				{
					value = value.substring(1, value.length() - 1);
				}

				// Trim trailing whitespace
				value = value.trim();

				// Get key
				final String key = attributeParser.getKey();

				// Put the attribute in the attributes hash
				if (null != tag.put(key, value))
				{
				    throw new ParseException("Same attribute found twice: " 
				            + key, this.inputPosition);
				}

				// The input has to match exactly (no left over junk after
				// attributes)
				if (pos == tagTextLength)
				{
					return tag;
				}
			}

			return tag;
		}

		return null;
	}

	/**
	 * Sets the input string to parse.
	 *
	 * @param input
	 *            The input string
	 */
	private void setInput(final CharSequence input)
	{
		this.input = input.toString();
		this.inputPosition = 0;
		this.positionMarker = 0;
	}
}
