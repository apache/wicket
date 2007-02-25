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

import java.io.Externalizable;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.ObjectStreamField;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Date;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.Component;
import wicket.WicketRuntimeException;

/**
 * Utility class that analyzes objects for non-serializable nodes. Construct
 * with the object you want to check, and then call {@link #check()}. When a
 * non-serializable object is found, a {@link WicketNotSerializableException} is
 * thrown with a message that shows the trace up to the not-serializable object.
 * The exception is thrown for the first non-serializable instance it
 * encounters, so multiple problems will not be shown.
 * <p>
 * As this class depends heavily on JDK's serialization internals using
 * introspection, analyzing may not be possible, for instance when the runtime
 * environment does not have sufficient rights to set fields accesible that
 * would otherwise be hidden. You should call
 * {@link SerializableChecker#isAvailable()} to see whether this class can
 * operate properly. If it doesn't, you should fall back to e.g. re-throwing/
 * printing the {@link NotSerializableException} you probably got before using
 * this class.
 * </p>
 * 
 * @author eelcohillenius
 * @author Al Maw
 */
public final class SerializableChecker extends ObjectOutputStream
{
	/**
	 * Exception that is thrown when a non-serializable object was found.
	 */
	public static final class WicketNotSerializableException extends WicketRuntimeException
	{
		private static final long serialVersionUID = 1L;

		WicketNotSerializableException(String message, Throwable cause)
		{
			super(message, cause);
		}
	}

	/**
	 * Does absolutely nothing.
	 */
	private static class NoopOutputStream extends OutputStream
	{
		public void close()
		{
		}

		public void flush()
		{
		}

		public void write(byte[] b)
		{
		}

		public void write(byte[] b, int i, int l)
		{
		}

		public void write(int b)
		{
		}
	}

	private static abstract class ObjectOutputAdaptor implements ObjectOutput
	{

		public void close() throws IOException
		{
		}

		public void flush() throws IOException
		{
		}

		public void write(byte[] b) throws IOException
		{
		}

		public void write(byte[] b, int off, int len) throws IOException
		{
		}

		public void write(int b) throws IOException
		{
		}

		public void writeBoolean(boolean v) throws IOException
		{
		}

		public void writeByte(int v) throws IOException
		{
		}

		public void writeBytes(String s) throws IOException
		{
		}

		public void writeChar(int v) throws IOException
		{
		}

		public void writeChars(String s) throws IOException
		{
		}

		public void writeDouble(double v) throws IOException
		{
		}

		public void writeFloat(float v) throws IOException
		{
		}

		public void writeInt(int v) throws IOException
		{
		}

		public void writeLong(long v) throws IOException
		{
		}

		public void writeShort(int v) throws IOException
		{
		}

		public void writeUTF(String str) throws IOException
		{
		}
	}

	/** Holds information about the field and the resulting object being traced. */
	private static final class TraceSlot
	{
		private final String fieldDescription;

		private final Object object;

		TraceSlot(Object object, String fieldDescription)
		{
			super();
			this.object = object;
			this.fieldDescription = fieldDescription;
		}

		public String toString()
		{
			return object.getClass() + " - " + fieldDescription;
		}
	}

	private static final NoopOutputStream DUMMY_OUTPUT_STREAM = new NoopOutputStream();

	/** log. */
	private static final Log log = LogFactory.getLog(SerializableChecker.class);

	/** Whether we can execute the tests. If false, check will just return. */
	private static boolean available = true;

	// this hack - accessing the serialization API through introspection - is
	// the only way to use Java serialization for our purposes without writing
	// the whole thing from scratch (and even then, it would be limited). This
	// way of working is of course fragile for internal API changes, but as we
	// do an extra check on availability and we report when we can't use this
	// introspection fu, we'll find out soon enough and clients on this class
	// can fall back on Java's default exception for serialization errors (which
	// sucks and is the main reason for this attempt).
	private static final Method lookup;

	private static final Method getClassDataLayoutMethod;

	private static final Method getNumObjFields;

	private static final Method getObjFieldValues;

	private static final Method fieldMethod;

	static
	{
		try
		{
			lookup = ObjectStreamClass.class.getDeclaredMethod("lookup", new Class[] { Class.class,
					Boolean.TYPE });
			lookup.setAccessible(true);

			getClassDataLayoutMethod = ObjectStreamClass.class.getDeclaredMethod(
					"getClassDataLayout", null);
			getClassDataLayoutMethod.setAccessible(true);

			getNumObjFields = ObjectStreamClass.class.getDeclaredMethod("getNumObjFields", null);
			getNumObjFields.setAccessible(true);

			getObjFieldValues = ObjectStreamClass.class.getDeclaredMethod("getObjFieldValues",
					new Class[] { Object.class, Object[].class });
			getObjFieldValues.setAccessible(true);

			fieldMethod = ObjectStreamField.class.getDeclaredMethod("getField", null);
			fieldMethod.setAccessible(true);
		}
		catch (SecurityException e)
		{
			available = false;
			throw new RuntimeException(e);
		}
		catch (NoSuchMethodException e)
		{
			available = false;
			throw new RuntimeException(e);
		}
	}

