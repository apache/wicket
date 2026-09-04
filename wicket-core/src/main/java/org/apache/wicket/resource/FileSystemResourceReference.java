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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;

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
 * 					return createResourceResponse(attributes, 
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
	 * Creates a new {@link FileSystemResource} and applies the
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
	 */
	public static Path getPath(URI uri, Map<String, String> env)
	{
		Iterator<FileSystemPathService> pathServiceIterator = ServiceLoader
			.load(FileSystemPathService.class).iterator();
		while (pathServiceIterator.hasNext())
		{
			FileSystemPathService pathService = pathServiceIterator.next();
			if (pathService.isResponsible(uri))
			{
				Path fileSystemPath = pathService.getPath(uri, env);
				if (fileSystemPath != null)
				{
					return fileSystemPath;
				}
			}
		}
		// fall back to just get the path from the URI
		return Paths.get(uri);
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
