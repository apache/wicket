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
package org.apache.wicket;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.wicket.util.crypt.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Pass in an interface type to the read method. The code will find all getters and setters, and use
 * their property names as keys for reading and writing to the underlying properties object.
 * 
 * @author Tim Boudreau
 */
public final class PageParametersMarshaller
{
	/** Log */
	private static final Logger log = LoggerFactory.getLogger(PageParametersMarshaller.class);

	/**
	 * Construct.
	 */
	public PageParametersMarshaller()
	{
	}

	/**
	 * Get a proxy object
	 * 
	 * @param <T>
	 * @param returnType
	 * @param data
	 * @return A new proxy object
	 */
	@SuppressWarnings("unchecked")
	public <T> T read(final Class<T> returnType, final PageParameters data)
	{
		if (!returnType.isInterface())
		{
			throw new IllegalArgumentException("Must be an interface: returnType=" +
				returnType.getName());
		}
		return (T)Proxy.newProxyInstance(returnType.getClassLoader(), new Class[] { returnType },
			new MyInvocationHandler(data));
	}

	/**
	 * 
	 */
	private static final class MyInvocationHandler implements InvocationHandler
	{
		private final PageParameters params;

		/**
		 * Construct.
		 * 
		 * @param params
		 */
		public MyInvocationHandler(final PageParameters params)
		{
			this.params = params;
		}

		/**
		 * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object,
		 *      java.lang.reflect.Method, java.lang.Object[])
		 */
		public Object invoke(final Object proxy, final Method method, final Object[] args)
			throws Throwable
		{
			try
			{
				String mname = method.getName();
				boolean isSetter = mname.startsWith("set");
				boolean isGetter = mname.startsWith("get") || mname.startsWith("is");
				Class<?> type = isSetter ? method.getParameterTypes()[0] : method.getReturnType();

				if (!isSetter && !isGetter)
				{
					return nullOrNumber(isGetter, type);
				}
				if (isSetter &&
					((method.getParameterTypes() == null) || (method.getParameterTypes().length == 0)))
				{
					return nullOrNumber(isGetter, type);
				}
				if (isGetter && method.getParameterTypes() != null &&
					method.getParameterTypes().length != 0)
				{
					return nullOrNumber(isGetter, type);
				}

				String name = stripName(method);
				for (Type t : Type.values())
				{
					if (t.match(type))
					{
						if (isGetter)
						{
							return read(params, t, name);
						}
						else
						{
							Object val = args == null ? null : args.length == 0 ? null : args[0];
							if (val == null)
							{
								params.remove(name);
							}
							else
							{
								write(params, t, name, val);
							}
						}
						break;
					}
				}
			}
			catch (RuntimeException e)
			{
				log.error("Error while reading/updating PageParamaters. ", e);
				throw e;
			}
			return null;
		}
	}

	/**
	 * @param method
	 * @return method name without set/get/is
	 */
	private static final String stripName(final Method method)
	{
		String s = method.getName();
		if (s.startsWith("get") || s.startsWith("set"))
		{
			s = s.substring(3);
		}
		else if (s.startsWith("is"))
		{
			s = s.substring(2);
		}
		StringBuilder sb = new StringBuilder(s);
		sb.setCharAt(0, Character.toLowerCase(sb.charAt(0)));
		return sb.toString();
	}

	/**
	 * 
	 * @param getter
	 * @param c
	 * @return
	 */
	private static final Object nullOrNumber(final boolean getter, final Class<?> c)
	{
		if (!getter)
		{
			return null;
		}
		if (c.isArray())
		{
			return Array.newInstance(c, 0);
		}
		for (Type t : Type.values())
		{
			if (t.match(c))
			{
				return t.noValue();
			}
		}
		return null;
	}

	/**
	 * 
	 * @param p
	 * @param type
	 * @param name
	 * @param o
	 */
	private static void write(final PageParameters p, final Type type, final String name,
		final Object o)
	{
		new PageParametersWriteStrategy().write(p, type, name, o);
	}

	/**
	 * 
	 * @param p
	 * @param t
	 * @param name
	 * @return
	 */
	private static Object read(final PageParameters p, final Type t, final String name)
	{
		return new PageParametersReadStrategy().read(p, t, name);
	}

	/**
	 * 
	 */
	private static enum Type {

		BOOLEAN, INT, STRING, LONG, DOUBLE, FLOAT, BYTE, SHORT, CHAR, BYTE_ARRAY, SERIALIZABLE;

		public boolean match(Class<?> type)
		{
			for (Class<?> c : getTypes())
			{
				if (c.isAssignableFrom(type))
				{
					return true;
				}
			}
			return false;
		}

		Set<Class<?>> types;

		public Set<Class<?>> getTypes()
		{
			if (types == null)
			{
				switch (this)
				{
					case BOOLEAN :
						types = toSet(Boolean.class, Boolean.TYPE);
						break;
					case BYTE :
						types = toSet(Byte.class, Byte.TYPE);
						break;
					case BYTE_ARRAY :
						types = toSet(new byte[0].getClass());
						break;
					case DOUBLE :
						types = toSet(Double.class, Double.TYPE);
						break;
					case FLOAT :
						types = toSet(Float.class, Float.TYPE);
						break;
					case INT :
						types = toSet(Integer.class, Integer.TYPE);
						break;
					case LONG :
						types = toSet(Long.class, Long.TYPE);
						break;
					case SHORT :
						types = toSet(Short.class, Short.TYPE);
						break;
					case STRING :
						types = toSet(String.class);
						break;
					case CHAR :
						types = toSet(Character.TYPE, Character.class);
						break;
					// Note: Serializable *must* remain the last item tested for
					// or everything will be resolved as serializable
					case SERIALIZABLE :
						types = toSet(Serializable.class);
						break;
					default :
						throw new AssertionError();
				}
			}
			return Collections.unmodifiableSet(types);
		}

