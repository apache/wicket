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

import org.apache.wicket.util.parse.metapattern.Group;
import org.apache.wicket.util.parse.metapattern.MetaPattern;

/**
 * Matches a 'word' surrounded by whitespace. See <a
 * href="http://java.sun.com/j2se/1.4.2/docs/api/java/util/regex/Pattern.html"
 * >java.util.regex.Pattern </a> for more details on what 'word' means.
 * 
 * @author Jonathan Locke
 */
public final class WordParser extends MetaPatternParser
{
	/**
	 * Make it a group to be able to access the word without surrounding whitespace
	 */
	private static final Group word = new Group(MetaPattern.WORD);

	/** Parse word surrounded by whitespace */
	private static final MetaPattern wordPattern = new MetaPattern(MetaPattern.OPTIONAL_WHITESPACE,
		word, MetaPattern.OPTIONAL_WHITESPACE);

	/**
	 * Construct.
	 * 
	 * @param input
	 *            to parse
	 */
	public WordParser(final CharSequence input)
	{
		super(wordPattern, input);
	}

	/**
	 * Gets the word including the optional whitespace surrounding the word.
	 * 
	 * @return the word surrounded by whitespace
	 */
	public String getWord()
	{
		return word.get(matcher());
	}
}
