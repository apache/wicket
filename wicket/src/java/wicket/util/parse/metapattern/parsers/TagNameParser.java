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
 * Parses XML tag names and attribute names which may include 
 * optional namespaces like "namespace:name" or "name".
 * Both ":name" and "namespace:" are not allowed. Both,
 * the namespace and the name have to follow nameing rules for
 * variable names (identifier).
 * 
 * @author Jonathan Locke
 * @author Juergen Donnerstag
 */
// TODO: do we need to support ":name" according to the xml spec?
public final class TagNameParser extends MetaPatternParser
{ 
    /** Namespace names must comply with variable name guidelines */
    private static final Group namespace = new Group(MetaPattern.VARIABLE_NAME);

    /** Tag names must comply with variable name guidelines */
    private static final Group name = new Group(MetaPattern.VARIABLE_NAME);

    /** pattern for tag names with optional namespace: (namespace:)?name */
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
     * Get the namespace part (eg 'html' in 'html:form') converted
     * to all lower case characters.
     * 
     * @return the namespace part. Will be null, if optonal namespace was not found
     */
    public String getNamespace()
    {
        String namespaceBuf = namespace.get(matcher());
        if (namespaceBuf != null)
        {
            namespaceBuf = namespaceBuf.toLowerCase();
        }
        
        return namespaceBuf;
    }

    /**
     * Gets the value part (eg 'form' in 'html:form' or 'form')
     * converted to all lower case characters.
     * 
     * @return the name part
     */
    public String getName()
    {
        return name.get(matcher()).toLowerCase();
    }
}

///////////////////////////////// End of File /////////////////////////////////
