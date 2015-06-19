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
package org.apache.wicket.util.io;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.CharArrayWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * General IO Stream manipulation.
 * <p>
 * This class provides static utility methods for input/output operations.
 * <ul>
 * <li>closeQuietly - these method closes any kind of closeable resource, e.g. an input/output
 * stream or reader/writer ignoring nulls and exceptions
 * <li>toXxx - these methods read data from a stream
 * <li>write - these methods write data to a stream
 * <li>copy - these methods copy all the data from one stream to another
 * <li>contentEquals - these methods compare the content of two streams
 * </ul>
 * <p>
 * The byte-to-char methods and char-to-byte methods involve a conversion step. Two methods are
 * provided in each case, one that uses the platform default encoding and the other which allows you
 * to specify an encoding. You are encouraged to always specify an encoding because relying on the
 * platform default can lead to unexpected results, for example when moving from development to
 * production.
 * <p>
 * All the methods in this class that read a stream are buffered internally. This means that there
 * is no cause to use a <code>BufferedInputStream</code> or <code>BufferedReader</code>. The default
 * buffer size of 4K has been show to be efficient in tests.
 * <p>
 * Wherever possible, the methods in this class do <em>not</em> flush or close the stream. This is
 * to avoid making non-portable assumptions about the streams' origin and further use. Thus the
 * caller is still responsible for closing streams after use.
 * <p>
 * Origin of code: Apache Avalon (Excalibur)
 * 
 * @author Peter Donald
 * @author Jeff Turner
 * @author Matthew Hawthorne
 * @author Stephen Colebourne
 * @author Gareth Davis
 */
public final class IOUtils
{
	private static final Logger log = LoggerFactory.getLogger(IOUtils.class);

	// NOTE: This class is focused on InputStream, OutputStream, Reader and
	// Writer. Each method should take at least one of these as a parameter.
	// NOTE: This class should not depend on any other classes

	/**
	 * The default buffer size to use.
	 */
	private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

	/**
	 * Instances should NOT be constructed in standard programming.
	 */
	public IOUtils()
	{
	}

	/**
	 * Closes a closeable. Guards against null closables.
	 * 
	 * @param closeable
	 *            closeable to close
	 * @throws IOException
	 *             when close fails
	 */
	public static void close(final Closeable closeable) throws IOException
	{
		if (closeable != null)
		{
			closeable.close();
		}
	}

	/**
	 * Unconditionally close a <code>Closeable</code>.
	 * <p>
	 * closeables can be input or output streams, reader, writers, and much more.
	 * 
	 * Equivalent to {@link Closeable#close()}, except any exceptions will be ignored. This is
	 * typically used in finally blocks.
	 * 
	 * @param closeable
	 *            the Closeable to close, may be null or already closed
	 */
	public static void closeQuietly(final Closeable closeable)
	{
		try
		{
			close(closeable);
		}
		catch (IOException e)
		{
			log.debug("closing resource failed: " + e.getMessage(), e);
		}
	}

	/**
	 * Get the contents of an <code>InputStream</code> as a <code>byte[]</code>.
	 * <p>
	 * This method buffers the input internally, so there is no need to use a
	 * <code>BufferedInputStream</code>.
	 * 
	 * @param input
	 *            the <code>InputStream</code> to read from
	 * @return the requested byte array
	 * @throws NullPointerException
	 *             if the input is null
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	public static byte[] toByteArray(final InputStream input) throws IOException
	{
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		copy(input, output);
		return output.toByteArray();
	}

	/**
	 * Get the contents of a <code>Reader</code> as a <code>byte[]</code> using the default
	 * character encoding of the platform.
	 * <p>
	 * This method buffers the input internally, so there is no need to use a
	 * <code>BufferedReader</code>.
	 * 
	 * @param input
	 *            the <code>Reader</code> to read from
	 * @return the requested byte array
	 * @throws NullPointerException
	 *             if the input is null
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	public static byte[] toByteArray(final Reader input) throws IOException
	{
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		copy(input, output);
		return output.toByteArray();
	}

	/**
	 * Get the contents of a <code>Reader</code> as a <code>byte[]</code> using the specified
	 * character encoding.
	 * <p>
	 * Character encoding names can be found at <a
	 * href="http://www.iana.org/assignments/character-sets">IANA</a>.
	 * <p>
	 * This method buffers the input internally, so there is no need to use a
	 * <code>BufferedReader</code>.
	 * 
	 * @param input
	 *            the <code>Reader</code> to read from
	 * @param encoding
	 *            the encoding to use, null means platform default
	 * @return the requested byte array
	 * @throws NullPointerException
	 *             if the input is null
	 * @throws IOException
	 *             if an I/O error occurs
	 * @since 1.1
	 */
	public static byte[] toByteArray(final Reader input, final String encoding) throws IOException
	{
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		copy(input, output, encoding);
		return output.toByteArray();
	}

