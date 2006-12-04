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
package wicket.util.file;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import wicket.util.io.Streams;
import wicket.util.string.Strings;

/**
 * File utility methods.
 * 
 * @author Jonathan Locke
 */
public class Files
{
	/**
	 * Strips off the given extension (probably returned from Files.extension())
	 * from the path, yielding a base pathname.
	 * 
	 * @param path
	 *            The path, possibly with an extension to strip
	 * @param extension
	 *            The extension to strip, or null if no extension exists
	 * @return The path without any extension
	 */
	public static String basePath(final String path, final String extension)
	{
		if (extension != null)
		{
			return path.substring(0, path.length() - extension.length() - 1);
		}
		return path;
	}

	/**
	 * Gets extension from path
	 * 
	 * @param path
	 *            The path
	 * @return The extension, like "bmp" or "html", or null if none can be found
	 */
	public static String extension(final String path)
	{
		if (path.indexOf('.') != -1)
		{
			return Strings.lastPathComponent(path, '.');
		}
		return null;
	}

	/**
	 * Gets filename from path
	 * 
	 * @param path
	 *            The path
	 * @return The filename
	 */
	public static String filename(final String path)
	{
		return Strings.lastPathComponent(path.replace('/', java.io.File.separatorChar),
				java.io.File.separatorChar);
	}

	/**
	 * Deletes a file, dealing with a particularly nasty bug on Windows.
	 * 
	 * @param file
	 *            File to delete
	 * @return True if file was deleted
	 */
	public static boolean remove(final java.io.File file)
	{
		// Delete current file
		if (!file.delete())
		{
			// NOTE: fix for java/win bug. see:
			// http://forum.java.sun.com/thread.jsp?forum=4&thread=158689&tstart=0&trange=15
			System.gc();
			try
			{
				Thread.sleep(100);
			}
			catch (InterruptedException e)
			{
			}

			// Try one more time to delete the file
			return file.delete();
		}
		return true;
	}

	/**
	 * Writes the given input stream to the given file
	 * 
	 * @param file
	 *            The file to write to
	 * @param input
	 *            The input
	 * @return Number of bytes written
	 * @throws IOException
	 */
	public static final int writeTo(final java.io.File file, final InputStream input)
			throws IOException
	{
		final FileOutputStream out = new FileOutputStream(file);
		try
		{
			return Streams.copy(input, out);
		}
		finally
		{
			out.close();
		}
	}

	/**
	 * Private constructor to prevent instantiation.
	 */
	private Files()
	{
	}
}
