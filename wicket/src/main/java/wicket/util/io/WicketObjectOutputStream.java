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

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.ObjectOutputStream.PutField;
import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import wicket.util.collections.HandleArrayListStack;

/**
 * @author jcompagner
 */
public final class WicketObjectOutputStream extends ObjectOutputStream
{
	private wicket.util.collections.HandleTable handledObjects = new wicket.util.collections.HandleTable(); 
	
	private HandleArrayListStack stack = new HandleArrayListStack();
	private HandleArrayListStack defaultWrite = new HandleArrayListStack();
	
	private final DataOutputStream out;
	private ClassStreamHandler classHandler;

	private int booleanCounter;

	private int byteCounter;

	private PutField curPut;
	
	
	/**
	 * Construct.
	 * @param out
	 * @throws IOException
	 */
	public WicketObjectOutputStream(OutputStream out) throws IOException
	{
		super();
		this.out = new DataOutputStream(out);
		
	}
	
	/**
	 * @see java.io.ObjectOutputStream#writeObjectOverride(java.lang.Object)
	 */
	protected final void writeObjectOverride(Object obj) throws IOException
	{
		if ( obj == null)
		{
			out.write(ClassStreamHandler.NULL);
			return;
		}
		int handle = handledObjects.lookup(obj);
		if ( handle != -1)
		{
			out.write(ClassStreamHandler.HANDLE);
			out.writeShort(handle);
		}
		else
		{
			if( obj instanceof Class)
			{
				ClassStreamHandler classHandler = ClassStreamHandler.lookup((Class)obj);
				out.write(ClassStreamHandler.CLASS);
				out.writeShort(classHandler.getClassId());
			}
			else
			{
				handledObjects.assign(obj);
				Class cls = obj.getClass();
	
				if(cls.isArray())
				{
					Class componentType = cls.getComponentType();
					ClassStreamHandler classHandler = ClassStreamHandler.lookup(componentType);
					if(componentType.isPrimitive())
					{
						out.write(ClassStreamHandler.PRIMITIVE_ARRAY);
						out.writeShort(classHandler.getClassId());
						classHandler.writeArray(obj,this);
					}
					else
					{
						out.write(ClassStreamHandler.ARRAY);
						out.writeShort(classHandler.getClassId());
						int length = Array.getLength(obj);
						out.writeInt(length);
						for (int i = 0; i < length; i++)
						{
							writeObjectOverride(Array.get(obj, i));
						}
					}
					return;
				}
				else
				{
					classHandler = ClassStreamHandler.lookup(cls);
					
					out.write(ClassStreamHandler.CLASS_DEF);
					out.writeShort(classHandler.getClassId());
					// handle strings directly.
					if (obj instanceof String)
					{
						out.writeUTF((String)obj);
					}
					else
					{
						PutField old = curPut;
						curPut = null;
						stack.push(obj);
						if (!classHandler.invokeWriteMethod(this,obj))
						{
							classHandler.writeFields(this,obj);
						}
						stack.pop();
						curPut = old;
					}
				}
			}
		}
	}
	
	/**
	 * @see java.io.ObjectOutputStream#defaultWriteObject()
	 */
	public void defaultWriteObject() throws IOException
	{
		Object currentObject = stack.peek();
		if ( !defaultWrite.contains(currentObject))
		{
			defaultWrite.add(currentObject);
			classHandler.writeFields(this,currentObject);
		}
	}
	
	  /**
     * Writes a boolean.
     *
     * @param	val the boolean to be written
     * @throws	IOException if I/O errors occur while writing to the underlying
     * 		stream
     */
    public void writeBoolean(boolean val) throws IOException {
	out.writeBoolean(val);
    }

    /**
     * Writes an 8 bit byte.
     *
     * @param	val the byte value to be written
     * @throws	IOException if I/O errors occur while writing to the underlying
     * 		stream
     */
    public void writeByte(int val) throws IOException  {
	out.writeByte(val);
    }

    /**
     * Writes a 16 bit short.
     *
     * @param	val the short value to be written
     * @throws	IOException if I/O errors occur while writing to the underlying
     * 		stream
     */
    public void writeShort(int val)  throws IOException {
	out.writeShort(val);
    }

    /**
     * Writes a 16 bit char.
     *
     * @param	val the char value to be written
     * @throws	IOException if I/O errors occur while writing to the underlying
     * 		stream
     */
    public void writeChar(int val)  throws IOException {
	out.writeChar(val);
    }

    /**
     * Writes a 32 bit int.
     *
     * @param	val the integer value to be written
     * @throws	IOException if I/O errors occur while writing to the underlying
     * 		stream
     */
    public void writeInt(int val)  throws IOException {
	out.writeInt(val);
    }

    /**
     * Writes a 64 bit long.
     *
     * @param	val the long value to be written
     * @throws	IOException if I/O errors occur while writing to the underlying
     * 		stream
     */
    public void writeLong(long val)  throws IOException {
	out.writeLong(val);
    }

