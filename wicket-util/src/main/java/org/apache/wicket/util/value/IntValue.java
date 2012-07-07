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
package org.apache.wicket.util.value;

import java.io.Serializable;

import org.apache.wicket.util.lang.Primitives;


/**
 * A base class based on the Java <code>int</code> primitive for value classes that want to
 * implement standard operations on that value without the pain of aggregating an
 * <code>Integer</code> object.
 * 
 * @author Jonathan Locke
 * @since 1.2.6
 */
public class IntValue implements Comparable<IntValue>, Serializable
{
	private static final long serialVersionUID = 1L;

	/** the <code>int</code> value */
	protected final int value;

	/**
	 * Constructor.
	 * 
	 * @param value
	 *            the <code>int</code> value
	 */
	public IntValue(final int value)
	{
		this.value = value;
	}

	/**
	 * @param that
	 *            The object to compare with
	 * @return 0 if equal, -1 if less than or 1 if greater than
	 */
	@Override
	public final int compareTo(final IntValue that)
	{
		if (value < that.value)
		{
			return -1;
		}

		if (value > that.value)
		{
			return 1;
		}

		return 0;
	}

	/**
	 * Compares this <code>Object</code> to a given <code>Object</code>.
	 * 
	 * @param that
	 *            the <code>Object</code> to compare with
	 * @return 0 if equal, -1 if less than the given <code>Object</code>'s value, or 1 if greater
	 *         than given <code>Object</code>'s value
	 */
	@Override
	public final boolean equals(final Object that)
	{
		if (that instanceof IntValue)
		{
			return value == ((IntValue)that).value;
		}

		return false;
	}

	/**
	 * Compares this <code>IntValue</code> with a primitive <code>int</code> value.
	 * 
	 * @param value
	 *            the <code>int</code> value to compare with
	 * @return <code>true</code> if this <code>IntValue</code> is greater than the given
	 *         <code>int</code> value
	 */
	public final boolean greaterThan(final int value)
	{
		return this.value > value;
	}

	/**
	 * Compares this <code>IntValue</code> with another <code>IntValue</code>.
	 * 
	 * @param that
	 *            the <code>IntValue</code> to compare with
	 * @return <code>true</code> if this <code>IntValue</code> is greater than the given
	 *         <code>IntValue</code>
	 */
	public final boolean greaterThan(final IntValue that)
	{
		return value > that.value;
	}

	/**
	 * Returns the hash code for this <code>Object</code>.
	 * 
	 * @return hash code for this <code>Object</code>
	 */
	@Override
	public final int hashCode()
	{
		return Primitives.hashCode(value);
	}

	/**
	 * Compares this <code>IntValue</code> with a primitive <code>int</code> value.
	 * 
	 * @param that
	 *            the <code>int</code> value to compare with
	 * @return <code>true</code> if this <code>IntValue</code> is less than the given
	 *         <code>int</code> value
	 */
	public final boolean lessThan(final int that)
	{
		return value < that;
	}

	/**
	 * Compares this <code>IntValue</code> with another <code>IntValue</code>.
	 * 
	 * @param that
	 *            the <code>IntValue</code> to compare with
	 * @return <code>true</code> if this <code>IntValue</code> is less than the given
	 *         <code>IntValue</code>
	 */
	public final boolean lessThan(final IntValue that)
	{
		return value < that.value;
	}

	/**
	 * Converts this <code>LongValue</code> to a <code>String</code>.
	 * 
	 * @return a <code>String</code> representation of this <code>LongValue</code>
	 */
	@Override
	public String toString()
	{
		return String.valueOf(value);
	}
}
