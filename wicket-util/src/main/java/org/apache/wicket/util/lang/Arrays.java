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

import java.lang.reflect.Array;

/**
 * {@code Arrays} contains static methods which operate on arrays. This code is taken from the
 * Apache Harmony JDK, licensed under the Apache Software License 2.0.
 * 
 * @since 1.2
 * @deprecated Use {@link java.util.Arrays} instead
 */
@Deprecated
public class Arrays
{
	/**
	 * Copies specified number of elements in original array to a new array. The padding value whose
	 * index is bigger than or equal to original.length is null.
	 * 
	 * @param <T>
	 *            type of element in array
	 * 
	 * @param original
	 *            the original array
	 * @param newLength
	 *            the length of copied array
	 * @return the new array
	 * @throws NegativeArraySizeException
	 *             if the newLength is smaller than zero
	 * @throws NullPointerException
	 *             if the original array is null
	 * @since 1.6
	 * @deprecated Use {@link java.util.Arrays#copyOf(Object[], int)}
	 */
	@Deprecated
	public static <T> T[] copyOf(final T[] original, final int newLength)
	{
		if (null == original)
		{
			throw new NullPointerException();
		}
		if (0 <= newLength)
		{
			return copyOfRange(original, 0, newLength);
		}
		throw new NegativeArraySizeException();
	}

	/**
	 * Copies elements in original array to a new array, from index start(inclusive) to
	 * end(exclusive). The first element (if any) in the new array is original[from], and other
	 * elements in the new array are in the original order. The padding value whose index is bigger
	 * than or equal to original.length - start is null.
	 * 
	 * @param <T>
	 *            type of element in array
	 * 
	 * @param original
	 *            the original array
	 * @param start
	 *            the start index, inclusive
	 * @param end
	 *            the end index, exclusive, may bigger than length of the array
	 * @return the new copied array
	 * @throws ArrayIndexOutOfBoundsException
	 *             if start is smaller than 0 or bigger than original.length
	 * @throws IllegalArgumentException
	 *             if start is bigger than end
	 * @throws NullPointerException
	 *             if original is null
	 * @since 1.6
	 * @deprecated Use {@link Arrays#copyOfRange(Object[], int, int)}
	 */
	@Deprecated
	@SuppressWarnings("unchecked")
	public static <T> T[] copyOfRange(final T[] original, final int start, final int end)
	{
		if ((original.length >= start) && (0 <= start))
		{
			if (start <= end)
			{
				int length = end - start;
				int copyLength = Math.min(length, original.length - start);
				T[] copy = (T[])Array.newInstance(original.getClass().getComponentType(), length);
				System.arraycopy(original, start, copy, 0, copyLength);
				return copy;
			}
			throw new IllegalArgumentException();
		}
		throw new ArrayIndexOutOfBoundsException();
	}
}
