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

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.wicket.util.lang.Bytes;

/**
 * This folder subclass provides some type safety and extensibility for "files" that hold other
 * files.
 * 
 * @author Jonathan Locke
 */
public class Folder extends File
{
	/**
	 * Filter for files
	 * 
	 * @author Jonathan Locke
	 */
	public static interface FileFilter
	{
		/**
		 * File filter that matches all files
		 */
		public static FileFilter ALL_FILES = new FileFilter()
		{
			@Override
			public boolean accept(final File file)
			{
				return true;
			}
		};

		/**
		 * @param file
		 *            The file to test
		 * @return True if the file should be accepted
		 */
		public boolean accept(File file);
	}

	/**
	 * Filter for folders
	 * 
	 * @author Jonathan Locke
	 */
	public static interface FolderFilter
	{
		/**
		 * @param folder
		 *            The folder to test
		 * @return True if the file should be accepted
		 */
		public boolean accept(Folder folder);
	}

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 * 
	 * @param parent
	 *            parent
	 * @param child
	 *            child
	 */
	public Folder(final Folder parent, final String child)
	{
		super(parent, child);
	}

	/**
	 * Construct.
	 * 
	 * @param file
	 *            File
	 */
	public Folder(final java.io.File file)
	{
		this(file.getPath());
	}

	/**
	 * Constructor.
	 * 
	 * @param pathname
	 *            path name
	 */
	public Folder(final String pathname)
	{
		super(pathname);
	}

	/**
	 * Constructor.
	 * 
	 * @param parent
	 *            parent
	 * @param child
	 *            child
	 */
	public Folder(final String parent, final String child)
	{
		super(parent, child);
	}

	/**
	 * Constructor.
	 * 
	 * @param uri
	 *            folder uri
	 */
	public Folder(final URI uri)
	{
		super(uri);
	}

	/**
	 * Does a mkdirs() on this folder if it does not exist. If the folder cannot be created, an
	 * IOException is thrown.
	 * 
	 * @throws IOException
	 *             Thrown if folder cannot be created
	 */
	public void ensureExists() throws IOException
	{
		if (!exists() && !mkdirs())
		{
			throw new IOException("Unable to create folder " + this);
		}
	}

	/**
	 * @param name
	 *            Name of child folder
	 * @return Child file object
	 */
	public Folder folder(final String name)
	{
		return new Folder(this, name);
	}

	/**
	 * @return Disk space free on the partition where this folder lives
	 */
	public Bytes freeDiskSpace()
	{
		return Bytes.bytes(super.getFreeSpace());
	}

	/**
	 * @return Files in this folder
	 */
	public File[] getFiles()
	{
		return getFiles(FileFilter.ALL_FILES);
	}

	/**
	 * @return All files nested within this folder
	 */
	public File[] getNestedFiles()
	{
		return getNestedFiles(FileFilter.ALL_FILES);
	}

	/**
	 * Gets files in this folder matching a given filter recursively.
	 * 
	 * @param filter
	 *            The filter
	 * @return The list of files
	 */
	public File[] getNestedFiles(final FileFilter filter)
	{
		final List<File> files = new ArrayList<>();
		files.addAll(Arrays.asList(getFiles(filter)));
		final Folder[] folders = getFolders();
		for (Folder folder : folders)
		{
			files.addAll(Arrays.asList(folder.getNestedFiles(filter)));
		}
		return files.toArray(new File[files.size()]);
	}

	/**
	 * @param filter
	 *            File filter
	 * @return Files
	 */
	public File[] getFiles(final FileFilter filter)
	{
		// Get list of java.io files
		final java.io.File[] files = listFiles(new java.io.FileFilter()
		{
			/**
			 * @see java.io.FileFilter#accept(java.io.File)
			 */
			@Override
			public boolean accept(final java.io.File file)
			{
				return file.isFile() && filter.accept(new File(file));
			}
		});

		// Convert java.io files to org.apache.wicket files
		if (files != null)
		{
			final File[] wicketFiles = new File[files.length];
			for (int i = 0; i < files.length; i++)
			{
				wicketFiles[i] = new File(files[i]);
			}
			return wicketFiles;
		}
		return new File[0];
	}

	/**
	 * Gets all folders in this folder, except "." and ".."
	 * 
	 * @return Folders
	 */
	public Folder[] getFolders()
	{
		return getFolders(new FolderFilter()
		{
			@Override
			public boolean accept(final Folder folder)
			{
				final String name = folder.getName();
				return !name.equals(".") && !name.equals("..");
			}
		});
	}

	/**
	 * @param filter
	 *            Folder filter
	 * @return Folders
	 */
	public Folder[] getFolders(final FolderFilter filter)
	{
		// Get java io files that are directories matching the filter
		final java.io.File[] files = listFiles(new java.io.FileFilter()
		{
			/**
			 * @see java.io.FileFilter#accept(java.io.File)
			 */
			@Override
			public boolean accept(final java.io.File file)
			{
				return file.isDirectory() && filter.accept(new Folder(file.getPath()));
			}
		});

		// Convert
		if (files != null)
		{
			final Folder[] wicketFolders = new Folder[files.length];
			for (int i = 0; i < files.length; i++)
			{
				wicketFolders[i] = new Folder(files[i]);
			}
			return wicketFolders;
		}
		return new Folder[0];
	}

	/**
	 * Removes this folder and everything in it, recursively. A best effort is made to remove nested
	 * folders and files in depth-first order.
	 * 
	 * @return True if the folder was successfully removed
	 */
	@Override
	public boolean remove()
	{
		return remove(this);
	}

	/**
	 * Removes all the files in this folder.
	 * 
	 * @return True if any files were successfully removed
	 */
	public boolean removeFiles()
	{
		final File[] files = getFiles();
		boolean success = true;
		for (File file : files)
		{
			success = file.remove() && success;
		}
		return success;
	}

	/**
	 * Removes everything in the given folder and then the folder itself.
	 * 
	 * @param folder
	 *            The folder
	 * @return True if the folder was successfully removed
	 */
	private boolean remove(final Folder folder)
	{
		final Folder[] folders = getFolders();
		boolean success = true;
		for (Folder subfolder : folders)
		{
			success = subfolder.remove() && success;
		}
		success = removeFiles() && success;
		return folder.delete() && success;
	}
}
