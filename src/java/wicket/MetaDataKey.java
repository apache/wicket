/*
 * $Id$
 * $Revision$
 * $Date$
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
 * Runtime type-checking key to a piece of metadata associated
 * with a Component.
 * 
 * @author Jonathan Locke
 */
public abstract class MetaDataKey
{
	private Class type;
	
	/**
	 * Constructor.
     * 
	 * @param type The type of value stored under this key
	 */
	public MetaDataKey(final Class type)
	{
		this.type = type;
	}

	/**
	 * Checks the type of the given object against the type for this
	 * metadata key.
	 * 
	 * @param object The object to check
	 * @throws InvalidMetaDataTypeException Thrown if the type of the
	 * given object does not match the type for this key.
	 */
	void checkType(final Object object)
	{
		if (object != null && object.getClass() != type)
		{
			throw new InvalidMetaDataTypeException("MetaDataKey " + getClass() + " expected " + type + ", not " + object.getClass());
		}
	}
	
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj)
	{
		return getClass().isInstance(obj);
	}
}
