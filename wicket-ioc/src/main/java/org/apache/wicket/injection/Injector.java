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
import java.util.List;

import org.apache.wicket.Application;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.util.collections.ClassMetaCache;

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

	private final ClassMetaCache<Field[]> cache = new ClassMetaCache<>();

	/**
	 * Binds current instance of the injector to the Application. After this method is called this
	 * instance of injector will be returned from subsequent calls to {@link #get()} whenever the
	 * specified application object is active in the thread.
	 * 
	 * @param application
	 */
	public void bind(final Application application)
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
	protected void inject(final Object object, final IFieldValueFactory factory)
	{
		final Class<?> clazz = object.getClass();

		Field[] fields = null;

		// try cache
		fields = cache.get(clazz);

		if (fields == null)
		{
			// cache miss, discover fields
			fields = findFields(clazz, factory);

			// write to cache
			cache.put(clazz, fields);
		}

		for (final Field field : fields)
		{
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
	private Field[] findFields(Class<?> clazz, final IFieldValueFactory factory)
	{
		List<Field> matched = new ArrayList<>();

		while (clazz != null)
		{
			Field[] fields = clazz.getDeclaredFields();
			for (final Field field : fields)
			{
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