	/**
	 * Get the contents of an <code>InputStream</code> as a character array using the default
	 * character encoding of the platform.
	 * <p>
	 * This method buffers the input internally, so there is no need to use a
	 * <code>BufferedInputStream</code>.
	 * 
	 * @param is
	 *            the <code>InputStream</code> to read from
	 * @return the requested character array
	 * @throws NullPointerException
	 *             if the input is null
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	public static char[] toCharArray(final InputStream is) throws IOException
	{
		CharArrayWriter output = new CharArrayWriter();
		copy(is, output);
		return output.toCharArray();
	}

	/**
	 * Get the contents of an <code>InputStream</code> as a character array using the specified
	 * character encoding.
	 * <p>
	 * Character encoding names can be found at <a
	 * href="http://www.iana.org/assignments/character-sets">IANA</a>.
	 * <p>
	 * This method buffers the input internally, so there is no need to use a
	 * <code>BufferedInputStream</code>.
	 * 
	 * @param is
	 *            the <code>InputStream</code> to read from
	 * @param encoding
	 *            the encoding to use, null means platform default
	 * @return the requested character array
	 * @throws NullPointerException
	 *             if the input is null
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	public static char[] toCharArray(final InputStream is, final String encoding)
		throws IOException
	{
		CharArrayWriter output = new CharArrayWriter();
		copy(is, output, encoding);
		return output.toCharArray();
	}

	/**
	 * Get the contents of a <code>Reader</code> as a character array.
	 * <p>
	 * This method buffers the input internally, so there is no need to use a
	 * <code>BufferedReader</code>.
	 * 
	 * @param input
	 *            the <code>Reader</code> to read from
	 * @return the requested character array
	 * @throws NullPointerException
	 *             if the input is null
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	public static char[] toCharArray(final Reader input) throws IOException
	{
		CharArrayWriter sw = new CharArrayWriter();
		copy(input, sw);
		return sw.toCharArray();
	}

	/**
	 * Get the contents of an <code>InputStream</code> as a String using the default character
	 * encoding of the platform.
	 * <p>
	 * This method buffers the input internally, so there is no need to use a
	 * <code>BufferedInputStream</code>.
	 * 
	 * @param input
	 *            the <code>InputStream</code> to read from
	 * @return the requested String
	 * @throws NullPointerException
	 *             if the input is null
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	public static String toString(final InputStream input) throws IOException
	{
		StringWriter sw = new StringWriter();
		copy(input, sw);
		return sw.toString();
	}

	/**
	 * Get the contents of an <code>InputStream</code> as a String using the specified character
	 * encoding.
	 * <p>
	 * Character encoding names can be found at <a
	 * href="http://www.iana.org/assignments/character-sets">IANA</a>.
	 * <p>
	 * This method buffers the input internally, so there is no need to use a
	 * <code>BufferedInputStream</code>.
	 * 
	 * @param input
	 *            the <code>InputStream</code> to read from
	 * @param encoding
	 *            the encoding to use, null means platform default
	 * @return the requested String
	 * @throws NullPointerException
	 *             if the input is null
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	public static String toString(final InputStream input, final String encoding)
		throws IOException
	{
		StringWriter sw = new StringWriter();
		copy(input, sw, encoding);
		return sw.toString();
	}

	/**
	 * Get the contents of a <code>Reader</code> as a String.
	 * <p>
	 * This method buffers the input internally, so there is no need to use a
	 * <code>BufferedReader</code>.
	 * 
	 * @param input
	 *            the <code>Reader</code> to read from
	 * @return the requested String
	 * @throws NullPointerException
	 *             if the input is null
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	public static String toString(final Reader input) throws IOException
	{
		StringWriter sw = new StringWriter();
		copy(input, sw);
		return sw.toString();
	}

	/**
	 * Writes bytes from a <code>byte[]</code> to an <code>OutputStream</code>.
	 * 
	 * @param data
	 *            the byte array to write, do not modify during output, null ignored
	 * @param output
	 *            the <code>OutputStream</code> to write to
	 * @throws NullPointerException
	 *             if output is null
	 * @throws IOException
	 *             if an I/O error occurs
	 * @since 1.1
	 */
	public static void write(final byte[] data, final OutputStream output) throws IOException
	{
		if (data != null)
		{
			output.write(data);
		}
	}

