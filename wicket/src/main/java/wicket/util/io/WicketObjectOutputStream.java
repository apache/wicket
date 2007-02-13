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
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

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
			out.writeShort((int)handle);
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
						// this should be different!!
						// write directly the primitives 
						int length = Array.getLength(obj);
						out.writeInt(length);
						for (int i = 0; i < length; i++)
						{
							writeObjectOverride(Array.get(obj, i));
						}
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
						stack.push(obj);
						if (!classHandler.invokeWriteMethod(this,obj))
						{
							classHandler.writeFields(this,obj);
						}
						stack.pop();
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
	 * @return
	 */
	DataOutputStream getOutputStream()
	{
		return out;
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
    {
    	out.write(buf);
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
	 * @see java.io.ObjectOutputStream#close()
	 */
	public void close() throws IOException
	{
		classHandler = null;
		stack = null;
		defaultWrite = null;
		out.close();
	}
}
