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
package org.apache.wicket.application;

import java.lang.ref.WeakReference;

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.util.concurrent.ConcurrentReaderHashMap;

/**
 * Resolves a class by using the classloader that loaded this class.
 * 
 * @see org.apache.wicket.settings.IApplicationSettings#getClassResolver()
 * 
 * @author Juergen Donnerstag
 * @author Jonathan Locke
 */
public final class DefaultClassResolver implements IClassResolver
{
	/**
	 * Usually class loaders implement more efficent caching strategies than we
	 * could possibly do, but we experienced synchronization issue resulting in
	 * stack traces like: java.lang.LinkageError: duplicate class definition:
	 * 
	 * <pre>
	 *    wicket/examples/repeater/RepeatingPage at java.lang.ClassLoader.defineClass1(Native Method) 
	 * </pre>
	 * 
	 * This problem has gone since we synchronize the access.
	 */
	private ConcurrentReaderHashMap classes = new ConcurrentReaderHashMap();

	/**
	 * @see org.apache.wicket.application.IClassResolver#resolveClass(java.lang.String)
	 */
	public final Class resolveClass(final String classname)
	{
		try
		{
			Class clazz = null;
			WeakReference ref = (WeakReference)classes.get(classname);
			// Might be garbage-collected between getting the WeakRef and retrieving the Class from it.
			if (ref != null)
			{
				clazz = (Class)ref.get();
			}
			if (clazz == null)
			{
				synchronized (classes)
				{
					ClassLoader loader = Thread.currentThread().getContextClassLoader();
					if (loader == null)
					{
						loader = DefaultClassResolver.class.getClassLoader();
					}
					clazz = loader.loadClass(classname);
					classes.put(classname, new WeakReference(clazz));
				}
			}
			return clazz;
		}
		catch (ClassNotFoundException ex)
		{
			throw new WicketRuntimeException("Unable to load class with name: " + classname, ex);
		}
	}
}
