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
package org.apache.wicket.core.util.resource;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Application;
import org.apache.wicket.util.io.Connections;
import org.apache.wicket.util.io.IOUtils;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.util.lang.Objects;
import org.apache.wicket.util.resource.AbstractResourceStream;
import org.apache.wicket.util.resource.IFixedLocationResourceStream;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
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

	/** Last known time the stream was last modified. */
	private Time lastModified;

	/**
	 * Meta data class for the stream attributes
	 */
	private static class StreamData
	{
		private URLConnection connection;

		/**
		 * The streams read from this connection.
		 * Some URLConnection implementations return the same instance of InputStream
		 * every time URLConnection#getInputStream() is called. Other return a new instance
		 * of InputStream.
		 * Here we keep a list of all returned ones and close them in UrlResourceStream#close().
		 * Even it is the same instance several times we will try to close it quietly several times.
		 */
		private List<InputStream> inputStreams;

		/** Length of stream. */
		private long contentLength;

		/** Content type for stream. */
		private String contentType;

	}

	/**
	 * Construct.
	 *
	 * @param url
	 *            URL of resource
	 */
	public UrlResourceStream(final URL url)
	{
		this.url = Args.notNull(url, "url");
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

				if (Application.exists())
				{
					streamData.contentType = Application.get().getMimeType(url.getFile());
				}
				else
				{
					streamData.contentType = streamData.connection.getContentType();
				}

				if (streamData.contentType == null || streamData.contentType.contains("unknown"))
				{
					streamData.contentType = URLConnection.getFileNameMap().getContentTypeFor(
						url.getFile());
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
	@Override
	public void close() throws IOException
	{
		StreamData data = getData(false);

		if (data != null)
		{
			Connections.closeQuietly(data.connection);
			if (data.inputStreams != null)
			{
				for (InputStream is : data.inputStreams) {
					IOUtils.closeQuietly(is);
				}
			}
			streamData = null;
		}
	}

	/**
	 * @return A readable input stream for this resource.
	 * @throws ResourceStreamNotFoundException
	 */
	@Override
	public InputStream getInputStream() throws ResourceStreamNotFoundException
	{
		try
		{
			StreamData data = getData(true);
			InputStream is = data.connection.getInputStream();
			if (data.inputStreams == null) {
				data.inputStreams = new ArrayList<>();
			}
			data.inputStreams.add(is);
			return is;
		}
		catch (IOException e)
		{
			throw new ResourceStreamNotFoundException("Resource " + url + " could not be opened", e);
		}
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
			// get url modification timestamp
			final Time time = Connections.getLastModified(url);

			// if timestamp changed: update content length and last modified date
			if (Objects.equal(time, lastModified) == false)
			{
				lastModified = time;
				updateContentLength();
			}
			return lastModified;
		}
		catch (IOException e)
		{
			log.warn("getLastModified for " + url + " failed: " + e.getMessage());

			// allow modification watcher to detect the problem
			return null;
		}
	}

	private void updateContentLength() throws IOException
	{
		StreamData data = getData(false);

		if (data != null)
		{
			URLConnection connection = url.openConnection();
			try {
				data.contentLength = connection.getContentLength();
			} finally {
				Connections.close(connection);
			}
		}
	}

	@Override
	public String toString()
	{
		return url.toString();
	}

	/**
	 * @return The content type of this resource, such as "image/jpeg" or "text/html"
	 */
	@Override
	public String getContentType()
	{
		return getData(true).contentType;
	}

	@Override
	public Bytes length()
	{
		long length = getData(true).contentLength;

		if (length == -1)
		{
			return null;
		}

		return Bytes.bytes(length);
	}

	@Override
	public String locationAsString()
	{
		return url.toExternalForm();
	}
}
