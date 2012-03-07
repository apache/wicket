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
package org.apache.wicket.util.parse.metapattern;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.wicket.util.io.IClusterable;


/**
 * Useful class for constructing readable and reusable regular expressions.
 * <p>
 * MetaPatterns can be constructed from a simple regular expression String, from other MetaPatterns
 * (copy constructor), from a list of MetaPatterns or from an array of MetaPatterns. In this way, it
 * is easy to build up larger patterns while transparently binding the capturing groups of each
 * MetaPattern for easy object oriented access to capturing group matches.
 * <p>
 * A given MetaPattern can be converted to a Matcher or Pattern. Groups within the MetaPattern can
 * be used to automatically reference capturing group values when a match is made with a Matcher
 * object.
 * <p>
 * A variety of static constants are provided for use in constructing compound MetaPatterns. Also, a
 * number of simple parsers have been constructed using MetaPatterns in the parsers subpackage.
 * 
 * @author Jonathan Locke
 */
public class MetaPattern implements IClusterable
{
	private static final long serialVersionUID = 1L;

	/**
	 * Compiled regular expression pattern, or null if patterns variable is valid instead
	 */
	private Pattern pattern;

	/** List of patterns, or null if pattern variable is valid instead */
	private List<MetaPattern> patterns;

	/** The compiled MetaPattern */
	private Pattern compiledPattern;

	// Regexps that are used multiple times in defining meta patterns
	private static final String _DOUBLE_QUOTED_STRING = "\"[^\"]*?\"";
	private static final String _SINGLE_QUOTED_STRING = "'[^']*?\'";
	private static final String _STRING = "(?:[\\w\\-\\.]+|" + _DOUBLE_QUOTED_STRING + "|" +
		_SINGLE_QUOTED_STRING + ")";
	private static final String _OPTIONAL_STRING = _STRING + "?";
	private static final String _VARIABLE_NAME = "[A-Za-z_][A-Za-z0-9_]*";
	private static final String _XML_NAME = "[A-Za-z_][A-Za-z0-9_.-]*";

	// Delimiters and punctuation
	/** Constant for whitespace. */
	public static final MetaPattern WHITESPACE = new MetaPattern("\\s+");

	/** Constant for optional whitespace. */
	public static final MetaPattern OPTIONAL_WHITESPACE = new MetaPattern("\\s*");

	/** Constant for non-word. */
	public static final MetaPattern NON_WORD = new MetaPattern("\\W+");

	/** Constant for comma. */
	public static final MetaPattern COMMA = new MetaPattern(",");

	/** Constant for colon. */
	public static final MetaPattern COLON = new MetaPattern(":");

	/** Constant for semicolon. */
	public static final MetaPattern SEMICOLON = new MetaPattern(";");

	/** Constant for slash. */
	public static final MetaPattern SLASH = new MetaPattern("/");

	/** Constant for backslash. */
	public static final MetaPattern BACKSLASH = new MetaPattern("\\\\");

	/** Constant for dot. */
	public static final MetaPattern DOT = new MetaPattern("\\.");

	/** Constant for plus. */
	public static final MetaPattern PLUS = new MetaPattern("\\+");

	/** Constant for minus. */
	public static final MetaPattern MINUS = new MetaPattern("-");

	/** Constant for dash. */
	public static final MetaPattern DASH = new MetaPattern("-");

	/** Constant for underscore. */
	public static final MetaPattern UNDERSCORE = new MetaPattern("_");

	/** Constant for ampersand. */
	public static final MetaPattern AMPERSAND = new MetaPattern("&");

	/** Constant for percent. */
	public static final MetaPattern PERCENT = new MetaPattern("%");

	/** Constant for dollar. */
	public static final MetaPattern DOLLAR_SIGN = new MetaPattern("$");

	/** Constant for pound. */
	public static final MetaPattern POUND_SIGN = new MetaPattern("#");

	/** Constant for at. */
	public static final MetaPattern AT_SIGN = new MetaPattern("@");

	/** Constant for excl. */
	public static final MetaPattern EXCLAMATION_POINT = new MetaPattern("!");

	/** Constant for tilde. */
	public static final MetaPattern TILDE = new MetaPattern("~");

	/** Constant for equals. */
	public static final MetaPattern EQUALS = new MetaPattern("=");

	/** Constant for star. */
	public static final MetaPattern STAR = new MetaPattern("\\*");

	/** Constant for pipe. */
	public static final MetaPattern PIPE = new MetaPattern("\\|");

	/** Constant for left paren. */
	public static final MetaPattern LEFT_PAREN = new MetaPattern("\\(");

	/** Constant for right paren. */
	public static final MetaPattern RIGHT_PAREN = new MetaPattern("\\)");

	/** Constant for left curly braces. */
	public static final MetaPattern LEFT_CURLY = new MetaPattern("\\{");

	/** Constant for right curly braces. */
	public static final MetaPattern RIGHT_CURLY = new MetaPattern("\\}");

	/** Constant for left square bracket. */
	public static final MetaPattern LEFT_SQUARE = new MetaPattern("\\[");

	/** Constant for right square bracket. */
	public static final MetaPattern RIGHT_SQUARE = new MetaPattern("\\]");

	/** Constant for digit. */
	public static final MetaPattern DIGIT = new MetaPattern("\\d");

	/** Constant for digits. */
	public static final MetaPattern DIGITS = new MetaPattern("\\d+");

	/** Constant for an integer (of any size). */
	public static final MetaPattern INTEGER = new MetaPattern("-?\\d+");

	/** Constant for a floating point number. */
	public static final MetaPattern FLOATING_POINT_NUMBER = new MetaPattern(
		"-?\\d+\\.?\\d*|-?\\.\\d+");

