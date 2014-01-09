/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.wicket.util.lang;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.wicket.util.string.StringValue;


/**
 * A base class for defining enumerated types. Since this class extends StringValue, every
 * enumerated type subclass is a StringValue that can be manipulated, converted and displayed in
 * useful ways. In addition to constructing a type with the given name, lists are kept of all
 * enumeration values by subclass. The list of available values in the enumeration represented by a
 * given subclass can be retrieved by calling getValues(Class).
 * 
 * @author Jonathan Locke
 */
public abstract class EnumeratedType extends StringValue
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/** Map of type values by class */
	private static final Map<String, List<EnumeratedType>> valueListByClass = Generics.newConcurrentHashMap();

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
		List<EnumeratedType> valueList = valueListByClass.get(c.getName());

		// If no list exists
		if (valueList == null)
		{
			// create lazily
			valueList = new ArrayList<>();
			valueListByClass.put(c.getName(), valueList);
		}

		return valueList;
	}

	/**
	 * Method to ensure that == works after deserialization
	 * 
	 * @return object instance
	 * @throws java.io.ObjectStreamException
	 */
	public Object readResolve() throws java.io.ObjectStreamException
	{
		EnumeratedType result = this;
		List<EnumeratedType> values = getValues(getClass());
		for (EnumeratedType value : values)
		{
			if ((value.toString() != null) && value.toString().equals(this.toString()))
			{
				result = value;
				break;
			}
		}
		return result;
	}
}
