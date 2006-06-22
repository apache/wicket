/*
 * $Id: EnumeratedType.java 5771 2006-05-19 12:04:06 +0000 (Fri, 19 May 2006)
 * joco01 $ $Revision$ $Date: 2006-05-19 12:04:06 +0000 (Fri, 19 May
 * 2006) $
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.util.lang;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import wicket.util.string.StringValue;

/**
 * A base class for defining enumerated types. Since this class extends
 * StringValue, every enumerated type subclass is a StringValue that can be
 * manipulated, converted and displayed in useful ways. In addition to
 * constructing a type with the given name, lists are kept of all enumeration
 * values by subclass. The list of available values in the enumeration
 * represented by a given subclass can be retrieved by calling getValues(Class).
 * 
 * @author Jonathan Locke
 * 
 * @deprecated To be replaced by JSE 5 constructs in Wicket 2.0; will be removed soon!
 */
@Deprecated
public abstract class EnumeratedType extends StringValue
{
	/** Map of type values by class */
	private static final Map<Class<? extends EnumeratedType>, List<EnumeratedType>> valueListByClass = new HashMap<Class<? extends EnumeratedType>, List<EnumeratedType>>();

	/**
	 * Constructor.
	 * 
	 * @param name
	 *            Name of this enumerated type value
	 */
	public EnumeratedType(final String name)
	{
		super(name);

		// Add this object to the list of values for our subclass
		getValues(getClass()).add(this);
	}

	/**
	 * Gets the enumerated type values for a given subclass of EnumeratedType.
	 * 
	 * @param c
	 *            The enumerated type subclass to get values for
	 * @return List of all values of the given subclass
	 */
	public static List<EnumeratedType> getValues(final Class<? extends EnumeratedType> c)
	{
		// Get values for class
		List<EnumeratedType> valueList = valueListByClass.get(c);

		// If no list exists
		if (valueList == null)
		{
			// create lazily
			valueList = new ArrayList<EnumeratedType>();
			valueListByClass.put(c, valueList);
		}

		return valueList;
	}
}
