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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.HashMap;

import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.application.IClassResolver;
import org.apache.wicket.model.IDetachable;
import org.apache.wicket.serialize.ISerializer;
import org.apache.wicket.serialize.java.JavaSerializer;
import org.apache.wicket.settings.ApplicationSettings;
import org.apache.wicket.util.io.ByteCountingOutputStream;
import org.apache.wicket.util.lang.Generics;
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

			ISerializer serializer;
			if (Application.exists())
			{
				serializer = Application.get().getFrameworkSettings().getSerializer();
			}
			else
			{
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

	private static final class ReplaceObjectInputStream extends ObjectInputStream
	{
		private final ClassLoader classloader;
		private final HashMap<String, Component> replacedComponents;

		private ReplaceObjectInputStream(InputStream in,
			HashMap<String, Component> replacedComponents, ClassLoader classloader)
			throws IOException
		{
			super(in);
			this.replacedComponents = replacedComponents;
			this.classloader = classloader;
			enableResolveObject(true);
		}

		// This override is required to resolve classes inside in different
		// bundle, i.e.
		// The classes can be resolved by OSGI classresolver implementation
		@Override
		protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException,
			ClassNotFoundException
		{
			String className = desc.getName();

			try
			{
				return Class.forName(className, true, classloader);
			}
			catch (ClassNotFoundException ex1)
			{
				// ignore this exception.
				log.debug("Class not found by using objects own classloader, trying the IClassResolver");
			}

			Application application = Application.get();
			ApplicationSettings applicationSettings = application.getApplicationSettings();
			IClassResolver classResolver = applicationSettings.getClassResolver();

			Class<?> candidate = null;
			try
			{
				candidate = classResolver.resolveClass(className);
				if (candidate == null)
				{
					candidate = super.resolveClass(desc);
				}
			}
			catch (WicketRuntimeException ex)
			{
				if (ex.getCause() instanceof ClassNotFoundException)
				{
					throw (ClassNotFoundException)ex.getCause();
				}
			}
			return candidate;
		}

		@Override
		protected Object resolveObject(Object obj) throws IOException
		{
			Object replaced = replacedComponents.get(obj);
			if (replaced != null)
			{
				return replaced;
			}
			return super.resolveObject(obj);
		}
	}

	private static final class ReplaceObjectOutputStream extends ObjectOutputStream
	{
		private final HashMap<String, Component> replacedComponents;

		private ReplaceObjectOutputStream(OutputStream out,
			HashMap<String, Component> replacedComponents) throws IOException
		{
			super(out);
			this.replacedComponents = replacedComponents;
			enableReplaceObject(true);
		}

		@Override
		protected Object replaceObject(Object obj) throws IOException
		{
			if (obj instanceof Component)
			{
				final Component component = (Component)obj;
				String name = component.getPath();
				replacedComponents.put(name, component);
				return name;
			}
			return super.replaceObject(obj);
		}
	}

	/**
	 * Makes a deep clone of an object by serializing and deserializing it. The object must be fully
	 * serializable to be cloned. This method will not clone wicket Components, it will just reuse
	 * those instances so that the complete component tree is not copied over only the model data.
	 *
	 * <strong>Warning</strong>: this method uses Java Serialization APIs to be able to avoid cloning
	 * of {@link org.apache.wicket.Component} instances. If the application uses custom
	 * {@link org.apache.wicket.serialize.ISerializer} then most probably this method cannot be used.
	 *
	 * @param object
	 *            The object to clone
	 * @return A deep copy of the object
	 * @deprecated Use {@linkplain #cloneObject(Object)} instead
	 */
	@Deprecated
	public static <T> T cloneModel(final T object)
	{
		if (object == null)
		{
			return null;
		}
		else
		{
			try
			{
				final ByteArrayOutputStream out = new ByteArrayOutputStream(256);
				final HashMap<String, Component> replacedObjects = Generics.newHashMap();
				ObjectOutputStream oos = new ReplaceObjectOutputStream(out, replacedObjects);
				oos.writeObject(object);
				ObjectInputStream ois = new ReplaceObjectInputStream(new ByteArrayInputStream(
					out.toByteArray()), replacedObjects, object.getClass().getClassLoader());
				return (T) ois.readObject();
			}
			catch (ClassNotFoundException | IOException e)
			{
				throw new WicketRuntimeException("Internal error cloning object", e);
			}
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
	public static <T> T cloneObject(final T object)
	{
		if (object == null)
		{
			return null;
		}
		else
		{
			ISerializer serializer;
			if (Application.exists())
			{
				serializer = Application.get().getFrameworkSettings().getSerializer();
			}
			else
			{
				serializer = new JavaSerializer(WicketObjects.class.getName());
			}
			byte[] serialized = serializer.serialize(object);
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
	public static <T> T newInstance(final String className)
	{
		if (!Strings.isEmpty(className))
		{
			try
			{
				Class<?> c = WicketObjects.resolveClass(className);
				return (T) c.newInstance();
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
