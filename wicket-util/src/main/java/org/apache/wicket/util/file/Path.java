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

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.util.resource.FileResourceStream;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.string.StringList;


/**
 * Maintains a list of folders as a path.
 * 
 * @author Jonathan Locke
 */
public final class Path implements IResourcePath
{
	/** The list of folders in the path */
	private final List<Folder> folders = new ArrayList<Folder>();

	/**
	 * Constructor
	 */
	public Path()
	{
	}

	/**
	 * Constructor
	 * 
	 * @param folder
	 *            A single folder to add to the path
	 */
	public Path(final Folder folder)
	{
		add(folder);
	}

	/**
	 * Constructor
	 * 
	 * @param folders
	 *            An array of folders to add to the path
	 */
	public Path(final Folder[] folders)
	{
		if (folders != null)
		{
			for (Folder folder : folders)
			{
				add(folder);
			}
		}
	}

	/**
	 * @param folder
	 *            Folder to add to path
	 */
	public void add(final Folder folder)
	{
		if (!folder.exists())
		{
			throw new IllegalArgumentException("Folder " + folder + " does not exist");
		}

		folders.add(folder);
	}

	/**
	 * @param path
	 *            Folder to add to path
	 * @see org.apache.wicket.util.file.IResourcePath#add(java.lang.String)
	 */
	public void add(final String path)
	{
		add(new Folder(path));
	}

	/**
	 * 
	 * @see org.apache.wicket.util.file.IResourceFinder#find(Class, String)
	 */
	public IResourceStream find(final Class<?> clazz, final String pathname)
	{
		for (Folder folder : folders)
		{
			final File file = new File(folder, pathname);

			if (file.exists())
			{
				return new FileResourceStream(file);
			}
		}

		return null;
	}

	/**
	 * @return Returns the folders.
	 */
	public List<Folder> getFolders()
	{
		return folders;
	}

	/**
	 * @return Number of folders on the path.
	 */
	public int size()
	{
		return folders.size();
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "[folders = " + StringList.valueOf(folders) + "]";
	}
}
