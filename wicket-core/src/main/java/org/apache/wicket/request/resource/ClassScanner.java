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
package org.apache.wicket.request.resource;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class ClassScanner
{
	private static final Logger log = LoggerFactory.getLogger(ClassScanner.class);

	private final Set<String> scannedClasses = ConcurrentHashMap.newKeySet();

	abstract boolean foundResourceReference(ResourceReference reference);

	ClassScanner()
	{
	}

	public final void clearCache()
	{
		scannedClasses.clear();
	}

	public int scanClass(final Class<?> klass)
	{
		// scanClass gets recursively called. If klass == null, than recursion stops
		if (klass == null)
		{
			return 0;
		}

		int count = 0;
		String className = klass.getName();
		if (scannedClasses.contains(className) == false)
		{
			scannedClasses.add(className);

			for (Field f : klass.getDeclaredFields())
			{
				if ((f.getModifiers() & Modifier.STATIC) == Modifier.STATIC)
				{
					f.setAccessible(true);
					try
					{
						Object value = f.get(null);
						if (value instanceof ResourceReference)
						{
							if (foundResourceReference((ResourceReference)value) == true)
							{
								count += 1;
							}
						}
					}
					catch (Exception e)
					{
						log.warn("Error accessing object property", e);
					}
				}
			}

			count += scanClass(klass.getSuperclass());
		}
		return count;
	}
}
