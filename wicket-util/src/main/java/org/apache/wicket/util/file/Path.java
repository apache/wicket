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

import org.apache.wicket.util.resource.FileResourceStream;
import org.apache.wicket.util.resource.IResourceStream;


/**
 * An {@link IResourceFinder} that looks for its resources in a filesystem path.
 * 
 * @author Jonathan Locke
 * @author Carl-Eric Menzel
 */
public class Path implements IResourceFinder
{
	private final Folder folder;

	/**
	 * Constructor
	 * 
	 * @param folder
	 *            The folder to look in
	 */
	public Path(final String folder)
	{
		this(new Folder(folder));
	}

	/**
	 * Constructor
	 * 
	 * @param folder
	 *            The folder to look in
	 */
	public Path(final Folder folder)
	{
		if (!folder.exists())
		{
			throw new IllegalArgumentException("Folder " + folder + " does not exist");
		}
		this.folder = folder;
	}

	/**
	 * Looks for {@code pathname} in the provided {@code folder}.
	 *
	 * @param clazz
	 *      The class requesting the resource stream
	 * @param pathname
	 *      the path to the needed resource. Must be relative to {@code folder}
	 * @see org.apache.wicket.util.file.IResourceFinder#find(Class, String)
	 */
	@Override
	public IResourceStream find(final Class<?> clazz, final String pathname)
	{
		final File file = new File(folder, pathname);

		if (file.exists())
		{
			return new FileResourceStream(file);
		}
		else
		{
			return null;
		}
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		try
		{
			return "[Path: folder = " + folder.getCanonicalPath() + "]";
		}
		catch (IOException e)
		{
			return "[Path: exception while inspecting folder]";
		}
	}
}
