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
package wicket.util.parse.metapattern;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Useful class for constructing readable and reusable regular expressions.
 * @author Jonathan Locke W. Locke
 */
public class MetaPattern
{
    // Regexps that are used multiple times in defining meta patterns
    private static final String _DOUBLE_QUOTED_STRING = "\"[^\"]*?\"";

    private static final String _STRING = "(?:\\w+|" + _DOUBLE_QUOTED_STRING + ")";

    private static final String _OPTIONAL_STRING = _STRING + "?";

    private static final String _VARIABLE_NAME = "[A-Za-z_][A-Za-z0-9_]*";

    private static final String _PERL_INTERPOLATION = "$\\{" + _VARIABLE_NAME + "\\}";

    // Delimiters and punctuation
    /** constant for whitespace. */
    public static final MetaPattern WHITESPACE = new MetaPattern("\\s+");

    /** constant for optional whitespace. */
    public static final MetaPattern OPTIONAL_WHITESPACE = new MetaPattern("\\s*");

    /** constant for non-word. */
    public static final MetaPattern NON_WORD = new MetaPattern("\\W+");

    /** constant for comma. */
    public static final MetaPattern COMMA = new MetaPattern(",");

    /** constant for colon. */
    public static final MetaPattern COLON = new MetaPattern(":");

    /** constant for semi colon. */
    public static final MetaPattern SEMICOLON = new MetaPattern(";");

    /** constant for slash. */
    public static final MetaPattern SLASH = new MetaPattern("/");

    /** constant for backslash. */
    public static final MetaPattern BACKSLASH = new MetaPattern("\\\\");

    /** constant for dot. */
    public static final MetaPattern DOT = new MetaPattern("\\.");

    /** constant for plus. */
    public static final MetaPattern PLUS = new MetaPattern("\\+");

    /** constant for minus. */
    public static final MetaPattern MINUS = new MetaPattern("-");

    /** constant for dash. */
    public static final MetaPattern DASH = new MetaPattern("-");

    /** constant for underscore. */
    public static final MetaPattern UNDERSCORE = new MetaPattern("_");

    /** constant for ampersand. */
    public static final MetaPattern AMPERSAND = new MetaPattern("&");

    /** constant for percent. */
    public static final MetaPattern PERCENT = new MetaPattern("");

    /** constant for dollar. */
    public static final MetaPattern DOLLAR_SIGN = new MetaPattern("$");

    /** constant for pound. */
    public static final MetaPattern POUND_SIGN = new MetaPattern("#");

    /** constant for at. */
    public static final MetaPattern AT_SIGN = new MetaPattern("@");

    /** constant for excl. */
    public static final MetaPattern EXCLAMATION_POINT = new MetaPattern("!");

    /** constant for tilde. */
    public static final MetaPattern TILDE = new MetaPattern("~");

    /** constant for equals. */
    public static final MetaPattern EQUALS = new MetaPattern("=");

    /** constant for star. */
    public static final MetaPattern STAR = new MetaPattern("\\*");

    /** constant for pipe. */
    public static final MetaPattern PIPE = new MetaPattern("\\|");

    /** constant for left paren. */
    public static final MetaPattern LEFT_PAREN = new MetaPattern("\\(");

    /** constant for right paren. */
    public static final MetaPattern RIGHT_PAREN = new MetaPattern("\\)");

    /** constant for left curly braces. */
    public static final MetaPattern LEFT_CURLY = new MetaPattern("\\{");

    /** constant for right curly braces. */
    public static final MetaPattern RIGHT_CURLY = new MetaPattern("\\}");

    /** constant for left square bracket. */
    public static final MetaPattern LEFT_SQUARE = new MetaPattern("\\[");

    /** constant for right square bracket. */
    public static final MetaPattern RIGHT_SQUARE = new MetaPattern("\\]");

    /** constant for digit. */
    public static final MetaPattern DIGIT = new MetaPattern("\\d");

    /** constant for digits. */
    public static final MetaPattern DIGITS = new MetaPattern("\\d+");

    /** constant for an integer. */
    public static final MetaPattern INTEGER = new MetaPattern("-?\\d+");

    /** constant for a positive integer. */
    public static final MetaPattern POSITIVE_INTEGER = new MetaPattern("\\d+");