    /**
     * Writes a 32 bit float.
     *
     * @param	val the float value to be written
     * @throws	IOException if I/O errors occur while writing to the underlying
     * 		stream
     */
    public void writeFloat(float val) throws IOException {
	out.writeFloat(val);
    }

    /**
     * Writes a 64 bit double.
     *
     * @param	val the double value to be written
     * @throws	IOException if I/O errors occur while writing to the underlying
     * 		stream
     */
    public void writeDouble(double val) throws IOException {
	out.writeDouble(val);
    }

    /**
     * Writes a String as a sequence of bytes.
     *
     * @param	str the String of bytes to be written
     * @throws	IOException if I/O errors occur while writing to the underlying
     * 		stream
     */
    public void writeBytes(String str) throws IOException {
	out.writeBytes(str);
    }

    /**
     * Writes a String as a sequence of chars.
     *
     * @param	str the String of chars to be written
     * @throws	IOException if I/O errors occur while writing to the underlying
     * 		stream
     */
    public void writeChars(String str) throws IOException {
    	out.writeChars(str);
    }
	
    /**
     * @see java.io.ObjectOutputStream#write(byte[])
     */
    public void write(byte[] buf) throws IOException
    {    	out.write(buf);
    }
    
    /**
     * @see java.io.ObjectOutputStream#write(byte[], int, int)
     */
    public void write(byte[] buf, int off, int len) throws IOException
    {
    	out.write(buf, off, len);
    }
    
    /**
     * @see java.io.ObjectOutputStream#write(int)
     */
    public void write(int val) throws IOException
    {
    	out.write(val);
    }
    
    /**
     * @see java.io.ObjectOutputStream#writeUTF(java.lang.String)
     */
    public void writeUTF(String str) throws IOException
    {
    	out.writeUTF(str);
    }
    
    /**
     * @see java.io.ObjectOutputStream#putFields()
     */
    public PutField putFields() throws IOException
    {
    	if (curPut == null)
    	{
    		curPut = new PutFieldImpl();
    	}
    	return curPut;
    }
    
    /**
     * @see java.io.ObjectOutputStream#writeFields()
     */
    public void writeFields() throws IOException
    {
    	if (curPut != null)
    	{
    		curPut.write(this);
    	}
    }
	/**
	 * @see java.io.ObjectOutputStream#close()
	 */
	public void close() throws IOException
	{
		classHandler = null;
		stack = null;
		defaultWrite = null;
		out.close();
	}
	
	private class PutFieldImpl extends PutField
	{
		private HashMap mapBytes;
		private HashMap mapChar;
		private HashMap mapDouble;
		private HashMap mapFloat;
		private HashMap mapInt;
		private HashMap mapLong;
		private HashMap mapShort;
		private HashMap mapBoolean;
		private HashMap mapObject;
		
		/**
		 * @see java.io.ObjectOutputStream.PutField#put(java.lang.String, byte)
		 */
		public void put(String name, byte val)
		{
			if (mapBytes == null) mapBytes = new HashMap(4);
			mapBytes.put(name, new Byte(val));
		}

		/**
		 * @see java.io.ObjectOutputStream.PutField#put(java.lang.String, char)
		 */
		public void put(String name, char val)
		{
			if (mapChar == null) mapChar = new HashMap(4);
			mapChar.put(name, new Character(val));
		}

		/**
		 * @see java.io.ObjectOutputStream.PutField#put(java.lang.String, double)
		 */
		public void put(String name, double val)
		{
			if (mapDouble == null) mapDouble = new HashMap(4);
			mapDouble.put(name, new Double(val));
		}

		/**
		 * @see java.io.ObjectOutputStream.PutField#put(java.lang.String, float)
		 */
		public void put(String name, float val)
		{
			if (mapFloat == null) mapFloat = new HashMap(4);
			mapFloat.put(name, new Float(val));
		}

		/**
		 * @see java.io.ObjectOutputStream.PutField#put(java.lang.String, int)
		 */
		public void put(String name, int val)
		{
			if (mapInt == null) mapInt = new HashMap(4);
			mapInt.put(name, new Integer(val));
		}

		/**
		 * @see java.io.ObjectOutputStream.PutField#put(java.lang.String, long)
		 */
		public void put(String name, long val)
		{
			if (mapLong == null) mapLong = new HashMap(4);
			mapLong.put(name, new Long(val));
		}

		/**
		 * @see java.io.ObjectOutputStream.PutField#put(java.lang.String, short)
		 */
		public void put(String name, short val)
		{
			if (mapShort == null) mapShort = new HashMap(4);
			mapShort.put(name, new Short(val));
		}

		/**
		 * @see java.io.ObjectOutputStream.PutField#put(java.lang.String, boolean)
		 */
		public void put(String name, boolean val)
		{
			if (mapBoolean== null) mapBoolean = new HashMap(4);
			mapBoolean.put(name, val?Boolean.TRUE:Boolean.FALSE);
		}

