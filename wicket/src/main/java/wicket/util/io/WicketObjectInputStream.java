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

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.io.Serializable;
import java.io.ObjectInputStream.GetField;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import wicket.util.collections.HandleArrayListStack;
import wicket.util.collections.IntHashMap;

/**
 * @author jcompagner
 */
public final class WicketObjectInputStream extends ObjectInputStream
{
	 
	private IntHashMap handledObjects = new IntHashMap(); 
	private short handleCounter = 0;

	
	private final DataInputStream in;
	private ClassStreamHandler currentStreamHandler;
	private HandleArrayListStack stack = new HandleArrayListStack();
	private HandleArrayListStack defaultRead = new HandleArrayListStack();

	/**
	 * Construct.
	 * @param in
	 * @throws IOException
	 */
	public WicketObjectInputStream(InputStream in) throws IOException
	{
		super();
		this.in = new DataInputStream(in);
	}
	
	/**
	 * @see java.io.ObjectInputStream#readObjectOverride()
	 */
	protected Object readObjectOverride() throws IOException, ClassNotFoundException
	{
		Object value = null;
		int token = in.read();
		if(token == ClassStreamHandler.NULL)
		{
			return null;
		}
		else if ( token == ClassStreamHandler.HANDLE)
		{
			short handle = in.readShort();
			value = handledObjects.get(handle);
			if (value == null)
			{
				throw new RuntimeException("Expected to find a handle for " + handle);
			}
		}
		else if (token == ClassStreamHandler.CLASS_DEF)
		{
			short classDef = in.readShort();
			currentStreamHandler = ClassStreamHandler.lookup(classDef);
			if (currentStreamHandler.getStreamClass() == String.class)
			{
				value = in.readUTF();
				handledObjects.put(handleCounter++,value);
			}
			else
			{
				try
				{
					value = currentStreamHandler.createObject();
					handledObjects.put(handleCounter++,value);
					stack.push(value);
					if ( !currentStreamHandler.invokeReadMethod(this, value))
					{
						currentStreamHandler.readFields(this,value);
					}
					stack.pop();
				}
				catch (IllegalArgumentException ex)
				{
					throw new RuntimeException(ex);
				}
				catch (InstantiationException ex)
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
		}
		else if (token == ClassStreamHandler.CLASS)
		{
			short classDef = in.readShort();
			ClassStreamHandler lookup = ClassStreamHandler.lookup(classDef);
			value = lookup.getStreamClass();
		}
		else if (token == ClassStreamHandler.ARRAY)
		{
			short classDef = in.readShort();
			ClassStreamHandler lookup = ClassStreamHandler.lookup(classDef);
			int length = in.readInt();
			Object[] array = (Object[])Array.newInstance(lookup.getStreamClass(), length);
			handledObjects.put(handleCounter++,array);
			for (int i = 0; i < array.length; i++)
			{
				array[i] = readObjectOverride();
			}
			value = array;
		}
		else if (token == ClassStreamHandler.PRIMITIVE_ARRAY)
		{
			short classDef = in.readShort();
			ClassStreamHandler lookup = ClassStreamHandler.lookup(classDef);
			value = lookup.readArray(this);
			handledObjects.put(handleCounter++,value);
		}
		else
		{
			throw new RuntimeException("not a valid token found: " + token);
		}
		return value;
	}
	
	/**
	 * @see java.io.ObjectInputStream#defaultReadObject()
	 */
	public void defaultReadObject() throws IOException, ClassNotFoundException
	{
		Object currentObject = stack.peek();
		if ( !defaultRead.contains(currentObject) )
		{
			defaultRead.add(currentObject);
			currentStreamHandler.readFields(this,currentObject);
		}
	}

	
	/**
	 * @see java.io.ObjectInputStream#close()
	 */
	public void close() throws IOException
	{
		stack = null;
		defaultRead = null;
		currentStreamHandler = null;
		in.close();
	}
	
    /**
     * Reads in a boolean.
     * 
     * @return	the boolean read.
     * @throws	EOFException If end of file is reached.
     * @throws	IOException If other I/O error has occurred.
     */
    public boolean readBoolean() throws IOException {
	return in.readBoolean();
    }

    /**
     * Reads an 8 bit byte.
     * 
     * @return	the 8 bit byte read.
     * @throws	EOFException If end of file is reached.
     * @throws	IOException If other I/O error has occurred.
     */
    public byte readByte() throws IOException  {
	return in.readByte();
    }

    /**
     * Reads an unsigned 8 bit byte.
     *
     * @return	the 8 bit byte read.
     * @throws	EOFException If end of file is reached.
     * @throws	IOException If other I/O error has occurred.
     */
    public int readUnsignedByte()  throws IOException {
	return in.readUnsignedByte();
    }

    /**
     * Reads a 16 bit char.
     *
     * @return	the 16 bit char read. 
     * @throws	EOFException If end of file is reached.
     * @throws	IOException If other I/O error has occurred.
     */
    public char readChar()  throws IOException {
	return in.readChar();
    }

    /**
     * Reads a 16 bit short.
     *
     * @return	the 16 bit short read.
     * @throws	EOFException If end of file is reached.
     * @throws	IOException If other I/O error has occurred.
     */
    public short readShort()  throws IOException {
	return in.readShort();
    }

    /**
     * Reads an unsigned 16 bit short.
     *
     * @return	the 16 bit short read.
     * @throws	EOFException If end of file is reached.
     * @throws	IOException If other I/O error has occurred.
     */
    public int readUnsignedShort() throws IOException {
	return in.readUnsignedShort();
    }

    /**
     * Reads a 32 bit int.
     *
     * @return	the 32 bit integer read.
     * @throws	EOFException If end of file is reached.
     * @throws	IOException If other I/O error has occurred.
     */
    public int readInt()  throws IOException {
	return in.readInt();
    }

    /**
     * Reads a 64 bit long.
     *
     * @return	the read 64 bit long.
     * @throws	EOFException If end of file is reached.
     * @throws	IOException If other I/O error has occurred.
     */
    public long readLong()  throws IOException {
	return in.readLong();
    }

    /**
     * Reads a 32 bit float.
     *
     * @return	the 32 bit float read.
     * @throws	EOFException If end of file is reached.
     * @throws	IOException If other I/O error has occurred.
     */
    public float readFloat() throws IOException {
	return in.readFloat();
    }

    /**
     * Reads a 64 bit double.
     *
     * @return	the 64 bit double read.
     * @throws	EOFException If end of file is reached.
     * @throws	IOException If other I/O error has occurred.
     */
    public double readDouble() throws IOException {
	return in.readDouble();
    }

    /**
     * Reads bytes, blocking until all bytes are read.
     *
     * @param	buf the buffer into which the data is read
     * @throws	EOFException If end of file is reached.
     * @throws	IOException If other I/O error has occurred.
     */
    public void readFully(byte[] buf) throws IOException {
	in.readFully(buf, 0, buf.length);
    }

    /**
     * Reads bytes, blocking until all bytes are read.
     *
     * @param	buf the buffer into which the data is read
     * @param	off the start offset of the data
     * @param	len the maximum number of bytes to read
     * @throws	EOFException If end of file is reached.
     * @throws	IOException If other I/O error has occurred.
     */
    public void readFully(byte[] buf, int off, int len) throws IOException {
	int endoff = off + len;
	if (off < 0 || len < 0 || endoff > buf.length || endoff < 0) {
	    throw new IndexOutOfBoundsException();
	}
	in.readFully(buf, off, len);
    }

    /**
     * @see java.io.ObjectInputStream#readUTF()
     */
    public String readUTF() throws IOException
    {
    	String s = in.readUTF();
    	return s;
    }
    
    /**
     * @see java.io.ObjectInputStream#read()
     */
    public int read() throws IOException
    {
    	return in.read();
    }
    
    /**
     * @see java.io.InputStream#read(byte[])
     */
    public int read(byte[] b) throws IOException
    {
    	return in.read(b);
    }
    
    /**
     * @see java.io.ObjectInputStream#read(byte[], int, int)
     */
    public int read(byte[] buf, int off, int len) throws IOException
    {
    	return in.read(buf, off, len);
    }
    
    /**
     * @see java.io.ObjectInputStream#readFields()
     */
    public GetField readFields() throws IOException, ClassNotFoundException
    {
    	GetFieldImpl field = new GetFieldImpl();
    	field.read();
    	return field;
    }
    
    
    private class GetFieldImpl extends GetField
    {
    	private HashMap values = new HashMap();
    	
    	private void read() throws IOException, ClassNotFoundException
    	{
    		short token = readShort();
			ClassStreamHandler lookup = ClassStreamHandler.lookup(boolean.class);
			if (token == lookup.getClassId())
			{
				short count = readShort();
				for (int i = 0; i < count; i++)
				{
					String key = (String)readObjectOverride();
					values.put(key, readBoolean()?Boolean.TRUE:Boolean.FALSE);
				}
				token = readShort();
				if (token == ClassStreamHandler.NULL) return;
			}
			lookup = ClassStreamHandler.lookup(byte.class);
			if (token == lookup.getClassId())
			{
				short count = readShort();
				for (int i = 0; i < count; i++)
				{
					String key = (String)readObjectOverride();
					values.put(key, new Byte(readByte()));
				}
				token = readShort();
				if (token == ClassStreamHandler.NULL) return;
			}
			lookup = ClassStreamHandler.lookup(short.class);
			if (token == lookup.getClassId())
			{
				short count = readShort();
				for (int i = 0; i < count; i++)
				{
					String key = (String)readObjectOverride();
					values.put(key, new Short(readShort()));
				}
				token = readShort();
				if (token == ClassStreamHandler.NULL) return;
			}
			lookup = ClassStreamHandler.lookup(char.class);
			if (token == lookup.getClassId())
			{
				short count = readShort();
				for (int i = 0; i < count; i++)
				{
					String key = (String)readObjectOverride();
					values.put(key, new Character(readChar()));
				}
				token = readShort();
				if (token == ClassStreamHandler.NULL) return;
			}
			lookup = ClassStreamHandler.lookup(int.class);
			if (token == lookup.getClassId())
			{
				short count = readShort();
				for (int i = 0; i < count; i++)
				{
					String key = (String)readObjectOverride();
					values.put(key, new Integer(readInt()));
				}
				token = readShort();
				if (token == ClassStreamHandler.NULL) return;
			}
			lookup = ClassStreamHandler.lookup(long.class);
			if (token == lookup.getClassId())
			{
				short count = readShort();
				for (int i = 0; i < count; i++)
				{
					String key = (String)readObjectOverride();
					values.put(key, new Long(readLong()));
				}
				token = readShort();
				if (token == ClassStreamHandler.NULL) return;
			}
			lookup = ClassStreamHandler.lookup(float.class);
			if (token == lookup.getClassId())
			{
				short count = readShort();
				for (int i = 0; i < count; i++)
				{
					String key = (String)readObjectOverride();
					values.put(key, new Float(readFloat()));
				}
				token = readShort();
				if (token == ClassStreamHandler.NULL) return;
			}
			lookup = ClassStreamHandler.lookup(double.class);
			if (token == lookup.getClassId())
			{
				short count = readShort();
				for (int i = 0; i < count; i++)
				{
					String key = (String)readObjectOverride();
					values.put(key, new Double(readDouble()));
				}
				token = readShort();
				if (token == ClassStreamHandler.NULL) return;
			}
			lookup = ClassStreamHandler.lookup(Serializable.class);
			if (token == lookup.getClassId())
			{
				short count = readShort();
				for (int i = 0; i < count; i++)
				{
					String key = (String)readObjectOverride();
					values.put(key, readObjectOverride());
				}
				token = readShort();
			}
			if (token != ClassStreamHandler.NULL)
			{
				throw new RuntimeException("Expected NULL end byte");
			}
    	}
    	
		/**
		 * @see java.io.ObjectInputStream.GetField#defaulted(java.lang.String)
		 */
		public boolean defaulted(String name) throws IOException
		{
			return values.get(name) == null;
		}

		/**
		 * @see java.io.ObjectInputStream.GetField#get(java.lang.String, byte)
		 */
		public byte get(String name, byte val) throws IOException
		{
			Object o = values.get(name);
			if (o instanceof Byte)
			{
				return ((Byte)o).byteValue();
			}
			return val;
		}

		/**
		 * @see java.io.ObjectInputStream.GetField#get(java.lang.String, char)
		 */
		public char get(String name, char val) throws IOException
		{
			Object o = values.get(name);
			if (o instanceof Byte)
			{
				return ((Character)o).charValue();
			}
			return val;
		}

		/**
		 * @see java.io.ObjectInputStream.GetField#get(java.lang.String, double)
		 */
		public double get(String name, double val) throws IOException
		{
			Object o = values.get(name);
			if (o instanceof Double)
			{
				return ((Double)o).doubleValue();
			}
			return val;
		}

		/**
		 * @see java.io.ObjectInputStream.GetField#get(java.lang.String, float)
		 */
		public float get(String name, float val) throws IOException
		{
			Object o = values.get(name);
			if (o instanceof Float)
			{
				return ((Float)o).floatValue();
			}
			return val;
		}

		/**
		 * @see java.io.ObjectInputStream.GetField#get(java.lang.String, int)
		 */
		public int get(String name, int val) throws IOException
		{
			Object o = values.get(name);
			if (o instanceof Integer)
			{
				return ((Integer)o).intValue();
			}
			return val;
		}

		/**
		 * @see java.io.ObjectInputStream.GetField#get(java.lang.String, long)
		 */
		public long get(String name, long val) throws IOException
		{
			Object o = values.get(name);
			if (o instanceof Long)
			{
				return ((Long)o).longValue();
			}
			return val;
		}

		/**
		 * @see java.io.ObjectInputStream.GetField#get(java.lang.String, short)
		 */
		public short get(String name, short val) throws IOException
		{
			Object o = values.get(name);
			if (o instanceof Short)
			{
				return ((Short)o).shortValue();
			}
			return val;
		}

		/**
		 * @see java.io.ObjectInputStream.GetField#get(java.lang.String, boolean)
		 */
		public boolean get(String name, boolean val) throws IOException
		{
			Object o = values.get(name);
			if (o instanceof Boolean)
			{
				return ((Boolean)o).booleanValue();
			}
			return val;
		}

		/**
		 * @see java.io.ObjectInputStream.GetField#get(java.lang.String, java.lang.Object)
		 */
		public Object get(String name, Object val) throws IOException
		{
			Object o = values.get(name);
			if (o != null)
			{
				return o;
			}
			return val;
		}

		/**
		 * @see java.io.ObjectInputStream.GetField#getObjectStreamClass()
		 */
		public ObjectStreamClass getObjectStreamClass()
		{
			return null;
		}
    	
    }
}
