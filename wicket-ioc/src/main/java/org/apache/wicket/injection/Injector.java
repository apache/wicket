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
package org.apache.wicket.injection;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.wicket.Application;
import org.apache.wicket.MetaDataKey;

/**
 * Injector scans fields of an object instance and checks if the specified
 * {@link IFieldValueFactory} can provide a value for a field; if it can, the field is set to that
 * value. Injector will ignore all non-null fields.
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public abstract class Injector
{
	private static final MetaDataKey<Injector> KEY = new MetaDataKey<Injector>()
	{
		private static final long serialVersionUID = 1L;
	};

	private final Map<ClassLoader, ConcurrentHashMap<String, Field[]>> cache = Collections.synchronizedMap(new WeakHashMap<ClassLoader, ConcurrentHashMap<String, Field[]>>());

	/**
	 * Binds current instance of the injector to the Application. After this method is called this
	 * instance of injector will be returned from subsequent calls to {@link #get()} whenever the
	 * specified application object is active in the thread.
	 * 
	 * @param application
	 */
	public void bind(Application application)
	{
		application.setMetaData(KEY, this);
	}

	/**
	 * @return Injector associated with the application instance
	 */
	public static Injector get()
	{
		return Application.get().getMetaData(KEY);
	}

	/**
	 * Injects the specified object. This method is usually implemented by delegating to
	 * {@link #inject(Object, IFieldValueFactory)} with some {@link IFieldValueFactory}
	 * 
	 * @param object
	 * 
	 * @see #inject(Object, IFieldValueFactory)
	 */
	public abstract void inject(Object object);

	/**
	 * traverse fields in the class hierarchy of the object and set their value with a locator
	 * provided by the locator factory.
	 * 
	 * @param object
	 * @param factory
	 */
	protected void inject(Object object, IFieldValueFactory factory)
	{
		final Class<?> clazz = object.getClass();

		Field[] fields = null;

		// try cache
		ConcurrentHashMap<String, Field[]> container = cache.get(clazz.getClassLoader());
		if (container != null)
		{
			fields = container.get(clazz.getName());
		}

		if (fields == null)
		{
			fields = findFields(clazz, factory);

			// write to cache
			container = new ConcurrentHashMap<String, Field[]>();
			container.put(clazz.getName(), fields);
			cache.put(clazz.getClassLoader(), container);
		}

		for (int i = 0; i < fields.length; i++)
		{
			final Field field = fields[i];

			if (!field.isAccessible())
			{
				field.setAccessible(true);
			}
			try
			{

				if (field.get(object) == null)
				{

					Object value = factory.getFieldValue(field, object);

					if (value != null)
					{
						field.set(object, value);
					}
				}
			}
			catch (IllegalArgumentException e)
			{
				throw new RuntimeException("error while injecting object [" + object.toString() +
					"] of type [" + object.getClass().getName() + "]", e);
			}
			catch (IllegalAccessException e)
			{
				throw new RuntimeException("error while injecting object [" + object.toString() +
					"] of type [" + object.getClass().getName() + "]", e);
			}
		}
	}

	/**
	 * Returns an array of fields that can be injected using the given field value factory
	 * 
	 * @param clazz
	 * @param factory
	 * @return an array of fields that can be injected using the given field value factory
	 */
	private Field[] findFields(Class<?> clazz, IFieldValueFactory factory)
	{
		List<Field> matched = new ArrayList<Field>();

		while (clazz != null)
		{
			Field[] fields = clazz.getDeclaredFields();
			for (int i = 0; i < fields.length; i++)
			{
				final Field field = fields[i];

				if (factory.supportsField(field))
				{
					matched.add(field);
				}
			}
			clazz = clazz.getSuperclass();
		}

		return matched.toArray(new Field[matched.size()]);
	}

}
