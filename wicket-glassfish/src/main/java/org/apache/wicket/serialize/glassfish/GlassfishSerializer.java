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
package org.apache.wicket.serialize.glassfish;

import com.sun.enterprise.container.common.spi.util.JavaEEObjectInputStream;
import com.sun.enterprise.container.common.spi.util.JavaEEObjectOutputStream;
import com.sun.enterprise.container.common.spi.util.JavaEEObjectStreamHandler;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import org.apache.wicket.serialize.ISerializer;
import org.apache.wicket.util.lang.Args;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Allows for Glassfish Proxy objects to be serialized and de-serialized when using CDI Works with
 * GF 3.1 GF 3.1.2 GF 4.0 Must include glassfish-embedded-all for compile only it is provided in app
 * server so don't add to the war Example Usage:
 * 
 * <pre>
 * 
 * protected void init()
 * {
 * 	super.init();
 * 	try
 * 	{
 * 		BeanManager beanManager = (BeanManager)new InitialContext().lookup(&quot;java:comp/BeanManager&quot;);
 * 
 * 		new CdiConfiguration(beanManager).setPropagation(ConversationPropagation.NONBOOKMARKABLE)
 * 			.configure(this);
 * 		getFrameworkSettings().setSerializer(
 * 			new GlassfishSerializer(getFrameworkSettings().getSerializer()));
 * 	}
 * 	catch (NamingException ne)
 * 	{
 * 		throw new RuntimeException(ne);
 * 	}
 * }
 * </pre>
 * 
 * @author jsarman
 */
public class GlassfishSerializer implements ISerializer
{

	private static final Logger LOG = LoggerFactory.getLogger(GlassfishSerializer.class);
	private ISerializer fallback;

	public GlassfishSerializer(ISerializer fallback)
	{
		Args.notNull(fallback, "Fallback Serializer");
		this.fallback = fallback;
	}

	@Override
	public Object deserialize(byte[] data) {
		try {
			ByteArrayInputStream bais = new ByteArrayInputStream(data);
			Collection<JavaEEObjectStreamHandler> handlers = new ArrayList<>();
			handlers.add(getHandler());
			JavaEEObjectInputStream q = new JavaEEObjectInputStream(bais,
					getClass().getClassLoader(), true, handlers);
			return q.readObject();
		} catch (Exception e) {
			LOG.warn(
					"Caught Exception attempting to deserialize. Falling Back\n {} ",
					e);
			return fallback.deserialize(data);
		}
	}

	@Override
	public byte[] serialize(Object object) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			Collection<JavaEEObjectStreamHandler> handlers = new ArrayList<>();
			handlers.add(getHandler());
			JavaEEObjectOutputStream oos = new JavaEEObjectOutputStream(baos,
					true, handlers);
			oos.writeObject(object);
			return baos.toByteArray();
		} catch (Exception e) {
			LOG.warn(
					"Caught Exception attempting to serialize. Falling Back\n {} ",
					e);
			return fallback.serialize(object);
		}
	}

	private JavaEEObjectStreamHandler getHandler() throws Exception
	{
		return new GlassfishStreamHandler();
	}

	public static class GlassfishStreamHandler implements JavaEEObjectStreamHandler
	{

		private Class indirectlySerializableClass;
		private Class serializableObjectFactoryClass;

		public GlassfishStreamHandler() throws ClassNotFoundException
		{

			try
			{
				indirectlySerializableClass = Class.forName("com.sun.ejb.spi.io.IndirectlySerializable");
				serializableObjectFactoryClass = Class.forName("com.sun.ejb.spi.io.SerializableObjectFactory");

			}
			catch (ClassNotFoundException cnfe)
			{
				indirectlySerializableClass = Class.forName("com.sun.enterprise.container.common.spi.util.IndirectlySerializable");
				serializableObjectFactoryClass = Class.forName("com.sun.enterprise.container.common.spi.util.SerializableObjectFactory");

			}

		}

		@Override
		public Object replaceObject(Object obj) throws IOException {
			Object result = obj;
			try {
				if (indirectlySerializableClass
						.isAssignableFrom(obj.getClass())) {
					result = indirectlySerializableClass.getMethod(
							"getSerializableObjectFactory", new Class[] {})
							.invoke(obj, new Object[] {}); 
				}
			} catch (NoSuchMethodException | SecurityException
					| IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				LOG.error("Caught Exception in replaceObject", e);
			}
			return result;
		}

		@Override
		public Object resolveObject(Object obj) throws IOException {
			Object result = obj;
			try {
				if (serializableObjectFactoryClass.isAssignableFrom(obj
						.getClass())) {
										
					result = serializableObjectFactoryClass.getMethod(
							"createObject", new Class[] {}).invoke(obj,
							new Object[] {});												
				}
			} catch (NoSuchMethodException | SecurityException
					| IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				LOG.error("Caught Exception in resolveObject", e);
			}
			return result;
		}
	}
}