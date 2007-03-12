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
package wicket.util.resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.Application;
import wicket.protocol.http.WebApplication;
import wicket.util.time.Time;

/**
 * UrlResourceStream implements IResource for URLs.
 * 
 * @see wicket.util.resource.IResourceStream
 * @see wicket.util.watch.IModifiable
 * @author Jonathan Locke
 */
public class UrlResourceStream extends AbstractResourceStream
{
	private static final long serialVersionUID = 1L;

	/** Logging. */
	private static final Log log = LogFactory.getLog(UrlResourceStream.class);

	/** Resource stream. */
	private transient InputStream inputStream;

	/** The URL to this resource. */
	private URL url;

	/**
	 * the handle to the file if it is a file resource
	 */
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
		// Save URL
		this.url = url;
		URLConnection connection = null;
		try
		{
			connection = url.openConnection();
			contentLength = connection.getContentLength();
			contentType = connection.getContentType();
			lastModified = connection.getLastModified();
			try
			{
				file = new File(new URI(url.toExternalForm()));
			}
			catch (Exception ex)
			{
				log.debug("cannot convert url: " + url + " to file (" + ex.getMessage()
						+ "), falling back to the inputstream for polling");
			}
			if (file != null && !file.exists())
			{
				file = null;
			}
		}
		catch (IOException ex)
		{
			// It should be impossible to get here or the original URL
			// couldn't have been constructed. But we re-throw with details
			// anyway.
			final IllegalArgumentException illegalArgumentException = new IllegalArgumentException(
					"Invalid URL parameter " + url);
			illegalArgumentException.initCause(ex);
			throw illegalArgumentException;
		}
		finally
		{
			// if applicable, disconnect
			if (connection != null)
			{
				if (connection instanceof HttpURLConnection)
				{
					((HttpURLConnection)connection).disconnect();
				}
				else
				{
					try
					{
						connection.getInputStream().close();
					}
					catch (Exception ex)
					{
						// ignore
					}
				}
			}
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
		testContentType();
		return contentType;
	}

	/**
	 * Method to test the content type on null or unknown. if this is the case
	 * the content type is tried to be resolved throw the servlet context
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
				contentType = ((WebApplication)application).getServletContext()
						.getMimeType(url.getFile());
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
				throw new ResourceStreamNotFoundException("Resource " + url
						+ " could not be opened", e);
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
			long lastModified = file.lastModified();
			if (lastModified != this.lastModified)
			{
				this.lastModified = lastModified;
				this.contentLength = (int)file.length();
			}
		}
		else
		{
			URLConnection urlConnection = null;
			boolean close = false;
			try
			{
				urlConnection = url.openConnection();
				long lastModified = this.lastModified;
				if (urlConnection instanceof JarURLConnection)
				{
					JarURLConnection jarUrlConnection = (JarURLConnection)urlConnection;
					URL jarFileUrl = jarUrlConnection.getJarFileURL();
					URLConnection jarFileConnection = jarFileUrl.openConnection();
					try
					{
						lastModified = jarFileConnection.getLastModified();
					}
					finally
					{
						jarFileConnection.getInputStream().close();
					}
				}
				else
				{
					close = true;
					lastModified = urlConnection.getLastModified();
				}
				// update the last modified time.
				if (lastModified != this.lastModified)
				{
					this.lastModified = lastModified;
					close = true;
					this.contentLength = urlConnection.getContentLength();
				}
			}
			catch (IOException e)
			{
				log.error("getLastModified for " + url + " failed: " + e.getMessage());
			}
			finally
			{
				// if applicable, disconnect
				if (urlConnection != null)
				{
					if (urlConnection instanceof HttpURLConnection)
					{
						((HttpURLConnection)urlConnection).disconnect();
					}
					else if (close)
					{
						try
						{
							urlConnection.getInputStream().close();
						}
						catch (Exception ex)
						{
							// ignore
						}
					}
				}
			}
		}
		return Time.milliseconds(lastModified);
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return url.toString();
	}

	/**
	 * @see wicket.util.resource.IResourceStream#length()
	 */
	public long length()
	{
		return contentLength;
	}
}
