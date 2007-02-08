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
import java.io.NotSerializableException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.LinkedList;

import wicket.Component;
import wicket.WicketRuntimeException;

/**
 * Captures information about what it is trying to stream with the sole purpose
 * of finding out what the object hierarchy looks like of an object that can't
 * be serialized.
 * <p>
 * If you have an object you want to analyze for this, call:
 * <code>new DebuggingObjectOutputStream().writeObject(value);</code> and
 * catch the {@link WicketRuntimeException}.
 * </p>
 * <p>
 * There's no point using this class if you are not trying to diagnose a
 * serialization issue.
 * </p>
 * 
 * @author Al Maw
 */
public class DebuggingObjectOutputStream extends ObjectOutputStream
{
	private static final long serialVersionUID = 1L;

	/** stack for string representations of objects that are handled. */
	private final LinkedList<CharSequence> stack = new LinkedList<CharSequence>();

	/** set for checking circular references. */
	private final HashSet<Object> set = new HashSet<Object>();

	/**
	 * Creates an ObjectOutputStream. Doesn't write anywhere
	 * 
	 * @throws IOException
	 *             IOException if an I/O error occurs while writing stream
	 *             header
	 */
	public DebuggingObjectOutputStream() throws IOException
	{
		super();
	}

	/**
	 * Dump with identation.
	 * 
	 * @param type
	 *            the type that couldn't be serialized
	 * @return A very pretty dump
	 */
	private String getPrettyPrintedStack(String type)
	{
		set.clear();
		StringBuilder result = new StringBuilder();
		StringBuilder spaces = new StringBuilder();
		result.append("Unable to serialize class: ");
		result.append(type);
		result.append("\nField hierarchy is:");
		while (!stack.isEmpty())
		{
			spaces.append("  ");
			result.append("\n").append(spaces).append(stack.removeFirst());
		}
		result.append(" <----- field that is not serializable");
		return result.toString();
	}

	/**
	 * @see java.io.ObjectOutputStream#writeObjectOverride(java.lang.Object)
	 * @throws IOException
	 *             never actually happens
	 * @throws WicketRuntimeException
	 *             on a serialization exception
	 */
	protected final void writeObjectOverride(Object obj) throws IOException
	{
		if (obj == null)
		{
			return;
		}
		// Check for circular reference.
		if (set.contains(obj))
		{
			return;
		}
		if (stack.isEmpty())
		{
			stack.add("Class " + obj.getClass().getName());
		}
		set.add(obj);
		Field[] fields = obj.getClass().getDeclaredFields();
		for (int i = 0; i < fields.length; i++)
		{
			fields[i].setAccessible(true);
			StringBuilder buffer = new StringBuilder();
			Field f = fields[i];
			int m = f.getModifiers();
			if (fields[i].getType().isPrimitive() || Modifier.isTransient(m)
					|| Modifier.isStatic(m))
			{
				continue;
			}

			if (Modifier.isPrivate(m))
			{
				buffer.append("private ");
			}
			if (Modifier.isProtected(m))
			{
				buffer.append("protected ");
			}
			if (Modifier.isPublic(m))
			{
				buffer.append("public ");
			}
			if (Modifier.isAbstract(m))
			{
				buffer.append("abstract ");
			}
			if (Modifier.isFinal(m))
			{
				buffer.append("final ");
			}
			if (Modifier.isStatic(m))
			{
				buffer.append("static ");
			}
			if (Modifier.isVolatile(m))
			{
				buffer.append("volatile ");
			}
			buffer.append(f.getType().getName()).append("");
			buffer.append(" ").append(f.getName()).append(" => ");

			// now that we have the reference, try to get the actual value
			try
			{
				Object val = f.get(obj);
				if (val != null)
				{
					buffer.append(val.getClass().getName());
					if (val instanceof Component)
					{
						buffer.append(" [path=").append(((Component<?>)val).getPath()).append("]");
					}
				}
				else
				{
					buffer.append(" null");
				}
			}
			catch (IllegalArgumentException e)
			{
				buffer.append("? (").append(e.getMessage()).append(")");
			}
			catch (IllegalAccessException e)
			{
				buffer.append("? (").append(e.getMessage()).append(")");
			}

			stack.add(buffer.toString());
			if (Serializable.class.isAssignableFrom(fields[i].getType()))
			{
				try
				{
					writeObjectOverride(fields[i].get(obj));
				}
				catch (IllegalAccessException e)
				{
					throw new WicketRuntimeException(getPrettyPrintedStack(fields[i].getType()
							.getName()), e);
				}
			}
			else
			{
				throw new WicketRuntimeException(getPrettyPrintedStack(
						fields[i].getType().getName()).toString(), new NotSerializableException(
						fields[i].getType().getName()));
			}
			stack.removeLast();
		}
		if (stack.size() == 1)
		{
			set.clear();
			stack.removeLast();
		}
	}
}