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
package org.apache.wicket.proxy.objenesis;

import org.apache.wicket.WicketRuntimeException;

/**
 * Instantiator for Objects without default constructor.
 */
@FunctionalInterface
public interface IInstantiator
{
	/**
	 * Create a new instance.
	 * 
	 * @param type
	 *            type of instance
	 * @return instance
	 */
	public Object newInstance(Class<?> type);

	public static IInstantiator getInstantiator()
	{
		try
		{
			return new ObjenesisInstantiator();
		}
		catch (NoClassDefFoundError e)
		{
			return (type) -> {
				throw new WicketRuntimeException(String.format(
					"Can't create proxy for %s without default constructor"
						+ " - you can remedy this by adding Objenesis to your project dependencies.",
					type.getName()));
			};
		}
	}
}
