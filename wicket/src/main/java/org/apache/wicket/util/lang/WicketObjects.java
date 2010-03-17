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
package org.apache.wicket.util.lang;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.OutputStream;
import java.util.HashMap;

import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.ThreadContext;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.application.IClassResolver;
import org.apache.wicket.settings.IApplicationSettings;
import org.apache.wicket.util.io.ByteCountingOutputStream;
import org.apache.wicket.util.io.IObjectStreamFactory;
import org.apache.wicket.util.io.IObjectStreamFactory.DefaultObjectStreamFactory;
import org.apache.wicket.util.string.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
		if (className == null)
		{
			return null;
		}
		try
		{
			if (Application.exists())
			{
				return (Class<T>)Application.get()
					.getApplicationSettings()
					.getClassResolver()
					.resolveClass(className);
			}
			return (Class<T>)Class.forName(className);
		}
		catch (ClassNotFoundException e)
		{
			log.warn("Could not resolve class: " + className);
			return null;
		}
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
		 *            Object to compute size of
		 * @return The size of the object in bytes.
		 */
		long sizeOf(Object object);
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
		/**
		 * @see org.apache.wicket.util.lang.Objects.IObjectSizeOfStrategy#sizeOf(java.lang.Object)
		 */
		public long sizeOf(Object object)
		{
			if (object == null)
			{
				return 0;
			}
			try
			{
				final ByteCountingOutputStream out = new ByteCountingOutputStream();
				new ObjectOutputStream(out).writeObject(object);
				out.close();
				return out.size();
			}
			catch (IOException e)
			{
				if (log.isWarnEnabled())
				{
					log.warn("Unable to determine object size: " + object.toString(), e);
				}
				return -1;
			}
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
			IApplicationSettings applicationSettings = application.getApplicationSettings();
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
	 * De-serializes an object from a byte array.
	 * 
	 * @param data
	 *            The serialized object
	 * @return The object
	 */
	public static Object byteArrayToObject(final byte[] data)
	{
		ThreadContext old = ThreadContext.get(false);
		try
		{
			final ByteArrayInputStream in = new ByteArrayInputStream(data);
			ObjectInputStream ois = null;
			try
			{
				ois = objectStreamFactory.newObjectInputStream(in);
				String applicationName = (String)ois.readObject();
				if (applicationName != null && !Application.exists())
				{
					Application app = Application.get(applicationName);
					if (app != null)
					{
						ThreadContext.setApplication(app);
					}
				}
				return ois.readObject();
			}
			finally
			{
				if (ois != null)
				{
					ois.close();
				}
				in.close();
			}
		}
		catch (ClassNotFoundException e)
		{
			throw new RuntimeException("Could not deserialize object using `" +
				objectStreamFactory.getClass().getName() + "` object factory", e);
		}
		catch (IOException e)
		{
			throw new RuntimeException("Could not deserialize object using `" +
				objectStreamFactory.getClass().getName() + "` object factory", e);
		}
		finally
		{
			ThreadContext.restore(old);
		}
	}

	/**
	 * Makes a deep clone of an object by serializing and deserializing it. The object must be fully
	 * serializable to be cloned. This method will not clone wicket Components, it will just reuse
	 * those instances so that the complete component tree is not copied over only the model data.
	 * 
	 * @param object
	 *            The object to clone
	 * @return A deep copy of the object
	 */
	public static Object cloneModel(final Object object)
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
				return ois.readObject();
			}
			catch (ClassNotFoundException e)
			{
				throw new WicketRuntimeException("Internal error cloning object", e);
			}
			catch (IOException e)
			{
				throw new WicketRuntimeException("Internal error cloning object", e);
			}
		}
	}

	/**
	 * The default object stream factory to use. Keep this as a static here opposed to in
	 * Application, as the Application most likely isn't available in the threads we'll be using
	 * this with.
	 */
	private static IObjectStreamFactory objectStreamFactory = new IObjectStreamFactory.DefaultObjectStreamFactory();

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
	public static Object cloneObject(final Object object)
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
				ObjectOutputStream oos = new ObjectOutputStream(out);
				oos.writeObject(object);
				ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(
					out.toByteArray()))
				{
					// This override is required to resolve classes inside in different bundle, i.e.
					// The classes can be resolved by OSGI classresolver implementation
					@Override
					protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException,
						ClassNotFoundException
					{
						String className = desc.getName();

						try
						{
							return Class.forName(className, true, object.getClass()
								.getClassLoader());
						}
						catch (ClassNotFoundException ex1)
						{
							// ignore this exception.
							log.debug("Class not found by using objects own classloader, trying the IClassResolver");
						}


						Application application = Application.get();
						IApplicationSettings applicationSettings = application.getApplicationSettings();
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
				};
				return ois.readObject();
			}
			catch (ClassNotFoundException e)
			{
				throw new WicketRuntimeException("Internal error cloning object", e);
			}
			catch (IOException e)
			{
				throw new WicketRuntimeException("Internal error cloning object", e);
			}
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
	public static Object newInstance(final String className)
	{
		if (!Strings.isEmpty(className))
		{
			try
			{
				Class<?> c = WicketObjects.resolveClass(className);
				if (c == null)
				{
					throw new WicketRuntimeException("Unable to create " + className);
				}
				return c.newInstance();
			}
			catch (ClassCastException e)
			{
				throw new WicketRuntimeException("Unable to create " + className, e);
			}
			catch (InstantiationException e)
			{
				throw new WicketRuntimeException("Unable to create " + className, e);
			}
			catch (IllegalAccessException e)
			{
				throw new WicketRuntimeException("Unable to create " + className, e);
			}
		}
		return null;
	}

	/**
	 * Serializes an object into a byte array.
	 * 
	 * @param object
	 *            The object
	 * 
	 * @param applicationName
	 *            The name of application - required when serialization and deserialisation happen
	 *            outside thread in which application thread local is set
	 * 
	 * @return The serialized object
	 */
	public static byte[] objectToByteArray(final Object object, String applicationName)
	{
		try
		{
			final ByteArrayOutputStream out = new ByteArrayOutputStream();
			ObjectOutputStream oos = null;
			try
			{
				oos = objectStreamFactory.newObjectOutputStream(out);
				oos.writeObject(applicationName);
				oos.writeObject(object);
			}
			finally
			{
				if (oos != null)
				{
					oos.close();
				}
				out.close();
			}
			return out.toByteArray();
		}
		catch (Exception e)
		{
			log.error("Error serializing object " + object.getClass() + " [object=" + object + "]",
				e);
		}
		return null;
	}

	/**
	 * Serializes an object into a byte array.
	 * 
	 * @param object
	 *            The object
	 * @return The serialized object
	 */
	public static byte[] objectToByteArray(final Object object)
	{
		try
		{
			final ByteArrayOutputStream out = new ByteArrayOutputStream();
			ObjectOutputStream oos = null;
			try
			{
				oos = objectStreamFactory.newObjectOutputStream(out);
				if (Application.exists())
				{
					oos.writeObject(Application.get().getApplicationKey());
				}
				else
				{
					oos.writeObject(null);
				}
				oos.writeObject(object);
			}
			finally
			{
				if (oos != null)
				{
					oos.close();
				}
				out.close();
			}
			return out.toByteArray();
		}
		catch (Exception e)
		{
			log.error("Error serializing object " + object.getClass() + " [object=" + object + "]",
				e);
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
	 * Configure this utility class to use the provided {@link IObjectStreamFactory} instance.
	 * 
	 * @param objectStreamFactory
	 *            The factory instance to use. If you pass in null, the
	 *            {@link DefaultObjectStreamFactory default} will be set (again). Pass null to reset
	 *            to the default.
	 */
	public static void setObjectStreamFactory(IObjectStreamFactory objectStreamFactory)
	{
		if (objectStreamFactory == null)
		{
			WicketObjects.objectStreamFactory = new IObjectStreamFactory.DefaultObjectStreamFactory();
		}
		else
		{
			WicketObjects.objectStreamFactory = objectStreamFactory;
		}
		log.info("using " + WicketObjects.objectStreamFactory + " for creating object streams");
	}

	/**
	 * Computes the size of an object. Note that this is an estimation, never an absolute accurate
	 * size.
	 * 
	 * @param object
	 *            Object to compute size of
	 * @return The size of the object in bytes
	 */
	public static long sizeof(final Object object)
	{
		return objectSizeOfStrategy.sizeOf(object);
	}
}
