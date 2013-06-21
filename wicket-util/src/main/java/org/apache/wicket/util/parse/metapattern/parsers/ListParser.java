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


import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.util.parse.metapattern.Group;
import org.apache.wicket.util.parse.metapattern.MetaPattern;


/**
 * Parses an arbitrary list format with a pattern for list entries and a pattern for list
 * separators.
 * 
 * @author Jonathan Locke
 */
public class ListParser extends MetaPatternParser
{
	/** The pattern in between the separators */
	private final Group entryGroup;

	/** The separator */
	private final MetaPattern separatorPattern;

	/** The list elements parsed */
	private final List<String> values = new ArrayList<>();

	/**
	 * Constructs a list parser from an entry MetaPattern, a separator MetaPattern and an input
	 * character sequence.
	 * 
	 * @param entryPattern
	 *            The pattern in between the separators
	 * @param separatorPattern
	 *            The separator pattern
	 * @param input
	 *            The input to parse
	 */
	public ListParser(final MetaPattern entryPattern, final MetaPattern separatorPattern,
		final CharSequence input)
	{
		super(input);
		entryGroup = new Group(entryPattern);
		this.separatorPattern = separatorPattern;
	}

	/**
	 * Parse the input and add the elements to an internal list to be accessed by
	 * 
	 * @see #getValues()
	 * @see org.apache.wicket.util.parse.metapattern.parsers.MetaPatternParser#matches()
	 */
	@Override
	public final boolean matches()
	{
		// Are there any more elements
		if (advance(entryGroup))
		{
			// Add the first element
			final String value = entryGroup.get(matcher());
			values.add(value);

			// All remaining elements must be preceded by the separator pattern
			while (advance(separatorPattern) && advance(entryGroup))
			{
				// Add the value not including the separator
				values.add(entryGroup.get(matcher()));
			}

			// Yes, we found at least on element
			return true;
		}

		// Nothing found, not even one element without separator
		return false;
	}

	/**
	 * Gets the parsed values. It depends on the elements pattern, whether empty elements, double or
	 * single quotes or escape characters are supported.
	 * 
	 * @return the parsed values
	 */
	public final List<String> getValues()
	{
		return values;
	}
}
