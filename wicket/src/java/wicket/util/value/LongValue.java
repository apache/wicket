/*
 * $Id$ $Revision:
 * 1.4 $ $Date$
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
package wicket.util.value;

import java.io.Serializable;

import wicket.util.lang.Primitives;

/**
 * A base class for value classes based on a Java long primitive which want to
 * implement standard operations on that value without the pain of aggregating a
 * Long object.
 * 
 * @author Jonathan Locke
 */
public class LongValue implements Comparable, Serializable
{
	private static final long serialVersionUID = 1L;
	
	/** The long value */
	protected final long value;

	/**
	 * Constructor
	 * 
	 * @param value
	 *            The long value
	 */
	public LongValue(final long value)
	{
		this.value = value;
	}

	/**
	 * @param object
	 *            The object to compare with
	 * @return 0 if equal, -1 if less than or 1 if greater than
	 */
	public final int compareTo(final Object object)
	{
		final LongValue that = (LongValue)object;

		if (this.value < that.value)
		{
			return -1;
		}

		if (this.value > that.value)
		{
			return 1;
		}

		return 0;
	}

	/**
	 * @param that
	 *            The value to compare against
	 * @return True if this value is equal to that value
	 */
	public final boolean equals(final Object that)
	{
		if (that instanceof LongValue)
		{
			return this.value == ((LongValue)that).value;
		}

		return false;
	}

	/**
	 * @param value
	 *            The value to compare against
	 * @return True if this value is greater than the given value
	 */
	public final boolean greaterThan(final long value)
	{
		return this.value > value;
	}

	/**
	 * @param that
	 *            The value to compare against
	 * @return True if this value is greater than that value
	 */
	public final boolean greaterThan(final LongValue that)
	{
		return this.value > that.value;
	}

	/**
	 * @return Hashcode for this object
	 */
	public final int hashCode()
	{
		return Primitives.hashCode(value);
	}

	/**
	 * @param that
	 *            The value to compare against
	 * @return True if this value is less than that value
	 */
	public final boolean lessThan(final long that)
	{
		return this.value < that;
	}

	/**
	 * @param that
	 *            The value to compare against
	 * @return True if this value is less than that value
	 */
	public final boolean lessThan(final LongValue that)
	{
		return this.value < that.value;
	}

	/**
	 * Converts this value to a string
	 * 
	 * @return The string for this value
	 */
	public String toString()
	{
		return String.valueOf(value);
	}
}
