/*
 * $Id: XmlPullParser.java 5791 2006-05-20 00:32:57 +0000 (Sat, 20 May 2006)
 * joco01 $ $Revision$ $Date: 2006-05-20 00:32:57 +0000 (Sat, 20 May
 * 2006) $
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
import java.text.ParseException;

import wicket.markup.MarkupElement;
import wicket.util.io.FullyBufferedReader;
import wicket.util.io.XmlReader;
import wicket.util.parse.metapattern.parsers.TagNameParser;
import wicket.util.parse.metapattern.parsers.VariableAssignmentParser;
import wicket.util.resource.IResourceStream;
import wicket.util.resource.ResourceStreamNotFoundException;
import wicket.util.resource.StringResourceStream;

/**
 * A fairly shallow markup pull parser which parses a markup string of a given
 * type of markup (for example, html, xml, vxml or wml) into ComponentTag and
 * RawMarkup tokens.
 * 
 * @author Jonathan Locke
 * @author Juergen Donnerstag
 */
public final class XmlPullParser extends AbstractMarkupFilter implements IXmlPullParser
{
	/**
	 * Reads the xml data from an input stream and converts the chars according
	 * to its encoding (<?xml ... encoding="..." ?>)
	 */
	private XmlReader xmlReader;

	/**
	 * A XML independent reader which loads the whole source data into memory
	 * and which provides convinience methods to access the data.
	 */
	private FullyBufferedReader input;

	/** temporary variable which will hold the name of the closing tag. */
	private String skipUntilText;

	/**
	 * Construct.
	 */
	public XmlPullParser()
	{
		// The xml parser does not have a parent filter
		super(null);
	}

	/**
	 * 
	 * @see wicket.markup.parser.IXmlPullParser#getEncoding()
	 */
	public String getEncoding()
	{
		return this.xmlReader.getEncoding();
	}

	/**
	 * 
	 * @see wicket.markup.parser.IXmlPullParser#getXmlDeclaration()
	 */
	public String getXmlDeclaration()
	{
		return this.xmlReader.getXmlDeclaration();
	}

	/**
	 * 
	 * @see wicket.markup.parser.IXmlPullParser#getInputFromPositionMarker(int)
	 */
	public final CharSequence getInputFromPositionMarker(final int toPos)
	{
		return this.input.getSubstring(toPos);
	}

	/**
	 * 
	 * @see wicket.markup.parser.IXmlPullParser#getInput(int, int)
	 */
	public final CharSequence getInput(final int fromPos, final int toPos)
	{
		return this.input.getSubstring(fromPos, toPos);
	}

	/**
	 * Whatever will be in between the current index and the closing tag, will
	 * be ignored (and thus treated as raw markup (text). This is useful for
	 * tags like 'script'.
	 * 
	 * @throws ParseException
	 */
	private final void skipUntil() throws ParseException
	{
		// this is a tag with non-XHTML text as body - skip this until the
		// skipUntilText is found.
		final int startIndex = this.input.getPosition();
		final int tagNameLen = this.skipUntilText.length();

		int pos = this.input.getPosition() - 1;
		String endTagText = null;
		int lastPos = 0;
		while (!skipUntilText.equalsIgnoreCase(endTagText))
		{
			pos = this.input.find("</", pos + 1);
			if ((pos == -1) || ((pos + (tagNameLen + 2)) >= this.input.size()))
			{
				throw new ParseException(skipUntilText + " tag not closed (line "
						+ this.input.getLineNumber() + ", column " + this.input.getColumnNumber()
						+ ")", startIndex);
			}

			lastPos = pos + 2;
			endTagText = this.input.getSubstring(lastPos, lastPos + tagNameLen).toString();
		}

		this.input.setPosition(pos);

		// Get index of closing tag and advance past the tag
		lastPos = this.input.find('>', lastPos + tagNameLen);
		if (lastPos == -1)
		{
			throw new ParseException("Script tag not closed (line " + this.input.getLineNumber()
					+ ", column " + this.input.getColumnNumber() + ")", startIndex);
		}

		// Reset the state variable
		this.skipUntilText = null;
	}

