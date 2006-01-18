/*
 * $Id$
 * $Revision$ $Date$
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
package wicket;

/**
 * A key to a piece of metadata associated with a Component at runtime. The key
 * contains type information that can be used to check the type of any metadata
 * value for the key when the value is set on the given Component. MetaDataKey
 * is abstract in order to force the creation of a subtype. That subtype is used
 * to test for identity when looking for the metadata because actual object
 * identity would suffer from problems under serialization. So, the correct way
 * to declare a MetaDataKey is like this: public static MetaDataKey ROLE = new
 * MetaDataKey(Role.class) { }
 * 
 * @author Jonathan Locke
 */
public abstract class MetaDataKey
{
	/** Type of data associated with this key */
	private Class type;

	/**
	 * Constructor.
	 * 
	 * @param type
	 *            The type of value stored under this key
	 */
	public MetaDataKey(final Class type)
	{
		this.type = type;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj)
	{
		return getClass().isInstance(obj);
	}

	/**
	 * Checks the type of the given object against the type for this metadata
	 * key.
	 * 
	 * @param object
	 *            The object to check
	 * @throws IllegalArgumentException
	 *             Thrown if the type of the given object does not match the
	 *             type for this key.
	 */
	void checkType(final Object object)
	{
		if (object != null && object.getClass() != type)
		{
			throw new IllegalArgumentException("MetaDataKey " + getClass()
					+ " requires argument of " + type + ", not " + object.getClass());
		}
	}
}