	/**
	 * Writes bytes from a <code>byte[]</code> to chars on a <code>Writer</code> using the default
	 * character encoding of the platform.
	 * <p>
	 * This method uses {@link String#String(byte[])}.
	 * 
	 * @param data
	 *            the byte array to write, do not modify during output, null ignored
	 * @param output
	 *            the <code>Writer</code> to write to
	 * @throws NullPointerException
	 *             if output is null
	 * @throws IOException
	 *             if an I/O error occurs
	 * @since 1.1
	 */
	public static void write(final byte[] data, final Writer output) throws IOException
	{
		if (data != null)
		{
			output.write(new String(data));
		}
	}

	/**
	 * Writes bytes from a <code>byte[]</code> to chars on a <code>Writer</code> using the specified
	 * character encoding.
	 * <p>
	 * Character encoding names can be found at <a
	 * href="http://www.iana.org/assignments/character-sets">IANA</a>.
	 * <p>
	 * This method uses {@link String#String(byte[], String)}.
	 * 
	 * @param data
	 *            the byte array to write, do not modify during output, null ignored
	 * @param output
	 *            the <code>Writer</code> to write to
	 * @param encoding
	 *            the encoding to use, null means platform default
	 * @throws NullPointerException
	 *             if output is null
	 * @throws IOException
	 *             if an I/O error occurs
	 * @since 1.1
	 */
	public static void write(final byte[] data, final Writer output, final String encoding)
		throws IOException
	{
		if (data != null)
		{
			if (encoding == null)
			{
				write(data, output);
			}
			else
			{
				output.write(new String(data, encoding));
			}
		}
	}

	/**
	 * Writes chars from a <code>char[]</code> to a <code>Writer</code> using the default character
	 * encoding of the platform.
	 * 
	 * @param data
	 *            the char array to write, do not modify during output, null ignored
	 * @param output
	 *            the <code>Writer</code> to write to
	 * @throws NullPointerException
	 *             if output is null
	 * @throws IOException
	 *             if an I/O error occurs
	 * @since 1.1
	 */
	public static void write(final char[] data, final Writer output) throws IOException
	{
		if (data != null)
		{
			output.write(data);
		}
	}

	/**
	 * Writes chars from a <code>char[]</code> to bytes on an <code>OutputStream</code>.
	 * <p>
	 * This method uses {@link String#String(char[])} and {@link String#getBytes()}.
	 * 
	 * @param data
	 *            the char array to write, do not modify during output, null ignored
	 * @param output
	 *            the <code>OutputStream</code> to write to
	 * @throws NullPointerException
	 *             if output is null
	 * @throws IOException
	 *             if an I/O error occurs
	 * @since 1.1
	 */
	public static void write(final char[] data, final OutputStream output) throws IOException
	{
		if (data != null)
		{
			output.write(new String(data).getBytes());
		}
	}

	/**
	 * Writes chars from a <code>char[]</code> to bytes on an <code>OutputStream</code> using the
	 * specified character encoding.
	 * <p>
	 * Character encoding names can be found at <a
	 * href="http://www.iana.org/assignments/character-sets">IANA</a>.
	 * <p>
	 * This method uses {@link String#String(char[])} and {@link String#getBytes(String)}.
	 * 
	 * @param data
	 *            the char array to write, do not modify during output, null ignored
	 * @param output
	 *            the <code>OutputStream</code> to write to
	 * @param encoding
	 *            the encoding to use, null means platform default
	 * @throws NullPointerException
	 *             if output is null
	 * @throws IOException
	 *             if an I/O error occurs
	 * @since 1.1
	 */
	public static void write(final char[] data, final OutputStream output, final String encoding)
		throws IOException
	{
		if (data != null)
		{
			if (encoding == null)
			{
				write(data, output);
			}
			else
			{
				output.write(new String(data).getBytes(encoding));
			}
		}
	}