	/**
	 * Gets the next tag from the input string.
	 * 
	 * @return The extracted tag (will always be of type XmlTag).
	 * @throws ParseException
	 */
	public final MarkupElement nextTag() throws ParseException
	{
		if (this.skipUntilText != null)
		{
			skipUntil();
		}

		// While we can find an open tag, parse the tag
		final int openBracketIndex = this.input.find('<');
		if (openBracketIndex == -1)
		{
			// There is no next matching tag
			return null;
		}

		// Determine line number
		this.input.countLinesTo(openBracketIndex);

		// Get index of closing tag and advance past the tag
		int closeBracketIndex = this.input.find('>', openBracketIndex + 1);

		if (closeBracketIndex == -1)
		{
			throw new ParseException("No matching close bracket at position " + openBracketIndex,
					this.input.getPosition());
		}

		// Get the tagtext between open and close brackets
		String tagText = this.input.getSubstring(openBracketIndex + 1, closeBracketIndex)
				.toString();

		// Handle comments
		if (tagText.startsWith("!--"))
		{
			// Skip ahead to "-->". Note that you can not simply test for
			// tagText.endsWith("--") as the comment might contain a '>'
			// inside.
			final int pos = this.input.find("-->", openBracketIndex + 1);
			if (pos == -1)
			{
				throw new ParseException("Unclosed comment beginning at line:"
						+ input.getLineNumber() + " column:" + input.getColumnNumber(),
						openBracketIndex);
			}

			this.input.setPosition(pos + 3);
			return nextTag();
		}

		// CDATA sections might contain "<" which is not part of an XML tag.
		// Make sure escaped "<" are treated right
		final String startText = (tagText.length() <= 8 ? tagText : tagText.substring(0, 8));
		if (startText.toUpperCase().equals("![CDATA["))
		{
			int pos1 = openBracketIndex;
			do
			{
				// Get index of closing tag and advance past the tag
				closeBracketIndex = findChar('>', pos1);

				if (closeBracketIndex == -1)
				{
					throw new ParseException("No matching close bracket at position "
							+ openBracketIndex, this.input.getPosition());
				}

				// Get the tagtext between open and close brackets
				tagText = this.input.getSubstring(openBracketIndex + 1, closeBracketIndex)
						.toString();

				pos1 = closeBracketIndex + 1;
			}
			while (tagText.endsWith("]]") == false);
			
			// Move to position after the tag
			this.input.setPosition(closeBracketIndex + 1);

			// Return next tag
			return nextTag();
		}

		{
			// Type of tag
			XmlTag.Type type = XmlTag.Type.OPEN;

			// If the tag ends in '/', it's a "simple" tag like <foo/>
			if (tagText.endsWith("/"))
			{
				type = XmlTag.Type.OPEN_CLOSE;
				tagText = tagText.substring(0, tagText.length() - 1);
			}
			else if (tagText.startsWith("/"))
			{
				// The tag text starts with a '/', it's a simple close tag
				type = XmlTag.Type.CLOSE;
				tagText = tagText.substring(1);
			}

			// We don't deeply parse tags like DOCTYPE that start with !
			// or XML document definitions that start with ?
			if (tagText.startsWith("!") || tagText.startsWith("?"))
			{
				// Move to position after the tag
				this.input.setPosition(closeBracketIndex + 1);

				// Return next tag
				return nextTag();
			}
			else
			{
				final String lowerCase = tagText.toLowerCase();

				// Often save a (longer) comparison at the expense of a
				// extra shorter one for 's' tags
				if ((type == XmlTag.Type.OPEN) && lowerCase.startsWith("s"))
				{
					if (lowerCase.startsWith("script"))
					{
						this.skipUntilText = "script";
					}
					else if (lowerCase.startsWith("style"))
					{
						this.skipUntilText = "style";
					}
				}

				// Parse remaining tag text, obtaining a tag object or null
				// if it's invalid
				final XmlTag tag = parseTagText(tagText);
				if (tag != null)
				{
					// Populate tag fields
					tag.type = type;
					tag.pos = openBracketIndex;
					tag.length = (closeBracketIndex + 1) - openBracketIndex;
					tag.text = this.input.getSubstring(openBracketIndex, closeBracketIndex + 1)
							.toString();
					tag.lineNumber = this.input.getLineNumber();
					tag.columnNumber = this.input.getColumnNumber();

					// Move to position after the tag
					this.input.setPosition(closeBracketIndex + 1);

					// Return the tag we found!
					return tag;
				}
				else
				{
					throw new ParseException("Malformed tag (line " + this.input.getLineNumber()
							+ ", column " + this.input.getColumnNumber() + ")", openBracketIndex);
				}
			}
		}
	}

	/**
	 * Find the char but ignore any text within ".." and '..'
	 * 
	 * @param ch
	 *            The character to search
	 * @param startIndex
	 *            Start index
	 * @return -1 if not found, else the index
	 */
	private int findChar(final char ch, int startIndex)
	{
		char quote = 0;

		for (; startIndex < this.input.size(); startIndex++)
		{
			final char charAt = this.input.charAt(startIndex);
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
	public void parse(final CharSequence string) throws IOException,
			ResourceStreamNotFoundException
	{
		parse(new StringResourceStream(string), null);
	}

	/**
	 * Reads and parses markup from a resource such as file.
	 * 
	 * @param resource
	 *            The resource to read and parse
	 * @param encoding
	 *            The default character encoding of the input
	 * @throws IOException
	 * @throws ResourceStreamNotFoundException
	 */
	public void parse(final IResourceStream resource, final String encoding) throws IOException,
			ResourceStreamNotFoundException
	{
		try
		{
			this.xmlReader = new XmlReader(
					new BufferedInputStream(resource.getInputStream(), 4000), encoding);
			this.input = new FullyBufferedReader(this.xmlReader);
		}
		finally
		{
			resource.close();
			this.xmlReader.close();
		}
	}

	/**
	 * 
	 * @see wicket.markup.parser.IXmlPullParser#setPositionMarker()
	 */
	public final void setPositionMarker()
	{
		this.input.setPositionMarker(this.input.getPosition());
	}

	/**
	 * 
	 * @see wicket.markup.parser.IXmlPullParser#setPositionMarker(int)
	 */
	public final void setPositionMarker(final int pos)
	{
		this.input.setPositionMarker(pos);
	}

	/**
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return this.input.toString();
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
					throw new ParseException("Same attribute found twice: " + key, this.input
							.getPosition());
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
}