    /** constant for hex digit. */
    public static final MetaPattern HEXADECIMAL_DIGIT = new MetaPattern("[0-9a-fA-F]");

    /** constant for hex digits. */
    public static final MetaPattern HEXADECIMAL_DIGITS = new MetaPattern("[0-9a-fA-F]+");

    /** constant for anything (string). */
    public static final MetaPattern ANYTHING = new MetaPattern(".*");

    /** constant for anything non-empty (string). */
    public static final MetaPattern ANYTHING_NON_EMPTY = new MetaPattern(".+");

    /** constant for a word. */
    public static final MetaPattern WORD = new MetaPattern("\\w+");

    /** constant for an optional word. */
    public static final MetaPattern OPTIONAL_WORD = new MetaPattern("\\w*");

    /** constant for a variable name. */
    public static final MetaPattern VARIABLE_NAME = new MetaPattern(_VARIABLE_NAME);

    /** constant for perl interpolation. */
    public static final MetaPattern PERL_INTERPOLATION = new MetaPattern(_PERL_INTERPOLATION);

    /** constant for a double quoted string. */
    public static final MetaPattern DOUBLE_QUOTED_STRING = new MetaPattern(_DOUBLE_QUOTED_STRING);

    /** constant for a string. */
    public static final MetaPattern STRING = new MetaPattern(_STRING);

    /** constant for an optional string. */
    public static final MetaPattern OPTIONAL_STRING = new MetaPattern(_OPTIONAL_STRING);

    // Either pattern is valid or patterns is valid, but not both
    private Pattern pattern;

    private List patterns;

    // The compiled pattern
    private Pattern compiledPattern;

    /**
     * Constructor for a simple pattern
     * @param pattern The pattern
     */
    public MetaPattern(final String pattern)
    {
        this.pattern = Pattern.compile(pattern);
    }

    /**
     * Copy constructor
     * @param pattern The meta pattern to copy
     */
    public MetaPattern(final MetaPattern pattern)
    {
        this.pattern = pattern.pattern;
        this.patterns = pattern.patterns;
        this.compiledPattern = pattern.compiledPattern;
    }

    /**
     * Constructor
     * @param patterns Array of MetaPatterns
     */
    public MetaPattern(final MetaPattern[] patterns)
    {
        this(Arrays.asList(patterns));
    }

    /**
     * Constructor
     * @param patterns List of MetaPatterns
     */
    public MetaPattern(final List patterns)
    {
        this.patterns = patterns;
    }

    /**
     * Creates a matcher
     * @param input The input to match
     * @return The matcher
     */
    public final Matcher matcher(final CharSequence input)
    {
        return matcher(input, 0);
    }

    /**
     * Creates a matcher with the given regexp compile flags. Once you call this method on
     * a MetaPattern, with a given regexp compile flag value, the pattern will be
     * compiled. Calling it again with a different value for flags will not recompile the
     * pattern.
     * @param input The input to match
     * @param flags The regexp flags to compile with
     * @return The matcher
     */
    public final Matcher matcher(final CharSequence input, final int flags)
    {
        compile(flags);

        return compiledPattern.matcher(input);
    }

    /**
     * @return Pattern with default regexp compile flags
     */
    public final Pattern pattern()
    {
        return pattern(0);
    }

    /**
     * @param flags Regexp flags
     * @return Pattern with given regexp compile flags
     */
    public final Pattern pattern(final int flags)
    {
        compile(flags);

        return compiledPattern;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        if (pattern != null)
        {
            return pattern.pattern();
        }
        else
        {
            final StringBuffer buffer = new StringBuffer();

            for (int i = 0; i < patterns.size(); i++)
            {
                buffer.append(patterns.get(i));
            }

            return buffer.toString();
        }
    }

    private void compile(final int flags)
    {
        if (compiledPattern == null)
        {
            bind(1);
            compiledPattern = Pattern.compile(toString(), flags);
        }
    }

    private int bind(int group)
    {
        if (this instanceof Group)
        {
            // System.out.println("binding " + this + " to group " + group);
            ((Group) this).bind(group++);
        }

        if (patterns != null)
        {
            for (int i = 0; i < patterns.size(); i++)
            {
                final MetaPattern pattern = (MetaPattern) patterns.get(i);

                group = pattern.bind(group);
            }
        }

        return group;
    }
}

///////////////////////////////// End of File /////////////////////////////////
