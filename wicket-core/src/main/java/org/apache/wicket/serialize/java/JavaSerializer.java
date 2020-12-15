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
package org.apache.wicket.serialize.java;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.OutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Objects;

import org.apache.wicket.Application;
import org.apache.wicket.ThreadContext;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.application.IClassResolver;
import org.apache.wicket.core.util.objects.checker.CheckingObjectOutputStream;
import org.apache.wicket.core.util.objects.checker.ObjectSerializationChecker;
import org.apache.wicket.serialize.ISerializer;
import org.apache.wicket.settings.ApplicationSettings;
import org.apache.wicket.util.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of {@link ISerializer} based on Java Serialization (ObjectOutputStream,
 * ObjectInputStream)
 * 
 * Requires the application key to enable serialization and deserialisation outside thread in which
 * application thread local is set
 */
public class JavaSerializer implements ISerializer
{
	private static final Logger log = LoggerFactory.getLogger(JavaSerializer.class);

	private static final StackWalker STACKWALKER;
	private static final ClassLoader PLATFORM_CLASS_LOADER;

	static {
		PrivilegedAction<StackWalker> pa1 =
				() -> StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
		PrivilegedAction<ClassLoader> pa2 = ClassLoader::getPlatformClassLoader;
		STACKWALKER = AccessController.doPrivileged(pa1);
		PLATFORM_CLASS_LOADER = AccessController.doPrivileged(pa2);
	}


	/**
	 * The key of the application which can be used later to find the proper {@link IClassResolver}
	 */
	private final String applicationKey;

	/**
	 * Construct.
	 * 
	 * @param applicationKey
	 *      the name of the application
	 */
	public JavaSerializer(final String applicationKey)
	{
		this.applicationKey = applicationKey;
	}

	@Override
	public byte[] serialize(final Object object)
	{
		try
		{
			final ByteArrayOutputStream out = new ByteArrayOutputStream();
			ObjectOutputStream oos = null;
			try
			{
				oos = newObjectOutputStream(out);
				oos.writeObject(applicationKey);
				oos.writeObject(object);
			}
			finally
			{
				try
				{
					IOUtils.close(oos);
				}
				finally
				{
					out.close();
				}
			}
			return out.toByteArray();
		}
		catch (Exception e)
		{
			log.error("Error serializing object {} [object={}]",
			          object.getClass(), object, e);
		}
		return null;
	}

	@Override
	public Object deserialize(final byte[] data)
	{
		ThreadContext old = ThreadContext.get(false);
		final ByteArrayInputStream in = new ByteArrayInputStream(data);
		ObjectInputStream ois = null;
		try
		{
			Application oldApplication = ThreadContext.getApplication();
			try
			{
				ois = newObjectInputStream(in);
				String applicationName = (String)ois.readObject();
				if (applicationName != null)
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
				try
				{
					ThreadContext.setApplication(oldApplication);
					IOUtils.close(ois);
				}
				finally
				{
					in.close();
				}
			}
		}
		catch (ClassNotFoundException | IOException cnfx)
		{
			throw new WicketRuntimeException("Could not deserialize object from byte[]", cnfx);
		}
		finally
		{
			ThreadContext.restore(old);
		}
	}

	/**
	 * Gets a new instance of an {@link ObjectInputStream} with the provided {@link InputStream}.
	 * 
	 * @param in
	 *            The input stream that should be used for the reading
	 * @return a new object input stream instance
	 * @throws IOException
	 *             if an I/O error occurs while reading stream header
	 */
	protected ObjectInputStream newObjectInputStream(InputStream in) throws IOException
	{
		return new ClassResolverObjectInputStream(in);
	}

	/**
	 * Gets a new instance of an {@link ObjectOutputStream} with the provided {@link OutputStream}.
	 * 
	 * @param out
	 *            The output stream that should be used for the writing
	 * @return a new object output stream instance
	 * @throws IOException
	 *             if an I/O error occurs while writing stream header
	 */
	protected ObjectOutputStream newObjectOutputStream(OutputStream out) throws IOException
	{
		return new SerializationCheckerObjectOutputStream(out);
	}

	/**
	 * Extend {@link ObjectInputStream} to add framework class resolution logic.
	 */
	private static class ClassResolverObjectInputStream extends ObjectInputStream
	{
		public ClassResolverObjectInputStream(InputStream in) throws IOException
		{
			super(in);
		}

		// This override is required to resolve classes inside in different bundle, i.e.
		// The classes can be resolved by OSGI classresolver implementation
		@Override
		protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException,
			ClassNotFoundException
		{
			try
			{
				return super.resolveClass(desc);
			}
			catch (ClassNotFoundException cnfEx)
			{
				// ignore this exception.
				log.debug(
					"Class not found by the object outputstream itself, trying the IClassResolver");

				Class< ? > candidate = resolveClassInWicket(desc.getName());
				if (candidate == null)
				{
					throw cnfEx;
				}
				return candidate;
			}
		}

		/*
		 * resolves a class by name, first using the default Class.forName, but looking in the
		 * Wicket ClassResolvers as well.
		 */
		private Class<?> resolveClassByName(String className, ClassLoader latestUserDefined)
			throws ClassNotFoundException
		{
			try
			{
				return Class.forName(className, false, latestUserDefined);
			}
			catch (ClassNotFoundException cnfEx)
			{
				Class<?> ret = resolveClassInWicket(className);
				if (ret == null)
					throw cnfEx;
				return ret;
			}
		}

