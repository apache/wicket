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
package wicket.util.io;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sun.misc.Unsafe;
import sun.reflect.ReflectionFactory;

/**
 * TODO DOC ME!
 * 
 * @author jcompagner
 */
public final class ClassStreamHandler
{
	private static Unsafe unsafe;

	private class FieldAndIndex
	{
		Field field;
		long index;

		FieldAndIndex(Field field, long index)
		{
			this.field = field;
			this.index = index;
		}
	}

	static
	{
		try
		{
			Class[] classes = ObjectStreamClass.class.getDeclaredClasses();
			for (int i = 0; i < classes.length; i++)
			{
				if (classes[i].getName().equals("java.io.ObjectStreamClass$FieldReflector"))
				{
					Field unsafeField = classes[i].getDeclaredField("unsafe");
					unsafeField.setAccessible(true);

					unsafe = (Unsafe)unsafeField.get(null);
					break;
				}
			}
		}
		catch (Throwable e)
		{
			// e.printStackTrace()
		}
	}

	private static final ReflectionFactory reflFactory = (ReflectionFactory)AccessController
			.doPrivileged(new ReflectionFactory.GetReflectionFactoryAction());

	/**
	 * 
	 */
	public static final byte HANDLE = 1;

	/**
	 * 
	 */
	public static final byte NULL = 0;

	/**
	 * 
	 */
	public static final byte CLASS_DEF = 2;

	/**
	 * 
	 */
	public static final byte ARRAY = 3;

	/**
	 * 
	 */
	public static final byte PRIMITIVE_ARRAY = 4;

	/**
	 * 
	 */
	public static final int CLASS = 5;

	private static Map handlesClasses = new HashMap();

	private static short classCounter = 0;

	static ClassStreamHandler lookup(Class cls)
	{
		ClassStreamHandler classHandler = (ClassStreamHandler)handlesClasses.get(cls.getName());
		if (classHandler == null)
		{
			classHandler = new ClassStreamHandler(cls);
			handlesClasses.put(cls.getName(), classHandler);
			handlesClasses.put(new Short(classHandler.getClassId()), classHandler);
		}
		return classHandler;
	}

	static ClassStreamHandler lookup(short s)
	{
		ClassStreamHandler classHandler = (ClassStreamHandler)handlesClasses.get(new Short(s));
		if (classHandler == null)
		{
			throw new RuntimeException("class not found for: " + s);
		}
		return classHandler;
	}

	/**
	 * 
	 */
	private final Class clz;
	private final List fields;
	private final short classId;

	private Constructor cons;

	private Method writeObjectMethod;

	private Method readObjectMethod;

	/**
	 * Construct.
	 * 
	 * @param cls
	 * @param wicketObjectOutputStream
	 *            TODO
	 */
	public ClassStreamHandler(Class cls)
	{
		this.classId = classCounter++;
		this.clz = cls;
		this.fields = new ArrayList();
		if (cons == null)
		{
			cons = getSerializableConstructor(clz);
			if (cons == null)
			{
				throw new RuntimeException("Failed to get the constructor from clz: " + clz);
			}
		}
		writeObjectMethod = getPrivateMethod(cls, "writeObject",
				new Class[] { ObjectOutputStream.class }, Void.TYPE);
		readObjectMethod = getPrivateMethod(cls, "readObject",
				new Class[] { ObjectInputStream.class }, Void.TYPE);

		fillFields(cls);
	}

	/**
	 * @return
	 */
	public Class getStreamClass()
	{
		return clz;
	}

	/**
	 * @param cls
	 */
	private void fillFields(Class cls)
	{
		Field[] fields = cls.getDeclaredFields();
		for (int i = 0; i < fields.length; i++)
		{
			Field field = fields[i];
			field.setAccessible(true);
			if (!Modifier.isStatic(field.getModifiers())
					&& !Modifier.isTransient(field.getModifiers()))
			{
				FieldAndIndex fai = new FieldAndIndex(field, unsafe.objectFieldOffset(field));
				this.fields.add(fai);
			}
		}
		cls = cls.getSuperclass();
		if (cls != Object.class)
		{
			fillFields(cls);
		}
		return;
	}

