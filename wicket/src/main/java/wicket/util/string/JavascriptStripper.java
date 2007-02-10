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
package wicket.util.string;


/**
 * Strips comments and whitespace from javascript
 * 
 * @author Matej Knopp
 */
public class JavascriptStripper
{
	/**
	 * Determines the state of script proessing.
	 * @author Matej Knopp
	 */
	private enum State {
		/** Inside regular text */
		REGULAR_TEXT, 
		
		/** String started with single quote (') */
		STRING_SINGLE_QUOTE,
		
		/** String started with double quotes (") */
		STRING_DOUBLE_QUOTES, 
		
		/** Inside two or more whitespace characters */
		WHITE_SPACE, 
		
		/** Inside a line comment (//   ) */
		LINE_COMMENT, 
		
		/** Inside a multi line comment */
		MULTILINE_COMMENT
	};

	/**
	 * Removes javascript comments and whitespaces from specified string.
	 * 
	 * @param original
	 *            Source string
	 * @return String with removed comments and whitespaces
	 */
	public static String stripCommentsAndWhitespace(String original)
	{
		// let's be optimistic
		StringBuilder result = new StringBuilder(original.length() / 2);
		State state = State.REGULAR_TEXT;

		for (int i = 0; i < original.length(); ++i)
		{
			char c = original.charAt(i);
			char next = (i < original.length() - 1) ? original.charAt(i + 1) : 0;
			char prev = (i > 0) ? original.charAt(i - 1) : 0;

			if (state == State.WHITE_SPACE)
			{
				if (Character.isWhitespace(next) == false)
				{
					state = State.REGULAR_TEXT;
				}
				continue;
			}

			if (state == State.REGULAR_TEXT)
			{
				if (c == '/' && next == '/')
				{
					state = State.LINE_COMMENT;
					continue;
				}
				else if (c == '/' && next == '*')
				{
					state = State.MULTILINE_COMMENT;
					++i;
					continue;
				}
				else if (Character.isWhitespace(c) && Character.isWhitespace(next))
				{
					// ignore all whitespace characters after this one
					state = State.WHITE_SPACE;
					c = ' ';
				}
				else if (c == '\'')
				{
					state = State.STRING_SINGLE_QUOTE;
				}
				else if (c == '"')
				{
					state = State.STRING_DOUBLE_QUOTES;
				}
				result.append(c);
				continue;
			}

			if (state == State.LINE_COMMENT)
			{
				if (c == '\n')
				{
					state = State.REGULAR_TEXT;
					continue;
				}
			}

			if (state == State.MULTILINE_COMMENT)
			{
				if (c == '*' && next == '/')
				{
					state = State.REGULAR_TEXT;
					++i;
					continue;
				}
			}

			if (state == State.STRING_SINGLE_QUOTE)
			{
				if (c == '\'' && prev != '\\')
				{
					state = State.REGULAR_TEXT;
				}
				result.append(c);
				continue;
			}

			if (state == State.STRING_DOUBLE_QUOTES)
			{
				if (c == '"' && prev != '\\')
				{
					state = State.REGULAR_TEXT;
				}
				result.append(c);
				continue;
			}
		}

		return result.toString();
	}
}
