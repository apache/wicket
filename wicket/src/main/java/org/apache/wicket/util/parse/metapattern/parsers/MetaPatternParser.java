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
package org.apache.wicket.util.parse.metapattern.parsers;

import java.util.regex.Matcher;

import org.apache.wicket.util.parse.metapattern.MetaPattern;


/**
 * Base class for various MetaPattern based parsers.
 * 
 * @author Jonathan Locke
 */
public abstract class MetaPatternParser
{
	/** The input to parse */
	private final CharSequence input;

	/** The length of the input; no. of characters */
	private final int length;

	/**
	 * The position (index) behind the last pattern group matched while advancing from one pattern
	 * group to the next one.
	 */
	private int pos;

	/** The object maintaining all the regex match details */
	private Matcher matcher;

	/**
	 * Construct the parser. You must call
	 * 
	 * @see #advance(MetaPattern) to initialize the matcher with the pattern.
	 * @param input
	 *            to parse
	 */
	public MetaPatternParser(final CharSequence input)
	{
		this.input = input;
		length = input.length();
	}

	/**
	 * Construct the parser and initialize the matcher with the pattern given.
	 * 
	 * @param pattern
	 *            Meta pattern
	 * @param input
	 *            Input to parse
	 */
	public MetaPatternParser(final MetaPattern pattern, final CharSequence input)
	{
		this(input);
		setPattern(pattern);
	}

	/**
	 * @param pattern
	 *            Pattern
	 */
	public void setPattern(final MetaPattern pattern)
	{
		matcher = pattern.matcher(input);
	}

	/**
	 * Advance parsing to the next element. The internal cursor will be moved to end of the string
	 * matched.
	 * 
	 * @param pattern
	 *            Meta pattern
	 * @return True if found, false otherwise
	 */
	protected final boolean advance(final MetaPattern pattern)
	{
		// get the remaining part of the input
		final CharSequence s = input.subSequence(pos, length);

		// does the pattern match?
		matcher = pattern.matcher(s);
		if (matcher.lookingAt())
		{
			// Yes, it does. Move the cursor to the end of the
			// char sequence matched.
			pos += matcher.end();

			// Found the pattern
			return true;
		}

		// Did not find the pattern.
		return false;
	}

	/**
	 * Whether the matcher matches the pattern.
	 * 
	 * @return whether the matcher matches
	 */
	public boolean matches()
	{
		return matcher.matches();
	}

	/**
	 * Gets the matcher.
	 * 
	 * @return the matcher
	 */
	public final Matcher matcher()
	{
		return matcher;
	}

	/**
	 * Whether the internal cursor has advanced to the end of the input.
	 * 
	 * @return whether the input is parsed
	 */
	public final boolean atEnd()
	{
		return pos == length;
	}
}
