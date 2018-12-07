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
package org.apache.wicket.core.util.lang;

import java.io.Serializable;

import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.model.IDetachable;
import org.apache.wicket.serialize.ISerializer;
import org.apache.wicket.serialize.java.JavaSerializer;
import org.apache.wicket.util.io.ByteCountingOutputStream;
import org.apache.wicket.util.string.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Object (de)serialization utilities.
 */
public class WicketObjects
{
	/** log. */
	private static final Logger log = LoggerFactory.getLogger(WicketObjects.class);

	private WicketObjects()
	{
	}

	/**
	 * @param <T>
	 *            class type
	 * @param className
	 *            Class to resolve
	 * @return Resolved class
	 */
	@SuppressWarnings("unchecked")
	public static <T> Class<T> resolveClass(final String className)
	{
		Class<T> resolved = null;
		try
		{
			if (Application.exists())
			{
				resolved = (Class<T>)Application.get()
					.getApplicationSettings()
					.getClassResolver()
					.resolveClass(className);
			}

			if (resolved == null)
			{
				resolved = (Class<T>)Class.forName(className, false, Thread.currentThread()
					.getContextClassLoader());
			}
		}
		catch (ClassNotFoundException cnfx)
		{
			log.warn("Could not resolve class [" + className + "]", cnfx);
		}
		return resolved;
	}

	/**
	 * Interface that enables users to plugin the way object sizes are calculated with Wicket.
	 */
	public static interface IObjectSizeOfStrategy
	{
		/**
		 * Computes the size of an object. This typically is an estimation, not an absolute accurate
		 * size.
		 *
		 * @param object
		 *            The serializable object to compute size of
		 * @return The size of the object in bytes.
		 */
		long sizeOf(Serializable object);
	}

	/**
	 * {@link IObjectSizeOfStrategy} that works by serializing the object to an instance of
	 * {@link ByteCountingOutputStream}, which records the number of bytes written to it. Hence,
	 * this gives the size of the object as it would be serialized,including all the overhead of
	 * writing class headers etc. Not very accurate (the real memory consumption should be lower)
	 * but the best we can do in a cheap way pre JDK 5.
	 */
	public static final class SerializingObjectSizeOfStrategy implements IObjectSizeOfStrategy
	{
		@Override
		public long sizeOf(Serializable object)
		{
			if (object == null)
			{
				return 0;
			}

			ISerializer serializer = null;
			if (Application.exists())
			{
				serializer = Application.get().getFrameworkSettings().getSerializer();
			}

			if (serializer == null || serializer instanceof JavaSerializer)
			{
				// WICKET-6334 create a new instance of JavaSerializer that doesn't use custom IObjectCheckers
				serializer = new JavaSerializer(SerializingObjectSizeOfStrategy.class.getName());
			}

			byte[] serialized = serializer.serialize(object);
			int size = -1;
			if (serialized != null)
			{
				size = serialized.length;
			}
			return size;
		}

	}

	/**
	 * Strategy for calculating sizes of objects. Note: I didn't make this an application setting as
	 * we have enough of those already, and the typical way this probably would be used is that
	 * install a different one according to the JDK version used, so varying them between
	 * applications doesn't make a lot of sense.
	 */
	private static IObjectSizeOfStrategy objectSizeOfStrategy = new SerializingObjectSizeOfStrategy();

	/**
	 * Makes a deep clone of an object by serializing and deserializing it. The object must be fully
	 * serializable to be cloned. No extra debug info is gathered.
	 *
	 * @param object
	 *            The object to clone
	 * @return A deep copy of the object
	 * @see #cloneModel(Object)
	 */
	@SuppressWarnings("unchecked")
	public static <T> T cloneObject(final T object)
	{
		if (object == null)
		{
			return null;
		}
		else
		{
			ISerializer serializer = null;
			if (Application.exists())
			{
				serializer = Application.get().getFrameworkSettings().getSerializer();
			}

			if (serializer == null || serializer instanceof JavaSerializer)
			{
				// WICKET-6334 create a new instance of JavaSerializer that doesn't use custom IObjectCheckers
				serializer = new JavaSerializer(SerializingObjectSizeOfStrategy.class.getName());
			}

			byte[] serialized = serializer.serialize(object);
			if (serialized == null)
			{
				throw new IllegalStateException("A problem occurred while serializing an object. " +
						"Please check the earlier logs for more details. Problematic object: " + object);
			}
			Object deserialized = serializer.deserialize(serialized);
			return (T) deserialized;
		}
	}

	/**
	 * Creates a new instance using the current application's class resolver. Returns null if
	 * className is null.
	 *
	 * @param className
	 *            The full class name
	 * @return The new object instance
	 */
	@SuppressWarnings("unchecked")
	public static <T> T newInstance(final String className)
	{
		if (!Strings.isEmpty(className))
		{
			try
			{
				Class<?> c = WicketObjects.resolveClass(className);
				return (T) c.getDeclaredConstructor().newInstance();
			}
			catch (Exception e)
			{
				throw new WicketRuntimeException("Unable to create " + className, e);
			}
		}
		return null;
	}

	/**
	 * Sets the strategy for determining the sizes of objects.
	 *
	 * @param objectSizeOfStrategy
	 *            the strategy. Pass null to reset to the default.
	 */
	public static void setObjectSizeOfStrategy(IObjectSizeOfStrategy objectSizeOfStrategy)
	{
		if (objectSizeOfStrategy == null)
		{
			WicketObjects.objectSizeOfStrategy = new SerializingObjectSizeOfStrategy();
		}
		else
		{
			WicketObjects.objectSizeOfStrategy = objectSizeOfStrategy;
		}
		log.info("using " + objectSizeOfStrategy + " for calculating object sizes");
	}

	/**
	 * Computes the size of an object. Note that this is an estimation, never an absolute accurate
	 * size.
	 *
	 * @param object
	 *            Object to compute size of
	 * @return The size of the object in bytes
	 */
	public static long sizeof(final Serializable object)
	{
		Serializable target = object;

		if (object instanceof Component)
		{
			// clone to not detach the original component (WICKET-5013, 5014)
			Component clone = (Component) cloneObject(object);
			clone.detach();

			target = clone;
		}
		else if (object instanceof IDetachable)
		{
			// clone to not detach the original IDetachable (WICKET-5013, 5014)
			IDetachable clone = (IDetachable) cloneObject(object);
			clone.detach();

			target = clone;
		}

		return objectSizeOfStrategy.sizeOf(target);
	}
}