	/**
	 * Writes chars from a <code>String</code> to a <code>Writer</code>.
	 * 
	 * @param data
	 *            the <code>String</code> to write, null ignored
	 * @param output
	 *            the <code>Writer</code> to write to
	 * @throws NullPointerException
	 *             if output is null
	 * @throws IOException
	 *             if an I/O error occurs
	 * @since 1.1
	 */
	public static void write(final String data, final Writer output) throws IOException
	{
		if (data != null)
		{
			output.write(data);
		}
	}

	/**
	 * Writes chars from a <code>String</code> to bytes on an <code>OutputStream</code> using the
	 * default character encoding of the platform.
	 * <p>
	 * This method uses {@link String#getBytes()}.
	 * 
	 * @param data
	 *            the <code>String</code> to write, null ignored
	 * @param output
	 *            the <code>OutputStream</code> to write to
	 * @throws NullPointerException
	 *             if output is null
	 * @throws IOException
	 *             if an I/O error occurs
	 * @since 1.1
	 */
	public static void write(final String data, final OutputStream output) throws IOException
	{
		if (data != null)
		{
			output.write(data.getBytes());
		}
	}

	/**
	 * Writes chars from a <code>String</code> to bytes on an <code>OutputStream</code> using the
	 * specified character encoding.
	 * <p>
	 * Character encoding names can be found at <a
	 * href="http://www.iana.org/assignments/character-sets">IANA</a>.
	 * <p>
	 * This method uses {@link String#getBytes(String)}.
	 * 
	 * @param data
	 *            the <code>String</code> to write, null ignored
	 * @param output
	 *            the <code>OutputStream</code> to write to
	 * @param encoding
	 *            the encoding to use, null means platform default
	 * @throws NullPointerException
	 *             if output is null
	 * @throws IOException
	 *             if an I/O error occurs
	 * @since 1.1
	 */
	public static void write(final String data, final OutputStream output, final String encoding)
		throws IOException
	{
		if (data != null)
		{
			if (encoding == null)
			{
				write(data, output);
			}
			else
			{
				output.write(data.getBytes(encoding));
			}
		}
	}

	/**
	 * Writes chars from a <code>AppendingStringBuffer</code> to a <code>Writer</code>.
	 * 
	 * @param data
	 *            the <code>AppendingStringBuffer</code> to write, null ignored
	 * @param output
	 *            the <code>Writer</code> to write to
	 * @throws NullPointerException
	 *             if output is null
	 * @throws IOException
	 *             if an I/O error occurs
	 * @since 1.1
	 */
	public static void write(final StringBuilder data, final Writer output) throws IOException
	{
		if (data != null)
		{
			output.write(data.toString());
		}
	}

	/**
	 * Writes chars from a <code>AppendingStringBuffer</code> to bytes on an
	 * <code>OutputStream</code> using the default character encoding of the platform.
	 * <p>
	 * This method uses {@link String#getBytes()}.
	 * 
	 * @param data
	 *            the <code>AppendingStringBuffer</code> to write, null ignored
	 * @param output
	 *            the <code>OutputStream</code> to write to
	 * @throws NullPointerException
	 *             if output is null
	 * @throws IOException
	 *             if an I/O error occurs
	 * @since 1.1
	 */
	public static void write(final StringBuilder data, final OutputStream output)
		throws IOException
	{
		if (data != null)
		{
			output.write(data.toString().getBytes());
		}
	}

