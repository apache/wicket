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
package wicket.util.value;

import java.io.Serializable;

import wicket.util.lang.Primitives;

/**
 * A base class for value classes based on a Java int primitive which want to
 * implement standard operations on that value without the pain of aggregating a
 * Integer object.
 * 
 * @author Jonathan Locke
 */
public class IntValue implements Comparable, Serializable
{
	private static final long serialVersionUID = 1L;

	/** The int value */
	protected final int value;

	/**
	 * Constructor
	 * 
	 * @param value
	 *            The int value
	 */
	public IntValue(final int value)
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
		final IntValue that = (IntValue)object;

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
	@Override
	public final boolean equals(final Object that)
	{
		if (that instanceof IntValue)
		{
			return this.value == ((IntValue)that).value;
		}

		return false;
	}

	/**
	 * @param value
	 *            The value to compare against
	 * @return True if this value is greater than the given value
	 */
	public final boolean greaterThan(final int value)
	{
		return this.value > value;
	}

	/**
	 * @param that
	 *            The value to compare against
	 * @return True if this value is greater than that value
	 */
	public final boolean greaterThan(final IntValue that)
	{
		return this.value > that.value;
	}

	/**
	 * @return Hashcode for this object
	 */
	@Override
	public final int hashCode()
	{
		return Primitives.hashCode(value);
	}

	/**
	 * @param that
	 *            The value to compare against
	 * @return True if this value is less than that value
	 */
	public final boolean lessThan(final int that)
	{
		return this.value < that;
	}

	/**
	 * @param that
	 *            The value to compare against
	 * @return True if this value is less than that value
	 */
	public final boolean lessThan(final IntValue that)
	{
		return this.value < that.value;
	}

	/**
	 * Converts this to a string
	 * 
	 * @return The string for this int value
	 */
	@Override
	public String toString()
	{
		return String.valueOf(value);
	}
}
