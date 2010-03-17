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
package org.apache.wicket.request.resource;

import java.util.Locale;

import javax.servlet.http.HttpServletResponse;

import org.apache.wicket.Application;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.markup.html.IPackageResourceGuard;
import org.apache.wicket.protocol.http.WebResponse;
import org.apache.wicket.protocol.http.request.WebErrorCodeResponseHandler;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.handler.basic.AbortRequestHandler;
import org.apache.wicket.util.lang.Packages;
import org.apache.wicket.util.lang.WicketObjects;
import org.apache.wicket.util.resource.IResourceStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PackageResource implements IResource
{
	/**
	 * Exception thrown when the creation of a package resource is not allowed.
	 */
	public static final class PackageResourceBlockedException extends WicketRuntimeException
	{
		private static final long serialVersionUID = 1L;

		/**
		 * Construct.
		 * 
		 * @param message
		 */
		public PackageResourceBlockedException(String message)
		{
			super(message);
		}
	}

	private static final long serialVersionUID = 1L;

	/** The path to the resource */
	private final String absolutePath;

	/** The resource's locale */
	private final Locale locale;

	/** The path this resource was created with. */
	private final String path;

	/** The scoping class, used for class loading and to determine the package. */
	private final String scopeName;

	/** The resource's style */
	private final String style;

	/** The component's variation (of the style) */
	private final String variation;

	/**
	 * Hidden constructor.
	 * 
	 * @param scope
	 *            This argument will be used to get the class loader for loading the package
	 *            resource, and to determine what package it is in
	 * @param name
	 *            The relative path to the resource
	 * @param locale
	 *            The locale of the resource
	 * @param style
	 *            The style of the resource
	 * @param variation
	 *            The component's variation (of the style)
	 */
	protected PackageResource(final Class<?> scope, final String name, final Locale locale,
		final String style, final String variation)
	{
		// Convert resource path to absolute path relative to base package
		absolutePath = Packages.absolutePath(scope, name);

		if (!accept(scope, name))
		{
			throw new PackageResourceBlockedException(
				"Access denied to (static) package resource " + absolutePath +
					". See IPackageResourceGuard");
		}

		// TODO NG: Check path for ../

		scopeName = scope.getName();
		path = name;
		this.locale = locale;
		this.style = style;
		this.variation = variation;
	}

	/**
	 * Gets the scoping class, used for class loading and to determine the package.
	 * 
	 * @return the scoping class
	 */
	public final Class<?> getScope()
	{
		return WicketObjects.resolveClass(scopeName);
	}

	/**
	 * Gets the style.
	 * 
	 * @return the style
	 */
	public final String getStyle()
	{
		return style;
	}


	public void respond(Attributes attributes)
	{
		// Locate resource
		IResourceStream resourceStream = Application.get()
			.getResourceSettings()
			.getResourceStreamLocator()
			.locate(getScope(), absolutePath, style, variation, locale, null);

		if (resourceStream == null)
		{
			String msg = "Unable to find package resource [path = " + absolutePath + ", style = " +
				style + ", variation = " + variation + ", locale = " + locale + "]";
			log.warn(msg);

			if (RequestCycle.get().getResponse() instanceof WebResponse)
			{
				RequestCycle.get().replaceAllRequestHandlers(
					new WebErrorCodeResponseHandler(HttpServletResponse.SC_NOT_FOUND, msg));
			}
			else
			{
				RequestCycle.get().replaceAllRequestHandlers(new AbortRequestHandler());
			}
			return;
		}

		new ResourceStreamResource(resourceStream).respond(attributes);
	}

	private boolean accept(Class<?> scope, String path)
	{
		IPackageResourceGuard guard = Application.get()
			.getResourceSettings()
			.getPackageResourceGuard();

		return guard.accept(scope, path);
	}

	private static final Logger log = LoggerFactory.getLogger(PackageResource.class);

	/**
	 * Gets whether a resource for a given set of criteria exists.
	 * 
	 * @param scope
	 *            This argument will be used to get the class loader for loading the package
	 *            resource, and to determine what package it is in. Typically this is the class in
	 *            which you call this method
	 * @param path
	 *            The path to the resource
	 * @param locale
	 *            The locale of the resource
	 * @param style
	 *            The style of the resource (see {@link org.apache.wicket.Session})
	 * @param variation
	 *            The component's variation (of the style)
	 * @return true if a resource could be loaded, false otherwise
	 */
	public static boolean exists(final Class<?> scope, final String path, final Locale locale,
		final String style, final String variation)
	{
		String absolutePath = Packages.absolutePath(scope, path);
		return Application.get().getResourceSettings().getResourceStreamLocator().locate(scope,
			absolutePath, style, variation, locale, null) != null;
	}

}
