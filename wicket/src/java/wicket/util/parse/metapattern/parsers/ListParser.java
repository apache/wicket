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


import java.util.ArrayList;
import java.util.List;

import wicket.util.parse.metapattern.Group;
import wicket.util.parse.metapattern.MetaPattern;

/**
 * Parses an arbitrary list format with a pattern for list entries and a pattern for list
 * separators.
 * @author Jonathan Locke W. Locke
 */
public class ListParser extends MetaPatternParser
{
    private final Group entryGroup;

    private final MetaPattern separatorPattern;

    private final List values = new ArrayList();

    /**
     * Construct.
     * @param entryPattern
     * @param separatorPattern
     * @param input
     */
    public ListParser(final MetaPattern entryPattern, final MetaPattern separatorPattern,
            final CharSequence input)
    {
        super(input);
        this.entryGroup = new Group(entryPattern);
        this.separatorPattern = separatorPattern;
    }

    /**
     * @see wicket.util.parse.metapattern.parsers.MetaPatternParser#matches()
     */
    public final boolean matches()
    {
        if (advance(entryGroup))
        {
            final String value = entryGroup.get(matcher);

            values.add(value);

            while (true)
            {
                if (advance(separatorPattern) && advance(entryGroup))
                {
                    values.add(entryGroup.get(matcher));
                }
                else
                {
                    return false;
                }
            }
        }

        return false;
    }

    /**
     * Gets the parsed values.
     * @return the parsed values
     */
    public final List getValues()
    {
        return values;
    }
}

///////////////////////////////// End of File /////////////////////////////////
