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
package org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.dynamic;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;

import org.apache.wicket.Application;
import org.apache.wicket.model.IDetachable;
import org.apache.wicket.util.convert.ConversionException;

/**
 * Supplies the rows of a {@link DynamicDataTable} and resolves a row back from its key.
 * <p>
 * The key is what ties a row in the browser to a row on the server: a click on one of its
 * actions, its selection, a repaint pushed for it. It has to stay stable for as long as the row
 * exists. In the browser the key is a string, {@link #formatKey(Serializable)}; what the browser
 * sends back is turned into a key again by {@link #parseKey(String)}, and has to be treated as
 * untrusted input: it arrives as a request parameter.
 *
 * @param <T>
 *            the type of the rows
 * @param <K>
 *            the type of the keys
 * @since 11.0.0
 */
public interface IDynamicDataProvider<T, K extends Serializable> extends IDetachable
{
	/**
	 * @param first
	 *            the index of the first row to return
	 * @param count
	 *            the number of rows to return
	 * @return the rows of the requested range, in display order
	 */
	List<T> rows(long first, long count);

	/**
	 * @return the total number of rows
	 */
	long size();

	/**
	 * Returns the key of a row: {@link IIdentifiable#getId()} by default, for rows that implement
	 * {@link IIdentifiable}. Override it for other rows.
	 *
	 * @param row
	 *            a row returned by {@link #rows(long, long)}
	 * @return the key identifying the row, never {@code null}; unique within the table and the
	 *         same for as long as the row exists
	 * @throws IllegalStateException
	 *             if the row does not implement {@link IIdentifiable} and the method is not
	 *             overridden
	 */
	@SuppressWarnings("unchecked")
	default K keyOf(T row)
	{
		if (row instanceof IIdentifiable<?> identifiable)
		{
			return (K)identifiable.getId();
		}
		throw new IllegalStateException("The row " + row + " does not implement " +
			IIdentifiable.class.getSimpleName() + "; override keyOf()");
	}

	/**
	 * Resolves a row from its key.
	 *
	 * @param key
	 *            the key, possibly one of a row that no longer exists
	 * @return the row, or {@code null} if there is none for the key
	 */
	T findByKey(K key);

	/**
	 * @return the type of the keys, which {@link #parseKey(String)} converts to by default
	 */
	Class<K> getKeyType();

	/**
	 * Writes a key the way the browser shows it: as the row element's {@code data-key} and in its
	 * id {@code <tableId>-row-<key>}. Returns {@link String#valueOf(Object)} by default.
	 *
	 * @param key
	 *            the key
	 * @return the key as a string, which {@link #parseKey(String)} turns into the same key again
	 */
	default String formatKey(K key)
	{
		return String.valueOf(key);
	}

	/**
	 * Turns a key sent by the browser into a key. By default the application's converter for
	 * {@link #getKeyType()} converts it, regardless of the user's locale.
	 *
	 * @param key
	 *            the key as sent by the browser, possibly not a key at all
	 * @return the key, or {@code null} if the string is none
	 */
	default K parseKey(String key)
	{
		if (key == null)
		{
			return null;
		}
		Class<K> type = getKeyType();
		if (type == String.class)
		{
			return type.cast(key);
		}
		try
		{
			return Application.get()
				.getConverterLocator()
				.getConverter(type)
				.convertToObject(key, Locale.ROOT);
		}
		catch (ConversionException e)
		{
			return null;
		}
	}

	@Override
	default void detach()
	{
	}
}
