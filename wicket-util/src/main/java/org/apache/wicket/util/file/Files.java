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
package org.apache.wicket.util.file;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;

import org.apache.wicket.util.io.IOUtils;
import org.apache.wicket.util.io.Streams;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * File utility methods.
 * 
 * @author Jonathan Locke
 */
public class Files
{
	private static final Logger logger = LoggerFactory.getLogger(Files.class);

	// protocols for urls
	private static final String URL_FILE_PREFIX = "file:";
	private static final String URL_LOCAL_JAR_FILE_PREFIX = "jar:file:";


	/**
	 * Private constructor to prevent instantiation.
	 */
	private Files()
	{
	}

	/**
	 * Strips off the given extension (probably returned from Files.extension()) from the path,
	 * yielding a base pathname.
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
	 * Deletes a normal file.
	 * <p>
	 * If the file cannot be deleted for any reason then at most 50 retries are attempted with delay
	 * of 100ms at each 10th attempt.
	 * 
	 * @param file
	 *            the file to delete
	 * @return {@code true} if file was deleted, {@code false} if the file don't exist, is a folder
	 *         or cannot be removed for some reason
	 */
	public static boolean remove(final java.io.File file)
	{
		if (file != null && file.isFile())
		{
			for (int j = 0; j < 5; ++j)
			{
				for (int i = 0; i < 10; ++i)
				{
					if (file.delete())
					{
						return true;
					}

					try
					{
						Thread.sleep(100);
					}
					catch (InterruptedException ix)
					{
						Thread.currentThread().interrupt();
					}
				}
			}
		}

		return false;
	}

	/**
	 * Deletes a folder by recursively removing the files and folders inside it. Delegates the work
	 * to {@link #remove(File)} for plain files.
	 * 
	 * @param folder
	 *            the folder to delete
	 * @return {@code true} if the folder is deleted successfully.
	 */
	public static final boolean removeFolder(final File folder)
	{
		if (folder == null)
		{
			return false;
		}

		if (folder.isDirectory())
		{
			File[] files = folder.listFiles();
			if (files != null)
			{
				for (File file : files)
				{
					if (file.isDirectory())
					{
						removeFolder(file);
					}
					else
					{
						remove(file);
					}
				}
			}
		}

		// delete the empty folder
		return folder.delete();
	}

	/**
	 * Schedules a file for removal asynchronously.
	 * 
	 * @param file
	 *            the file to be removed
	 * @param fileCleaner
	 *            the file cleaner that will be used to remove the file
	 * @return {@code false} if the {@code file} is <em>null</em> or a folder, {@code true} -
	 *         otherwise (i.e. if it is scheduled)
	 */
	public static final boolean removeAsync(final File file, final IFileCleaner fileCleaner)
	{
		if (file == null || file.isDirectory())
		{
			return false;
		}

		Args.notNull(fileCleaner, "fileCleaner");

		fileCleaner.track(file, new Object());

		return true;
	}


	/**
	 * Schedules a folder and all files inside it for asynchronous removal.
	 * 
	 * @param folder
	 *            the folder to be removed
	 * @param fileCleaner
	 *            the file cleaner that will be used to remove the file
	 * @return {@code false} if the {@code folder} is <em>null</em> or a normal file, {@code true} -
	 *         otherwise (i.e. if it is scheduled)
	 */
	public static final boolean removeFolderAsync(final File folder, final IFileCleaner fileCleaner)
	{
		if (folder == null || folder.isFile())
		{
			return false;
		}

		Args.notNull(fileCleaner, "fileCleaner");

		fileCleaner.track(folder, new Object(), new FolderDeleteStrategy());

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
		return writeTo(file, input, 4096);
	}

	/**
	 * read binary file fully
	 * 
	 * @param file
	 *            file to read
	 * @return byte array representing the content of the file
	 * @throws IOException
	 *             is something went wrong
	 */
	public static byte[] readBytes(final File file) throws IOException
	{
		FileInputStream stream = new FileInputStream(file);

		try
		{
			return IOUtils.toByteArray(stream);
		}
		finally
		{
			stream.close();
		}
	}

	/**
	 * Writes the given input stream to the given file
	 * 
	 * @param file
	 *            The file to write to
	 * @param input
	 *            The input
	 * @param bufSize
	 *            The memory buffer size. 4096 is a good value.
	 * @return Number of bytes written
	 * @throws IOException
	 */
	public static final int writeTo(final java.io.File file, final InputStream input,
		final int bufSize) throws IOException
	{
		final FileOutputStream out = new FileOutputStream(file);
		try
		{
			return Streams.copy(input, out, bufSize);
		}
		finally
		{
			out.close();
		}
	}

