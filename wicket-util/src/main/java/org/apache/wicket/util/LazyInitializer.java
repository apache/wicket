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

import org.apache.wicket.util.io.IClusterable;

/**
 * An abstraction for lazy-initializing values. Guarantees only a single instance of the value is
 * created.
 * 
 * Initialized value <strong>WILL NOT</strong> be serialized, and will be recreated upon
 * de-serialization.
 * 
 * @author igor
 * @param <T>
 *            type of value
 */
public abstract class LazyInitializer<T> implements IProvider<T>, IClusterable
{
	private static final long serialVersionUID = 1L;

	private transient volatile T instance = null;

	@Override
	public T get()
	{
		if (instance == null)
		{
			synchronized (this)
			{
				if (instance == null)
				{
					instance = createInstance();
				}
			}
		}
		return instance;
	}

	/**
	 * Creates the lazy value
	 * 
	 * @return new instance of the value
	 */
	protected abstract T createInstance();
}
