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
package org.apache.wicket.core.util.resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import org.apache.wicket.Application;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.core.util.resource.locator.IResourceStreamLocator;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.util.lang.Packages;
import org.apache.wicket.util.resource.AbstractResourceStream;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
import org.apache.wicket.util.time.Time;


/**
 * An {@link IResourceStream} that reads data from a resource in the classpath. It simply delegates
 * all operations to the {@link IResourceStream} returned by the application's
 * {@link IResourceStreamLocator}.
 *
 * @author <a href="mailto:jbq@apache.org">Jean-Baptiste Quenot</a>
 * @author Tobias Soloschenko
 */
public class PackageResourceStream extends AbstractResourceStream
{
	private static final long serialVersionUID = 1L;

	private final IResourceStream resourceStream;

	/**
	 * Obtains an {@link IResourceStream} from the application's
	 * {@link IResourceStreamLocator#locate(Class, String)}
	 *
	 * @param scope
	 *            This argument will be used to get the class loader for loading the package
	 *            resource, and to determine what package it is in.
	 * @param path
	 *            The path to the resource
	 */
	public PackageResourceStream(Class<?> scope, String path)
	{
		this(scope, path, null, null, null);
	}

	/**
	 * Obtains an {@link IResourceStream} from the application's
	 * {@link IResourceStreamLocator#locate(Class, String)}
	 *
	 * @param scope
	 *            This argument will be used to get the class loader for loading the package
	 *            resource, and to determine what package it is in.
	 * @param path
	 *            The path to the resource
	 * @param locale
	 *            the locale of the resource to get
	 * @param style
	 *            the style of the resource to get
	 * @param variation
	 *            the variation of the resource to get
	 */
	public PackageResourceStream(Class<?> scope, String path, Locale locale, String style,
		String variation)
	{
		String absolutePath = Packages.absolutePath(scope, path);
		resourceStream = Application.get()
			.getResourceSettings()
			.getResourceStreamLocator()
			.locate(scope, absolutePath, style, variation, locale, null, false);

		if (resourceStream == null)
		{
			throw new WicketRuntimeException("Cannot find resource with " + scope.getName() +
				" and path " + path);
		}
	}

	@Override
	public void close() throws IOException
	{
		resourceStream.close();
	}

	@Override
	public String getContentType()
	{
		return resourceStream.getContentType();
	}

	@Override
	public InputStream getInputStream() throws ResourceStreamNotFoundException
	{
		return resourceStream.getInputStream();
	}

	@Override
	public Bytes length()
	{
		return resourceStream.length();
	}

	@Override
	public Time lastModifiedTime()
	{
		return resourceStream.lastModifiedTime();
	}
}
