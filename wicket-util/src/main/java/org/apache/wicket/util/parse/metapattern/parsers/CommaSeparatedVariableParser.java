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

import org.apache.wicket.util.parse.metapattern.MetaPattern;

/**
 * Parses out strings separated by commas.
 * <p>
 * Notes:
 * <p>
 * <ul>
 * <li>It'll not trim the elements (it'll not remove whitespace)</li>
 * <li>It is able to handle quotes like "a", 'b', "b,c" etc..</li>
 * <li>But no escapes like "c\"d"</li>
 * <li>Empty list elements like "a,," are not supported. It'll return the "a" only.</li>
 * </ul>
 * 
 * @author Jonathan Locke
 */
public final class CommaSeparatedVariableParser extends ListParser
{
	/** Pattern to use. */
	private static final MetaPattern patternEntry = new MetaPattern(
		MetaPattern.OPTIONAL_WHITESPACE, MetaPattern.STRING, MetaPattern.OPTIONAL_WHITESPACE);

	/**
	 * Construct a new parser with parameter 'input' to be parsed. Base classes provide the method
	 * to access the elements of the input parsed.
	 * 
	 * @param input
	 *            to parse
	 */
	public CommaSeparatedVariableParser(final CharSequence input)
	{
		super(patternEntry, MetaPattern.COMMA, input);
	}
}
