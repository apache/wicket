/*
 * $Id$
 * $Revision$ $Date$
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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.util.file.File;
import wicket.util.time.Time;

/**
 * UrlResource implements IResource for URLs.
 * 
 * @see wicket.util.resource.IResource
 * @see wicket.util.watch.IModifiable
 * @author Jonathan Locke
 */
public final class UrlResource extends AbstractResource
{
	/** Logging */
	private static Log log = LogFactory.getLog(UrlResource.class);

	/** The underlying file if this URL points to a file */
	private File file;

	/** Resource stream */
	private transient InputStream inputStream;

	/** The URL to this resource */
	private URL url;

	/**
	 * Private constructor to force use of static factory methods.
	 * 
	 * @param url
	 *            URL of resource
	 */
	public UrlResource(final URL url)
	{
		try
		{
			// Get file from URL (see
			// http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4701321)
			final File file = new File(new URI(url.toExternalForm()));

			// If file exists
			if (file != null && file.exists())
			{
				// save that file for future modification time queries
				this.file = file;
			}

			// Save URL
			this.url = url;
		}
		catch (URISyntaxException e)
		{
			final IllegalArgumentException illegalArgumentException = new IllegalArgumentException(
					"Invalid URL parameter " + url);
			illegalArgumentException.initCause(e);
			throw illegalArgumentException;
		}
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
		return URLConnection.getFileNameMap().getContentTypeFor(url.getFile());
	}

	/**
	 * @return The file this resource resides in, if any.
	 */
	public File getFile()
	{
		return file;
	}

	/**
	 * @return A readable input stream for this resource.
	 * @throws ResourceNotFoundException
	 */
	public InputStream getInputStream() throws ResourceNotFoundException
	{
		if (inputStream == null)
		{
			try
			{
				inputStream = url.openStream();
			}
			catch (IOException e)
			{
				throw new ResourceNotFoundException("Resource " + url + " could not be opened", e);
			}
		}

		return inputStream;
	}

	/**
	 * @return The URL to this resource (if any)
	 */
	public URL getURL()
	{
		return url;
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
	public String toString()
	{
		return url.toString();
	}
}
