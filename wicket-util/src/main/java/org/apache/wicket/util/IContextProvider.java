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

import java.util.function.Function;

/**
 * An object that can provide instances of type {@code T} depending on context parameter of type
 * {@code C}
 * 
 * @param <T>
 *            the type of the instance
 * @param <C>
 *            the type of the context parameter
 * @deprecated Use {@link Function<C, T>} instead
 */
@Deprecated
public interface IContextProvider<T, C> extends Function<C, T>
{
	/**
	 * Provides an instance of type {@code T}
	 * 
	 * @param context
	 *            the context parameter on which the instance depends
	 * @return instance of type {@code T}
	 */
	T get(C context);

	@Override
	default T apply(C context) {
		return get(context);
	}
}
