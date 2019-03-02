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
package org.apache.wicket.resource;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.Application;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.WicketRuntimeException;

/**
 * Gets the actual path for a jar file system
 * 
 * @author Tobias Soloschenko
 *
 */
public class FileSystemJarPathService implements FileSystemPathService
{

	/** The key for the file system meta data **/
	public static final MetaDataKey<Map<String, FileSystem>> FILE_SYSTEM_META_DATA_KEY = new MetaDataKey<>()
	{
		private static final long serialVersionUID = 1L;
	};

	@Override
	public Path getPath(URI uri, Map<String, String> env)
	{
		try
		{
			String uriString = uri.toString();
			int indexOfExclamationMark = uriString.indexOf('!');
			String jarFile = uriString.substring(0, indexOfExclamationMark);
			FileSystem fileSystem = null;

			synchronized (FILE_SYSTEM_META_DATA_KEY)
			{
				Map<String, FileSystem> metaData = Application.get()
					.getMetaData(FILE_SYSTEM_META_DATA_KEY);
				if (metaData == null)
				{
					metaData = new HashMap<String, FileSystem>();
					Application.get().setMetaData(FILE_SYSTEM_META_DATA_KEY, metaData);
				}
				fileSystem = metaData.get(jarFile);
				if (fileSystem == null)
				{
					if (env == null)
					{
						env = new HashMap<>();
						env.put("create", "true");
						env.put("encoding", "UTF-8");
					}
					fileSystem = FileSystems.newFileSystem(new URI(jarFile), env);
					metaData.put(jarFile, fileSystem);
				}
			}
			String fileName = uriString.substring(uriString.indexOf('!') + 1);

			return fileSystem.getPath(fileName);
		}
		catch (IOException | URISyntaxException e)
		{
			throw new WicketRuntimeException("Error while creating a jar file system", e);
		}
	}

	@Override
	public boolean isResponsible(URI uri)
	{
		return uri.getScheme().equals("jar") && uri.toString().indexOf('!') != -1;
	}
}
