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
package org.apache.wicket.util.resource;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

import org.apache.wicket.util.file.File;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.util.time.Time;


/**
 * A FileResourceStream is an IResource implementation for files.
 * 
 * @see org.apache.wicket.util.resource.IResourceStream
 * @see org.apache.wicket.util.watch.IModifiable
 * @author Jonathan Locke
 */
public class FileResourceStream extends AbstractResourceStream
	implements
		IFixedLocationResourceStream
{
	private static final long serialVersionUID = 1L;

	/** Any associated file */
	private final File file;

	/** Resource stream */
	private transient InputStream inputStream;

	/**
	 * Constructor.
	 * 
	 * @param file
	 *            {@link File} containing resource
	 */
	public FileResourceStream(final File file)
	{
		Args.notNull(file, "file");
		this.file = file;
	}

	/**
	 * Constructor.
	 * 
	 * @param file
	 *            {@link java.io.File} containing resource
	 */
	public FileResourceStream(final java.io.File file)
	{
		this.file = new File(file);
	}

	/**
	 * Closes this resource.
	 * 
	 * @throws IOException
	 */
	@Override
	public void close() throws IOException
	{
		if (inputStream != null)
		{
			inputStream.close();
			inputStream = null;
		}
	}

	@Override
	public String getContentType()
	{
		String contentType = null;
		if (file != null)
		{
			contentType = URLConnection.getFileNameMap().getContentTypeFor(file.getName());
		}
		return contentType;
	}

	/**
	 * @return The file this resource resides in, if any.
	 */
	public File getFile()
	{
		return file;
	}

	/**
	 * @return A readable input stream for this resource. The same input stream is returned until
	 *         <tt>FileResourceStream.close()</tt> is invoked.
	 * 
	 * @throws ResourceStreamNotFoundException
	 */
	@Override
	public InputStream getInputStream() throws ResourceStreamNotFoundException
	{
		if (inputStream == null)
		{
			try
			{
				inputStream = new FileInputStream(file);
			}
			catch (FileNotFoundException e)
			{
				throw new ResourceStreamNotFoundException("Resource " + file +
					" could not be found", e);
			}
		}

		return inputStream;
	}

	/**
	 * @see org.apache.wicket.util.watch.IModifiable#lastModifiedTime()
	 * @return The last time this resource was modified
	 */
	@Override
	public Time lastModifiedTime()
	{
		if (file != null)
		{
			return file.lastModifiedTime();
		}
		return null;
	}

	@Override
	public String toString()
	{
		if (file != null)
		{
			return file.toString();
		}
		return "";
	}

	@Override
	public Bytes length()
	{
		if (file != null)
		{
			return Bytes.bytes(file.length());
		}
		return null;
	}

	@Override
	public String locationAsString()
	{
		if (file != null)
		{
			return file.getAbsolutePath();
		}
		return null;
	}
}
