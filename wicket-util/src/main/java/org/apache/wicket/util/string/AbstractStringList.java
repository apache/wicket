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
package org.apache.wicket.util.string;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * An abstract base class for string list implementations. Besides having an implementation for
 * IStringSequence (iterator(), get(int index) and size()), an AbstractStringList can be converted
 * to a String array or a List of Strings.
 * <p>
 * The total length of all Strings in the list can be determined by calling totalLength().
 * <p>
 * Strings or a subset of Strings in the list can be formatted using three join() methods:
 * <p>
 * <ul>
 * <li>join(String) Joins strings together using a given separator
 * <li>join() Joins Strings using comma as a separator
 * <li>join(int first, int last, String) Joins a sublist of strings using a given separator
 * </ul>
 * 
 * @author Jonathan Locke
 */
public abstract class AbstractStringList implements IStringSequence, Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @return String iterator
	 * @see org.apache.wicket.util.string.IStringSequence#iterator()
	 */
	@Override
	public abstract IStringIterator iterator();

	/**
	 * @return Number of strings in this string list
	 * @see org.apache.wicket.util.string.IStringSequence#size()
	 */
	@Override
	public abstract int size();

	/**
	 * @param index
	 *            The index into this string list
	 * @return The string at the given index
	 * @see org.apache.wicket.util.string.IStringSequence#get(int)
	 */
	@Override
	public abstract String get(int index);

	/**
	 * Returns this String sequence as an array of Strings. Subclasses may provide a more efficient
	 * implementation than the one provided here.
	 * 
	 * @return An array containing exactly this sequence of Strings
	 */
	public String[] toArray()
	{
		// Get number of Strings
		final int size = size();

		// Allocate array
		final String[] strings = new String[size];

		// Copy string references
		for (int i = 0; i < size; i++)
		{
			strings[i] = get(i);
		}

		return strings;
	}

	/**
	 * Returns this String sequence as an array of Strings. Subclasses may provide a more efficient
	 * implementation than the one provided here.
	 * 
	 * @return An array containing exactly this sequence of Strings
	 */
	public final List<String> toList()
	{
		// Get number of Strings
		final int size = size();

		// Allocate list of exactly the right size
		final List<String> strings = new ArrayList<String>(size);

		// Add strings to list
		for (int i = 0; i < size; i++)
		{
			strings.add(get(i));
		}

		return strings;
	}

	/**
	 * @return The total length of all Strings in this sequence.
	 */
	public int totalLength()
	{
		// Get number of Strings
		final int size = size();

		// Add strings to list
		int totalLength = 0;

		for (int i = 0; i < size; i++)
		{
			totalLength += get(i).length();
		}

		return totalLength;
	}

	/**
	 * Joins this sequence of strings using a comma separator. For example, if this sequence
	 * contains [1 2 3], the result of calling this method will be "1, 2, 3".
	 * 
	 * @return The joined String
	 */
	public final String join()
	{
		return join(", ");
	}

	/**
	 * Joins this sequence of strings using a separator
	 * 
	 * @param separator
	 *            The separator to use
	 * @return The joined String
	 */
	public final String join(final String separator)
	{
		return join(0, size(), separator);
	}

	/**
	 * Joins this sequence of strings from first index to last using a separator
	 * 
	 * @param first
	 *            The first index to use, inclusive
	 * @param last
	 *            The last index to use, exclusive
	 * @param separator
	 *            The separator to use
	 * @return The joined String
	 */
	public final String join(final int first, final int last, final String separator)
	{
		// Allocate buffer of exactly the right length
		final int length = totalLength() + (separator.length() * (Math.max(0, last - first - 1)));
		final AppendingStringBuffer buf = new AppendingStringBuffer(length);

		// Loop through indexes requested
		for (int i = first; i < last; i++)
		{
			// Add next string
			buf.append(get(i));

			// Add separator?
			if (i != (last - 1))
			{
				buf.append(separator);
			}
		}

		return buf.toString();
	}

	/**
	 * Converts this object to a string representation
	 * 
	 * @return String version of this object
	 */
	@Override
	public String toString()
	{
		return "[" + join() + "]";
	}
}
