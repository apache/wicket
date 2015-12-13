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

import java.net.URI;
import java.nio.file.Path;
import java.util.Map;

/**
 * Gets the actual path for a specific file system. Have a look into
 * {@link org.apache.wicket.resource.FileSystemJarPathService} to find a reference implementation.
 * 
 * @see org.apache.wicket.resource.FileSystemJarPathService
 * @author Tobias Soloschenko
 *
 */
public interface FileSystemPathService
{
	/**
	 * Gets the actual path for a specific file system to work on
	 * 
	 * @param uri
	 *            the uri to get the path from
	 * @param env
	 *            environment variables to be applied to the file system
	 * @return the actual path or null if the implementation is not responsible
	 * 
	 */
	Path getPath(URI uri, Map<String, String> env);

	/**
	 * Checks if the file system path service is responsible to handle the given URI
	 * 
	 * @param uri
	 *            the URI to detect if the file system path service is responsible
	 * @return if the file system path service is responsible to handle the given URI
	 */
	boolean isResponsible(URI uri);
}
