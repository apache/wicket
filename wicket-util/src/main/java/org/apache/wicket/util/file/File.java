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
package org.apache.wicket.util.file;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.URI;

import org.apache.wicket.util.io.Streams;
import org.apache.wicket.util.time.Time;
import org.apache.wicket.util.watch.IModifiable;


/**
 * Simple extension of File that adds an implementation of IModifiable for files. This allows the
 * ModificationWatcher class to watch files for modification. The IModifiable.lastModifiedTime()
 * method also returns a Time object with a more convenient API than either Date or a value in
 * milliseconds.
 * 
 * @author Jonathan Locke
 */
public class File extends java.io.File implements IModifiable
{
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 * 
	 * @param parent
	 *            parent
	 * @param child
	 *            child
	 */
	public File(final File parent, final String child)
	{
		super(parent, child);
	}

	/**
	 * Construct.
	 * 
	 * @param parent
	 *            parent
	 * @param child
	 *            child
	 */
	public File(final java.io.File parent, final String child)
	{
		super(parent, child);
	}

	/**
	 * Construct.
	 * 
	 * @param file
	 *            File from java.io package
	 */
	public File(final java.io.File file)
	{
		super(file.getAbsolutePath());
	}

	/**
	 * Constructor.
	 * 
	 * @param pathname
	 *            path name
	 */
	public File(final String pathname)
	{
		super(pathname);
	}

	/**
	 * Constructor.
	 * 
	 * @param parent
	 *            parent
	 * @param child
	 *            child
	 */
	public File(final String parent, final String child)
	{
		super(parent, child);
	}

	/**
	 * Constructor.
	 * 
	 * @param uri
	 *            file uri
	 */
	public File(final URI uri)
	{
		super(uri);
	}

	/**
	 * @param name
	 *            Name of child file
	 * @return Child file object
	 */
	public File file(final String name)
	{
		return new File(this, name);
	}

	/**
	 * @return File extension (whatever is after the last '.' in the file name)
	 */
	public String getExtension()
	{
		final int lastDot = getName().lastIndexOf('.');
		if (lastDot >= 0)
		{
			return getName().substring(lastDot + 1);
		}
		return null;
	}

	/**
	 * @return Parent folder
	 */
	public Folder getParentFolder()
	{
		return new Folder(getParent());
	}

	/**
	 * @return Input stream that reads this file
	 * @throws FileNotFoundException
	 *             Thrown if the file cannot be found
	 */
	public InputStream inputStream() throws FileNotFoundException
	{
		return new BufferedInputStream(new FileInputStream(this));
	}

	/**
	 * Returns a Time object representing the most recent time this file was modified.
	 *
	 * @return This file's lastModified() value as a Time object or <code>null</code> if
	 * that information is not available
	 */
	@Override
	public Time lastModifiedTime()
	{
		final long time = lastModified();
		
		if(time == 0)
		{
			return null;
		}
		return Time.millis(time);
	}

	/**
	 * Creates a buffered output stream that writes to this file. If the parent folder does not yet
	 * exist, creates all necessary folders in the path.
	 * 
	 * @return Output stream that writes to this file
	 * @throws FileNotFoundException
	 *             Thrown if the file cannot be found
	 */
	public OutputStream outputStream() throws FileNotFoundException
	{
		final Folder parent = getParentFolder();
		if (!parent.exists())
		{
			if (!parent.mkdirs())
			{
				throw new FileNotFoundException("Couldn't create path " + parent);
			}
		}
		return new BufferedOutputStream(new FileOutputStream(this));
	}

	/**
	 * @return String read from this file
	 * @throws IOException
	 */
	public String readString() throws IOException
	{
		final InputStream in = new FileInputStream(this);
		try
		{
			return Streams.readString(in);
		}
		finally
		{
			in.close();
		}
	}

	/**
	 * @return Object read from serialization file
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public Object readObject() throws IOException, ClassNotFoundException
	{
		return new ObjectInputStream(inputStream()).readObject();
	}

	/**
	 * @param object
	 *            Object to write to this file
	 * @throws IOException
	 */
	public void writeObject(final Serializable object) throws IOException
	{
		new ObjectOutputStream(outputStream()).writeObject(object);
	}

	/**
	 * @return True if the file was removed
	 * @see java.io.File#delete()
	 */
	public boolean remove()
	{
		return Files.remove(this);
	}

	/**
	 * Force contents of file to physical storage
	 * 
	 * @throws IOException
	 */
	public void sync() throws IOException
	{
		final FileInputStream in = new FileInputStream(this);
		try
		{
			in.getFD().sync();
		}
		finally
		{
			in.close();
		}
	}

	/**
	 * @return This file in double quotes (useful for passing to commands and tools that have issues
	 *         with spaces in filenames)
	 */
	public String toQuotedString()
	{
		return "\"" + toString() + "\"";
	}

	/**
	 * Writes the given file to this one
	 * 
	 * @param file
	 *            The file to copy
	 * @return number of bytes written
	 * @throws IOException
	 */
	public int write(final File file) throws IOException
	{
		final InputStream in = new BufferedInputStream(new FileInputStream(file));
		try
		{
			return write(in);
		}
		finally
		{
			in.close();
		}
	}

	/**
	 * Writes the given input stream to this file
	 * 
	 * @param input
	 *            The input
	 * @return Number of bytes written
	 * @throws IOException
	 */
	public int write(final InputStream input) throws IOException
	{
		return Files.writeTo(this, input);
	}

	/**
	 * Write the given string to this file
	 * 
	 * @param string
	 *            The string to write
	 * @throws IOException
	 */
	public void write(final String string) throws IOException
	{
		final FileWriter out = new FileWriter(this);
		try
		{
			out.write(string);
		}
		finally
		{
			out.close();
		}
	}
}
