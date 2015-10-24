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

import java.io.IOException;
import java.io.Reader;
import java.text.ParseException;

/**
 * This is not a reader like e.g. FileReader. It rather reads the whole data until the end from a
 * source reader into memory and besides that it maintains the current position (like a reader) it
 * provides String like methods which conveniently let you navigate (usually forward) in the stream.
 * <p>
 * Because the source data are expected to be text, the line and column numbers are maintained as
 * well for location precise error messages. But it does NOT automatically update the line and
 * column numbers. You must call {@link #countLinesTo(int)}
 * 
 * @author Juergen Donnerstag
 */
public final class FullyBufferedReader
{
	/** All the chars from the resource */
	private final String input;

	/** Position in parse. */
	private int inputPosition;

	/** Current line number */
	private int lineNumber = 1;

	/** current column number. */
	private int columnNumber = 1;

	/** Last place we counted lines from. */
	private int lastLineCountIndex;

	/** A variable to remember a certain position in the markup */
	private int positionMarker;

	/**
	 * Read all the data from the resource into memory.
	 * 
	 * @param reader
	 *            The source reader to load the data from
	 * @throws IOException
	 */
	public FullyBufferedReader(final Reader reader) throws IOException
	{
		this(Streams.readString(reader));
	}

	/**
	 * Construct.
	 * 
	 * @param input
	 *            The source string
	 */
	public FullyBufferedReader(String input)
	{
		this.input = input;
	}

	/**
	 * Get the characters from the position marker to toPos.
	 * <p>
	 * If toPos < 0, than get all data from the position marker until the end. If toPos less than
	 * the current position marker than return an empty string ""
	 * 
	 * @param toPos
	 *            Index of first character not included
	 * @return Raw markup (a string) in between these two positions.
	 */
	public final CharSequence getSubstring(int toPos)
	{
		if (toPos < 0)
		{
			toPos = input.length();
		}
		else if (toPos < positionMarker)
		{
			return "";
		}
		return input.subSequence(positionMarker, toPos);
	}

	/**
	 * Get the characters from in between both positions including the char at fromPos, excluding
	 * the char at toPos
	 * 
	 * @param fromPos
	 *            first index
	 * @param toPos
	 *            second index
	 * @return the string (raw markup) in between both positions
	 */
	public final CharSequence getSubstring(final int fromPos, final int toPos)
	{
		return input.subSequence(fromPos, toPos);
	}

	/**
	 * Gets the current input position
	 * 
	 * @return input position
	 */
	public final int getPosition()
	{
		return inputPosition;
	}

	/**
	 * Remember the current position in markup
	 * 
	 * @param pos
	 */
	public final void setPositionMarker(final int pos)
	{
		positionMarker = pos;
	}

	/**
	 * @return The markup to be parsed
	 */
	@Override
	public String toString()
	{
		return input;
	}

	/**
	 * Counts lines starting where we last left off up to the index provided.
	 * 
	 * @param end
	 *            End index
	 */
	public final void countLinesTo(final int end)
	{
		for (int i = lastLineCountIndex; i < end; i++)
		{
			final char ch = input.charAt(i);
			if (ch == '\n')
			{
				columnNumber = 1;
				lineNumber++;
			}
			else if (ch != '\r')
			{
				columnNumber++;
			}
		}

		lastLineCountIndex = end;
	}

	/**
	 * Find a char starting at the current input position
	 * 
	 * @param ch
	 *            The char to search for
	 * @return -1 if not found
	 */
	public final int find(final char ch)
	{
		return input.indexOf(ch, inputPosition);
	}

	/**
	 * Find a char starting at the position provided
	 * 
	 * @param ch
	 *            The char to search for
	 * @param startPos
	 *            The index to start at
	 * @return -1 if not found
	 */
	public final int find(final char ch, final int startPos)
	{
		return input.indexOf(ch, startPos);
	}

	/**
	 * Find the string starting at the current input position
	 * 
	 * @param str
	 *            The string to search for
	 * @return -1 if not found
	 */
	public final int find(final String str)
	{
		return input.indexOf(str, inputPosition);
	}

	/**
	 * Find the string starting at the position provided
	 * 
	 * @param str
	 *            The string to search for
	 * @param startPos
	 *            The index to start at
	 * @return -1 if not found
	 */
	public final int find(final String str, final int startPos)
	{
		return input.indexOf(str, startPos);
	}

	/**
	 * Find a char starting at the position provided. The char must not be inside a quoted string
	 * (single or double)
	 * 
	 * @param ch
	 *            The char to search for
	 * @param startPos
	 *            The index to start at
	 * @return -1 if not found
	 * @throws ParseException 
	 */
	public int findOutOfQuotes(final char ch, int startPos) throws ParseException
	{
		return findOutOfQuotes(ch, startPos, (char)0);
	}

	/**
	 * Find a char starting at the position provided. The char must not be inside a quoted string
	 * (single or double)
	 * 
	 * @param ch
	 *            The char to search for
	 * @param startPos
	 *            The index to start at
	 * @param quotationChar
	 *            The current quotation char. Must be ' or ", otherwise will be ignored.
	 * @return -1 if not found
	 * @throws ParseException 
	 */
	public int findOutOfQuotes(final char ch, int startPos, char quotationChar)
		throws ParseException
	{
		int closeBracketIndex = find(ch, startPos + 1);

		if (closeBracketIndex != -1)
		{
			CharSequence tagCode = getSubstring(startPos, closeBracketIndex + 1);

			for (int i = 0; i < tagCode.length(); i++)
			{
				char currentChar = tagCode.charAt(i);
				char previousTag = tagCode.charAt(i > 0 ? i - 1 : 0);

				if (quotationChar == 0 && (currentChar == '\'' || currentChar == '\"'))
				{// I'm entering inside a quoted string. Set quotationChar
					quotationChar = currentChar;
					countLinesTo(startPos + i);
				}
				else if (currentChar == quotationChar && previousTag != '\\')
				{ // I'm out of quotes, reset quotationChar
					quotationChar = 0;
				}
				// I've found character but I'm inside quotes
				if (currentChar == ch && quotationChar != 0)
				{
					return findOutOfQuotes(ch, closeBracketIndex + 1, quotationChar);
				}
			}
		}
		else if (quotationChar != 0)
		{
			// quotes not balanced!
			throw new ParseException("Opening/closing quote not found for quote at " + "(line " +
				getLineNumber() + ", column " + getColumnNumber() + ")", startPos);
		}

		return closeBracketIndex;
	}

	/**
	 * Position the reader at the index provided. Could be anywhere within the data
	 * 
	 * @param pos
	 *            The new current position
	 */
	public final void setPosition(final int pos)
	{
		inputPosition = pos;
	}

	/**
	 * Get the column number. Note: The column number depends on you calling countLinesTo(pos). It
	 * is not necessarily the column number matching the current position in the stream.
	 * 
	 * @return column number
	 */
	public final int getColumnNumber()
	{
		return columnNumber;
	}

	/**
	 * Get the line number. Note: The line number depends on you calling countLinesTo(pos). It is
	 * not necessarily the line number matching the current position in the stream.
	 * 
	 * @return line number
	 */
	public final int getLineNumber()
	{
		return lineNumber;
	}

	/**
	 * Get the number of character read from the source resource. The whole content, not just until
	 * the current position.
	 * 
	 * @return Size of the data
	 */
	public final int size()
	{
		return input.length();
	}

	/**
	 * Get the character at the position provided
	 * 
	 * @param pos
	 *            The position
	 * @return char at position
	 */
	public final char charAt(final int pos)
	{
		return input.charAt(pos);
	}
}