	/**
	 * Writes chars from a <code>AppendingStringBuffer</code> to bytes on an
	 * <code>OutputStream</code> using the specified character encoding.
	 * <p>
	 * Character encoding names can be found at <a
	 * href="http://www.iana.org/assignments/character-sets">IANA</a>.
	 * <p>
	 * This method uses {@link String#getBytes(String)}.
	 * 
	 * @param data
	 *            the <code>AppendingStringBuffer</code> to write, null ignored
	 * @param output
	 *            the <code>OutputStream</code> to write to
	 * @param encoding
	 *            the encoding to use, null means platform default
	 * @throws NullPointerException
	 *             if output is null
	 * @throws IOException
	 *             if an I/O error occurs
	 * @since 1.1
	 */
	public static void write(final StringBuilder data, final OutputStream output,
		final String encoding) throws IOException
	{
		if (data != null)
		{
			if (encoding == null)
			{
				write(data, output);
			}
			else
			{
				output.write(data.toString().getBytes(encoding));
			}
		}
	}

	/**
	 * Copy bytes from an <code>InputStream</code> to an <code>OutputStream</code>.
	 * <p>
	 * This method buffers the input internally, so there is no need to use a
	 * <code>BufferedInputStream</code>.
	 * 
	 * @param input
	 *            the <code>InputStream</code> to read from
	 * @param output
	 *            the <code>OutputStream</code> to write to
	 * @return the number of bytes copied
	 * @throws NullPointerException
	 *             if the input or output is null
	 * @throws IOException
	 *             if an I/O error occurs
	 * @since 1.1
	 */
	public static int copy(final InputStream input, final OutputStream output) throws IOException
	{
		byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
		int count = 0;
		int n = 0;
		while (-1 != (n = input.read(buffer)))
		{
			output.write(buffer, 0, n);
			count += n;
		}
		return count;
	}

	/**
	 * Copy bytes from an <code>InputStream</code> to chars on a <code>Writer</code> using the
	 * default character encoding of the platform.
	 * <p>
	 * This method buffers the input internally, so there is no need to use a
	 * <code>BufferedInputStream</code>.
	 * <p>
	 * This method uses {@link InputStreamReader}.
	 * 
	 * @param input
	 *            the <code>InputStream</code> to read from
	 * @param output
	 *            the <code>Writer</code> to write to
	 * @throws NullPointerException
	 *             if the input or output is null
	 * @throws IOException
	 *             if an I/O error occurs
	 * @since 1.1
	 */
	public static void copy(final InputStream input, final Writer output) throws IOException
	{
		InputStreamReader in = new InputStreamReader(input);
		copy(in, output);
	}

	/**
	 * Copy bytes from an <code>InputStream</code> to chars on a <code>Writer</code> using the
	 * specified character encoding.
	 * <p>
	 * This method buffers the input internally, so there is no need to use a
	 * <code>BufferedInputStream</code>.
	 * <p>
	 * Character encoding names can be found at <a
	 * href="http://www.iana.org/assignments/character-sets">IANA</a>.
	 * <p>
	 * This method uses {@link InputStreamReader}.
	 * 
	 * @param input
	 *            the <code>InputStream</code> to read from
	 * @param output
	 *            the <code>Writer</code> to write to
	 * @param encoding
	 *            the encoding to use, null means platform default
	 * @throws NullPointerException
	 *             if the input or output is null
	 * @throws IOException
	 *             if an I/O error occurs
	 * @since 1.1
	 */
	public static void copy(final InputStream input, final Writer output, final String encoding)
		throws IOException
	{
		if (encoding == null)
		{
			copy(input, output);
		}
		else
		{
			InputStreamReader in = new InputStreamReader(input, encoding);
			copy(in, output);
		}
	}

	/**
	 * Copy chars from a <code>Reader</code> to a <code>Writer</code>.
	 * <p>
	 * This method buffers the input internally, so there is no need to use a
	 * <code>BufferedReader</code>.
	 * 
	 * @param input
	 *            the <code>Reader</code> to read from
	 * @param output
	 *            the <code>Writer</code> to write to
	 * @return the number of characters copied
	 * @throws NullPointerException
	 *             if the input or output is null
	 * @throws IOException
	 *             if an I/O error occurs
	 * @since 1.1
	 */
	public static int copy(final Reader input, final Writer output) throws IOException
	{
		char[] buffer = new char[DEFAULT_BUFFER_SIZE];
		int count = 0;
		int n = 0;
		while (-1 != (n = input.read(buffer)))
		{
			output.write(buffer, 0, n);
			count += n;
		}
		return count;
	}

