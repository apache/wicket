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

import wicket.util.parse.metapattern.MetaPattern;

/**
 * Parses out strings separated by commas.
 * @author Jonathan Locke W. Locke
 */
public final class CommaSeparatedVariableParser extends ListParser
{
    /** pattern to use. */
    private static final MetaPattern patternEntry = new MetaPattern(new MetaPattern[] {
            MetaPattern.OPTIONAL_WHITESPACE, MetaPattern.STRING, MetaPattern.OPTIONAL_WHITESPACE});

    /**
     * Construct.
     * @param input to parse
     */
    public CommaSeparatedVariableParser(final CharSequence input)
    {
        super(patternEntry, MetaPattern.COMMA, input);
    }
}

///////////////////////////////// End of File /////////////////////////////////
