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
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

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
	 * @return last modified timestamp
	 * @throws IOException
	 */
	public static long getLastModified(URL url) throws IOException
	{

		URLConnection connection = url.openConnection();

		try
		{
			if (connection instanceof JarURLConnection)
			{
				JarURLConnection jarUrlConnection = (JarURLConnection)connection;
				URL jarFileUrl = jarUrlConnection.getJarFileURL();
				URLConnection jarFileConnection = jarFileUrl.openConnection();
				try
				{
					return jarFileConnection.getLastModified();
				}
				finally
				{
					close(jarFileConnection);
				}
			}
			else
			{
				return connection.getLastModified();
			}

		}
		finally
		{
			close(connection);
		}
	}

	/**
	 * Tries to find a file on the harddisk that the url points to
	 * 
	 * @param url
	 * @return file file pointing to the connection
	 * @throws Exception
	 *             if file could not be located
	 */
	public static File findFile(final URL url) throws Exception
	{
		File file = null;
		URL fileUrl = url;
		URLConnection connection = null;

		try
		{
			connection = url.openConnection();

			// if this is a connection to a file inside a jar we point the file to the jar
			// itself
			if (connection instanceof JarURLConnection)
			{
				fileUrl = ((JarURLConnection)connection).getJarFileURL();
			}

			file = new File(new URI(fileUrl.toExternalForm()));

			if (file != null && !file.exists())
			{
				file = null;
			}

			return file;
		}
		finally
		{
			close(connection);
		}
	}

	/**
	 * Closes a connection, ignoring any exceptions if they occur
	 * 
	 * @param connection
	 */
	public static void closeQuietly(URLConnection connection)
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
	public static void close(URLConnection connection) throws IOException
	{
		if (connection == null)
		{
			return;
		}

		if (connection instanceof HttpURLConnection)
		{
			((HttpURLConnection)connection).disconnect();
		}
		else
		{
			connection.getInputStream().close();
		}
	}

}