	/** Constant for a positive integer. */
	public static final MetaPattern POSITIVE_INTEGER = new MetaPattern("\\d+");

	/** Constant for hex digit. */
	public static final MetaPattern HEXADECIMAL_DIGIT = new MetaPattern("[0-9a-fA-F]");

	/** Constant for hex digits. */
	public static final MetaPattern HEXADECIMAL_DIGITS = new MetaPattern("[0-9a-fA-F]+");

	/** Constant for anything (string). */
	public static final MetaPattern ANYTHING = new MetaPattern(".*");

	/** Constant for anything non-empty (string). */
	public static final MetaPattern ANYTHING_NON_EMPTY = new MetaPattern(".+");

	/** Constant for a word. */
	public static final MetaPattern WORD = new MetaPattern("\\w+");

	/** Constant for an optional word. */
	public static final MetaPattern OPTIONAL_WORD = new MetaPattern("\\w*");

	/** Constant for a variable name. */
	public static final MetaPattern VARIABLE_NAME = new MetaPattern(_VARIABLE_NAME);

	/** Constant for an XML element name. */
	public static final MetaPattern XML_ELEMENT_NAME = new MetaPattern(_XML_NAME);

	/** Constant for an XML attribute name. */
	public static final MetaPattern XML_ATTRIBUTE_NAME = new MetaPattern(_XML_NAME);

	/** Constant for perl interpolation. */
	public static final MetaPattern PERL_INTERPOLATION = new MetaPattern("$\\{" + _VARIABLE_NAME +
		"\\}");

	/** Constant for a double quoted string. */
	public static final MetaPattern DOUBLE_QUOTED_STRING = new MetaPattern(_DOUBLE_QUOTED_STRING);

	/** Constant for a string. */
	public static final MetaPattern STRING = new MetaPattern(_STRING);

	/** Constant for an optional string. */
	public static final MetaPattern OPTIONAL_STRING = new MetaPattern(_OPTIONAL_STRING);

	/**
	 * Constructor for a simple pattern.
	 * 
	 * @param pattern
	 *            The regular expression pattern to compile
	 */
	public MetaPattern(final String pattern)
	{
		this.pattern = Pattern.compile(pattern);
	}

	/**
	 * Copy constructor.
	 * 
	 * @param pattern
	 *            The meta pattern to copy
	 */
	public MetaPattern(final MetaPattern pattern)
	{
		this.pattern = pattern.pattern;
		patterns = pattern.patterns;
		compiledPattern = pattern.compiledPattern;
	}

	/**
	 * Constructs from an array of MetaPatterns.
	 * 
	 * @param patterns
	 *            Array of MetaPatterns
	 */
	public MetaPattern(final MetaPattern... patterns)
	{
		this(Arrays.asList(patterns));
	}

	/**
	 * Constructs from a list of MetaPatterns
	 * 
	 * @param patterns
	 *            List of MetaPatterns
	 */
	public MetaPattern(final List<MetaPattern> patterns)
	{
		this.patterns = patterns;
	}

	/**
	 * Creates a matcher against a given input character sequence.
	 * 
	 * @param input
	 *            The input to match against
	 * @return The matcher
	 */
	public final Matcher matcher(final CharSequence input)
	{
		return matcher(input, 0);
	}

	/**
	 * Creates a matcher with the given regexp compile flags. Once you call this method with a given
	 * regexp compile flag value, the pattern will be compiled. Calling it again with a different
	 * value for flags will not recompile the pattern.
	 * 
	 * @param input
	 *            The input to match
	 * @param flags
	 *            One or more of the standard Java regular expression compile flags (see
	 *            {@link Pattern#compile(String, int)})
	 * @return The matcher
	 */
	public final Matcher matcher(final CharSequence input, final int flags)
	{
		compile(flags);
		return compiledPattern.matcher(input);
	}

	/**
	 * Gets the regular expression Pattern for this MetaPattern by compiling it.
	 * 
	 * @return Pattern compiled with default Java regular expression compile flags
	 */
	public final Pattern pattern()
	{
		return pattern(0);
	}

	/**
	 * Gets the regular expression Pattern for this MetaPattern by compiling it using the given
	 * flags.
	 * 
	 * @param flags
	 *            One or more of the standard Java regular expression compile flags (see
	 *            {@link Pattern#compile(String, int)})
	 * @return Equivalent Java regular expression Pattern compiled with the given flags
	 */
	public final Pattern pattern(final int flags)
	{
		compile(flags);
		return compiledPattern;
	}

	/**
	 * Converts this MetaPattern to a String.
	 * 
	 * @return A String representing this MetaPattern
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		if (pattern != null)
		{
			return pattern.pattern();
		}
		else
		{
			final StringBuilder buffer = new StringBuilder();
			for (MetaPattern metaPattern : patterns)
			{
				buffer.append(metaPattern);
			}
			return buffer.toString();
		}
	}

	/**
	 * Compiles this MetaPattern with the given Java regular expression flags.
	 * 
	 * @param flags
	 *            One or more of the standard Java regular expression compile flags (see
	 *            {@link Pattern#compile(String, int)})
	 */
	private synchronized void compile(final int flags)
	{
		if (compiledPattern == null)
		{
			bind(1);
			compiledPattern = Pattern.compile(toString(), flags);
		}
	}

	/**
	 * Binds this MetaPattern to one or more capturing groups. Since MetaPatterns can nest, the
	 * binding process can recurse.
	 * 
	 * @param group
	 *            The initial capturing group number
	 * @return The final capturing group (for use in recursion)
	 */
	private int bind(int group)
	{
		if (this instanceof Group)
		{
			((Group)this).bind(group++);
		}

		if (patterns != null)
		{
			for (MetaPattern metaPattern : patterns)
			{
				group = metaPattern.bind(group);
			}
		}

		return group;
	}
}
