/*
 * $Id: AbstractTimeValue.java 1115 2005-02-22 17:48:25 +0000 (Tue, 22 Feb 2005)
 * jonathanlocke $ $Revision$ $Date: 2005-02-22 17:48:25 +0000 (Tue, 22
 * Feb 2005) $
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
package wicket.util.time;

import wicket.util.value.LongValue;

/**
 * Package local class for representing immutable time values in milliseconds
 * and typical operations on such values.
 * 
 * @author Jonathan Locke
 */
abstract class AbstractTimeValue extends LongValue
{
	/**
	 * Package local constructor for package subclasses only
	 * 
	 * @param milliseconds
	 *            The number of milliseconds in this time value
	 */
	AbstractTimeValue(final long milliseconds)
	{
		super(milliseconds);
	}

	/**
	 * @return Number of milliseconds in this abstract time value
	 */
	public final long getMilliseconds()
	{
		return value;
	}
}