	/**
	 * Gets whether we can execute the tests. If false, calling {@link #check()}
	 * will just return and you are advised to rely on the
	 * {@link NotSerializableException}. Clients are advised to call this
	 * method prior to calling the check method.
	 * 
	 * @return whether security settings and underlying API etc allow for
	 *         accessing the serialization API using introspection
	 */
	public static boolean isAvailable()
	{
		return available;
	}

	/** object stack that with the trace path. */
	private final LinkedList traceStack = new LinkedList();

	/** set for checking circular references. */
	private final Map checked = new IdentityHashMap();

	/** string stack with current names pushed. */
	private LinkedList nameStack = new LinkedList();

	/** root object being analyzed. */
	private Object root;

	/** cache for classes - writeObject methods. */
	private Map writeObjectMethodCache = new HashMap();

	/** current simple field name. */
	private String simpleName = "";

	/** current full field description. */
	private String fieldDescription;

	/** Exception that should be set as the cause when throwing a new exception. */
	private NotSerializableException exception;

	/**
	 * Construct.
	 * 
	 * @param exception
	 *            exception that should be set as the cause when throwing a new
	 *            exception
	 * 
	 * @throws IOException
	 */
	public SerializableChecker(NotSerializableException exception) throws IOException
	{
		this.exception = exception;
	}

	/**
	 * @see java.io.ObjectOutputStream#reset()
	 */
	public void reset() throws IOException
	{
		root = null;
		checked.clear();
		fieldDescription = null;
		simpleName = null;
		traceStack.clear();
		nameStack.clear();
		writeObjectMethodCache.clear();
	}

	private void check(final Object obj)
	{
		if (obj == null)
		{
			return;
		}

		Class cls = obj.getClass();
		nameStack.add(simpleName);
		traceStack.add(new TraceSlot(obj, fieldDescription));

		if (!(obj instanceof Serializable))
		{
			throw new WicketNotSerializableException(toPrettyPrintedStack(obj.getClass().getName())
					.toString(), exception);
		}

		final ObjectStreamClass desc;
		try
		{
			desc = (ObjectStreamClass)lookup.invoke(null, new Object[] { cls, Boolean.TRUE });
		}
		catch (IllegalAccessException e)
		{
			throw new RuntimeException(e);
		}
		catch (InvocationTargetException e)
		{
			throw new RuntimeException(e);
		}

		if (cls.isPrimitive())
		{
			// skip
		}
		else if (cls.isArray())
		{
			checked.put(obj, null);
			Class ccl = cls.getComponentType();
			if (!(ccl.isPrimitive()))
			{
				Object[] objs = (Object[])obj;
				for (int i = 0; i < objs.length; i++)
				{
					String arrayPos = "[" + i + "]";
					simpleName = arrayPos;
					fieldDescription += arrayPos;
					check(objs[i]);
				}
			}
		}
		else if (obj instanceof Externalizable && (!Proxy.isProxyClass(cls)))
		{
			Externalizable extObj = (Externalizable)obj;
			try
			{
				extObj.writeExternal(new ObjectOutputAdaptor()
				{
					private int count = 0;

					public void writeObject(Object streamObj) throws IOException
					{
						// Check for circular reference.
						if (checked.containsKey(streamObj))
						{
							return;
						}

						checked.put(streamObj, null);
						String arrayPos = "[write:" + count++ + "]";
						simpleName = arrayPos;
						fieldDescription += arrayPos;

						check(streamObj);
					}
				});
			}
			catch (Exception e)
			{
				if (e instanceof WicketNotSerializableException)
				{
					throw (WicketNotSerializableException)e;
				}
				log.warn("error delegating to Externalizable : " + e.getMessage() + ", path: "
						+ currentPath());
			}
		}
		else
		{
			Method writeObjectMethod = null;
			Object o = writeObjectMethodCache.get(cls);
			if (o != null)
			{
				if (o instanceof Method)
				{
					writeObjectMethod = (Method)o;
				}
			}
			else
			{
				try
				{
					writeObjectMethod = cls.getDeclaredMethod("writeObject",
							new Class[] { java.io.ObjectOutputStream.class });
				}
				catch (SecurityException e)
				{
					// we can't access/ set accessible to true
					writeObjectMethodCache.put(cls, Boolean.FALSE);
				}
				catch (NoSuchMethodException e)
				{
					// cls doesn't have that method
					writeObjectMethodCache.put(cls, Boolean.FALSE);
				}
			}

			if (writeObjectMethod != null)
			{
				class InterceptingObjectOutputStream extends ObjectOutputStream
				{
					private int counter;

					InterceptingObjectOutputStream() throws IOException
					{
						super(DUMMY_OUTPUT_STREAM);
						enableReplaceObject(true);
					}

					protected Object replaceObject(Object streamObj) throws IOException
					{
						if (streamObj == obj)
						{
							return streamObj;
						}

						counter++;
						// Check for circular reference.
						if (checked.containsKey(streamObj))
						{
							return null;
						}

						checked.put(obj, null);
						String arrayPos = "[write:" + counter + "]";
						simpleName = arrayPos;
						fieldDescription += arrayPos;
						check(streamObj);
						return streamObj;
					}
				}
				try
				{
					InterceptingObjectOutputStream ioos = new InterceptingObjectOutputStream();
					ioos.writeObject(obj);
				}
				catch (Exception e)
				{
					if (e instanceof WicketNotSerializableException)
					{
						throw (WicketNotSerializableException)e;
					}
					log.warn("error delegating to writeObject : " + e.getMessage() + ", path: "
							+ currentPath());
				}
			}
			else
			{
				Object[] slots;
				try
				{
					slots = (Object[])getClassDataLayoutMethod.invoke(desc, null);
				}
				catch (Exception e)
				{
					throw new RuntimeException(e);
				}
				for (int i = 0; i < slots.length; i++)
				{
					ObjectStreamClass slotDesc;
					try
					{
						Field descField = slots[i].getClass().getDeclaredField("desc");
						descField.setAccessible(true);
						slotDesc = (ObjectStreamClass)descField.get(slots[i]);
					}
					catch (Exception e)
					{
						throw new RuntimeException(e);
					}
					checked.put(obj, null);
					checkFields(obj, slotDesc);
				}
			}
		}

		traceStack.removeLast();
		nameStack.removeLast();
	}

