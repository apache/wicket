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
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author jcompagner
 */
public final class WicketObjectInputStream extends ObjectInputStream
{
	 
	private Map handledObjects = new HashMap(); 
	private short handleCounter = 0;

	
	private final DataInputStream in;
	private ClassStreamHandler currentStreamHandler;
	private Object currentObject;

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
			value = handledObjects.get(new Short(handle));
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
				handledObjects.put(new Short(handleCounter++),value);
			}
			else
			{
				try
				{
					value = currentStreamHandler.createObject();
					handledObjects.put(new Short(handleCounter++),value);
					currentObject = value;
					if ( !currentStreamHandler.invokeReadMethod(this, value))
					{
						currentStreamHandler.readFields(this,value);
					}
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
			handledObjects.put(new Short(handleCounter++),array);
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
			int length = in.readInt();
			Object[] array = (Object[])Array.newInstance(lookup.getStreamClass(), length);
			handledObjects.put(new Short(handleCounter++),array);
			for (int i = 0; i < array.length; i++)
			{
				array[i] = readObjectOverride();
			}
			value = array;
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
		currentStreamHandler.readFields(this,currentObject);
	}

	/**
	 * @return
	 */
	DataInputStream getInputStream()
	{
		return in;
	}
	
	/**
	 * @see java.io.ObjectInputStream#close()
	 */
	public void close() throws IOException
	{
		currentObject = null;
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
    	return in.readUTF();
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
}
