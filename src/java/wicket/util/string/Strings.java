/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket.util.string;

import org.dom4j.Node;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * A variety of string utilities
 * @author Jonathan Locke
 */
public final class Strings
{
    /**
     * Private constructor prevents construction
     */
    private Strings()
    {
    }

    /**
     * @param s The string to escape
     * @see Strings#escapeMarkup(String, boolean)
     * @return Escaped markup
     */
    public static String escapeMarkup(final String s)
    {
        return escapeMarkup(s, false);
    }

    /**
     * Encodes a markup string using HTML entities where appropriate
     * @param s The string to remove HTML from
     * @param escapeSpaces True to replace ' ' with nonbreaking space
     * @return The escaped string
     */
    public static String escapeMarkup(final String s, final boolean escapeSpaces)
    {
        if (s == null)
        {
            return null;
        }
        else
        {
            final StringBuffer buffer = new StringBuffer();

            for (int i = 0; i < s.length(); i++)
            {
                final char c = s.charAt(i);

                switch (c)
                {
                    case '\t':

                        if (escapeSpaces)
                        {
                            // Assumption is four space tabs (sorry, but that's just
                            // how it is!)
                            buffer.append("&nbsp;&nbsp;&nbsp;&nbsp;");
                        }
                        else
                        {
                            buffer.append(c);
                        }

                        break;

                    case ' ':

                        if (escapeSpaces)
                        {
                            buffer.append("&nbsp;");
                        }
                        else
                        {
                            buffer.append(c);
                        }

                        break;

                    case '<':
                        buffer.append("&lt;");

                        break;

                    case '>':
                        buffer.append("&gt;");

                        break;

                    default:
                        buffer.append(c);

                        break;
                }
            }

            return buffer.toString();
        }
    }

    /**
     * @param s String to transform
     * @return String with all single occurrences of newline replaced with &lt;BR&gt; and
     *         all multiple occurrences of newline replaced with &lt;P&gt;.
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
                case '\n':
                    newlineCount++;

                    break;

                case '\r':
                    break;

                default:

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
     * @param path A path
     * @param separator The path separator
     * @return The first component in the path
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
     * @param path A path
     * @param separator The path separator
     * @return The first component in the path
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
     * @param path A path
     * @param separator The path separator
     * @return Everything after the first component in the path
     */
    public static String afterFirstPathComponent(final String path, final char separator)
    {
        final int index = path.indexOf(separator);

        if (index == -1)
        {
            return "";
        }

        return path.substring(index + 1);
    }

    /**
     * @param path The path
     * @param c The character
     * @return Everything before the last occurrence of the given character in text. If
     *         the character cannot be found, the path itself is returned.
     */
    public static String beforeLastPathComponent(final String path, final char c)
    {
        final int index = path.lastIndexOf(c);

        if (index == -1)
        {
            return path;
        }

        return path.substring(0, index);
    }

    /**
     * @param text The string
     * @param c The character
     * @return Everything before the last occurrence of the given character in text. If
     *         the character cannot be found, an empty string is returned.
     */
    public static String beforeLast(final String text, final char c)
    {
        final int index = text.lastIndexOf(c);

        if (index == -1)
        {
            return "";
        }

        return text.substring(0, index);
    }

    /**
     * @param text The string
     * @param c The character
     * @return Everything before the first occurrence of the given character in text. If
     *         the character cannot be found, an empty string is returned.
     */
    public static String beforeFirst(final String text, final char c)
    {
        final int index = text.indexOf(c);

        if (index == -1)
        {
            return "";
        }

        return text.substring(0, index);
    }

    /**
     * @param text The string
     * @param c The character
     * @return Everything after the first occurrence of the given character in text. If
     *         the character cannot be found, an empty string is returned.
     */
    public static String afterFirst(final String text, final char c)
    {
        final int index = text.indexOf(c);

        if (index == -1)
        {
            return "";
        }

        return text.substring(index + 1);
    }

    /**
     * @param text The string
     * @param c The character
     * @return Everything after the last occurrence of the given character in text. If the
     *         character cannot be found, an empty string is returned.
     */
    public static String afterLast(final String text, final char c)
    {
        final int index = text.lastIndexOf(c);

        if (index == -1)
        {
            return "";
        }

        return text.substring(index + 1);
    }

    /**
     * @param s The string to strip
     * @param ending The ending to strip off
     * @return The stripped string or the original string if the ending did not exist
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
     * Replace all occurrences of one string replaceWith another string
     * @param text The string
     * @param searchFor The value to search for
     * @param replaceWith The value to searchFor replaceWith
     * @return The resulting string with searchFor replaced with replaceWith
     */
    public static String replaceAll(final String text, final String searchFor,
            final String replaceWith)
    {
        if (text != null)
        {
            // Go through the string
            final StringBuffer buf = new StringBuffer();
            int pos = 0;

            while (true)
            {
                // Get the next index of the string to searchFor
                // starting from the position pos
                final int matchIndex = text.indexOf(searchFor, pos);

                // If there's no match
                if (matchIndex == -1)
                {
                    // Append rest
                    buf.append(text.substring(pos));

                    break;
                }
                else
                {
                    // Found a match. Append up to the match
                    buf.append(text.substring(pos, matchIndex));

                    // Move the forward past the searchFor string
                    pos = matchIndex + searchFor.length();

                    // Add replaceWith
                    buf.append(replaceWith);
                }
            }

            return buf.toString();
        }

        return null;
    }

    /**
     * @param text String
     * @return Boolean value
     * @throws StringValueConversionException
     */
    public static boolean toBoolean(final String text) throws StringValueConversionException
    {
        if (text != null)
        {
            if (text.equalsIgnoreCase("true"))
            {
                return true;
            }

            if (text.equalsIgnoreCase("false"))
            {
                return false;
            }

            throw new StringValueConversionException("Boolean value was not 'true' or 'false'");
        }

        throw new StringValueConversionException("Boolean value was null");
    }

    /**
     * @param text String
     * @return Character value
     * @throws StringValueConversionException
     */
    public static char toChar(final String text) throws StringValueConversionException
    {
        if (text != null)
        {
            if (text.length() == 1)
            {
                return text.charAt(0);
            }
            else
            {
                throw new StringValueConversionException(
                        "Expected single character value rather than '" + text + "'");
            }
        }

        throw new StringValueConversionException("Character value was null");
    }

    /**
     * Converts the given object to a string
     * @param object The object
     * @return The string
     */
    public static String toString(final Object object)
    {
        if (object instanceof Throwable)
        {
            return toString((Throwable) object);
        }
        else if (object instanceof Node)
        {
            return ((Node) object).getText();
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
     * @param string The string
     * @return True if the string is null or ""
     */
    public static boolean isEmpty(final String string)
    {
        return (string == null) || string.trim().equals("");
    }

    /**
     * Converts a throwable to a string
     * @param throwable The throwable
     * @return The string
     */
    public static String toString(final Throwable throwable)
    {
        final StringWriter stringWriter = new StringWriter();

        throwable.printStackTrace(new PrintWriter(stringWriter));

        return stringWriter.toString().replaceAll("\t", "    ");
    }

    /**
     * Capitalizes a string
     * @param s The string
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
}

///////////////////////////////// End of File /////////////////////////////////