	private void checkFields(Object obj, ObjectStreamClass desc)
	{
		int numFields;
		try
		{
			numFields = ((Integer)getNumObjFields.invoke(desc, null)).intValue();
		}
		catch (IllegalAccessException e)
		{
			throw new RuntimeException(e);
		}
		catch (InvocationTargetException e)
		{
			throw new RuntimeException(e);
		}

		if (numFields > 0)
		{
			int numPrimFields;
			ObjectStreamField[] fields = desc.getFields();
			Object[] objVals = new Object[numFields];
			numPrimFields = fields.length - objVals.length;
			try
			{
				getObjFieldValues.invoke(desc, new Object[] { obj, objVals });
			}
			catch (IllegalAccessException e)
			{
				throw new RuntimeException(e);
			}
			catch (InvocationTargetException e)
			{
				throw new RuntimeException(e);
			}
			for (int i = 0; i < objVals.length; i++)
			{
				if (objVals[i] instanceof String || objVals[i] instanceof Number
						|| objVals[i] instanceof Date || objVals[i] instanceof Boolean
						|| objVals[i] instanceof Class)
				{
					// fitler out common cases
					continue;
				}

				// Check for circular reference.
				if (checked.containsKey(objVals[i]))
				{
					continue;
				}

				ObjectStreamField fieldDesc = fields[numPrimFields + i];
				Field field;
				try
				{
					field = (Field)fieldMethod.invoke(fieldDesc, null);
				}
				catch (IllegalAccessException e)
				{
					throw new RuntimeException(e);
				}
				catch (InvocationTargetException e)
				{
					throw new RuntimeException(e);
				}

				String fieldName = field.getName();
				simpleName = field.getName();
				fieldDescription = field.toString();
				check(objVals[i]);
			}
		}
	}

	/**
	 * @return name from root to current node concatted with slashes
	 */
	private StringBuffer currentPath()
	{
		StringBuffer b = new StringBuffer();
		for (Iterator it = nameStack.iterator(); it.hasNext();)
		{
			b.append(it.next());
			if (it.hasNext())
			{
				b.append('/');
			}
		}
		return b;
	}

	/**
	 * Dump with identation.
	 * 
	 * @param type
	 *            the type that couldn't be serialized
	 * @return A very pretty dump
	 */
	private final String toPrettyPrintedStack(String type)
	{
		StringBuffer result = new StringBuffer();
		StringBuffer spaces = new StringBuffer();
		result.append("Unable to serialize class: ");
		result.append(type);
		result.append("\nField hierarchy is:");
		for (Iterator i = traceStack.listIterator(); i.hasNext();)
		{
			spaces.append("  ");
			TraceSlot slot = (TraceSlot)i.next();
			result.append("\n").append(spaces).append(slot.fieldDescription);
			result.append(" [class=").append(slot.object.getClass().getName());
			if (slot.object instanceof Component)
			{
				Component component = (Component)slot.object;
				result.append(", path=").append(component.getPath());
			}
			result.append("]");
		}
		result.append(" <----- field that is not serializable");
		return result.toString();
	}

	/**
	 * @see java.io.ObjectOutputStream#writeObjectOverride(java.lang.Object)
	 */
	protected final void writeObjectOverride(Object obj) throws IOException
	{
		if (!available)
		{
			return;
		}
		root = obj;
		if (fieldDescription == null)
		{
			fieldDescription = (root instanceof Component) ? ((Component)root).getPath() : "";
		}

		check(root);
	}
}