	/**
	 * Copy chars from a <code>Reader</code> to bytes on an <code>OutputStream</code> using the
	 * default character encoding of the platform, and calling flush.
	 * <p>
	 * This method buffers the input internally, so there is no need to use a
	 * <code>BufferedReader</code>.
	 * <p>
	 * Due to the implementation of OutputStreamWriter, this method performs a flush.
	 * <p>
	 * This method uses {@link OutputStreamWriter}.
	 * 
	 * @param input
	 *            the <code>Reader</code> to read from
	 * @param output
	 *            the <code>OutputStream</code> to write to
	 * @throws NullPointerException
	 *             if the input or output is null
	 * @throws IOException
	 *             if an I/O error occurs
	 * @since 1.1
	 */
	public static void copy(final Reader input, final OutputStream output) throws IOException
	{
		OutputStreamWriter out = new OutputStreamWriter(output);
		copy(input, out);
		out.flush();
	}

	/**
	 * Copy chars from a <code>Reader</code> to bytes on an <code>OutputStream</code> using the
	 * specified character encoding, and calling flush.
	 * <p>
	 * This method buffers the input internally, so there is no need to use a
	 * <code>BufferedReader</code>.
	 * <p>
	 * Character encoding names can be found at <a
	 * href="http://www.iana.org/assignments/character-sets">IANA</a>.
	 * <p>
	 * Due to the implementation of OutputStreamWriter, this method performs a flush.
	 * <p>
	 * This method uses {@link OutputStreamWriter}.
	 * 
	 * @param input
	 *            the <code>Reader</code> to read from
	 * @param output
	 *            the <code>OutputStream</code> to write to
	 * @param encoding
	 *            the encoding to use, null means platform default
	 * @throws NullPointerException
	 *             if the input or output is null
	 * @throws IOException
	 *             if an I/O error occurs
	 * @since 1.1
	 */
	public static void copy(final Reader input, final OutputStream output, final String encoding)
		throws IOException
	{
		if (encoding == null)
		{
			copy(input, output);
		}
		else
		{
			OutputStreamWriter out = new OutputStreamWriter(output, encoding);
			copy(input, out);
			out.flush();
		}
	}

	/**
	 * Compare the contents of two Streams to determine if they are equal or not.
	 * <p>
	 * This method buffers the input internally using <code>BufferedInputStream</code> if they are
	 * not already buffered.
	 * 
	 * @param input1
	 *            the first stream
	 * @param input2
	 *            the second stream
	 * @return true if the content of the streams are equal or they both don't exist, false
	 *         otherwise
	 * @throws NullPointerException
	 *             if either input is null
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	public static boolean contentEquals(InputStream input1, InputStream input2) throws IOException
	{
		if (!(input1 instanceof BufferedInputStream))
		{
			input1 = new BufferedInputStream(input1);
		}
		if (!(input2 instanceof BufferedInputStream))
		{
			input2 = new BufferedInputStream(input2);
		}

		int ch = input1.read();
		while (-1 != ch)
		{
			int ch2 = input2.read();
			if (ch != ch2)
			{
				return false;
			}
			ch = input1.read();
		}

		int ch2 = input2.read();
		return (ch2 == -1);
	}

	/**
	 * Compare the contents of two Readers to determine if they are equal or not.
	 * <p>
	 * This method buffers the input internally using <code>BufferedReader</code> if they are not
	 * already buffered.
	 * 
	 * @param input1
	 *            the first reader
	 * @param input2
	 *            the second reader
	 * @return true if the content of the readers are equal or they both don't exist, false
	 *         otherwise
	 * @throws NullPointerException
	 *             if either input is null
	 * @throws IOException
	 *             if an I/O error occurs
	 * @since 1.1
	 */
	public static boolean contentEquals(Reader input1, Reader input2) throws IOException
	{
		if (!(input1 instanceof BufferedReader))
		{
			input1 = new BufferedReader(input1);
		}
		if (!(input2 instanceof BufferedReader))
		{
			input2 = new BufferedReader(input2);
		}

		int ch = input1.read();
		while (-1 != ch)
		{
			int ch2 = input2.read();
			if (ch != ch2)
			{
				return false;
			}
			ch = input1.read();
		}

		int ch2 = input2.read();
		return (ch2 == -1);
	}
}