		/*
		 * Resolves a class from Wicket's ClassResolver
		 */
		private Class<?> resolveClassInWicket(String className) throws ClassNotFoundException
		{
			Class<?> candidate;
			try
			{
				Application application = Application.get();
				ApplicationSettings applicationSettings = application.getApplicationSettings();
				IClassResolver classResolver = applicationSettings.getClassResolver();

				candidate = classResolver.resolveClass(className);
			}
			catch (WicketRuntimeException ex)
			{
				if (ex.getCause() instanceof ClassNotFoundException)
				{
					throw (ClassNotFoundException)ex.getCause();
				}
				else
				{
					ClassNotFoundException wrapperCnf = new ClassNotFoundException();
					wrapperCnf.initCause(ex);
					throw wrapperCnf;
				}
			}
			return candidate;
		}

		/*
		 * This method is an a copy of the super-method, with Class.forName replaced with a call to
		 * resolveClassByName.
		 */
		@Override
		protected Class<?> resolveProxyClass(String[] interfaces)
			throws ClassNotFoundException, IOException
		{
			try
			{
				return super.resolveProxyClass(interfaces);
			}
			catch (ClassNotFoundException cnfEx)
			{
				// ignore this exception.
				log.debug(
					"Proxy Class not found by the ObjectOutputStream itself, trying the IClassResolver");

				ClassLoader latestLoader = latestUserDefinedLoader();
				ClassLoader nonPublicLoader = null;
				boolean hasNonPublicInterface = false;

				// define proxy in class loader of non-public interface(s), if any
				Class<?>[] classObjs = new Class<?>[interfaces.length];
				for (int i = 0; i < interfaces.length; i++)
				{
					Class<?> cl = resolveClassByName(interfaces[i], latestLoader);
					if ((cl.getModifiers() & Modifier.PUBLIC) == 0)
					{
						if (hasNonPublicInterface)
						{
							if (nonPublicLoader != cl.getClassLoader())
							{
								throw new IllegalAccessError(
									"conflicting non-public interface class loaders");
							}
						}
						else
						{
							nonPublicLoader = cl.getClassLoader();
							hasNonPublicInterface = true;
						}
					}
					classObjs[i] = cl;
				}
				try
				{
					final InvocationHandler invocationHandler = (proxy, method, args) -> null;
					final Object proxyInstance = Proxy.newProxyInstance(
							hasNonPublicInterface ? nonPublicLoader : latestLoader, classObjs, invocationHandler);
					return proxyInstance.getClass();
				}
				catch (IllegalArgumentException e)
				{
					throw new ClassNotFoundException(null, e);
				}
			}
		}

		private static ClassLoader latestUserDefinedLoader()
		{
			try
			{
				return STACKWALKER.walk(s ->
                    s.map(StackWalker.StackFrame::getDeclaringClass)
                     .map(Class::getClassLoader)
                     .filter(Objects::nonNull)
                     .filter(cl -> !PLATFORM_CLASS_LOADER.equals(cl))
                     .findFirst()
                     .orElse(PLATFORM_CLASS_LOADER));
			}
			catch (IllegalArgumentException | SecurityException e)
			{
				throw new WicketRuntimeException(e);
			}
		}
	}

	/**
	 * Write objects to the wrapped output stream and log a meaningful message for serialization
	 * problems.
	 *
	 *  <p>
	 *     Note: the checking functionality is used only if the serialization fails with NotSerializableException.
	 *     This is done so to save some CPU time to make the checks for no reason.
	 * </p>
	 */
	private static class SerializationCheckerObjectOutputStream extends ObjectOutputStream
	{
		private final OutputStream outputStream;

		private final ObjectOutputStream oos;

		private SerializationCheckerObjectOutputStream(OutputStream outputStream) throws IOException
		{
			this.outputStream = outputStream;
			oos = new ObjectOutputStream(outputStream);
		}

		@Override
		protected final void writeObjectOverride(Object obj) throws IOException
		{
			try
			{
				oos.writeObject(obj);
			}
			catch (NotSerializableException nsx)
			{
				if (CheckingObjectOutputStream.isAvailable())
				{
					try
					{
						// trigger serialization again, but this time gather some more info
						CheckingObjectOutputStream checkingObjectOutputStream =
							new CheckingObjectOutputStream(outputStream, new ObjectSerializationChecker(nsx));
						checkingObjectOutputStream.writeObject(obj);
					}
					catch (CheckingObjectOutputStream.ObjectCheckException x)
					{
						throw x;
					}
					catch (Exception x)
					{
						x.initCause(nsx);
						throw new WicketRuntimeException("A problem occurred while trying to collect debug information about not serializable object", x);
					}

					// if we get here, we didn't fail, while we should
					throw nsx;
				}
				throw nsx;
			}
			catch (Exception e)
			{
				log.error("error writing object {} : {}", obj, e.getMessage(), e);
				throw new WicketRuntimeException(e);
			}
		}

		@Override
		public void flush() throws IOException
		{
			oos.flush();
		}

		@Override
		public void close() throws IOException
		{
			oos.close();
		}
	}
}
