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
package wicket.util.parse.metapattern.parsers;


import java.util.regex.Matcher;

import wicket.util.parse.metapattern.MetaPattern;

/**
 * Base class for various MetaPattern based parsers.
 * @author Jonathan Locke W. Locke
 */
public abstract class MetaPatternParser
{
    protected final CharSequence input;

    protected final int length;

    protected int pos;

    protected Matcher matcher;

    /**
     * Construct.
     * @param input to parse
     */
    public MetaPatternParser(final CharSequence input)
    {
        this.input = input;
        this.length = input.length();
    }

    /**
     * Construct.
     * @param pattern meta pattern
     * @param input to parse
     */
    public MetaPatternParser(final MetaPattern pattern, final CharSequence input)
    {
        this.input = input;
        this.length = input.length();
        this.matcher = pattern.matcher(input);
    }

    /**
     * Advance parsing.
     * @param pattern pattern
     * @return true if found, false otherwise
     */
    protected final boolean advance(final MetaPattern pattern)
    {
        final CharSequence s = input.subSequence(pos, length);

        this.matcher = pattern.matcher(s);

        if (matcher.lookingAt())
        {
            pos += matcher.end();

            return true;
        }

        return false;
    }

    /**
     * Whether the matcher matches.
     * @return whether the matcher matches
     */
    public boolean matches()
    {
        return matcher.matches();
    }

    /**
     * Gets the matcher.
     * @return the matcher
     */
    public final Matcher matcher()
    {
        return matcher;
    }

    /**
     * Whether the input is parsed.
     * @return whether the input is parsed
     */
    public final boolean atEnd()
    {
        return pos == length;
    }
}

///////////////////////////////// End of File /////////////////////////////////