	/**
	 * Returns non-static private method with given signature defined by given
	 * class, or null if none found. Access checks are disabled on the returned
	 * method (if any).
	 */
	private static Method getPrivateMethod(Class cl, String name, Class[] argTypes, Class returnType)
	{
		try
		{
			Method meth = cl.getDeclaredMethod(name, argTypes);
			meth.setAccessible(true);
			int mods = meth.getModifiers();
			return ((meth.getReturnType() == returnType) && ((mods & Modifier.STATIC) == 0) && ((mods & Modifier.PRIVATE) != 0))
					? meth
					: null;
		}
		catch (NoSuchMethodException ex)
		{
			return null;
		}
	}

	/**
	 * @param woos
	 * @param obj
	 * @param out
	 */
	public void writeFields(WicketObjectOutputStream woos, Object obj)
	{
		try
		{
			for (int i = 0; i < fields.size(); i++)
			{
				FieldAndIndex fai = (FieldAndIndex)fields.get(i);
				Field field = fai.field;
				Object value = field.get(obj);
				if (value instanceof Boolean)
				{
					woos.getOutputStream().writeBoolean(((Boolean)value).booleanValue());
					continue;
				}
				else if (value instanceof Byte)
				{
					woos.getOutputStream().writeByte(((Byte)value).intValue());
					continue;
				}
				else if (value instanceof Short)
				{
					woos.getOutputStream().writeShort(((Short)value).intValue());
					continue;
				}
				else if (value instanceof Character)
				{
					woos.getOutputStream().writeChar(((Character)value).charValue());
					continue;
				}
				else if (value instanceof Integer)
				{
					woos.getOutputStream().writeInt(((Integer)value).intValue());
					continue;
				}
				else if (value instanceof Long)
				{
					woos.getOutputStream().writeLong(((Long)value).intValue());
					continue;
				}
				else if (value instanceof Float)
				{
					woos.getOutputStream().writeFloat(((Float)value).floatValue());
					continue;
				}
				else if (value instanceof Double)
				{
					woos.getOutputStream().writeDouble(((Double)value).doubleValue());
					continue;
				}
				else
				{
					woos.writeObject(value);
				}
			}
		}
		catch (IllegalArgumentException ex)
		{
			throw new RuntimeException(ex);
		}
		catch (IllegalAccessException ex)
		{
			throw new RuntimeException(ex);
		}
		catch (IOException ex)
		{
			throw new RuntimeException(ex);
		}

	}

	/**
	 * @return
	 */
	public short getClassId()
	{
		return classId;
	}

	/**
	 * @return
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws IllegalArgumentException
	 */
	public Object createObject() throws IllegalArgumentException, InstantiationException,
			IllegalAccessException, InvocationTargetException
	{
		return cons.newInstance(null);
	}


	/**
	 * @param wois
	 */
	public void readFields(WicketObjectInputStream wois, Object object)
	{
		try
		{
			for (int i = 0; i < fields.size(); i++)
			{
				Object value;
				FieldAndIndex fai = (FieldAndIndex)fields.get(i);
				Field field = fai.field;
				Class cls = field.getType();
				boolean prim = cls.isPrimitive();
				if (!prim)
				{
					prim = Number.class.isAssignableFrom(cls);
				}
				if (prim)
				{
					if (cls == Boolean.class || cls == boolean.class)
					{
						value = new Boolean(wois.getInputStream().readBoolean());
						if (cls == boolean.class)
						{
							unsafe.putBoolean(object, fai.index, ((Boolean)value).booleanValue());
						}
						else
						{
							unsafe.putObject(object, fai.index, value);
						}
					}
					else if (cls == Byte.class || cls == byte.class)
					{
						value = new Byte(wois.getInputStream().readByte());
						if (cls == byte.class)
						{
							unsafe.putByte(object, fai.index, ((Byte)value).byteValue());
						}
						else
						{
							unsafe.putObject(object, fai.index, value);
						}
					}
					else if (cls == Short.class || cls == short.class)
					{
						value = new Short(wois.getInputStream().readShort());
						if (cls == short.class)
						{
							unsafe.putShort(object, fai.index, ((Short)value).byteValue());
						}
						else
						{
							unsafe.putObject(object, fai.index, value);
						}
					}
					else if (cls == Character.class || cls == char.class)
					{
						value = new Character(wois.getInputStream().readChar());
						if (cls == char.class)
						{
							unsafe.putChar(object, fai.index, ((Character)value).charValue());
						}
						else
						{
							unsafe.putObject(object, fai.index, value);
						}
					}
					else if (cls == Integer.class || cls == int.class)
					{
						value = new Integer(wois.getInputStream().readInt());
						if (cls == int.class)
						{
							unsafe.putInt(object, fai.index, ((Integer)value).intValue());
						}
						else
						{
							unsafe.putObject(object, fai.index, value);
						}
					}
					else if (cls == Long.class || cls == long.class)
					{
						value = new Long(wois.getInputStream().readLong());
						if (cls == long.class)
						{
							unsafe.putLong(object, fai.index, ((Long)value).longValue());
						}
						else
						{
							unsafe.putObject(object, fai.index, value);
						}
					}
					else if (cls == Float.class || cls == float.class)
					{
						value = new Float(wois.getInputStream().readFloat());
						if (cls == float.class)
						{
							unsafe.putFloat(object, fai.index, ((Float)value).floatValue());
						}
						else
						{
							unsafe.putObject(object, fai.index, value);
						}
					}
					else if (cls == Double.class || cls == double.class)
					{
						value = new Double(wois.getInputStream().readDouble());
						if (cls == double.class)
						{
							unsafe.putDouble(object, fai.index, ((Double)value).doubleValue());
						}
						else
						{
							unsafe.putObject(object, fai.index, value);
						}
					}
					else
					{
						throw new RuntimeException("not support prim type?? "  + cls);
					}
				}
				else
				{
					value = wois.readObject();
					unsafe.putObject(object, fai.index, value);
				}
			}
		}
		catch (IllegalArgumentException ex)
		{
			throw new RuntimeException(ex);
		}
		catch (IOException ex)
		{
			throw new RuntimeException(ex);
		}
		catch (ClassNotFoundException ex)
		{
			throw new RuntimeException(ex);
		}
	}

