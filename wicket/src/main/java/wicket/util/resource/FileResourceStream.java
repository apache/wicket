/*
 * $Id: FileResourceStream.java 5791 2006-05-20 00:32:57 +0000 (Sat, 20 May
 * 2006) joco01 $ $Revision$ $Date: 2006-05-20 00:32:57 +0000 (Sat, 20
 * May 2006) $
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.util.resource;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

import wicket.util.file.File;
import wicket.util.time.Time;

/**
 * A FileResourceStream is an IResource implementation for files.
 * 
 * @see wicket.util.resource.IResourceStream
 * @see wicket.util.watch.IModifiable
 * @author Jonathan Locke
 */
public class FileResourceStream extends AbstractResourceStream
		implements
			IFixedLocationResourceStream
{
	private static final long serialVersionUID = 1L;

	/** Any associated file */
	private File file;

	/** Resource stream */
	private transient InputStream inputStream;

	/**
	 * Constructor.
	 * 
	 * @param file
	 *            File containing resource
	 */
	public FileResourceStream(final File file)
	{
		this.file = file;
	}

	/**
	 * Closes this resource.
	 * 
	 * @throws IOException
	 */
	public void close() throws IOException
	{
		if (inputStream != null)
		{
			inputStream.close();
			inputStream = null;
		}
	}

	/**
	 * @return The content type of this resource, such as "image/jpeg" or
	 *         "text/html"
	 */
	public String getContentType()
	{
		return URLConnection.getFileNameMap().getContentTypeFor(file.getName());
	}

	/**
	 * @return The file this resource resides in, if any.
	 */
	public File getFile()
	{
		return file;
	}

	/**
	 * @return A readable input stream for this resource. The same input stream
	 *         is returned until <tt>FileResourceStream.close()</tt> is
	 *         invoked.
	 * @throws ResourceStreamNotFoundException
	 */
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
				throw new ResourceStreamNotFoundException("Resource " + file
						+ " could not be found", e);
			}
		}

		return inputStream;
	}

	/**
	 * @see wicket.util.watch.IModifiable#lastModifiedTime()
	 * @return The last time this resource was modified
	 */
	public Time lastModifiedTime()
	{
		if (file != null)
		{
			return file.lastModifiedTime();
		}
		return null;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		if (file != null)
		{
			return file.toString();
		}
		return "";
	}

	/**
	 * @see wicket.util.resource.IResourceStream#length()
	 */
	public long length()
	{
		if (file != null)
		{
			return file.length();
		}
		return 0;
	}

	/**
	 * @see wicket.util.resource.IFixedLocationResourceStream#locationAsString()
	 */
	public String locationAsString()
	{
		if (file != null)
		{
			return file.getAbsolutePath();
		}
		return null;
	}
}
