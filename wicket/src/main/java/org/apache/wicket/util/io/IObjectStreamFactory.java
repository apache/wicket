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
package org.apache.wicket.util.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.OutputStream;

import org.apache.wicket.Application;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.application.IClassResolver;
import org.apache.wicket.settings.IApplicationSettings;
import org.apache.wicket.util.lang.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Interface for serializing and deserializing so that we can vary the implementation of the
 * {@link ObjectOutputStream} and {@link ObjectInputStream} implementations.
 * 
 * @see Objects#objectToByteArray(Object)
 * @see Objects#byteArrayToObject(byte[])
 * 
 * @author eelcohillenius
 */
public interface IObjectStreamFactory
{
	/**
	 * Default implementation that uses the JDK's plain implementations.
	 */
	public static final class DefaultObjectStreamFactory implements IObjectStreamFactory
	{
		private static final Logger log = LoggerFactory.getLogger(DefaultObjectStreamFactory.class);

		/**
		 * @see org.apache.wicket.util.io.IObjectStreamFactory#newObjectInputStream(java.io.InputStream)
		 */
		public ObjectInputStream newObjectInputStream(InputStream in) throws IOException
		{
			return new ObjectInputStream(in)
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
						return super.resolveClass(desc);
					}
					catch (ClassNotFoundException ex1)
					{
						// ignore this exception.
						log.debug("Class not found by the object outputstream itself, trying the IClassResolver");
					}


					Class<?> candidate = null;
					try
					{
						// Can the application always be taken??
						Application application = Application.get();
						IApplicationSettings applicationSettings = application.getApplicationSettings();
						IClassResolver classResolver = applicationSettings.getClassResolver();

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
		}

		/**
		 * @see org.apache.wicket.util.io.IObjectStreamFactory#newObjectOutputStream(java.io.OutputStream)
		 */
		public ObjectOutputStream newObjectOutputStream(final OutputStream out) throws IOException
		{
			final ObjectOutputStream oos = new ObjectOutputStream(out);
			return new ObjectOutputStream()
			{
				@Override
				protected final void writeObjectOverride(final Object obj) throws IOException
				{
					try
					{
						oos.writeObject(obj);
					}
					catch (IOException e)
					{
						if (SerializableChecker.isAvailable())
						{
							// trigger serialization again, but this time gather
							// some more info
							new SerializableChecker((NotSerializableException)e).writeObject(obj);
							// if we get here, we didn't fail, while we
							// should;
							throw e;
						}
						throw e;
					}
					catch (RuntimeException e)
					{
						log.error("error writing object " + obj + ": " + e.getMessage(), e);
						throw e;
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
			};
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
	ObjectInputStream newObjectInputStream(InputStream in) throws IOException;

	/**
	 * Gets a new instance of an {@link ObjectOutputStream} with the provided {@link OutputStream}.
	 * 
	 * @param out
	 *            The output stream that should be used for the writing
	 * @return a new object output stream instance
	 * @throws IOException
	 *             if an I/O error occurs while writing stream header
	 */
	ObjectOutputStream newObjectOutputStream(OutputStream out) throws IOException;
}
