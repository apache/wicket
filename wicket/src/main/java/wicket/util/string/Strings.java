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

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import wicket.WicketRuntimeException;

/**
 * A variety of static String utility methods.
 * <p>
 * The escapeMarkup() and toMultilineMarkup() methods are useful for turning
 * normal Java Strings into HTML strings.
 * <p>
 * The lastPathComponent(), firstPathComponent(), afterFirstPathComponent() and
 * beforeLastPathComponent() methods can chop up a String into path components
 * using a separator character. If the separator cannot be found the original
 * String is returned.
 * <p>
 * Similarly, the beforeLast(), beforeFirst(), afterFirst() and afterLast()
 * methods return sections before and after a separator character. But if the
 * separator cannot be found, an empty string is returned.
 * <p>
 * Some other miscellaneous methods will strip a given ending off a String if it
 * can be found (stripEnding()), replace all occurrences of one String with
 * another (replaceAll), do type conversions (toBoolean(), toChar(),
 * toString()), check a String for emptiness (isEmpty()), convert a Throwable to
 * a String (toString(Throwable)) or capitalize a String (capitalize()).
 * 
 * @author Jonathan Locke
 */
public final class Strings
{
	/**
	 * The line seperator for the current platform.
	 */
	public static final String LINE_SEPARATOR;

