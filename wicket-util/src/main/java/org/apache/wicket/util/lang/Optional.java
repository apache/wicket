/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.wicket.util.lang;

import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;

/**
 * Represents an optional value.
 * <p>
 * This class aids in making handling of possible {@code null} values (whether return values or
 * parameter values) explicit and hopefully reduces the number of {@link NullPointerException}s that
 * will be thrown.
 * </p>
 * <p>
 * It also makes tracking down NPEs easier by not allowing {@code null} return values to be
 * propagated as parameters to further function calls, it does it by making {@link #get()} throw an
 * NPE if called on an {@link Optional} constructed with a {@code null} value.
 * </p>
 * <p>
 * By supporting {@link Iterable} this class allows the developer to rewrite code like this: <code>
 * if (optional.isSet()) {
 *    optional.get().foo();
 *    optional.get().bar();
 *    optional.get().baz();
 * }
 * </code> With the following: <code>
 * for (Value value:optional) {
 *   value.foo();
 *   value.bar();
 *   value.baz();
 * }
 * </code> which some developers find easier to read.
 * </p>
 * 
 * @author igor
 * @param <T>
 */
public final class Optional<T> implements Iterable<T>, Serializable
{
	private static final long serialVersionUID = 1L;

	private static final Optional<Void> NULL = new Optional<Void>(null);

	private static final Iterator<?> EMPTY_ITERATOR = Collections.emptyList().iterator();

	private final T value;

	private Optional(T value)
	{
		this.value = value;
	}

	/**
	 * Gets the stored value or throws {@link NullPointerException} if the value is {@code null}
	 * 
	 * @throws NullPointerException
	 *             if the value is {@code null}
	 * 
	 * @return stored value
	 */
	public T get()
	{
		if (value == null)
		{
			throw new NullPointerException();
		}
		return value;
	}

	/**
	 * Gets the stored value or returns {@code defaultValue} if value is {@code null}
	 * 
	 * @param defaultValue
	 *            default value
	 * 
	 * @return stored value or {@code defaultValue}
	 */
	public T get(T defaultValue)
	{
		if (value == null)
		{
			return defaultValue;
		}
		return value;
	}

	/**
	 * @return {@code true} iff value is not {@code null}
	 */
	public boolean exists()
	{
		return value != null;
	}

	/**
	 * @return {code true} if the store value is {@code null}
	 */
	public boolean isNull()
	{
		return value == null;
	}

	/**
	 * @return {code true} if the store value is not {@code null}
	 */
	public boolean isNotNull()
	{
		return value != null;
	}

	/**
	 * @return {code true} if the store value is not {@code null}
	 */
	public boolean isSet()
	{
		return value != null;
	}

	/**
	 * @return {code true} if the store value is {@code null}
	 */
	public boolean isNotSet()
	{
		return value == null;
	}

	@SuppressWarnings("unchecked")
	public Iterator<T> iterator()
	{
		if (value == null)
		{
			return (Iterator<T>)EMPTY_ITERATOR;
		}
		else
		{
			return Collections.singleton(value).iterator();
		}
	}

	/**
	 * Factory method for creating {@link Optional} values
	 * 
	 * @param <Z>
	 * @param value
	 * @return optional that represents the specified value
	 */
	@SuppressWarnings("unchecked")
	public static <Z> Optional<Z> of(Z value)
	{
		if (value == null)
		{
			return (Optional<Z>)NULL;
		}
		else
		{
			return new Optional<Z>(value);
		}
	}

	/**
	 * Factory method for creating an {@link Optional} value that represents a {@code null}
	 * 
	 * @param <T>
	 * @return optional that represents a {@code null}
	 */
	@SuppressWarnings("unchecked")
	public static <Z> Optional<Z> ofNull()
	{
		return (Optional<Z>)NULL;
	}

	/**
	 * Checks if the stored value is the same as the {@code other} value
	 * 
	 * @param other
	 * @return {@code true} iff stored value == other
	 */
	public boolean isValueSameAs(T other)
	{
		return value == other;
	}

	/**
	 * A null-safe checks to see if the stored value is equal to the {@code other} value
	 * 
	 * @param other
	 * @return {@code true} iff stored value equals other
	 */
	public boolean isValueEqualTo(T other)
	{
		return Objects.equal(value, other);
	}


	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj == null)
		{
			return false;
		}
		if (!(obj instanceof Optional))
		{
			return false;
		}
		Optional<?> other = (Optional<?>)obj;
		return Objects.equal(value, other.value);
	}

	/**
	 * @return either the String "null" or the String "value: " followed by the contained value's
	 *         toString
	 */
	@Override
	public String toString()
	{
		return "[Optional value=" + value + "]";
	}

	/**
	 * Performs an unsafe {@link #get()}, may return {@code null}
	 * 
	 * @return stored {@code value} including {@code null}
	 */
	public T getUnsafe()
	{
		return value;
	}
}