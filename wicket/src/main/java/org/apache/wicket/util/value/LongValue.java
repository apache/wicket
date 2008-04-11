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
 * A base class based on the Java <code>long</code> primitive for value classes that want to
 * implement standard operations on that value without the pain of aggregating a <code>Long</code>
 * object.
 * 
 * @author Jonathan Locke
 * @since 1.2.6
 */
public class LongValue implements Comparable, Serializable
{
	private static final long serialVersionUID = 1L;

	/** the <code>long</code> value */
	protected final long value;

	/**
	 * Constructor.
	 * 
	 * @param value
	 *            the <code>long</code> value
	 */
	public LongValue(final long value)
	{
		this.value = value;
	}

	/**
	 * Compares this <code>Object</code> to a given <code>Object</code>.
	 * 
	 * @param object
	 *            the <code>Object</code> to compare with
	 * @return 0 if equal, -1 if less than the given <code>Object</code>'s value, or 1 if greater
	 *         than given <code>Object</code>'s value
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
	 * Tests for equality.
	 * 
	 * @param that
	 *            the <code>Object</code> to compare with
	 * @return <code>true</code> if this <code>Object</code>'s value is equal to the given
	 *         <code>Object</code>'s value
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
	 * Compares this <code>LongValue</code> with a primitive <code>long</code> value.
	 * 
	 * @param value
	 *            the <code>long</code> value to compare with
	 * @return <code>true</code> if this <code>LongValue</code> is greater than the given
	 *         <code>long</code> value
	 */
	public final boolean greaterThan(final long value)
	{
		return this.value > value;
	}

	/**
	 * Compares this <code>LongValue</code> with another <code>LongValue</code>.
	 * 
	 * @param that
	 *            the <code>LongValue</code> to compare with
	 * @return <code>true</code> if this <code>LongValue</code> is greater than the given
	 *         <code>LongValue</code>
	 */
	public final boolean greaterThan(final LongValue that)
	{
		return this.value > that.value;
	}

	/**
	 * Returns the hash code for this <code>Object</code>.
	 * 
	 * @return hash code for this <code>Object</code>
	 */
	public final int hashCode()
	{
		return Primitives.hashCode(value);
	}

	/**
	 * Compares this <code>LongValue</code> with a primitive <code>long</code> value.
	 * 
	 * @param that
	 *            the <code>long</code> value to compare with
	 * @return <code>true</code> if this <code>LongValue</code> is less than the given
	 *         <code>long</code> value
	 */
	public final boolean lessThan(final long that)
	{
		return this.value < that;
	}

	/**
	 * Compares this <code>LongValue</code> with another <code>LongValue</code>.
	 * 
	 * @param that
	 *            the <code>LongValue</code> value to compare with
	 * @return <code>true</code> if this <code>LongValue</code> is less than the given
	 *         <code>LongValue</code>
	 */
	public final boolean lessThan(final LongValue that)
	{
		return this.value < that.value;
	}

	/**
	 * Converts this <code>LongValue</code> to a <code>String</code>.
	 * 
	 * @return a <code>String</code> representation of this <code>LongValue</code>
	 */
	public String toString()
	{
		return String.valueOf(value);
	}
}