	/** A table of hex digits */
	private static final char[] hexDigit = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A',
			'B', 'C', 'D', 'E', 'F' };

	private static final Pattern htmlNumber = Pattern.compile("\\&\\#\\d+\\;");

	static
	{
		LINE_SEPARATOR = AccessController.doPrivileged(new PrivilegedAction<String>()
		{
			public String run()
			{
				return System.getProperty("line.separator");
			}
		});
	}

	/**
	 * Returns everything after the first occurrence of the given character in
	 * s.
	 * 
	 * @param s
	 *            The string
	 * @param c
	 *            The character
	 * @return Everything after the first occurrence of the given character in
	 *         s. If the character cannot be found, an empty string is returned.
	 */
	public static String afterFirst(final String s, final char c)
	{
		if (s == null)
		{
			return null;
		}
		final int index = s.indexOf(c);

		if (index == -1)
		{
			return "";
		}

		return s.substring(index + 1);
	}

	/**
	 * Gets everything after the first path component of a path using a given
	 * separator. If the separator cannot be found, an empty String is returned.
	 * <p>
	 * For example, afterFirstPathComponent("foo.bar.baz", '.') would return
	 * "bar.baz" and afterFirstPathComponent("foo", '.') would return "".
	 * 
	 * @param path
	 *            The path to parse
	 * @param separator
	 *            The path separator character
	 * @return Everything after the first component in the path
	 */
	public static String afterFirstPathComponent(final String path, final char separator)
	{
		return afterFirst(path, separator);
	}

	/**
	 * Returns everything after the last occurence of the given character in s.
	 * 
	 * @param s
	 *            The string
	 * @param c
	 *            The character
	 * @return Everything after the last occurrence of the given character in s.
	 *         If the character cannot be found, an empty string is returned.
	 */
	public static String afterLast(final String s, final char c)
	{
		if (s == null)
		{
			return null;
		}
		final int index = s.lastIndexOf(c);

		if (index == -1)
		{
			return "";
		}

		return s.substring(index + 1);
	}

	/**
	 * Returns everything before the first occurrence of the given character in
	 * s.
	 * 
	 * @param s
	 *            The string
	 * @param c
	 *            The character
	 * @return Everything before the first occurrence of the given character in
	 *         s. If the character cannot be found, an empty string is returned.
	 */
	public static String beforeFirst(final String s, final char c)
	{
		if (s == null)
		{
			return null;
		}
		final int index = s.indexOf(c);

		if (index == -1)
		{
			return "";
		}

		return s.substring(0, index);
	}

	/**
	 * Returns everything before the last occurrence of the given character in
	 * s.
	 * 
	 * @param s
	 *            The string
	 * @param c
	 *            The character
	 * @return Everything before the last occurrence of the given character in
	 *         s. If the character cannot be found, an empty string is returned.
	 */
	public static String beforeLast(final String s, final char c)
	{
		if (s == null)
		{
			return null;
		}
		final int index = s.lastIndexOf(c);

		if (index == -1)
		{
			return "";
		}

		return s.substring(0, index);
	}

	/**
	 * Gets everything before the last path component of a path using a given
	 * separator. If the separator cannot be found, the path itself is returned.
	 * <p>
	 * For example, beforeLastPathComponent("foo.bar.baz", '.') would return
	 * "foo.bar" and beforeLastPathComponent("foo", '.') would return "".
	 * 
	 * @param path
	 *            The path to parse
	 * @param separator
	 *            The path separator character
	 * @return Everything before the last component in the path
	 */
	public static String beforeLastPathComponent(final String path, final char separator)
	{
		return beforeLast(path, separator);
	}

	/**
	 * Capitalizes a string.
	 * 
	 * @param s
	 *            The string
	 * @return The capitalized string
	 */
	public static String capitalize(final String s)
	{
		if (s == null)
		{
			return null;
		}
		final char[] chars = s.toCharArray();

		if (chars.length > 0)
		{
			chars[0] = Character.toUpperCase(chars[0]);
		}

		return new String(chars);
	}

	/**
	 * Converts a Java String to an HTML markup string, but does not convert
	 * normal spaces to non-breaking space entities (&lt;nbsp&gt;).
	 * 
	 * @param s
	 *            The string to escape
	 * @see Strings#escapeMarkup(String, boolean)
	 * @return The escaped string
	 */
	public static CharSequence escapeMarkup(final String s)
	{
		return escapeMarkup(s, false);
	}

	/**
	 * Converts a Java String to an HTML markup String by replacing illegal
	 * characters with HTML entities where appropriate. Spaces are converted to
	 * non-breaking spaces (&lt;nbsp&gt;) if escapeSpaces is true, tabs are
	 * converted to four non-breaking spaces, less than signs are converted to
	 * &amp;lt; entities and greater than signs to &amp;gt; entities.
	 * 
	 * @param s
	 *            The string to escape
	 * @param escapeSpaces
	 *            True to replace ' ' with nonbreaking space
	 * @return The escaped string
	 */
	public static CharSequence escapeMarkup(final String s, final boolean escapeSpaces)
	{
		return escapeMarkup(s, escapeSpaces, false);
	}

	/**
	 * Converts a Java String to an HTML markup String by replacing illegal
	 * characters with HTML entities where appropriate. Spaces are converted to
	 * non-breaking spaces (&lt;nbsp&gt;) if escapeSpaces is true, tabs are
	 * converted to four non-breaking spaces, less than signs are converted to
	 * &amp;lt; entities and greater than signs to &amp;gt; entities.
	 * 
	 * @param s
	 *            The string to escape
	 * @param escapeSpaces
	 *            True to replace ' ' with nonbreaking space
	 * @param convertToHtmlUnicodeEscapes
	 *            True to convert non-7 bit characters to unicode HTML (&#...)
	 * @return The escaped string
	 */
	public static CharSequence escapeMarkup(final String s, final boolean escapeSpaces,
			final boolean convertToHtmlUnicodeEscapes)
	{
		if (s == null)
		{
			return null;
		}
		else
		{
			int len = s.length();
			final AppendingStringBuffer buffer = new AppendingStringBuffer((int)(len * 1.1));

			for (int i = 0; i < len; i++)
			{
				final char c = s.charAt(i);

				switch (c)
				{
					case '\t' :
						if (escapeSpaces)
						{
							// Assumption is four space tabs (sorry, but that's
							// just how it is!)
							buffer.append("&nbsp;&nbsp;&nbsp;&nbsp;");
						}
						else
						{
							buffer.append(c);
						}
						break;

					case ' ' :
						if (escapeSpaces)
						{
							buffer.append("&nbsp;");
						}
						else
						{
							buffer.append(c);
						}
						break;

					case '<' :
						buffer.append("&lt;");
						break;

					case '>' :
						buffer.append("&gt;");
						break;

					case '&' :

						// if this is an entity (&#), then do not convert
						if ((i < len - 1) && (s.charAt(i + 1) == '#'))
						{
							buffer.append(c);

						}
						else
						{
							// it is not an entity, so convert it to &amp;
							buffer.append("&amp;");
						}
						break;

					case '"' :
						buffer.append("&quot;");
						break;

					case '\'' :
						buffer.append("&#039;");
						break;

					default :

						if (convertToHtmlUnicodeEscapes)
						{
							int ci = 0xffff & c;
							if (ci < 160)
							{
								// nothing special only 7 Bit
								buffer.append(c);
							}
							else
							{
								// Not 7 Bit use the unicode system
								buffer.append("&#");
								buffer.append(new Integer(ci).toString());
								buffer.append(';');
							}
						}
						else
						{
							buffer.append(c);
						}

						break;
				}
			}

			return buffer;
		}
	}

	/**
	 * Gets the first path component of a path using a given separator. If the
	 * separator cannot be found, the path itself is returned.
	 * <p>
	 * For example, firstPathComponent("foo.bar", '.') would return "foo" and
	 * firstPathComponent("foo", '.') would return "foo".
	 * 
	 * @param path
	 *            The path to parse
	 * @param separator
	 *            The path separator character
	 * @return The first component in the path or path itself if no separator
	 *         characters exist.
	 */
	public static String firstPathComponent(final String path, final char separator)
	{
		if (path == null)
		{
			return null;
		}
		final int index = path.indexOf(separator);

		if (index == -1)
		{
			return path;
		}

		return path.substring(0, index);
	}

	/**
	 * Converts encoded &#92;uxxxx to unicode chars and changes special saved
	 * chars to their original forms.
	 * 
	 * @param escapedUnicodeString
	 *            escaped unicode string, like '\u4F60\u597D'.
	 * 
	 * @return The actual unicode. Can be used for instance with message bundles
	 */
	public static String fromEscapedUnicode(String escapedUnicodeString)
	{
		int off = 0;
		char[] in = escapedUnicodeString.toCharArray();
		int len = in.length;
		char[] convtBuf = new char[len];

		if (convtBuf.length < len)
		{
			int newLen = len * 2;
			if (newLen < 0)
			{
				newLen = Integer.MAX_VALUE;
			}
			convtBuf = new char[newLen];
		}
		char aChar;
		char[] out = convtBuf;
		int outLen = 0;
		int end = off + len;

		while (off < end)
		{
			aChar = in[off++];
			if (aChar == '\\')
			{
				aChar = in[off++];
				if (aChar == 'u')
				{
					// Read the xxxx
					int value = 0;
					for (int i = 0; i < 4; i++)
					{
						aChar = in[off++];
						switch (aChar)
						{
							case '0' :
							case '1' :
							case '2' :
							case '3' :
							case '4' :
							case '5' :
							case '6' :
							case '7' :
							case '8' :
							case '9' :
								value = (value << 4) + aChar - '0';
								break;
							case 'a' :
							case 'b' :
							case 'c' :
							case 'd' :
							case 'e' :
							case 'f' :
								value = (value << 4) + 10 + aChar - 'a';
								break;
							case 'A' :
							case 'B' :
							case 'C' :
							case 'D' :
							case 'E' :
							case 'F' :
								value = (value << 4) + 10 + aChar - 'A';
								break;
							default :
								throw new IllegalArgumentException("Malformed \\uxxxx encoding.");
						}
					}
					out[outLen++] = (char)value;
				}
				else
				{
					if (aChar == 't')
					{
						aChar = '\t';
					}
					else if (aChar == 'r')
					{
						aChar = '\r';
					}
					else if (aChar == 'n')
					{
						aChar = '\n';
					}
					else if (aChar == 'f')
					{
						aChar = '\f';
					}
					out[outLen++] = aChar;
				}
			}
			else
			{
				out[outLen++] = aChar;
			}
		}
		return new String(out, 0, outLen);
	}

	/**
	 * Checks whether the <code>string</code> is considered empty. Empty means
	 * that the string may contain whitespace, but no visible characters.
	 * 
	 * "\n\t " is considered empty, while " a" is not.
	 * 
	 * @param string
	 *            The string
	 * @return True if the string is null or ""
	 */
	public static boolean isEmpty(final CharSequence string)
	{
		return string == null || string.length() == 0 || string.toString().trim().equals("");
	}

	/**
	 * Checks whether two strings are equals taken care of 'null' values and
	 * treating 'null' same as trim(string).equals("")
	 * 
	 * @param string1
	 * @param string2
	 * @return true, if both strings are equal
	 */
	public static boolean isEqual(final String string1, final String string2)
	{
		if ((string1 == null) && (string2 == null))
		{
			return true;
		}

		if (isEmpty(string1) && isEmpty(string2))
		{
			return true;
		}
		if (string1 == null || string2 == null)
		{
			return false;
		}

		return string1.equals(string2);
	}

	/**
	 * Converts the text in <code>s</code> to a corresponding boolean. On,
	 * yes, y, true and 1 are converted to <code>true</code>. Off, no, n,
	 * false and 0 (zero) are converted to <code>false</code>. An empty
	 * string is converted to <code>false</code>. Conversion is
	 * case-insensitive, and does <em>not</em> take internationalization into
	 * account.
	 * 
	 * 'Ja', 'Oui', 'Igen', 'Nein', 'Nee', 'Non', 'Nem' are all illegal values.
	 * 
	 * @param s
	 *            the value to convert into a boolean
	 * @return Boolean the converted value of <code>s</code>
	 * @throws StringValueConversionException
	 *             when the value of <code>s</code> is not recognized.
	 */
	public static boolean isTrue(final String s) throws StringValueConversionException
	{
		if (s != null)
		{
			if (s.equalsIgnoreCase("true"))
			{
				return true;
			}

			if (s.equalsIgnoreCase("false"))
			{
				return false;
			}

			if (s.equalsIgnoreCase("on") || s.equalsIgnoreCase("yes") || s.equalsIgnoreCase("y")
					|| s.equalsIgnoreCase("1"))
			{
				return true;
			}

			if (s.equalsIgnoreCase("off") || s.equalsIgnoreCase("no") || s.equalsIgnoreCase("n")
					|| s.equalsIgnoreCase("0"))
			{
				return false;
			}

			if (isEmpty(s))
			{
				return false;
			}

			throw new StringValueConversionException("Boolean value \"" + s + "\" not recognized");
		}

		return false;
	}

	/**
	 * Joins string fragments using the specified separator
	 * 
	 * @param separator
	 * @param fragments
	 * @return combined fragments
	 */
	public static String join(String separator, String... fragments)
	{
		if (fragments.length < 1)
		{
			// no elements
			return "";
		}
		else if (fragments.length < 2)
		{
			// single element
			return fragments[0];
		}
		else
		{
			// two or more elements
			StringBuilder buff = new StringBuilder(128);
			if (fragments[0] != null)
			{
				buff.append(fragments[0]);
			}
			for (int i = 1; i < fragments.length; i++)
			{
				if ((fragments[i - 1] != null)  || (fragments[i] != null))
				{
					boolean lhsClosed = fragments[i - 1].endsWith(separator);
					boolean rhsClosed = fragments[i].startsWith(separator);
					if (lhsClosed && rhsClosed)
					{
						buff.append(fragments[i].substring(1));
					}
					else if (!lhsClosed && !rhsClosed)
					{
						buff.append(separator).append(fragments[i]);
					}
					else
					{
						buff.append(fragments[i]);
					}
				}
			}
			return buff.toString();
		}
	}

	/**
	 * Gets the last path component of a path using a given separator. If the
	 * separator cannot be found, the path itself is returned.
	 * <p>
	 * For example, lastPathComponent("foo.bar", '.') would return "bar" and
	 * lastPathComponent("foo", '.') would return "foo".
	 * 
	 * @param path
	 *            The path to parse
	 * @param separator
	 *            The path separator character
	 * @return The last component in the path or path itself if no separator
	 *         characters exist.
	 */
	public static String lastPathComponent(final String path, final char separator)
	{
		final int index = path.lastIndexOf(separator);

		if (index == -1)
		{
			return path;
		}

		return path.substring(index + 1);
	}

	/**
	 * Replace all occurrences of one string replaceWith another string.
	 * 
	 * @param s
	 *            The string to process
	 * @param searchFor
	 *            The value to search for
	 * @param replaceWith
	 *            The value to searchFor replaceWith
	 * @return The resulting string with searchFor replaced with replaceWith
	 */
	public static CharSequence replaceAll(final CharSequence s, final CharSequence searchFor,
			CharSequence replaceWith)
	{
		if (s == null)
		{
			return null;
		}

		// If searchFor is null or the empty string, then there is nothing to
		// replace, so returning s is the only option here.
		if (searchFor == null || "".equals(searchFor))
		{
			return s;
		}

		// If replaceWith is null, then the searchFor should be replaced with
		// nothing, which can be seen as the empty string.
		if (replaceWith == null)
		{
			replaceWith = "";
		}

		String searchString = searchFor.toString();
		// Look for first occurrence of searchFor
		int matchIndex = search(s, searchString, 0);
		if (matchIndex == -1)
		{
			// No replace operation needs to happen
			return s;
		}
		else
		{
			// Allocate a AppendingStringBuffer that will hold one replacement
			// with a
			// little extra room.
			int size = s.length();
			final int replaceWithLength = replaceWith.length();
			final int searchForLength = searchFor.length();
			if (replaceWithLength > searchForLength)
			{
				size += (replaceWithLength - searchForLength);
			}
			final AppendingStringBuffer buffer = new AppendingStringBuffer(size + 16);

			int pos = 0;
			do
			{
				// Append text up to the match`
				append(buffer, s, pos, matchIndex);

				// Add replaceWith text
				buffer.append(replaceWith);

				// Find next occurrence, if any
				pos = matchIndex + searchForLength;
				matchIndex = search(s, searchString, pos);
			}
			while (matchIndex != -1);

			// Add tail of s
			buffer.append(s.subSequence(pos, s.length()));

			// Return processed buffer
			return buffer;
		}
	}

	/**
	 * Replace HTML numbers like &#20540 by the appropriate character.
	 * 
	 * @param str
	 *            The text to be evaluated
	 * @return The text with "numbers" replaced
	 */
	public static String replaceHtmlEscapeNumber(String str)
	{
		if (str == null)
		{
			return null;
		}
		Matcher matcher = htmlNumber.matcher(str);
		while (matcher.find())
		{
			int pos = matcher.start();
			int end = matcher.end();
			int number = Integer.parseInt(str.substring(pos + 2, end - 1));
			char ch = (char)number;
			str = str.substring(0, pos) + ch + str.substring(end);
			matcher = htmlNumber.matcher(str);
		}

		return str;
	}

	/**
	 * Simpler, faster version of String.split() for splitting on a simple
	 * character.
	 * 
	 * @param s
	 *            The string to split
	 * @param c
	 *            The character to split on
	 * @return The array of strings
	 */
	public static String[] split(final String s, final char c)
	{
		if (s == null)
		{
			return new String[0];
		}
		final List<String> strings = new ArrayList<String>();
		int pos = 0;
		while (true)
		{
			int next = s.indexOf(c, pos);
			if (next == -1)
			{
				strings.add(s.substring(pos));
				break;
			}
			else
			{
				strings.add(s.substring(pos, next));
			}
			pos = next + 1;
		}
		final String[] result = new String[strings.size()];
		strings.toArray(result);
		return result;
	}

	/**
	 * Strips the ending from the string <code>s</code>.
	 * 
	 * @param s
	 *            The string to strip
	 * @param ending
	 *            The ending to strip off
	 * @return The stripped string or the original string if the ending did not
	 *         exist
	 */
	public static String stripEnding(final String s, final String ending)
	{
		if (s == null)
		{
			return null;
		}

		// Stripping a null or empty string from the end returns the
		// original string.
		if (ending == null || "".equals(ending))
		{
			return s;
		}
		final int endingLength = ending.length();
		final int sLength = s.length();

		// When the length of the ending string is larger
		// than the original string, the original string is returned.
		if (endingLength > sLength)
		{
			return s;
		}
		final int index = s.lastIndexOf(ending);
		final int endpos = sLength - endingLength;

		if (index == endpos)
		{
			return s.substring(0, endpos);
		}

		return s;
	}

	/**
	 * Strip any jsessionid and possibly other redundant info that might be in
	 * our way.
	 * 
	 * @param url
	 *            The url to strip
	 * @return The stripped url
	 */
	public static String stripJSessionId(CharSequence url)
	{
		if (url == null)
		{
			return null;
		}
		StringBuilder path = new StringBuilder(url);
		int ixSemiColon = path.indexOf(";");
		// strip off any jsession id
		if (ixSemiColon != -1)
		{
			int ixEnd = path.indexOf("?");
			if (ixEnd == -1)
			{
				ixEnd = path.length();
			}
			path.delete(ixSemiColon, ixEnd);
		}
		return path.toString();
	}

	/**
	 * Converts the string s to a Boolean. See <code>isTrue</code> for valid
	 * values of s.
	 * 
	 * @param s
	 *            The string to convert.
	 * @return Boolean <code>TRUE</code> when <code>isTrue(s)</code>.
	 * @throws StringValueConversionException
	 *             when s is not a valid value
	 * @see #isTrue(String)
	 */
	public static Boolean toBoolean(final String s) throws StringValueConversionException
	{
		return Boolean.valueOf(isTrue(s));
	}


	/**
	 * Converts the 1 character string s to a character.
	 * 
	 * @param s
	 *            The 1 character string to convert to a char.
	 * @return Character value to convert
	 * @throws StringValueConversionException
	 *             when the string is longer or shorter than 1 character, or
	 *             <code>null</code>.
	 */
	public static char toChar(final String s) throws StringValueConversionException
	{
		if (s != null)
		{
			if (s.length() == 1)
			{
				return s.charAt(0);
			}
			else
			{
				throw new StringValueConversionException("Expected single character, not \"" + s
						+ "\"");
			}
		}

		throw new StringValueConversionException("Character value was null");
	}

	/**
	 * Converts unicodes to encoded &#92;uxxxx.
	 * 
	 * @param unicodeString
	 *            The unicode string
	 * @return The escaped unicode string, like '\u4F60\u597D'.
	 */
	public static String toEscapedUnicode(final String unicodeString)
	{
		if ((unicodeString == null) || (unicodeString.length() == 0))
		{
			return unicodeString;
		}
		int len = unicodeString.length();
		int bufLen = len * 2;
		StringBuffer outBuffer = new StringBuffer(bufLen);
		for (int x = 0; x < len; x++)
		{
			char aChar = unicodeString.charAt(x);
			// Handle common case first, selecting largest block that
			// avoids the specials below
			if ((aChar > 61) && (aChar < 127))
			{
				if (aChar == '\\')
				{
					outBuffer.append('\\');
					outBuffer.append('\\');
					continue;
				}
				outBuffer.append(aChar);
				continue;
			}
			switch (aChar)
			{
				case ' ' :
					if (x == 0)
					{
						outBuffer.append('\\');
					}
					outBuffer.append(' ');
					break;
				case '\t' :
					outBuffer.append('\\');
					outBuffer.append('t');
					break;
				case '\n' :
					outBuffer.append('\\');
					outBuffer.append('n');
					break;
				case '\r' :
					outBuffer.append('\\');
					outBuffer.append('r');
					break;
				case '\f' :
					outBuffer.append('\\');
					outBuffer.append('f');
					break;
				case '=' : // Fall through
				case ':' : // Fall through
				case '#' : // Fall through
				case '!' :
					outBuffer.append('\\');
					outBuffer.append(aChar);
					break;
				default :
					if ((aChar < 0x0020) || (aChar > 0x007e))
					{
						outBuffer.append('\\');
						outBuffer.append('u');
						outBuffer.append(toHex((aChar >> 12) & 0xF));
						outBuffer.append(toHex((aChar >> 8) & 0xF));
						outBuffer.append(toHex((aChar >> 4) & 0xF));
						outBuffer.append(toHex(aChar & 0xF));
					}
					else
					{
						outBuffer.append(aChar);
					}
			}
		}
		return outBuffer.toString();
	}

	/**
	 * Converts a String to multiline HTML markup by replacing newlines with
	 * line break entities (&lt;br/&gt;) and multiple occurrences of newline
	 * with paragraph break entities (&lt;p&gt;).
	 * 
	 * @param s
	 *            String to transform
	 * @return String with all single occurrences of newline replaced with
	 *         &lt;br/&gt; and all multiple occurrences of newline replaced with
	 *         &lt;p&gt;.
	 */
	public static CharSequence toMultilineMarkup(final CharSequence s)
	{
		if (s == null)
		{
			return null;
		}

		final AppendingStringBuffer buffer = new AppendingStringBuffer();
		int newlineCount = 0;

		buffer.append("<p>");
		for (int i = 0; i < s.length(); i++)
		{
			final char c = s.charAt(i);

			switch (c)
			{
				case '\n' :
					newlineCount++;
					break;

				case '\r' :
					break;

				default :
					if (newlineCount == 1)
					{
						buffer.append("<br/>");
					}
					else if (newlineCount > 1)
					{
						buffer.append("</p><p>");
					}

					buffer.append(c);
					newlineCount = 0;
					break;
			}
		}
		if (newlineCount == 1)
		{
			buffer.append("<br/>");
		}
		else if (newlineCount > 1)
		{
			buffer.append("</p><p>");
		}
		buffer.append("</p>");
		return buffer;
	}

	/**
	 * Converts the given object to a string.
	 * 
	 * @param object
	 *            The object
	 * @return The string
	 */
	public static String toString(final Object object)
	{
		if (object == null)
		{
			return null;
		}
		else if (object instanceof Throwable)
		{
			return toString((Throwable)object);
		}
		else
		{
			return object.toString();
		}
	}

	/**
	 * Converts a Throwable to a string.
	 * 
	 * @param throwable
	 *            The throwable
	 * @return The string
	 */
	public static String toString(final Throwable throwable)
	{
		if (throwable != null)
		{
			ArrayList<Throwable> al = new ArrayList<Throwable>();
			Throwable cause = throwable;
			al.add(cause);
			while (cause.getCause() != null && cause != cause.getCause())
			{
				cause = cause.getCause();
				al.add(cause);
			}

			AppendingStringBuffer sb = new AppendingStringBuffer(256);
			// first print the last cause
			int length = al.size() - 1;
			cause = al.get(length);
			if (throwable instanceof WicketRuntimeException)
			{
				sb.append("WicketMessage: ");
				sb.append(throwable.getMessage());
				sb.append("\n\n");
			}
			sb.append("Root cause:\n\n");
			outputThrowable(cause, sb, false);

			if (length > 0)
			{
				sb.append("\n\nComplete stack:\n\n");
				for (int i = 0; i < length; i++)
				{
					outputThrowable(al.get(i), sb, true);
					sb.append("\n");
				}
			}
			return sb.toString();
		}
		else
		{
			return "<Null Throwable>";
		}
	}

	private static void append(AppendingStringBuffer buffer, CharSequence s, int from, int to)
	{
		if (s instanceof AppendingStringBuffer)
		{
			AppendingStringBuffer asb = (AppendingStringBuffer)s;
			buffer.append(asb.getValue(), from, to - from);
		}
		else if (s instanceof StringBuffer)
		{
			buffer.append((StringBuffer)s, from, to - from);
		}
		else
		{
			buffer.append(s.subSequence(from, to));
		}
	}

	/**
	 * Outputs the throwable and its stacktrace to the stringbuffer. If
	 * stopAtWicketSerlvet is true then the output will stop when the wicket
	 * servlet is reached. sun.reflect. packages are filtered out.
	 * 
	 * @param cause
	 * @param sb
	 * @param stopAtWicketServlet
	 */
	private static void outputThrowable(Throwable cause, AppendingStringBuffer sb,
			boolean stopAtWicketServlet)
	{
		sb.append(cause);
		sb.append("\n");
		StackTraceElement[] trace = cause.getStackTrace();
		for (int i = 0; i < trace.length; i++)
		{
			String traceString = trace[i].toString();
			if (!(traceString.startsWith("sun.reflect.") && i > 1))
			{
				sb.append("     at ");
				sb.append(traceString);
				sb.append("\n");
				if (stopAtWicketServlet
						&& traceString.startsWith("wicket.protocol.http.WicketServlet"))
				{
					return;
				}
			}
		}
	}

	private static int search(final CharSequence s, String searchString, int pos)
	{
		int matchIndex = -1;
		if (s instanceof String)
		{
			matchIndex = ((String)s).indexOf(searchString, pos);
		}
		else if (s instanceof StringBuffer)
		{
			matchIndex = ((StringBuffer)s).indexOf(searchString, pos);
		}
		else if (s instanceof AppendingStringBuffer)
		{
			matchIndex = ((AppendingStringBuffer)s).indexOf(searchString, pos);
		}
		else
		{
			matchIndex = s.toString().indexOf(searchString);
		}
		return matchIndex;
	}

	/**
	 * Convert a nibble to a hex character
	 * 
	 * @param nibble
	 *            the nibble to convert.
	 * @return hex character
	 */
	private static char toHex(int nibble)
	{
		return hexDigit[(nibble & 0xF)];
	}

	/**
	 * Private constructor prevents construction.
	 */
	private Strings()
	{
	}
}
