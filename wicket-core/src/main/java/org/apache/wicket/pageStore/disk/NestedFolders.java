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
package org.apache.wicket.pageStore.disk;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.apache.wicket.util.file.Files;
import org.apache.wicket.util.lang.Args;

/**
 * Keep files in a nested filed structure to minimize amount of directory entries (inodes) in a single directory. 
 * 
 * @author svenmeier
 */
public class NestedFolders
{
	private final File base;
	
	/**
	 * Create folders in the given base folder.
	 * 
	 * @param base base has to be a folder
	 */
	public NestedFolders(File base)
	{
		this.base = Args.notNull(base, "base");
	}
	
	public File getBase()
	{
		return base;
	}
	
	/**
	 * Get a nested folder for the given name.
	 * 
	 * @param name name 
	 * @param create
	 * @return
	 */
	public File get(String name, final boolean create)
	{
		name = name.replace('*', '_');
		name = name.replace('/', '_');
		name = name.replace(':', '_');

		String path = createPathFrom(name);

		File folder = new File(base, path);
		if (create && folder.exists() == false)
		{
			Files.mkdirs(folder);
		}
		return folder;
	}

	private String createPathFrom(final String name)
	{
		int hash = Math.abs(name.hashCode());
		String low = String.valueOf(hash % 9973);
		String high = String.valueOf((hash / 9973) % 9973);
		StringBuilder bs = new StringBuilder(name.length() + 10);
		bs.append(low);
		bs.append(File.separator);
		bs.append(high);
		bs.append(File.separator);
		bs.append(name);

		return bs.toString();
	}

	/**
	 * Remove a nested folder.
	 * 
	 * @param name name of folder
	 */
	public void remove(String name)
	{
		File folder = get(name, false);
		if (folder.exists())
		{
			Files.removeFolder(folder);
			
			File high = folder.getParentFile();
			if (high.list().length == 0)
			{
				if (Files.removeFolder(high))
				{
					File low = high.getParentFile();
					if (low.list().length == 0)
					{
						Files.removeFolder(low);
					}
				}
			}
		}
	}

	/**
	 * Get all files inside.
	 * 
	 * @return files
	 */
	public Set<File> getAll()
	{
		Set<File> files = new HashSet<>();
		
		if (base.exists())
		{
			for (File low : Files.list(base))
			{
				for (File high: Files.list(low))
				{
					for (File file : Files.list(high))
					{
						files.add(file);
					}
				}
			}
		}
		
		return files;
	}
}
