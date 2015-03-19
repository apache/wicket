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

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;

import org.apache.wicket.util.file.Files;
import org.apache.wicket.util.time.Time;

/**
 * {@link URLConnection} related utilities
 * 
 * @author igor.vaynberg
 */
public class Connections
{
	private Connections()
	{
	}

	/**
	 * Gets last modified date of the given {@link URL}
	 * 
	 * @param url
	 * @return last modified timestamp or <code>null</code> if not available
	 * @throws IOException
	 */
	public static Time getLastModified(final URL url) throws IOException
	{
		// check if url points to a local file
		final File file = Files.getLocalFileFromUrl(url);

		if (file != null)
		{
			// in that case we can get the timestamp faster
			return Files.getLastModified(file);
		}

		// otherwise open the url and proceed
		URLConnection connection = url.openConnection();
		connection.setDoInput(false);

		final long milliseconds;

		try
		{
			if (connection instanceof JarURLConnection)
			{
				JarURLConnection jarUrlConnection = (JarURLConnection)connection;
				URL jarFileUrl = jarUrlConnection.getJarFileURL();
				URLConnection jarFileConnection = jarFileUrl.openConnection();
				jarFileConnection.setDoInput(false);
				// get timestamp from JAR
				milliseconds = jarFileConnection.getLastModified();
			}
			else
			{
				// get timestamp from URL
				milliseconds = connection.getLastModified();
			}

			// return null if timestamp is unavailable
			if (milliseconds == 0)
			{
				return null;
			}

			// return UNIX timestamp
			return Time.millis(milliseconds);

		}
		finally
		{
			closeQuietly(connection);
		}
	}

	/**
	 * Closes a connection, ignoring any exceptions if they occur
	 * 
	 * @param connection
	 */
	public static void closeQuietly(final URLConnection connection)
	{
		try
		{
			close(connection);
		}
		catch (Exception e)
		{
			// ignore
		}
	}

	/**
	 * Closes a connection
	 * 
	 * @param connection
	 * @throws IOException
	 */
	public static void close(final URLConnection connection) throws IOException
	{
		if (connection == null)
		{
			return;
		}

		String protocol = connection.getURL().getProtocol();
		if ("file".equals(protocol)) // XXX what about JarUrlConnection ?!
		{
			// Close FileUrlConnection's input stream because
			// otherwise it leaks open file handles. See WICKET-4359.
			// Most other connection types should not call getInputStream() here,
			// especially remote connections.
			connection.getInputStream().close();
		}

		if (connection instanceof HttpURLConnection)
		{
			((HttpURLConnection)connection).disconnect();
		}
	}
}
