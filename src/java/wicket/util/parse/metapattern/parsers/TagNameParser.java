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
import wicket.util.parse.metapattern.OptionalMetaPattern;

/**
 * @author Jonathan Locke
 */
public final class TagNameParser extends MetaPatternParser
{ // TODO finalize javadoc
    private static final Group namespace = new Group(MetaPattern.VARIABLE_NAME);

    private static final Group name = new Group(MetaPattern.VARIABLE_NAME);

    /** pattern for tag names with optional namespace */
    private static final MetaPattern pattern =
        	new MetaPattern( new MetaPattern[] {
        	        new OptionalMetaPattern(new MetaPattern[] {
        	                namespace, MetaPattern.COLON }),
        	        name });

    /**
     * Construct.
     * @param input to parse
     */
    public TagNameParser(final CharSequence input)
    {
        super(pattern, input);
    }

    /**
     * Gets the key part (eg 'foo' in 'foo=bar').
     * @return the key part
     */
    public String getNamespace()
    {
        return namespace.get(matcher);
    }

    /**
     * Gets the value part (eg 'bar' in 'foo=bar').
     * @return the value part
     */
    public String getName()
    {
        return name.get(matcher);
    }
}

///////////////////////////////// End of File /////////////////////////////////
