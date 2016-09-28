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
 * An object that can provide instances of type {@code T}
 * 
 * @author igor.vaynberg
 * @param <T>
 * 
 * @deprecated  Use {@code java.util.function.Supplier<T>} instead
 */
public interface IProvider<T>
{
	/**
	 * Provides an instance of type {@code T}
	 * 
	 * @return instance of type {@code T}
	 */
	T get();
}