		/**
		 * @see java.io.ObjectOutputStream.PutField#put(java.lang.String, java.lang.Object)
		 */
		public void put(String name, Object val)
		{
			if (mapObject == null) mapObject = new HashMap(4);
			mapObject.put(name,val);
		}

		/**
		 * @see java.io.ObjectOutputStream.PutField#write(java.io.ObjectOutput)
		 */
		public void write(ObjectOutput out) throws IOException
		{
			// i don't know if all the fields (names in the map)
			// are really also always real fields.. So i just
			// write them by name->value
			// maybe in the further we can really calculate an offset?
			if (mapBoolean != null)
			{
				ClassStreamHandler lookup = ClassStreamHandler.lookup(boolean.class);
				writeShort(lookup.getClassId());
				writeShort(mapBoolean.size());
				Iterator it = mapBoolean.entrySet().iterator();
				while (it.hasNext())
				{
					Map.Entry entry = (Entry)it.next();
					// write the key.
					writeObjectOverride(entry.getKey());
					writeBoolean(((Boolean)entry.getValue()).booleanValue());
				}
			}
			if (mapBytes != null)
			{
				ClassStreamHandler lookup = ClassStreamHandler.lookup(byte.class);
				writeShort(lookup.getClassId());
				writeShort(mapBytes.size());
				Iterator it = mapBytes.entrySet().iterator();
				while (it.hasNext())
				{
					Map.Entry entry = (Entry)it.next();
					// write the key.
					writeObjectOverride(entry.getKey());
					writeByte( ((Byte)entry.getValue()).byteValue());
				}
			}
			if (mapShort != null)
			{
				ClassStreamHandler lookup = ClassStreamHandler.lookup(short.class);
				writeShort(lookup.getClassId());
				writeShort(mapShort.size());
				Iterator it = mapShort.entrySet().iterator();
				while (it.hasNext())
				{
					Map.Entry entry = (Entry)it.next();
					// write the key.
					writeObjectOverride(entry.getKey());
					writeShort( ((Short)entry.getValue()).shortValue());
				}
			}
			if (mapChar != null)
			{
				ClassStreamHandler lookup = ClassStreamHandler.lookup(char.class);
				writeShort(lookup.getClassId());
				writeShort(mapChar.size());
				Iterator it = mapChar.entrySet().iterator();
				while (it.hasNext())
				{
					Map.Entry entry = (Entry)it.next();
					// write the key.
					writeObjectOverride(entry.getKey());
					writeChar( ((Character)entry.getValue()).charValue());
				}
			}
			if (mapInt != null)
			{
				ClassStreamHandler lookup = ClassStreamHandler.lookup(int.class);
				writeShort(lookup.getClassId());
				writeShort(mapInt.size());
				Iterator it = mapInt.entrySet().iterator();
				while (it.hasNext())
				{
					Map.Entry entry = (Entry)it.next();
					// write the key.
					writeObjectOverride(entry.getKey());
					writeInt( ((Integer)entry.getValue()).intValue());
				}
			}
			if (mapLong != null)
			{
				ClassStreamHandler lookup = ClassStreamHandler.lookup(long.class);
				writeShort(lookup.getClassId());
				writeShort(mapLong.size());
				Iterator it = mapLong.entrySet().iterator();
				while (it.hasNext())
				{
					Map.Entry entry = (Entry)it.next();
					// write the key.
					writeObjectOverride(entry.getKey());
					writeLong( ((Long)entry.getValue()).longValue());
				}
			}
			if (mapFloat != null)
			{
				ClassStreamHandler lookup = ClassStreamHandler.lookup(float.class);
				writeShort(lookup.getClassId());
				writeShort(mapFloat.size());
				Iterator it = mapFloat.entrySet().iterator();
				while (it.hasNext())
				{
					Map.Entry entry = (Entry)it.next();
					// write the key.
					writeObjectOverride(entry.getKey());
					writeFloat( ((Float)entry.getValue()).floatValue());
				}
			}
			if (mapDouble != null)
			{
				ClassStreamHandler lookup = ClassStreamHandler.lookup(double.class);
				writeShort(lookup.getClassId());
				writeShort(mapDouble.size());
				Iterator it = mapDouble.entrySet().iterator();
				while (it.hasNext())
				{
					Map.Entry entry = (Entry)it.next();
					// write the key.
					writeObjectOverride(entry.getKey());
					writeDouble( ((Double)entry.getValue()).doubleValue());
				}
			}
			if (mapObject != null)
			{
				ClassStreamHandler lookup = ClassStreamHandler.lookup(Serializable.class);
				writeShort(lookup.getClassId());
				writeShort(mapObject.size());
				Iterator it = mapObject.entrySet().iterator();
				while (it.hasNext())
				{
					Map.Entry entry = (Entry)it.next();
					// write the key.
					writeObjectOverride(entry.getKey());
					writeObjectOverride(entry.getValue());
				}
			}
			// end byte.
			writeShort(ClassStreamHandler.NULL);
		}
		
	}
}
