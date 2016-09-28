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

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileDeleteStrategy;

/**
 * A {@link FileDeleteStrategy} that can delete folders.
 */
public class FolderDeleteStrategy extends FileDeleteStrategy
{
	/**
	 * Construct.
	 */
	protected FolderDeleteStrategy()
	{
		super("folder");
	}

	@Override
	public boolean deleteQuietly(final File folder)
	{
		if (folder == null || folder.isFile())
		{
			return false;
		}

		File[] files = folder.listFiles();
		if (files != null)
		{
			for (File file : files)
			{
				if (file.isDirectory())
				{
					deleteQuietly(file);
				}
				else
				{
					super.deleteQuietly(file);
				}
			}
		}

		return super.deleteQuietly(folder);
	}

	@Override
	public void delete(final File folder) throws IOException
	{
		if (folder == null || folder.isFile())
		{
			return;
		}

		File[] files = folder.listFiles();
		if (files != null)
		{
			for (File file : files)
			{
				super.delete(file);
			}
		}
	}


}
