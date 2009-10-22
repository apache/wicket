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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.apache.wicket.Application;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.io.Connections;
import org.apache.wicket.util.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * UrlResourceStream implements IResource for URLs.
 * 
 * @see org.apache.wicket.util.resource.IResourceStream
 * @see org.apache.wicket.util.watch.IModifiable
 * @author Jonathan Locke
 * @author Igor Vaynberg
 */
public class UrlResourceStream extends AbstractResourceStream
	implements
		IFixedLocationResourceStream
{
	private static final long serialVersionUID = 1L;

	/** Logging. */
	private static final Logger log = LoggerFactory.getLogger(UrlResourceStream.class);

	/** Resource stream. */
	private transient InputStream inputStream;

	/** The URL to this resource. */
	private final URL url;

	/** the handle to the file if it is a file resource */
	private File file;

	/** Length of stream. */
	private int contentLength;

	/** Content type for stream. */
	private String contentType;

	/** Last known time the stream was last modified. */
	private long lastModified;

	/**
	 * Construct.
	 * 
	 * @param url
	 *            URL of resource
	 */
	public UrlResourceStream(final URL url)
	{
		// save the url
		this.url = url;

		// retrieve the content type and length
		URLConnection connection = null;
		try
		{
			connection = url.openConnection();
			contentLength = connection.getContentLength();
			contentType = connection.getContentType();
		}
		catch (IOException ex)
		{
			throw new IllegalArgumentException("Invalid URL parameter " + url, ex);
		}
		finally
		{
			Connections.closeQuietly(connection);
		}

		try
		{
			file = Connections.findFile(url);
		}
		catch (Exception e)
		{
			log.debug("cannot convert url: " + url + " to file (" + e.getMessage() +
				"), falling back to the inputstream for polling");
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
	 * @return The content type of this resource, such as "image/jpeg" or "text/html"
	 */
	@Override
	public String getContentType()
	{
		testContentType();
		return contentType;
	}

	/**
	 * Method to test the content type on null or unknown. if this is the case the content type is
	 * tried to be resolved throw the servlet context
	 */
	private void testContentType()
	{
		if (contentType == null || contentType.indexOf("unknown") != -1)
		{
			Application application = Application.get();
			if (application instanceof WebApplication)
			{
				// TODO Post 1.2: General: For non webapplication another method
				// should be implemented (getMimeType on application?)
				contentType = ((WebApplication)application).getServletContext().getMimeType(
					url.getFile());
				if (contentType == null)
				{
					contentType = URLConnection.getFileNameMap().getContentTypeFor(url.getFile());
				}
			}
			else
			{
				contentType = URLConnection.getFileNameMap().getContentTypeFor(url.getFile());
			}
		}
	}

	/**
	 * @return A readable input stream for this resource.
	 * @throws ResourceStreamNotFoundException
	 */
	public InputStream getInputStream() throws ResourceStreamNotFoundException
	{
		if (inputStream == null)
		{
			try
			{
				inputStream = url.openStream();
			}
			catch (IOException e)
			{
				throw new ResourceStreamNotFoundException("Resource " + url +
					" could not be opened", e);
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
	 * @see org.apache.wicket.util.watch.IModifiable#lastModifiedTime()
	 * @return The last time this resource was modified
	 */
	@Override
	public Time lastModifiedTime()
	{
		if (file != null)
		{
			// in case the file has been removed by now
			if (file.exists() == false)
			{
				return null;
			}

			long lastModified = file.lastModified();

			// if last modified changed update content length and last modified date
			if (lastModified != this.lastModified)
			{
				this.lastModified = lastModified;
				contentLength = (int)file.length();
			}
		}
		else
		{
			try
			{
				long lastModified = Connections.getLastModified(url);

				// if last modified changed update content length and last modified date
				if (lastModified != this.lastModified)
				{
					this.lastModified = lastModified;

					URLConnection connection = url.openConnection();
					contentLength = connection.getContentLength();
					Connections.close(connection);
				}
			}
			catch (IOException e)
			{
				if (url.toString().indexOf(".jar!") >= 0)
				{
					if (log.isDebugEnabled())
					{
						log.debug("getLastModified for " + url + " failed: " + e.getMessage());
					}
				}
				else
				{
					log.warn("getLastModified for " + url + " failed: " + e.getMessage());
				}

				// allow modification watcher to detect the problem
				return null;
			}

		}
		return Time.milliseconds(lastModified);
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return url.toString();
	}

	/**
	 * @see org.apache.wicket.util.resource.IResourceStream#length()
	 */
	@Override
	public long length()
	{
		return contentLength;
	}

	/**
	 * @see org.apache.wicket.util.resource.IFixedLocationResourceStream#locationAsString()
	 */
	public String locationAsString()
	{
		return url.toExternalForm();
	}
}