	private static Constructor getSerializableConstructor(Class cl)
	{
		Class initCl = cl;
		while (Serializable.class.isAssignableFrom(initCl))
		{
			if ((initCl = initCl.getSuperclass()) == null)
			{
				return null;
			}
		}
		try
		{
			Constructor cons = initCl.getDeclaredConstructor((Class[])null);
			int mods = cons.getModifiers();
			if ((mods & Modifier.PRIVATE) != 0
					|| ((mods & (Modifier.PUBLIC | Modifier.PROTECTED)) == 0 && !packageEquals(cl,
							initCl)))
			{
				return null;
			}
			cons = reflFactory.newConstructorForSerialization(cl, cons);
			cons.setAccessible(true);
			return cons;
		}
		catch (NoSuchMethodException ex)
		{
			return null;
		}
	}

	private static boolean packageEquals(Class cl1, Class cl2)
	{
		return (cl1.getClassLoader() == cl2.getClassLoader() && getPackageName(cl1).equals(
				getPackageName(cl2)));
	}

	/**
	 * Returns package name of given class.
	 */
	private static String getPackageName(Class cl)
	{
		String s = cl.getName();
		int i = s.lastIndexOf('[');
		if (i >= 0)
		{
			s = s.substring(i + 2);
		}
		i = s.lastIndexOf('.');
		return (i >= 0) ? s.substring(0, i) : "";
	}

	/**
	 * @param woos
	 * @param obj
	 * @return
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	public boolean invokeWriteMethod(WicketObjectOutputStream woos, Object obj)
	{
		if (writeObjectMethod != null)
		{
			try
			{
				writeObjectMethod.invoke(obj, new Object[] { woos });
			}
			catch (IllegalArgumentException ex)
			{
				throw new RuntimeException(ex);
			}
			catch (IllegalAccessException ex)
			{
				throw new RuntimeException(ex);
			}
			catch (InvocationTargetException ex)
			{
				throw new RuntimeException(ex);
			}
			return true;
		}
		return false;
	}

	/**
	 * @param wois
	 * @return
	 */
	public boolean invokeReadMethod(WicketObjectInputStream wois, Object obj)
	{
		if (readObjectMethod != null)
		{
			try
			{
				readObjectMethod.invoke(obj, new Object[] { wois });
			}
			catch (IllegalArgumentException ex)
			{
				throw new RuntimeException(ex);
			}
			catch (IllegalAccessException ex)
			{
				throw new RuntimeException(ex);
			}
			catch (InvocationTargetException ex)
			{
				throw new RuntimeException(ex);
			}
			return true;
		}
		return false;
	}

}