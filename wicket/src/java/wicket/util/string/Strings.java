/*
 * $Id$ $Revision:
 * 1.4 $ $Date$
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
package wicket.util.string;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dom4j.Node;

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
    private static final Pattern htmlNumber = Pattern.compile("\\&\\#\\d+\\;");

	/**
	 * @param s
	 *            The string
	 * @param c
	 *            The character
	 * @return Everything after the first occurrence of the given character in
	 *         s. If the character cannot be found, an empty string is returned.
	 */
	public static String afterFirst(final String s, final char c)
	{
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
	 * @param s
	 *            The string
	 * @param c
	 *            The character
	 * @return Everything after the last occurrence of the given character in s.
	 *         If the character cannot be found, an empty string is returned.
	 */
	public static String afterLast(final String s, final char c)
	{
		final int index = s.lastIndexOf(c);

		if (index == -1)
		{
			return "";
		}

		return s.substring(index + 1);
	}

	/**
	 * @param s
	 *            The string
	 * @param c
	 *            The character
	 * @return Everything before the first occurrence of the given character in
	 *         s. If the character cannot be found, an empty string is returned.
	 */
	public static String beforeFirst(final String s, final char c)
	{
		final int index = s.indexOf(c);

		if (index == -1)
		{
			return "";
		}

		return s.substring(0, index);
	}

	/**
	 * @param s
	 *            The string
	 * @param c
	 *            The character
	 * @return Everything before the last occurrence of the given character in
	 *         s. If the character cannot be found, an empty string is returned.
	 */
	public static String beforeLast(final String s, final char c)
	{
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
	 * Capitalizes a string
	 * 
	 * @param s
	 *            The string
	 * @return The capitalized string
	 */
	public static String capitalize(final String s)
	{
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
	public static String escapeMarkup(final String s)
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
	public static String escapeMarkup(final String s, final boolean escapeSpaces)
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
	public static String escapeMarkup(final String s,
			final boolean escapeSpaces, final boolean convertToHtmlUnicodeEscapes)
	{
		if (s == null)
		{
			return null;
		}
		else
		{
			final StringBuffer buffer = new StringBuffer();

			int len = s.length();
			for (int i = 0; i < len; i++)
			{
				final char c = s.charAt(i);

				switch (c)
				{
					case '\t' :
						if (escapeSpaces)
						{
							// Assumption is four space tabs (sorry, but that's
							// just
							// how it is!)
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

					case '&':

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

					case '"':
					    buffer.append("&quot;");
					    break;

					case '\'':
					    buffer.append("&#039;");
					    break;

					default :

						if (convertToHtmlUnicodeEscapes)
						{
			                int ci = 0xffff & c;
			                if (ci < 160 )
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

			return buffer.toString();
		}
	}

	/**
	 * Replace HTML numbers like &#20540 by the appropriate character.
	 * 
	 * @param str The text to be evaluated
	 * @return The text with "numbers" replaced
	 */
	public static String replaceHtmlEscapeNumber(String str)
	{
		Matcher matcher = htmlNumber.matcher(str);
		while (matcher.find())
		{
		    int pos = matcher.start();
		    int end = matcher.end();
		    int number = Integer.parseInt(str.substring(pos+2, end-1));
		    char ch = (char)number;
		    str = str.substring(0, pos) + ch + str.substring(end);
			matcher = htmlNumber.matcher(str);
		}
		
	    return str;
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
		final int index = path.indexOf(separator);

		if (index == -1)
		{
			return path;
		}

		return path.substring(0, index);
	}

	/**
	 * @param string
	 *            The string
	 * @return True if the string is null or ""
	 */
	public static boolean isEmpty(final String string)
	{
		return string == null || string.trim().equals("");
	}

	/**
	 * @param s
	 *            String
	 * @return Boolean value
	 * @throws StringValueConversionException
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
	 * Replace all occurrences of one string replaceWith another string
	 * 
	 * @param s
	 *            The string to process
	 * @param searchFor
	 *            The value to search for
	 * @param replaceWith
	 *            The value to searchFor replaceWith
	 * @return The resulting string with searchFor replaced with replaceWith
	 */
	public static String replaceAll(final String s, final String searchFor, final String replaceWith)
	{
		// Look for first occurrence of searchFor
		int matchIndex = s.indexOf(searchFor);
		if (matchIndex == -1)
		{
			// No replace operation needs to happen
			return s;
		}
		else
		{
			// Allocate a StringBuffer that will hold one replacement with a
			// little extra room.
			int size = s.length();
			final int replaceWithLength = replaceWith.length();
			final int searchForLength = searchFor.length();
			if (replaceWithLength > searchForLength)
			{
				size += (replaceWithLength - searchForLength);
			}
			final StringBuffer buffer = new StringBuffer(size + 16);

			int pos = 0;
			do
			{
				// Append text up to the match
				buffer.append(s.substring(pos, matchIndex));

				// Add replaceWith text
				buffer.append(replaceWith);

				// Find next occurrence, if any
				pos = matchIndex + searchForLength;
				matchIndex = s.indexOf(searchFor, pos);
			}
			while (matchIndex != -1);

			// add tail of s
			buffer.append(s.substring(pos));

			// return processed buffer
			return buffer.toString();
		}
	}

	/**
	 * Simpler, faster version of String.split() for splitting on a simple
	 * character
	 * 
	 * @param s
	 *            The string to split
	 * @param c
	 *            The character to split on
	 * @return The array of strings
	 */
	public static String[] split(final String s, final char c)
	{
		final List strings = new ArrayList();
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
	 * @param s
	 *            The string to strip
	 * @param ending
	 *            The ending to strip off
	 * @return The stripped string or the original string if the ending did not
	 *         exist
	 */
	public static String stripEnding(final String s, final String ending)
	{
		final int index = s.lastIndexOf(ending);
		final int endpos = s.length() - ending.length();

		if (index == endpos)
		{
			return s.substring(0, endpos);
		}

		return s;
	}


	/**
	 * @param s
	 *            String
	 * @return Boolean value
	 * @throws StringValueConversionException
	 */
	public static Boolean toBoolean(final String s) throws StringValueConversionException
	{
		return Boolean.valueOf(isTrue(s));
	}

	/**
	 * @param s
	 *            String
	 * @return Character value
	 * @throws StringValueConversionException
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
	 * Converts a String to multiline HTML markup by replacing newlines with
	 * line break entities (&lt;br&gt;) and multiple occurrences of newline with
	 * paragraph break entities (&lt;p&gt;).
	 * 
	 * @param s
	 *            String to transform
	 * @return String with all single occurrences of newline replaced with
	 *         &lt;br&gt; and all multiple occurrences of newline replaced with
	 *         &lt;p&gt;.
	 */
	public static String toMultilineMarkup(final String s)
	{
		final StringBuffer buffer = new StringBuffer();
		int newlineCount = 0;

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
						buffer.append("<br>");
					}
					else if (newlineCount > 1)
					{
						buffer.append("<p>");
					}

					buffer.append(c);
					newlineCount = 0;
					break;
			}
		}

		return buffer.toString();
	}

	/**
	 * Converts the given object to a string
	 * 
	 * @param object
	 *            The object
	 * @return The string
	 */
	public static String toString(final Object object)
	{
		if (object instanceof Throwable)
		{
			return toString((Throwable)object);
		}
		else if (object instanceof Node)
		{
			return ((Node)object).getText();
		}
		else if (object == null)
		{
			return null;
		}
		else
		{
			return object.toString();
		}
	}

	/**
	 * Converts a Throwable to a string
	 * 
	 * @param throwable
	 *            The throwable
	 * @return The string
	 */
	public static String toString(final Throwable throwable)
	{
		if (throwable != null)
		{
			final StringWriter stringWriter = new StringWriter();
			throwable.printStackTrace(new PrintWriter(stringWriter));
			return Strings.replaceAll(stringWriter.toString(), "\t", "    ");
		}
		else
		{
			return "<Null Throwable>";
		}
	}

	/**
	 * Private constructor prevents construction
	 */
	private Strings()
	{
	}
}
