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

import wicket.util.parse.metapattern.Group;
import wicket.util.parse.metapattern.MetaPattern;

/**
 * Parses key value assignment statements like "foo=bar".
 * @author Jonathan Locke
 */
public final class VariableAssignmentParser extends MetaPatternParser
{
    // Parse variable = 9 and variable = "string" syntaxes
    private static final Group key = new Group(MetaPattern.VARIABLE_NAME);

    private static final Group value = new Group(MetaPattern.STRING);

    private static final MetaPattern pattern = new MetaPattern(new MetaPattern[] {
            MetaPattern.OPTIONAL_WHITESPACE, key, MetaPattern.OPTIONAL_WHITESPACE,
            MetaPattern.EQUALS, MetaPattern.OPTIONAL_WHITESPACE, value,
            MetaPattern.OPTIONAL_WHITESPACE,});

    /**
     * Construct.
     * @param input to parse
     */
    public VariableAssignmentParser(final CharSequence input)
    {
        super(pattern, input);
    }

    /**
     * Gets the key part (eg 'foo' in 'foo=bar').
     * @return the key part
     */
    public String getKey()
    {
        return key.get(matcher);
    }

    /**
     * Gets the value part (eg 'bar' in 'foo=bar').
     * @return the value part
     */
    public String getValue()
    {
        return value.get(matcher);
    }
}

///////////////////////////////// End of File /////////////////////////////////