		/**
		 * 
		 * @param types
		 * @return a new HashSet with all the types provided
		 */
		private Set<Class<?>> toSet(final Class<?>... types)
		{
			return new HashSet<Class<?>>(Arrays.asList(types));
		}

		/**
		 * 
		 * @return The "null" or "no" values for all types
		 */
		public Object noValue()
		{
			switch (this)
			{
				case BOOLEAN :
					return Boolean.FALSE;
				case INT :
					return -1;
				case STRING :
					return null;
				case LONG :
					return -1L;
				case DOUBLE :
					return -1D;
				case FLOAT :
					return -1F;
				case BYTE :
					return new Byte((byte)-1);
				case SHORT :
					return new Short((short)-1);
				case CHAR :
					return (char)0;
				default :
					return null;
			}
		}
	}

	/**
	 * 
	 */
	private static final class PageParametersWriteStrategy
	{
		public void write(final PageParameters p, final Type type, final String name, final Object o)
		{
			if (o == null)
			{
				p.remove(name);
				return;
			}

			switch (type)
			{
				case BOOLEAN :
				case INT :
				case STRING :
				case LONG :
				case DOUBLE :
				case FLOAT :
				case BYTE :
				case SHORT :
				case CHAR :
					p.put(name, o);
					break;
				case BYTE_ARRAY :
					byte[] bytes = (byte[])o;
					String s = bytesToString(bytes);
					p.put(name, s);
					break;
				case SERIALIZABLE :
					ByteArrayOutputStream out = new ByteArrayOutputStream(512);
					try
					{
						ObjectOutputStream oout = new ObjectOutputStream(out);
						oout.writeObject(o);
						out.close();
						String asString = bytesToString(out.toByteArray());
						p.put(name, asString);
					}
					catch (IOException ex)
					{
						log.error("Error while reading Serializable into a String", ex);
					}
					finally
					{
						try
						{
							out.close();
						}
						catch (IOException ex)
						{
							// Should normally not happen
							log.error(null, ex);
						}
					}
					break;
				default :
					throw new AssertionError(
						"Must be primitive type, byte array or serializable: " + o +
							" does not match type " + type);
			}
		}

		/**
		 * Convert a byte array into a hexadecimal string
		 * 
		 * @param bytes
		 * @return String
		 */
		static String bytesToString(byte[] bytes)
		{
			try
			{
				return new String(Base64.encodeBase64(bytes), "UTF-8");
			}
			catch (UnsupportedEncodingException ex)
			{
				log.error(
					"Error while converting UTF-8 byte[] into String. Retrying with default Locale.",
					ex);

				return new String(Base64.decodeBase64(bytes));
			}
		}
	}

	/**
	 * 
	 */
	private static final class PageParametersReadStrategy
	{
		public Object read(final PageParameters p, final Type type, final String name)
		{
			switch (type)
			{
				case BOOLEAN :
					return Boolean.valueOf(p.getAsBoolean(name, false));
				case INT :
					return new Integer(p.getInt(name, -1));
				case STRING :
					return p.getString(name, null);
				case LONG :
					return new Long(p.getLong(name, -1));
				case DOUBLE :
					return new Double(p.getDouble(name, -1));
				case FLOAT :
					return new Float(p.getAsDouble(name, -1));
				case BYTE :
					return new Byte((byte)p.getInt(name, -1));
				case SHORT :
					return new Short((short)p.getInt(name, -1));
				case CHAR :
					String s = p.getString(name);
					if (s == null || s.length() == 0)
					{
						return -1;
					}
					return s.charAt(0);
				case BYTE_ARRAY :
					String hex = p.getString(name);
					return stringToBytes(hex);
				case SERIALIZABLE :
					byte[] data = (byte[])read(p, Type.BYTE_ARRAY, name);
					if (data != null && data.length > 0)
					{
						try
						{
							ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(
								data));
							try
							{
								return in.readObject();
							}
							catch (ClassNotFoundException ex)
							{
								log.error("Error converting serialized data stream into an Object",
									ex);
							}
							finally
							{
								in.close();
							}
						}
						catch (IOException ex)
						{
							log.error("Error reading serialized data", ex);
						}
					}
					break;
				default :
					throw new AssertionError();
			}
			return null;
		}

		/**
		 * 
		 * @param s
		 * @return String converted into byte[] using UTF-8
		 */
		private static byte[] stringToBytes(final String s)
		{
			try
			{
				return Base64.decodeBase64(s.getBytes("UTF-8"));
			}
			catch (UnsupportedEncodingException ex)
			{
				// should not happen
				log.error("Error while converting String into byte[]", ex);

				return Base64.encodeBase64(s.getBytes(), false);
			}
		}
	}
}
