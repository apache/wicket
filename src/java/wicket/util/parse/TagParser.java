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
package wicket.util.parse;


import java.text.ParseException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import wicket.util.string.StringValue;

/**
 * A very simple parser to extract tagged values (like XML or HTML or some custom tag
 * scheme) from a String.
 * @author Jonathan Locke
 */
public final class TagParser
{
    private static final Pattern TAG_NAME_PATTERN =
        Pattern.compile("\\s*(\\w+)\\s*");

    private static final Pattern ATTRIBUTE_PATTERN =
        Pattern.compile("\\s*(\\w+)\\s*=\\s*(\\w+|\\\".*?\\\")\\s*");

    private int pos;

    private String input;

    private final String closeBracket;

    private final String openBracket;

    private final int openBracketLength;

    private final int closeBracketLength;

    /**
     * Constructs a simple parser for HTML tags.
     */
    public TagParser()
    {
        this("<", ">");
    }

    /**
     * Constructs a parser for a certain kind of markup-like tagging.
     * @param openBracket The open tag symbol (in HTML this is " <")
     * @param closeBracket The close tag symbol (in HTML this is ">")
     */
    public TagParser(final String openBracket, final String closeBracket)
    {
        this.openBracket = openBracket;
        this.closeBracket = closeBracket;
        openBracketLength = openBracket.length();
        closeBracketLength = closeBracket.length();
    }

    /**
     * Sets the input string to parse.
     * @param input The input string
     */
    public void setInput(final String input)
    {
        this.input = input;
        pos = 0;
    }

    /**
     * Set position in input.
     * @param pos The new position to start parsing from
     */
    public void setPos(final int pos)
    {
        this.pos = pos;
    }

    /**
     * Gets the next tag from the input string.
     * @return The extracted tag
     * @throws ParseException
     */
    public Tag nextTag() throws ParseException
    {
        return nextTag(null);
    }

    /**
     * Gets the next tag from the input string.
     * @param name Name of the tag to get, or null to get any next tag. Matching is case
     *            independent, so "a" and "A" are the same tag.
     * @return The extracted tag.
     * @throws ParseException
     */
    public Tag nextTag(final String name) throws ParseException
    {
        // Index of open tag string like "<"
        final int openBracketIndex = input.indexOf(openBracket, pos);

        // While we can find an open tag, parse the tag
        if (openBracketIndex != -1)
        {
            // Get index of closing tag and advance past the tag
            final int closeBracketIndex = input.indexOf(closeBracket, openBracketIndex);

            if (closeBracketIndex == -1)
            {
                throw new ParseException("No matching close bracket at position "
                        + openBracketIndex, pos);
            }

            // Get the tagtext between open and close brackets
            String tagText = input.substring(openBracketIndex + openBracketLength,
                    closeBracketIndex);

            // Handle comments
            if (tagText.startsWith("!--"))
            {
                // Skip ahead to -->
                pos = input.indexOf("--" + closeBracket, openBracketIndex + openBracketLength + 3);

                if (pos == -1)
                {
                    throw new ParseException("Unclosed comment beginning at " + openBracketIndex,
                            openBracketIndex);
                }
            }
            else
            {
                // Everything is an open tag by default
                boolean isOpen = true;
                boolean isClose = false;

                // If the tag ends in '/', it's an open and close tag
                if (tagText.endsWith("/"))
                {
                    isOpen = true;
                    isClose = true;
                    tagText = tagText.substring(0, tagText.length() - 1);
                }

                // If the tagtext starts with a '/', it's a simple close tag
                if (tagText.startsWith("/"))
                {
                    isClose = true;
                    isOpen = false;
                    tagText = tagText.substring(1);
                }

                // Parse remainting tagtext, obtaining a (the) tag object or
                // null if it's invalid
                final Tag tag = parseTagText(tagText);

                if (tag != null)
                {
                    // If the caller either doesn't care what the name is, or
                    // the tag has the name
                    // they asked for then return the tag
                    if ((name == null) || tag.name.equalsIgnoreCase(name))
                    {
                        // Populate tag fields
                        tag.isClose = isClose;
                        tag.isOpen = isOpen;
                        tag.pos = openBracketIndex;
                        tag.length = (closeBracketIndex + closeBracketLength) - openBracketIndex;
                        tag.getAttributes().makeImmutable();
                        tag.text = input.substring(openBracketIndex, closeBracketIndex
                                + closeBracketLength);

                        // Move to position after the tag
                        pos = closeBracketIndex + closeBracketLength;

                        // Return the tag we found!
                        return tag;
                    }
                }
                else
                {
                    throw new ParseException("Malformed tag at position " + openBracketIndex,
                            openBracketIndex);
                }
            }
        }

        // There is no next matching tag
        return null;
    }

    /**
     * Parses the text between tags. For example, "a href=foo.html".
     * @param tagText The text between tags
     * @return A new Tag object or null if the tag is invalid
     */
    private Tag parseTagText(final String tagText)
    {
        // Get the length of the tagtext
        final int tagTextLength = tagText.length();

        // If we match tagname pattern
        final Matcher tagnameMatcher = TAG_NAME_PATTERN.matcher(tagText);
        final Tag tag = new Tag();

        if (tagnameMatcher.lookingAt())
        {
            // Extract the tag from the pattern matcher
            tag.name = tagnameMatcher.group(1).toLowerCase();

            int pos = tagnameMatcher.end(0);

            // Are we at the end? Then there are no attributes, so we just
            // return the tag
            if (pos == tagTextLength)
            {
                return tag;
            }

            // Extract attributes
            final Matcher attributeMatcher = ATTRIBUTE_PATTERN.matcher(tagText);

            while (attributeMatcher.find(pos))
            {
                // Get key and value using attribute pattern
                final String key = attributeMatcher.group(1);
                String value = attributeMatcher.group(2);

                // Set new position to end of attribute
                pos = attributeMatcher.end(0);

                // Chop off double quotes
                if (value.startsWith("\""))
                {
                    value = value.substring(1, value.length() - 1);
                }

                // Put the attribute in the attributes hash
                tag.attributes.put(key.toLowerCase(), StringValue.valueOf(value));

                // The input has to match exactly (no left over junk after
                // attributes)
                if (pos == tagTextLength)
                {
                    return tag;
                }
            }
        }

        return null;
    }
}

///////////////////////////////// End of File /////////////////////////////////
