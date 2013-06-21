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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

/**
 * A typesafe, mutable list of strings supporting a variety of convenient operations as well as
 * expected operations from List such as add(), size(), remove(), iterator(), get(int index) and
 * toArray(). Instances of the class are not threadsafe.
 * <p>
 * StringList objects can be constructed empty or they can be created using any of several static
 * factory methods:
 * <ul>
 * <li>valueOf(String[])
 * <li>valueOf(String)
 * <li>valueOf(Collection)
 * <li>valueOf(Object[])
 * </ul>
 * In the case of the Collection and Object[] factory methods, each Object in the collection or
 * array is converted to a String via toString() before being added to the StringList.
 * <p>
 * The tokenize() factory methods allow easy creation of StringLists via StringTokenizer. The
 * repeat() static factory method creates a StringList that repeats a given String a given number of
 * times.
 * <p>
 * The prepend() method adds a String to the beginning of the StringList. The removeLast() method
 * pops a String off the end of the list. The sort() method sorts strings in the List using
 * Collections.sort(). The class also inherits useful methods from AbstractStringList that include
 * join() methods ala Perl and a toString() method which joins the list of strings with comma
 * separators for easy viewing.
 * 
 * @author Jonathan Locke
 */
public final class StringList extends AbstractStringList
{
	private static final long serialVersionUID = 1L;

	// The underlying list of strings
	private final List<String> strings;

	// The total length of all strings in the list
	private int totalLength;

	/**
	 * Returns a list of a string repeated a given number of times.
	 * 
	 * @param count
	 *            The number of times to repeat the string
	 * @param string
	 *            The string to repeat
	 * @return The list of strings
	 */
	public static StringList repeat(final int count, final String string)
	{
		final StringList list = new StringList(count);

		for (int i = 0; i < count; i++)
		{
			list.add(string);
		}

		return list;
	}

	/**
	 * Extracts tokens from a comma and space delimited string.
	 * 
	 * @param string
	 *            The string
	 * @return The string tokens as a list
	 */
	public static StringList tokenize(final String string)
	{
		return tokenize(string, ", ");
	}

	/**
	 * Extracts tokens from a delimited string.
	 * 
	 * @param string
	 *            The string
	 * @param delimiters
	 *            The delimiters
	 * @return The string tokens as a list
	 */
	public static StringList tokenize(final String string, final String delimiters)
	{
		final StringTokenizer tokenizer = new StringTokenizer(string, delimiters);
		final StringList strings = new StringList();

		while (tokenizer.hasMoreTokens())
		{
			strings.add(tokenizer.nextToken());
		}

		return strings;
	}

	/**
	 * Converts a collection of objects into a list of string values by using the conversion methods
	 * of the StringValue class.
	 * 
	 * @param collection
	 *            The collection to add as strings
	 * @return The list
	 */
	public static StringList valueOf(final Collection<?> collection)
	{
		if (collection != null)
		{
			final StringList strings = new StringList(collection.size());

			for (Object object : collection)
			{
				strings.add(StringValue.valueOf(object));
			}

			return strings;
		}
		else
		{
			return new StringList();
		}
	}

	/**
	 * Converts an array of objects into a list of strings by using the object to string conversion
	 * method of the StringValue class.
	 * 
	 * @param objects
	 *            The objects to convert
	 * @return The list of strings
	 */
	public static StringList valueOf(final Object[] objects)
	{
		// check for null parameter
		int length = (objects == null) ? 0 : objects.length;
		final StringList strings = new StringList(length);

		for (int i = 0; i < length; i++)
		{
			strings.add(StringValue.valueOf(objects[i]));
		}

		return strings;
	}

	/**
	 * Returns a string list with just one string in it.
	 * 
	 * @param string
	 *            The string
	 * @return The list of one string
	 */
	public static StringList valueOf(final String string)
	{
		final StringList strings = new StringList();

		if (string != null)
		{
			strings.add(string);
		}

		return strings;
	}

	/**
	 * Converts a string array to a string list.
	 * 
	 * @param array
	 *            The array
	 * @return The list
	 */
	public static StringList valueOf(final String[] array)
	{
		int length = (array == null) ? 0 : array.length;
		final StringList strings = new StringList(length);

		for (int i = 0; i < length; i++)
		{
			strings.add(array[i]);
		}

		return strings;
	}

	/**
	 * Constructor.
	 */
	public StringList()
	{
		strings = new ArrayList<>();
	}

	/**
	 * Constructor.
	 * 
	 * @param size
	 *            Number of elements to preallocate
	 */
	public StringList(final int size)
	{
		strings = new ArrayList<>(size);
	}

	/**
	 * Adds a string to the back of this list.
	 * 
	 * @param string
	 *            String to add
	 */
	public void add(final String string)
	{
		// Add to list
		add(size(), string);
	}

	/**
	 * Adds the string to the stringlist at position pos.
	 * 
	 * @param pos
	 *            the position to add the string at
	 * @param string
	 *            the string to add.
	 */
	public void add(final int pos, final String string)
	{
		strings.add(pos, string == null ? "" : string);

		// Increase total length
		totalLength += string == null ? 0 : string.length();
	}

	/**
	 * Adds a string value to this list as a string.
	 * 
	 * @param value
	 *            The value to add
	 */
	public void add(final StringValue value)
	{
		add(value.toString());
	}

	/**
	 * @param string
	 *            The string to look for
	 * @return True if the list contains the string
	 */
	public boolean contains(final String string)
	{
		return strings.contains(string);
	}

	/**
	 * Gets the string at the given index.
	 * 
	 * @param index
	 *            The index
	 * @return The string at the index
	 * @throws IndexOutOfBoundsException
	 */
	@Override
	public String get(final int index)
	{
		return strings.get(index);
	}

	/**
	 * @return List value (not a copy of this list)
	 */
	public List<String> getList()
	{
		return strings;
	}

	/**
	 * Returns a typesafe iterator over this collection of strings.
	 * 
	 * @return Typesafe string iterator
	 */
	@Override
	public IStringIterator iterator()
	{
		return new IStringIterator()
		{
			private final Iterator<String> iterator = strings.iterator();

			@Override
			public boolean hasNext()
			{
				return iterator.hasNext();
			}

			@Override
			public String next()
			{
				return iterator.next();
			}
		};
	}

	/**
	 * Adds the given string to the front of the list.
	 * 
	 * @param string
	 *            The string to add
	 */
	public void prepend(final String string)
	{
		add(0, string);
	}

	/**
	 * Removes the string at the given index.
	 * 
	 * @param index
	 *            The index
	 */
	public void remove(final int index)
	{
		String string = strings.remove(index);
		totalLength = totalLength - string.length();
	}

	/**
	 * Removes the last string in this list.
	 */
	public void removeLast()
	{
		remove(size() - 1);
	}

	/**
	 * @return The number of strings in this list.
	 */
	@Override
	public int size()
	{
		return strings.size();
	}

	/**
	 * Sorts this string list alphabetically.
	 */
	public void sort()
	{
		Collections.sort(strings);
	}

	/**
	 * Converts this string list to a string array.
	 * 
	 * @return The string array
	 */
	@Override
	public String[] toArray()
	{
		return strings.toArray(new String[size()]);
	}

	/**
	 * @return The total length of all strings in this list.
	 */
	@Override
	public int totalLength()
	{
		return totalLength;
	}
}
