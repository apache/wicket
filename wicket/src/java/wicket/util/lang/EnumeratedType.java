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
package wicket.util.lang;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import wicket.util.string.StringValue;

/**
 * A base class for defining enumerated types.
 * @author Jonathan Locke
 */
public class EnumeratedType extends StringValue
{ // TODO finalize javadoc
    // Map of type values by class
    private static final Map valueListByClass = new HashMap();

    /**
     * Constructor
     * @param name Name of this enum
     */
    public EnumeratedType(final String name)
    {
        super(name);

        final List types = getValues(getClass());

        types.add(this);
    }

    /**
     * @param c The enumerated type class to get values for
     * @return List of all type objects of the given subclass
     */
    public static List getValues(final Class c)
    {
        List valueList = (List) valueListByClass.get(c);

        if (valueList == null)
        {
            valueList = new ArrayList();
            valueListByClass.put(c, valueList);
        }

        return valueList;
    }
}

///////////////////////////////// End of File /////////////////////////////////
