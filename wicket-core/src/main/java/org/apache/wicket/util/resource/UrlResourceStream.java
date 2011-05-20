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
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

import org.apache.wicket.Application;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.file.Files;
import org.apache.wicket.util.io.Connections;
import org.apache.wicket.util.io.IOUtils;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.util.lang.Objects;
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

	/**
	 * The meta data for this stream. Lazy loaded on demand.
	 */
	private transient StreamData streamData;

	/** The URL to this resource. */
	private final URL url;

	/** the handle to the file if it is a file resource 
	 * (only for checking last modified timestamp) */
	private File fileForLastModified;

	/**
	 * Meta data class for the stream attributes
	 */
	private static class StreamData
	{
		private URLConnection connection;

		/** Length of stream. */
		private long contentLength;

		/** Content type for stream. */
		private String contentType;

		/** Last known time the stream was last modified. */
		private Time lastModified;

	}

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

		// try to retrieve local file from url
		this.fileForLastModified = Files.getLocalFileFromUrl(url);

		// try to retrieve file location otherwise
		if (this.fileForLastModified == null)
		{

			try
			{
				fileForLastModified = new File(new URI(url.toExternalForm()));
			}
			catch (Exception e)
			{
				log.debug("cannot convert url: " + url + " to file (" + e.getMessage() +
				          "), falling back to the inputstream for polling");
			}
		}
		if (fileForLastModified != null && !fileForLastModified.exists())
		{
			fileForLastModified = null;
		}
	}

	/**
	 * Lazy loads the stream settings on demand
	 * 
	 * @param initialize
	 *            a flag indicating whether to load the settings
	 * @return the meta data with the stream settings
	 */
	private StreamData getData(boolean initialize)
	{
		if (streamData == null && initialize)
		{
			streamData = new StreamData();

			try
			{
				streamData.connection = url.openConnection();
				streamData.contentLength = streamData.connection.getContentLength();
				streamData.contentType = streamData.connection.getContentType();

				if (streamData.contentType == null || streamData.contentType.contains("unknown"))
				{
					if (Application.exists() && Application.get() instanceof WebApplication)
					{
						// TODO Post 1.2: General: For non webapplication another method
						// should be implemented (getMimeType on application?)
						streamData.contentType = WebApplication.get()
							.getServletContext()
							.getMimeType(url.getFile());
						if (streamData.contentType == null)
						{
							streamData.contentType = URLConnection.getFileNameMap()
								.getContentTypeFor(url.getFile());
						}
					}
					else
					{
						streamData.contentType = URLConnection.getFileNameMap().getContentTypeFor(
							url.getFile());
					}
				}
			}
			catch (IOException ex)
			{
				throw new IllegalArgumentException("Invalid URL parameter " + url, ex);
			}
		}

		return streamData;
	}

	/**
	 * Closes this resource.
	 * 
	 * @throws IOException
	 */
	public void close() throws IOException
	{
		StreamData data = getData(false);

		if (data != null)
		{
			Connections.closeQuietly(data.connection);
			streamData = null;
		}
	}

	/**
	 * @return The content type of this resource, such as "image/jpeg" or "text/html"
	 */
	@Override
	public String getContentType()
	{
		return getData(true).contentType;
	}

	/**
	 * @return A readable input stream for this resource.
	 * @throws ResourceStreamNotFoundException
	 */
	public InputStream getInputStream() throws ResourceStreamNotFoundException
	{
		InputStream inputStream;
		try
		{
			inputStream = getData(true).connection.getInputStream();
		}
		catch (IOException e)
		{
			throw new ResourceStreamNotFoundException("Resource " + url + " could not be opened", e);
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
		try
		{
			StreamData data = getData(true);

			final Time lastModified;

			if (fileForLastModified != null)
			{
				// get file modification timestamp
				lastModified = IOUtils.getLastModified(fileForLastModified);
			}
			else
			{
				// get url modification timestamp
				lastModified = Connections.getLastModified(url);
			}

			// if timestamp changed: update content length and last modified date
			if (Objects.equal(lastModified, data.lastModified) == false)
			{
				data.lastModified = lastModified;
				setContentLength();
			}
			return data.lastModified;
		}
		catch (IOException e)
		{
			if (url.toString().contains(".jar!"))
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

	private void setContentLength() throws IOException
	{
		StreamData data = getData(true);
		URLConnection connection = url.openConnection();
		data.contentLength = connection.getContentLength();
		Connections.close(connection);
	}

	@Override
	public String toString()
	{
		return url.toString();
	}

	@Override
	public Bytes length()
	{
		long contentLength = getData(true).contentLength;
		return Bytes.bytes(contentLength);
	}

	public String locationAsString()
	{
		return url.toExternalForm();
	}
}
