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

import java.util.regex.Matcher;

/**
 * A Group that captures integers (positive and negative whole numbers).
 * @author Jonathan Locke W. Locke
 */
public final class IntegerGroup extends Group
{
    /** the radix. */
    private final int radix;

    /**
     * Construct.
     */
    public IntegerGroup()
    {
        this(INTEGER);
    }

    /**
     * Construct.
     * @param pattern
     */
    public IntegerGroup(final MetaPattern pattern)
    {
        this(pattern, 10);
    }

    /**
     * Construct.
     * @param pattern
     * @param radix
     */
    public IntegerGroup(final MetaPattern pattern, final int radix)
    {
        super(pattern);
        this.radix = radix;
    }

    /**
     * Gets an int.
     * @param matcher the matcher
     * @return the int
     */
    public int getInt(final Matcher matcher)
    {
        return Integer.parseInt(get(matcher), radix);
    }

    /**
     * Gets a long.
     * @param matcher the matcher
     * @return the long
     */
    public long getLong(final Matcher matcher)
    {
        return Long.parseLong(get(matcher), radix);
    }
}

///////////////////////////////// End of File /////////////////////////////////
