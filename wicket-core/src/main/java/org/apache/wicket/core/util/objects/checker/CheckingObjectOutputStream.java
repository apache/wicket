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
package org.apache.wicket.core.util.objects.checker;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.ObjectStreamField;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Date;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.apache.wicket.Component;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.util.lang.Classes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Checks an object tree during serialization for wrong state by delegating the work
 * to the used {@link IObjectChecker IObjectChecker}s.
 * <p>
 * As this class depends heavily on JDK's serialization internals using introspection, analyzing may
 * not be possible, for instance when the runtime environment does not have sufficient rights to set
 * fields accessible that would otherwise be hidden. You should call
 * {@link CheckingObjectOutputStream#isAvailable()} to see whether this class can operate properly.
 *
 *
 * An ObjectOutputStream that uses {@link IObjectChecker IObjectChecker}s to check the
 * state of the object before serializing it. If the checker returns
 * {@link org.apache.wicket.core.util.objects.checker.IObjectChecker.Result.Status#FAILURE}
 * then the serialization process is stopped and the error is logged.
 * </p>
 */
public class CheckingObjectOutputStream extends ObjectOutputStream
{
	private static final Logger log = LoggerFactory.getLogger(CheckingObjectOutputStream.class);

	public static class ObjectCheckException extends WicketRuntimeException
	{
		public ObjectCheckException(String message, Throwable cause)
		{
			super(message, cause);
		}
	}

	/**
	 * Does absolutely nothing.
	 */
	private static class NoopOutputStream extends OutputStream
	{
		@Override
		public void close()
		{
		}

		@Override
		public void flush()
		{
		}

		@Override
		public void write(byte[] b)
		{
		}

		@Override
		public void write(byte[] b, int i, int l)
		{
		}

		@Override
		public void write(int b)
		{
		}
	}

	private static abstract class ObjectOutputAdaptor implements ObjectOutput
	{

		@Override
		public void close() throws IOException
		{
		}

		@Override
		public void flush() throws IOException
		{
		}

		@Override
		public void write(byte[] b) throws IOException
		{
		}

		@Override
		public void write(byte[] b, int off, int len) throws IOException
		{
		}

		@Override
		public void write(int b) throws IOException
		{
		}

		@Override
		public void writeBoolean(boolean v) throws IOException
		{
		}

		@Override
		public void writeByte(int v) throws IOException
		{
		}

		@Override
		public void writeBytes(String s) throws IOException
		{
		}

		@Override
		public void writeChar(int v) throws IOException
		{
		}

		@Override
		public void writeChars(String s) throws IOException
		{
		}

		@Override
		public void writeDouble(double v) throws IOException
		{
		}

		@Override
		public void writeFloat(float v) throws IOException
		{
		}

		@Override
		public void writeInt(int v) throws IOException
		{
		}

		@Override
		public void writeLong(long v) throws IOException
		{
		}

		@Override
		public void writeShort(int v) throws IOException
		{
		}

		@Override
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
			this.object = object;
			this.fieldDescription = fieldDescription;
		}

		@Override
		public String toString()
		{
			return object.getClass() + " - " + fieldDescription;
		}
	}

	private static final NoopOutputStream DUMMY_OUTPUT_STREAM = new NoopOutputStream();

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
	private static Method LOOKUP_METHOD;

	private static Method GET_CLASS_DATA_LAYOUT_METHOD;

	private static Method GET_NUM_OBJ_FIELDS_METHOD;

	private static Method GET_OBJ_FIELD_VALUES_METHOD;

	private static Method GET_FIELD_METHOD;

	private static Method HAS_WRITE_REPLACE_METHOD_METHOD;

	private static Method INVOKE_WRITE_REPLACE_METHOD;

	static
	{
		try
		{
			LOOKUP_METHOD = ObjectStreamClass.class.getDeclaredMethod("lookup", new Class[] {
					Class.class, Boolean.TYPE });
			LOOKUP_METHOD.setAccessible(true);

			GET_CLASS_DATA_LAYOUT_METHOD = ObjectStreamClass.class.getDeclaredMethod(
					"getClassDataLayout", (Class[])null);
			GET_CLASS_DATA_LAYOUT_METHOD.setAccessible(true);

			GET_NUM_OBJ_FIELDS_METHOD = ObjectStreamClass.class.getDeclaredMethod(
					"getNumObjFields", (Class[])null);
			GET_NUM_OBJ_FIELDS_METHOD.setAccessible(true);

			GET_OBJ_FIELD_VALUES_METHOD = ObjectStreamClass.class.getDeclaredMethod(
					"getObjFieldValues", new Class[] { Object.class, Object[].class });
			GET_OBJ_FIELD_VALUES_METHOD.setAccessible(true);

			GET_FIELD_METHOD = ObjectStreamField.class.getDeclaredMethod("getField", (Class[])null);
			GET_FIELD_METHOD.setAccessible(true);

			HAS_WRITE_REPLACE_METHOD_METHOD = ObjectStreamClass.class.getDeclaredMethod(
					"hasWriteReplaceMethod", (Class[])null);
			HAS_WRITE_REPLACE_METHOD_METHOD.setAccessible(true);

			INVOKE_WRITE_REPLACE_METHOD = ObjectStreamClass.class.getDeclaredMethod(
					"invokeWriteReplace", new Class[] { Object.class });
			INVOKE_WRITE_REPLACE_METHOD.setAccessible(true);
		}
		catch (Exception e)
		{
			log.warn("SerializableChecker not available", e);
			available = false;
		}
	}

	private final IObjectChecker[] checkers;

	/**
	 * Gets whether we can execute the tests. If false, calling {@link #check(Object)} will just
	 * return and you are advised to rely on the {@link java.io.NotSerializableException}. Clients are
	 * advised to call this method prior to calling the check method.
	 *
	 * @return whether security settings and underlying API etc allow for accessing the
	 *         serialization API using introspection
	 */
	public static boolean isAvailable()
	{
		return available;
	}

	/**
	 * The output stream where the serialized object will be written upon successful check
	 */
	private final ObjectOutputStream out;

	/** object stack with the trace path. */
	private final LinkedList<TraceSlot> traceStack = new LinkedList<TraceSlot>();

	/** set for checking circular references. */
	private final Map<Object, Object> checked = new IdentityHashMap<Object, Object>();

	/** string stack with current names pushed. */
	private final LinkedList<CharSequence> nameStack = new LinkedList<CharSequence>();

	/** root object being analyzed. */
	private Object root;

	/** set of classes that had no writeObject methods at lookup (to avoid repeated checking) */
	private final Set<Class<?>> writeObjectMethodMissing = new HashSet<Class<?>>();

	/** current simple field name. */
	private CharSequence simpleName = "";

	/** current full field description. */
	private String fieldDescription;

	/**
	 * Constructor.
	 *
	 * @param outputStream
	 *      the output stream where the serialized object will be written upon successful check
	 * @param checkers
	 *      the {@link IObjectChecker checkers} that will actually check the objects
	 * @throws IOException
	 * @throws SecurityException
	 */
	public CheckingObjectOutputStream(final OutputStream outputStream, final IObjectChecker... checkers) throws IOException, SecurityException
	{
		this.out = new ObjectOutputStream(outputStream);
		this.checkers = checkers;
	}

	private void check(Object obj)
	{
		if (obj == null)
		{
			return;
		}

		if (checked.containsKey(obj))
		{
			return;
		}

		internalCheck(obj);
	}

	private void internalCheck(Object obj)
	{
		if (obj == null)
		{
			return;
		}

		Class<?> cls = obj.getClass();
		nameStack.add(simpleName);
		traceStack.add(new TraceSlot(obj, fieldDescription));

		for (IObjectChecker checker : checkers)
		{
			IObjectChecker.Result result = checker.check(obj);
			if (result.status == IObjectChecker.Result.Status.FAILURE)
			{
				String prettyPrintMessage = toPrettyPrintedStack(Classes.name(cls));
				String exceptionMessage = result.reason + '\n' + prettyPrintMessage;
				throw new ObjectCheckException(exceptionMessage, result.cause);
			}
		}

		ObjectStreamClass desc;
		for (;;)
		{
			try
			{
				desc = (ObjectStreamClass)LOOKUP_METHOD.invoke(null, cls, Boolean.TRUE);
				Class<?> repCl;
				if (!(Boolean)HAS_WRITE_REPLACE_METHOD_METHOD.invoke(desc, (Object[])null) ||
						(obj = INVOKE_WRITE_REPLACE_METHOD.invoke(desc, obj)) == null ||
						(repCl = obj.getClass()) == cls)
				{
					break;
				}
				cls = repCl;
			}
			catch (IllegalAccessException e)
			{
				throw new RuntimeException(e);
			}
			catch (InvocationTargetException e)
			{
				throw new RuntimeException(e);
			}
		}

		if (cls.isPrimitive())
		{
			// skip
		}
		else if (cls.isArray())
		{
			checked.put(obj, null);
			Class<?> ccl = cls.getComponentType();
			if (!(ccl.isPrimitive()))
			{
				Object[] objs = (Object[])obj;
				for (int i = 0; i < objs.length; i++)
				{
					CharSequence arrayPos = new StringBuilder(4).append('[').append(i).append(']');
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

					@Override
					public void writeObject(Object streamObj) throws IOException
					{
						// Check for circular reference.
						if (checked.containsKey(streamObj))
						{
							return;
						}

						checked.put(streamObj, null);
						CharSequence arrayPos = new StringBuilder(10).append("[write:").append(count++).append(']');
						simpleName = arrayPos;
						fieldDescription += arrayPos;

						check(streamObj);
					}
				});
			}
			catch (Exception e)
			{
				if (e instanceof ObjectCheckException)
				{
					throw (ObjectCheckException)e;
				}
				log.warn("Error delegating to Externalizable : {}, path: {}", e.getMessage(), currentPath());
			}
		}
		else
		{
			Method writeObjectMethod = null;
			if (writeObjectMethodMissing.contains(cls) == false)
			{
				try
				{
					writeObjectMethod = cls.getDeclaredMethod("writeObject",
							new Class[] { java.io.ObjectOutputStream.class });
				}
				catch (SecurityException e)
				{
					// we can't access / set accessible to true
					writeObjectMethodMissing.add(cls);
				}
				catch (NoSuchMethodException e)
				{
					// cls doesn't have that method
					writeObjectMethodMissing.add(cls);
				}
			}

			final Object original = obj;
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

					@Override
					protected Object replaceObject(Object streamObj) throws IOException
					{
						if (streamObj == original)
						{
							return streamObj;
						}

						counter++;
						// Check for circular reference.
						if (checked.containsKey(streamObj))
						{
							return null;
						}

						checked.put(streamObj, null);
						CharSequence arrayPos = new StringBuilder(10).append("[write:").append(counter).append(']');
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
					if (e instanceof ObjectCheckException)
					{
						throw (ObjectCheckException)e;
					}
					log.warn("error delegating to writeObject : {}, path: {}", e.getMessage(), currentPath());
				}
			}
			else
			{
				Object[] slots;
				try
				{
					slots = (Object[])GET_CLASS_DATA_LAYOUT_METHOD.invoke(desc, (Object[])null);
				}
				catch (Exception e)
				{
					throw new RuntimeException(e);
				}
				for (Object slot : slots)
				{
					ObjectStreamClass slotDesc;
					try
					{
						Field descField = slot.getClass().getDeclaredField("desc");
						descField.setAccessible(true);
						slotDesc = (ObjectStreamClass)descField.get(slot);
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
			numFields = (Integer)GET_NUM_OBJ_FIELDS_METHOD.invoke(desc, (Object[])null);
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
				GET_OBJ_FIELD_VALUES_METHOD.invoke(desc, obj, objVals);
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
				if (objVals[i] instanceof String || objVals[i] instanceof Number ||
						objVals[i] instanceof Date || objVals[i] instanceof Boolean ||
						objVals[i] instanceof Class)
				{
					// filter out common cases
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
					field = (Field)GET_FIELD_METHOD.invoke(fieldDesc, (Object[])null);
				}
				catch (IllegalAccessException e)
				{
					throw new RuntimeException(e);
				}
				catch (InvocationTargetException e)
				{
					throw new RuntimeException(e);
				}

				simpleName = field.getName();
				fieldDescription = field.toString();
				check(objVals[i]);
			}
		}
	}

	/**
	 * @return name from root to current node concatenated with slashes
	 */
	private StringBuilder currentPath()
	{
		StringBuilder b = new StringBuilder();
		for (Iterator<CharSequence> it = nameStack.iterator(); it.hasNext();)
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
	 * Dump with indentation.
	 *
	 * @param type
	 *            the type that couldn't be serialized
	 * @return A very pretty dump
	 */
	protected final String toPrettyPrintedStack(String type)
	{
		StringBuilder result = new StringBuilder(512);
		StringBuilder spaces = new StringBuilder(32);
		result.append("A problem occurred while checking object with type: ");
		result.append(type);
		result.append("\nField hierarchy is:");
		for (TraceSlot slot : traceStack)
		{
			spaces.append(' ').append(' ');
			result.append('\n').append(spaces).append(slot.fieldDescription);
			result.append(" [class=").append(Classes.name(slot.object.getClass()));
			if (slot.object instanceof Component)
			{
				Component component = (Component)slot.object;
				result.append(", path=").append(component.getPath());
			}
			result.append(']');
		}
		result.append(" <----- field that is causing the problem");
		return result.toString();
	}

	/**
	 * @see java.io.ObjectOutputStream#writeObjectOverride(java.lang.Object)
	 */
	@Override
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
		out.writeObject(obj);
	}

	/**
	 * @see java.io.ObjectOutputStream#reset()
	 */
	@Override
	public void reset() throws IOException
	{
		root = null;
		checked.clear();
		fieldDescription = null;
		simpleName = null;
		traceStack.clear();
		nameStack.clear();
		writeObjectMethodMissing.clear();
	}

	@Override
	public void close() throws IOException
	{
		// do not call super.close() because SerializableChecker uses ObjectOutputStream's no-arg constructor

		// just null-ify the declared members
		reset();
	}
}
