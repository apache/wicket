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
import java.lang.reflect.Array;
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


	private static Map handlesClasses = new HashMap();
	
	private static short classCounter = 0;

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

	private List writeObjectMethod;

	private List readObjectMethod;


	private PrimitiveArray primitiveArray;

	/**
	 * Construct.
	 * 
	 * @param cls
	 * @param wicketObjectOutputStream
	 *            TODO
	 */
	private ClassStreamHandler(Class cls)
	{
		this.classId = classCounter++;
		this.clz = cls;
		if (!cls.isPrimitive())
		{
			this.fields = new ArrayList();
			cons = getSerializableConstructor(clz);
			if (cons == null)
			{
				throw new RuntimeException("Failed to get the constructor from clz: " + clz);
			}
			
			writeObjectMethod = new ArrayList();
			readObjectMethod = new ArrayList();
			
			Class parent = cls;
			while(parent != Object.class)
			{
				Method method = getPrivateMethod(parent, "writeObject", new Class[] { ObjectOutputStream.class }, Void.TYPE);
				if (method != null) writeObjectMethod.add(method);
				method = getPrivateMethod(parent, "readObject", new Class[] { ObjectInputStream.class }, Void.TYPE);
				if (method != null) readObjectMethod.add(method);
				
				parent = parent.getSuperclass();
			}
			fillFields(cls);
		}
		else
		{
			fields = null;
			cons = null;
			writeObjectMethod = null;
			readObjectMethod = null;
			if (clz == boolean.class)
			{
				primitiveArray = new BooleanPrimitiveArray();
			}
			else if (clz == byte.class)
			{
				primitiveArray = new BytePrimitiveArray();
			}
			else if (clz == short.class)
			{
				primitiveArray = new ShortPrimitiveArray();
			}
			else if (clz == char.class)
			{
				primitiveArray = new CharPrimitiveArray();
			}
			else if (clz == int.class)
			{
				primitiveArray = new IntPrimitiveArray();
			}
			else if (clz == long.class)
			{
				primitiveArray = new LongPrimitiveArray();
			}
			else if (clz == float.class)
			{
				primitiveArray = new FloatPrimitiveArray();
			}
			else if (clz == double.class)
			{
				primitiveArray = new DoublePrimitiveArray();
			}
		}

	}

	/**
	 * @return
	 */
	public Class getStreamClass()
	{
		return clz;
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
				FieldAndIndex fai = null;
				Class clz = field.getType();
				long offset = unsafe.objectFieldOffset(field);
				if (clz == boolean.class)
				{
					fai = new BooleanFieldAndIndex(field);
				}
				else if (clz == byte.class)
				{
					fai = new ByteFieldAndIndex(field);
				}
				else if (clz == short.class)
				{
					fai = new ShortFieldAndIndex(field);
				}
				else if (clz == char.class)
				{
					fai = new CharFieldAndIndex(field);
				}
				else if (clz == int.class)
				{
					fai = new IntFieldAndIndex(field);
				}
				else if (clz == long.class)
				{
					fai = new LongFieldAndIndex(field);
				}
				else if (clz == float.class)
				{
					fai = new FloatFieldAndIndex(field);
				}
				else if (clz == double.class)
				{
					fai = new DoubleFieldAndIndex(field);
				}
				else
				{
					fai = new ObjectFieldAndIndex(field);
				}
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
				fai.writeField(obj, woos);
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
				FieldAndIndex fai = (FieldAndIndex)fields.get(i);
				fai.readField(object, wois);
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
	
	public void writeArray(Object obj, WicketObjectOutputStream wois) throws IOException
	{
		primitiveArray.writeArray(obj,wois);
	}

	public Object readArray(WicketObjectInputStream wois) throws IOException
	{
		return primitiveArray.readArray(wois);
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
		if (writeObjectMethod.size() > 0)
		{
			for (int i = writeObjectMethod.size(); --i >= 0;)
			{
				Method method = (Method)writeObjectMethod.get(i);
				
				try
				{
					method.invoke(obj, new Object[] { woos });
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
		if (readObjectMethod.size() > 0)
		{
			for (int i = readObjectMethod.size(); --i >= 0;)
			{
				Method method = (Method)readObjectMethod.get(i);
				try
				{
					method.invoke(obj, new Object[] { wois });
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
			}
			return true;
		}
		return false;
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

	private abstract class FieldAndIndex
	{
		final Field field;
		final long index;

		FieldAndIndex(Field field)
		{
			this.field = field;
			this.index = unsafe.objectFieldOffset(field);
		}
		
		public abstract void writeField(Object object, WicketObjectOutputStream dos)  throws IOException;
		
		public abstract void readField(Object object, WicketObjectInputStream dos)  throws IOException, ClassNotFoundException;
	}
	
	private final class BooleanFieldAndIndex extends FieldAndIndex
	{
		BooleanFieldAndIndex(Field field)
		{
			super(field);
		}
		
		/**
		 * @throws IOException 
		 * @see wicket.util.io.ClassStreamHandler.FieldAndIndex#writeField(java.lang.Object)
		 */
		public void writeField(Object object, WicketObjectOutputStream dos) throws IOException
		{
			dos.writeBoolean(unsafe.getBoolean(object, index));
		}
		
		/**
		 * @see wicket.util.io.ClassStreamHandler.FieldAndIndex#readField(java.lang.Object, java.io.WicketObjectInputStream)
		 */
		public void readField(Object object, WicketObjectInputStream dos) throws IOException
		{
			unsafe.putBoolean(object, index, dos.readBoolean());
		}
	}
	
	private final class ByteFieldAndIndex extends FieldAndIndex
	{
		ByteFieldAndIndex(Field field)
		{
			super(field);
		}
		
		/**
		 * @throws IOException 
		 * @see wicket.util.io.ClassStreamHandler.FieldAndIndex#writeField(java.lang.Object)
		 */
		public void writeField(Object object, WicketObjectOutputStream dos) throws IOException
		{
			dos.writeByte(unsafe.getByte(object, index));
		}
		
		/**
		 * @see wicket.util.io.ClassStreamHandler.FieldAndIndex#readField(java.lang.Object, java.io.WicketObjectInputStream)
		 */
		public void readField(Object object, WicketObjectInputStream dos) throws IOException
		{
			unsafe.putByte(object, index, dos.readByte());
		}
	}	

	private final class ShortFieldAndIndex extends FieldAndIndex
	{
		ShortFieldAndIndex(Field field)
		{
			super(field);
		}
		
		/**
		 * @throws IOException 
		 * @see wicket.util.io.ClassStreamHandler.FieldAndIndex#writeField(java.lang.Object)
		 */
		public void writeField(Object object, WicketObjectOutputStream dos) throws IOException
		{
			dos.writeShort(unsafe.getShort(object, index));
		}
		
		/**
		 * @see wicket.util.io.ClassStreamHandler.FieldAndIndex#readField(java.lang.Object, java.io.WicketObjectInputStream)
		 */
		public void readField(Object object, WicketObjectInputStream dos) throws IOException
		{
			unsafe.putShort(object, index, dos.readShort());
		}
	}	
	
	private final class CharFieldAndIndex extends FieldAndIndex
	{
		CharFieldAndIndex(Field field)
		{
			super(field);
		}
		
		/**
		 * @throws IOException 
		 * @see wicket.util.io.ClassStreamHandler.FieldAndIndex#writeField(java.lang.Object)
		 */
		public void writeField(Object object, WicketObjectOutputStream dos) throws IOException
		{
			dos.writeChar(unsafe.getChar(object, index));
		}
		
		/**
		 * @see wicket.util.io.ClassStreamHandler.FieldAndIndex#readField(java.lang.Object, java.io.WicketObjectInputStream)
		 */
		public void readField(Object object, WicketObjectInputStream dos) throws IOException
		{
			unsafe.putChar(object, index, dos.readChar());
		}
	}	
	
	private final class IntFieldAndIndex extends FieldAndIndex
	{
		IntFieldAndIndex(Field field)
		{
			super(field);
		}
		
		/**
		 * @throws IOException 
		 * @see wicket.util.io.ClassStreamHandler.FieldAndIndex#writeField(java.lang.Object)
		 */
		public void writeField(Object object, WicketObjectOutputStream dos) throws IOException
		{
			dos.writeInt(unsafe.getInt(object, index));
		}
		
		/**
		 * @see wicket.util.io.ClassStreamHandler.FieldAndIndex#readField(java.lang.Object, java.io.WicketObjectInputStream)
		 */
		public void readField(Object object, WicketObjectInputStream dos) throws IOException
		{
			unsafe.putInt(object, index, dos.readInt());
		}
	}	
	
	private final class LongFieldAndIndex extends FieldAndIndex
	{
		LongFieldAndIndex(Field field)
		{
			super(field);
		}
		
		/**
		 * @throws IOException 
		 * @see wicket.util.io.ClassStreamHandler.FieldAndIndex#writeField(java.lang.Object)
		 */
		public void writeField(Object object, WicketObjectOutputStream dos) throws IOException
		{
			dos.writeLong(unsafe.getLong(object, index));
		}
		
		/**
		 * @see wicket.util.io.ClassStreamHandler.FieldAndIndex#readField(java.lang.Object, java.io.WicketObjectInputStream)
		 */
		public void readField(Object object, WicketObjectInputStream dos) throws IOException
		{
			unsafe.putLong(object, index, dos.readLong());
		}
	}	
	
	private final class FloatFieldAndIndex extends FieldAndIndex
	{
		FloatFieldAndIndex(Field field)
		{
			super(field);
		}
		
		/**
		 * @throws IOException 
		 * @see wicket.util.io.ClassStreamHandler.FieldAndIndex#writeField(java.lang.Object)
		 */
		public void writeField(Object object, WicketObjectOutputStream dos) throws IOException
		{
			dos.writeFloat(unsafe.getFloat(object, index));
		}
		
		/**
		 * @see wicket.util.io.ClassStreamHandler.FieldAndIndex#readField(java.lang.Object, java.io.WicketObjectInputStream)
		 */
		public void readField(Object object, WicketObjectInputStream dos) throws IOException
		{
			unsafe.putFloat(object, index, dos.readFloat());
		}
	}	
	
	private final class DoubleFieldAndIndex extends FieldAndIndex
	{
		DoubleFieldAndIndex(Field field)
		{
			super(field);
		}
		
		/**
		 * @throws IOException 
		 * @see wicket.util.io.ClassStreamHandler.FieldAndIndex#writeField(java.lang.Object)
		 */
		public void writeField(Object object, WicketObjectOutputStream dos) throws IOException
		{
			dos.writeDouble(unsafe.getDouble(object, index));
		}
		
		/**
		 * @see wicket.util.io.ClassStreamHandler.FieldAndIndex#readField(java.lang.Object, java.io.WicketObjectInputStream)
		 */
		public void readField(Object object, WicketObjectInputStream dos) throws IOException
		{
			unsafe.putDouble(object, index, dos.readDouble());
		}
	}
	
	private final class ObjectFieldAndIndex extends FieldAndIndex
	{
		ObjectFieldAndIndex(Field field)
		{
			super(field);
		}
		
		/**
		 * @throws IOException 
		 * @see wicket.util.io.ClassStreamHandler.FieldAndIndex#writeField(java.lang.Object)
		 */
		public void writeField(Object object, WicketObjectOutputStream dos) throws IOException
		{
			dos.writeObject(unsafe.getObject(object, index));
		}
		
		/**
		 * @throws ClassNotFoundException 
		 * @see wicket.util.io.ClassStreamHandler.FieldAndIndex#readField(java.lang.Object, java.io.WicketObjectInputStream)
		 */
		public void readField(Object object, WicketObjectInputStream dos) throws IOException, ClassNotFoundException
		{
			unsafe.putObject(object, index, dos.readObject());
		}
	}
	

	private abstract class PrimitiveArray
	{
		public abstract void writeArray(Object object, WicketObjectOutputStream dos)  throws IOException;
		
		public abstract Object readArray(WicketObjectInputStream dos)  throws IOException;
	}
	
	private final class BooleanPrimitiveArray extends PrimitiveArray
	{
		/**
		 * @throws IOException 
		 * @see wicket.util.io.ClassStreamHandler.PrimitiveArray#writeArray(java.lang.Object)
		 */
		public void writeArray(Object object, WicketObjectOutputStream dos) throws IOException
		{
			int length = Array.getLength(object);
			dos.writeInt(length);
			for (int i = 0; i < length; i++)
			{
				dos.writeBoolean(Array.getBoolean(object, i));
			}
		}
		
		/**
		 * @see wicket.util.io.ClassStreamHandler.PrimitiveArray#readArray(java.lang.Object, java.io.WicketObjectInputStream)
		 */
		public Object readArray(WicketObjectInputStream dos) throws IOException
		{
			int length = dos.readInt();
			Object array = Array.newInstance(getStreamClass(), length);
			for (int i = 0; i < length; i++)
			{
				Array.setBoolean(array, i, dos.readBoolean());
			}
			return array;
		}
	}
	
	private final class BytePrimitiveArray extends PrimitiveArray
	{
		/**
		 * @throws IOException 
		 * @see wicket.util.io.ClassStreamHandler.PrimitiveArray#writeArray(java.lang.Object)
		 */
		public void writeArray(Object object, WicketObjectOutputStream dos) throws IOException
		{
			int length = Array.getLength(object);
			dos.writeInt(length);
			for (int i = 0; i < length; i++)
			{
				dos.writeByte(Array.getByte(object, i));
			}
		}
		
		/**
		 * @see wicket.util.io.ClassStreamHandler.PrimitiveArray#readArray(java.lang.Object, java.io.WicketObjectInputStream)
		 */
		public Object readArray(WicketObjectInputStream dos) throws IOException
		{
			int length = dos.readInt();
			Object array = Array.newInstance(getStreamClass(), length);
			for (int i = 0; i < length; i++)
			{
				Array.setByte(array, i, dos.readByte());
			}
			return array;
		}
	}	

	private final class ShortPrimitiveArray extends PrimitiveArray
	{
		/**
		 * @throws IOException 
		 * @see wicket.util.io.ClassStreamHandler.PrimitiveArray#writeArray(java.lang.Object)
		 */
		public void writeArray(Object object, WicketObjectOutputStream dos) throws IOException
		{
			int length = Array.getLength(object);
			dos.writeInt(length);
			for (int i = 0; i < length; i++)
			{
				dos.writeShort(Array.getShort(object, i));
			}
		}
		
		/**
		 * @see wicket.util.io.ClassStreamHandler.PrimitiveArray#readArray(java.lang.Object, java.io.WicketObjectInputStream)
		 */
		public Object readArray(WicketObjectInputStream dos) throws IOException
		{
			int length = dos.readInt();
			Object array = Array.newInstance(getStreamClass(), length);
			for (int i = 0; i < length; i++)
			{
				Array.setShort(array, i, dos.readShort());
			}
			return array;
		}
	}	
	
	private final class CharPrimitiveArray extends PrimitiveArray
	{
	
		/**
		 * @throws IOException 
		 * @see wicket.util.io.ClassStreamHandler.PrimitiveArray#writeArray(java.lang.Object)
		 */
		public void writeArray(Object object, WicketObjectOutputStream dos) throws IOException
		{
			int length = Array.getLength(object);
			dos.writeInt(length);
			for (int i = 0; i < length; i++)
			{
				dos.writeChar(Array.getChar(object, i));
			}
		}
		
		/**
		 * @see wicket.util.io.ClassStreamHandler.PrimitiveArray#readArray(java.lang.Object, java.io.WicketObjectInputStream)
		 */
		public Object readArray(WicketObjectInputStream dos) throws IOException
		{
			int length = dos.readInt();
			Object array = Array.newInstance(getStreamClass(), length);
			for (int i = 0; i < length; i++)
			{
				Array.setChar(array, i, dos.readChar());
			}
			return array;
		}
	}	
	
	private final class IntPrimitiveArray extends PrimitiveArray
	{
		/**
		 * @throws IOException 
		 * @see wicket.util.io.ClassStreamHandler.PrimitiveArray#writeArray(java.lang.Object)
		 */
		public void writeArray(Object object, WicketObjectOutputStream dos) throws IOException
		{
			int length = Array.getLength(object);
			dos.writeInt(length);
			for (int i = 0; i < length; i++)
			{
				dos.writeInt(Array.getInt(object, i));
			}
		}
		
		/**
		 * @see wicket.util.io.ClassStreamHandler.PrimitiveArray#readArray(java.lang.Object, java.io.WicketObjectInputStream)
		 */
		public Object readArray(WicketObjectInputStream dos) throws IOException
		{
			int length = dos.readInt();
			Object array = Array.newInstance(getStreamClass(), length);
			for (int i = 0; i < length; i++)
			{
				Array.setInt(array, i, dos.readInt());
			}
			return array;
		}
	}	
	
	private final class LongPrimitiveArray extends PrimitiveArray
	{
		/**
		 * @throws IOException 
		 * @see wicket.util.io.ClassStreamHandler.PrimitiveArray#writeArray(java.lang.Object)
		 */
		public void writeArray(Object object, WicketObjectOutputStream dos) throws IOException
		{
			int length = Array.getLength(object);
			dos.writeInt(length);
			for (int i = 0; i < length; i++)
			{
				dos.writeLong(Array.getLong(object, i));
			}
		}
		
		/**
		 * @see wicket.util.io.ClassStreamHandler.PrimitiveArray#readArray(java.lang.Object, java.io.WicketObjectInputStream)
		 */
		public Object readArray(WicketObjectInputStream dos) throws IOException
		{
			int length = dos.readInt();
			Object array = Array.newInstance(getStreamClass(), length);
			for (int i = 0; i < length; i++)
			{
				Array.setLong(array, i, dos.readLong());
			}
			return array;
		}
	}	
	
	private final class FloatPrimitiveArray extends PrimitiveArray
	{
		/**
		 * @throws IOException 
		 * @see wicket.util.io.ClassStreamHandler.PrimitiveArray#writeArray(java.lang.Object)
		 */
		public void writeArray(Object object, WicketObjectOutputStream dos) throws IOException
		{
			int length = Array.getLength(object);
			dos.writeInt(length);
			for (int i = 0; i < length; i++)
			{
				dos.writeFloat(Array.getFloat(object, i));
			}
		}
		
		/**
		 * @see wicket.util.io.ClassStreamHandler.PrimitiveArray#readArray(java.lang.Object, java.io.WicketObjectInputStream)
		 */
		public Object readArray(WicketObjectInputStream dos) throws IOException
		{
			int length = dos.readInt();
			Object array = Array.newInstance(getStreamClass(), length);
			for (int i = 0; i < length; i++)
			{
				Array.setFloat(array, i, dos.readFloat());
			}
			return array;
		}
	}	
	
	private final class DoublePrimitiveArray extends PrimitiveArray
	{
		/**
		 * @throws IOException 
		 * @see wicket.util.io.ClassStreamHandler.PrimitiveArray#writeArray(java.lang.Object)
		 */
		public void writeArray(Object object, WicketObjectOutputStream dos) throws IOException
		{
			int length = Array.getLength(object);
			dos.writeInt(length);
			for (int i = 0; i < length; i++)
			{
				dos.writeDouble(Array.getDouble(object, i));
			}
		}
		
		/**
		 * @see wicket.util.io.ClassStreamHandler.PrimitiveArray#readArray(java.lang.Object, java.io.WicketObjectInputStream)
		 */
		public Object readArray(WicketObjectInputStream dos) throws IOException
		{
			int length = dos.readInt();
			Object array = Array.newInstance(getStreamClass(), length);
			for (int i = 0; i < length; i++)
			{
				Array.setDouble(array, i, dos.readDouble());
			}
			return array;
		}
	}
	
}