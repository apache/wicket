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

import java.util.Locale;

import org.apache.wicket.Application;
import org.apache.wicket.request.resource.JavaScriptPackageResource;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.settings.IResourceSettings;
import org.apache.wicket.util.lang.Packages;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.locator.IResourceStreamLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link ResourceReference} which checks for a minified version of a resource. The minified version
 * is used when {@link IResourceSettings#getUseMinifiedResources()} returns true. When no minified
 * version is found, a warning is logged and the resource is processed as normal, possibly with
 * on-the-fly minification.
 * 
 * The filename format for the 2 versions is:
 * <ul>
 * <li>Normal version: <i>foo.js</i> / <i>foo.css</i></li>
 * <li>Minimized version: <i>foo.min.js</i> / <i>foo.min.css</i></li>
 * </ul>
 * 
 * @author Hielke Hoeve, papegaaij
 */
public class MinifiedAwareJavaScriptResourceReference extends JavaScriptResourceReference
{
	private static final long serialVersionUID = 1L;

	private static final Logger log = LoggerFactory.getLogger(MinifiedAwareJavaScriptResourceReference.class);

	/* cache for existence check of minified file */
	private Boolean minifiedExists = null;

	/**
	 * Creates a new {@code MinifiedAwareJavaScriptResourceReference}
	 * 
	 * @param scope
	 *            mandatory parameter
	 * @param name
	 *            mandatory parameter
	 * @param locale
	 *            resource locale
	 * @param style
	 *            resource style
	 * @param variation
	 *            resource variation
	 */
	public MinifiedAwareJavaScriptResourceReference(Class<?> scope, String name, Locale locale,
		String style, String variation)
	{
		super(scope, name, locale, style, variation);
	}

	/**
	 * Creates a new {@code MinifiedAwareJavaScriptResourceReference}
	 * 
	 * @param scope
	 *            mandatory parameter
	 * @param name
	 *            mandatory parameter
	 */
	public MinifiedAwareJavaScriptResourceReference(Class<?> scope, String name)
	{
		super(scope, name);
	}

	/**
	 * Initializes the cache for the existence of the minified resource.
	 */
	private void initMinifiedExists()
	{
		if (minifiedExists != null)
			return;
		String name = getMinifiedName();
		IResourceStreamLocator locator = Application.get()
			.getResourceSettings()
			.getResourceStreamLocator();
		String absolutePath = Packages.absolutePath(getScope(), name);
		IResourceStream stream = locator.locate(getScope(), absolutePath, getStyle(),
			getVariation(), getLocale(), null, true);
		minifiedExists = stream != null;
		if (!minifiedExists && log.isWarnEnabled())
			log.warn("No minified version of '" + super.getName() +
				"' found, expected a file with the name '" + name + "', using full version");
	}

	/**
	 * @return How the minified file should be named.
	 */
	private String getMinifiedName()
	{
		String name = super.getName();
		String extension = name.substring(name.lastIndexOf('.'));
		return name.substring(0, name.length() - extension.length() + 1) + "min" + extension;
	}

	/**
	 * Returns the name of the file: minified or full version. This method is called in a
	 * multithreaded context, so it has to be thread safe.
	 * 
	 * @see org.apache.wicket.request.resource.ResourceReference#getName()
	 */
	@Override
	public String getName()
	{
		initMinifiedExists();
		if (minifiedExists && Application.get().getResourceSettings().getUseMinifiedResources())
			return getMinifiedName();
		return super.getName();
	}

	@Override
	public JavaScriptPackageResource getResource()
	{
		JavaScriptPackageResource resource = super.getResource();
		if (minifiedExists)
			resource.setCompress(false);
		return resource;
	}
}