	private static String FORBIDDEN_IN_NAME = "\"*/:<>?\\|,";

	/**
	 * <p>
	 * Replaces commonly unsupported characters with '_'
	 * </p>
	 * 
	 * @param filename
	 *            to be cleaned
	 * @return cleaned filename
	 */
	public static final String cleanupFilename(final String filename)
	{
		String name = filename;
		for (int i = 0; i < FORBIDDEN_IN_NAME.length(); i++)
		{
			name = name.replace(FORBIDDEN_IN_NAME.charAt(i), '_');
		}
		return name;
	}

	/**
	 * make a copy of a file
	 * 
	 * @param sourceFile
	 *            source file that needs to be cloned
	 * @param targetFile
	 *            target file that should be a duplicate of source file
	 * @throws IOException
	 *             if something went wrong
	 */
	public static void copy(final File sourceFile, final File targetFile) throws IOException
	{
		BufferedInputStream in = null;
		BufferedOutputStream out = null;

		try
		{
			in = new BufferedInputStream(new FileInputStream(sourceFile));
			out = new BufferedOutputStream(new FileOutputStream(targetFile));

			IOUtils.copy(in, out);
		}
		finally
		{
			try
			{
				IOUtils.close(in);

			}
			finally
			{
				IOUtils.close(out);
			}
		}
	}

	/**
	 * for urls that point to local files (e.g. 'file:' or 'jar:file:') this methods returns a
	 * reference to the local file
	 * 
	 * @param url
	 *            url of the resource
	 * 
	 * @return reference to a local file if url contains one, <code>null</code> otherwise
	 * 
	 * @see #getLocalFileFromUrl(String)
	 */
	public static File getLocalFileFromUrl(URL url)
	{
		final URL location = Args.notNull(url, "url");

		try
		{
			return getLocalFileFromUrl(URLDecoder.decode(location.toExternalForm(), "UTF-8"));
		}
		catch (UnsupportedEncodingException ex)
		{
			return null;
		}
	}

	/**
	 * for urls that point to local files (e.g. 'file:' or 'jar:file:') this methods returns a
	 * reference to the local file
	 * 
	 * @param url
	 *            url of the resource
	 * 
	 * @return reference to a local file if url contains one, <code>null</code> otherwise
	 * 
	 * @see #getLocalFileFromUrl(URL)
	 */
	public static File getLocalFileFromUrl(String url)
	{
		final String location = Args.notNull(url, "url");

		// check for 'file:'
		if (location.startsWith(URL_FILE_PREFIX))
		{
			return new File(location.substring(URL_FILE_PREFIX.length()));
		}
		// check for 'jar:file:'
		else if (location.startsWith(URL_LOCAL_JAR_FILE_PREFIX))
		{
			final String path = location.substring(URL_LOCAL_JAR_FILE_PREFIX.length());
			final int resourceAt = path.indexOf('!');

			// for jar:file: the '!' is mandatory
			if (resourceAt == -1)
			{
				return null;
			}
			return new File(path.substring(0, resourceAt));
		}
		else
		{
			return null;
		}
	}

	/**
	 * get last modification timestamp for file
	 * 
	 * @param file
	 * 
	 * @return timestamp
	 */
	public static Time getLastModified(File file)
	{
		// get file modification timestamp
		long millis = file.lastModified();

		// zero indicates the timestamp could not be retrieved or the file does not exist
		if (millis == 0)
		{
			return null;
		}

		// last file modification timestamp
		return Time.millis(millis);
	}

	/**
	 * Utility method for creating a directory. If the creation didn't succeed for some reason then
	 * at most 50 attempts are made with delay of 100ms at every 10th attempt.
	 * 
	 * @param folder
	 *            the folder to create
	 * @return {@code true} if the creation is successful, {@code false} - otherwise
	 */
	public static boolean mkdirs(File folder)
	{
		// for some reason, simple file.mkdirs sometimes fails under heavy load
		for (int j = 0; j < 5; ++j)
		{
			for (int i = 0; i < 10; ++i)
			{
				if (folder.mkdirs())
				{
					return true;
				}
			}
			try
			{
				Thread.sleep(100);
				if (folder.exists()) return true;
			}
			catch (InterruptedException ix)
			{
				Thread.currentThread().interrupt();
			}
		}
		logger.error("Failed to create directory: " + folder);
		return false;
	}
}
