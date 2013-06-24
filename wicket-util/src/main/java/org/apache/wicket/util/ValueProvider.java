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
package org.apache.wicket.util;

/**
 * Provider that always provides the specified value.
 * 
 * @author igor.vaynberg
 * @param <T>
 *            type of value this provider provides
 */
public class ValueProvider<T> implements IProvider<T>
{
	private final T value;

	/**
	 * Construct.
	 * 
	 * @param value
	 *            value to provide
	 */
	public ValueProvider(final T value)
	{
		this.value = value;
	}

	@Override
	public T get()
	{
		return value;
	}

	/**
	 * Creates a provider for the specified value
	 * 
	 * @param <T>
	 *            type of value
	 * @param value
	 *            value
	 * @return provider
	 */
	public static <T> ValueProvider<T> of(final T value)
	{
		return new ValueProvider<>(value);
	}
}
