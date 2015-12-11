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
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.Application;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;

/**
 * This resource reference is used to provide a reference to a resource based on Java NIO FileSystem
 * API.<br>
 * <br>
 * To implement a mime type detection refer to the documentation of
 * {@link java.nio.file.Files#probeContentType(Path)} and provide an implementation for
 * java.nio.file.spi.FileTypeDetector in the META-INF/services folder for jars or in the
 * /WEB-INF/classes/META-INF/services folder for webapps<br>
 * <br>
 * You can optionally override {@link #getFileSystemResource()} to provide an inline mime type
 * detection, which is preferred to the default detection.<br>
 * <br>
 * Example:
 * 
 * <pre>
 * <code>
 * Path path = FileSystemResourceReference.getPath(URI.create("jar:file:///folder/file.zip!/folderInZip/video.mp4"));
 * add(new Video("video", new FileSystemResourceReference(path)));
 * </code>
 * </pre>
 * 
 * Example 2:
 * 
 * <pre>
 * <code>
 * mountResource("/filecontent/${name}", new FileSystemResourceReference("filesystem")
 * {
 * 	private static final long serialVersionUID = 1L;
 * 
 * 	{@literal @}Override
 * 	public IResource getResource()
 * 	{
 * 		return new FileSystemResource()
 * 		{
 * 			private static final long serialVersionUID = 1L;
 * 
 * 			protected ResourceResponse newResourceResponse(Attributes attributes)
 * 			{
 * 				try
 * 				{
 * 					String name = attributes.getParameters().get("name").toString("");
 * 					URI uri = URI.create(
 * 						"jar:file:////folder/example.zip!/zipfolder/" + name);
 * 					return createResourceResponse(
 * 						FileSystemResourceReference.getPath(uri));
 * 				}
 * 				catch (IOException | URISyntaxException e)
 * 				{
 * 					throw new WicketRuntimeException("Error while reading the file.", e);
 * 				}
 * 			};
 * 		};
 * 	}
 * });
 * </code>
 * </pre>
 * 
 * @author Tobias Soloschenko
 */
public class FileSystemResourceReference extends ResourceReference
{
	private static final long serialVersionUID = 1L;

	private Path path;

	/** The key for the file system meta data **/
	public static final MetaDataKey<Map<String, FileSystem>> FILE_SYSTEM_META_DATA_KEY = new MetaDataKey<Map<String, FileSystem>>()
	{
		private static final long serialVersionUID = 1L;
	};

	/**
	 * Creates a file system resource reference based on the given path
	 * 
	 * @param name
	 *            the name of the resource reference to expose data
	 * @param path
	 *            the path to create the resource reference
	 */
	public FileSystemResourceReference(String name, Path path)
	{
		super(name);
		this.path = path;
	}

	/**
	 * Creates a file system resource reference based on the given name
	 * 
	 * @param name
	 *            the name of the resource reference
	 * 
	 */
	public FileSystemResourceReference(String name)
	{
		super(name);
	}

	/**
	 * Creates a file system resource reference based on the given scope and name
	 * 
	 * @param scope
	 *            the scope as class
	 * @param name
	 *            the name of the resource reference
	 * 
	 */
	public FileSystemResourceReference(Class<?> scope, String name)
	{
		super(scope, name);
	}

	/**
	 * Creates a new {@link org.apache.wicket.markup.html.media.FileSystemResource} and applies the
	 * path to it.
	 */
	@Override
	public IResource getResource()
	{
		return getFileSystemResource();
	}

	/**
	 * Gets the file system resource to be used for the resource reference
	 * 
	 * @return the file system resource to be used for the resource reference
	 */
	protected FileSystemResource getFileSystemResource()
	{
		if (path == null)
		{
			throw new WicketRuntimeException(
				"Please override #getResource() and provide a path if using a constructor which doesn't take one as argument.");
		}
		return new FileSystemResource(path);
	}

	/**
	 * Creates a path and a file system (if required) based on the given URI
	 * 
	 * @param uri
	 *            the URI to create the file system and the path of
	 * @param env
	 *            the environment parameter to create the file system with
	 * @return the path of the file in the file system
	 * @throws IOException
	 *             if the file system could'nt be created
	 * @throws URISyntaxException
	 *             if the URI has no valid syntax
	 */
	public static Path getPath(URI uri, Map<String, String> env)
		throws IOException, URISyntaxException
	{
		String uriString = uri.toString();
		int indexOfExclamationMark = uriString.indexOf('!');
		if (indexOfExclamationMark == -1)
		{
			return Paths.get(uri);
		}
		String zipFile = uriString.substring(0, indexOfExclamationMark);
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
			fileSystem = metaData.get(zipFile);
			if (fileSystem == null)
			{
				if (env == null)
				{
					env = new HashMap<>();
					env.put("create", "true");
					env.put("encoding", "UTF-8");
				}
				fileSystem = FileSystems.newFileSystem(new URI(zipFile), env);
				metaData.put(zipFile, fileSystem);
			}
		}
		String fileName = uriString.substring(uriString.indexOf('!') + 1);

		return fileSystem.getPath(fileName);
	}

	/**
	 * Creates a path and a file system (if required) based on the given URI
	 * 
	 * @param uri
	 *            the URI to create the file system and the path of
	 * @return the path of the file in the file system
	 * @throws IOException
	 *             if the file system could'nt be created
	 * @throws URISyntaxException
	 *             if the URI has no valid syntax
	 */
	public static Path getPath(URI uri) throws IOException, URISyntaxException
	{
		return getPath(uri, null);
	}
}
