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
import java.util.concurrent.ConcurrentMap;

import org.apache.wicket.Application;
import org.apache.wicket.Session;
import org.apache.wicket.util.lang.Generics;
import org.apache.wicket.util.lang.Packages;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.locator.IResourceStreamLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO javadoc
 */
public class PackageResourceReference extends ResourceReference
{
	private static final long serialVersionUID = 1L;

	private static final Logger log = LoggerFactory.getLogger(PackageResourceReference.class);

	private static final String CSS_EXTENSION = "css";
	private static final String JAVASCRIPT_EXTENSION = "js";

	private transient ConcurrentMap<UrlAttributes, UrlAttributes> urlAttributesCacheMap;

	/**
	 * Cache for existence check of minified file
	 */
	private Boolean minifiedExists = null;

	/**
	 * Construct.
	 * 
	 * @param key
	 */
	public PackageResourceReference(final Key key)
	{
		super(key);
	}

	/**
	 * Construct.
	 * 
	 * @param scope
	 * @param name
	 * @param locale
	 * @param style
	 * @param variation
	 */
	public PackageResourceReference(final Class<?> scope, final String name, final Locale locale,
		final String style, String variation)
	{
		super(scope, name, locale, style, variation);
	}

	/**
	 * Construct.
	 * 
	 * @param scope
	 * @param name
	 */
	public PackageResourceReference(final Class<?> scope, final String name)
	{
		super(scope, name);
	}

	/**
	 * Construct.
	 * 
	 * @param name
	 */
	public PackageResourceReference(final String name)
	{
		super(name);
	}

	/**
	 * @see org.apache.wicket.request.resource.ResourceReference#getResource()
	 */
	@Override
	public PackageResource getResource()
	{
		final String extension = getExtension();

		final PackageResource resource;

		if (CSS_EXTENSION.equals(extension))
		{
			resource = new CssPackageResource(getScope(), getName(), getLocale(), getStyle(),
				getVariation());
		}
		else if (JAVASCRIPT_EXTENSION.equals(extension))
		{
			resource = new JavaScriptPackageResource(getScope(), getName(), getLocale(), getStyle(),
				getVariation());
		}
		else
		{
			resource = new PackageResource(getScope(), getName(), getLocale(), getStyle(),
				getVariation());
		}

		if (minifiedExists)
		{
			resource.setCompress(false);
		}

		return resource;
	}

	private UrlAttributes getUrlAttributes(Locale locale, String style, String variation)
	{
		IResourceStreamLocator locator = Application.get()
			.getResourceSettings()
			.getResourceStreamLocator();

		String absolutePath = Packages.absolutePath(getScope(), getName());

		IResourceStream stream = locator.locate(getScope(), absolutePath, style, variation, locale,
			null, false);

		if (stream == null)
			return new UrlAttributes(null, null, null);

		return new UrlAttributes(stream.getLocale(), stream.getStyle(), stream.getVariation());
	}

	private Locale getCurrentLocale()
	{
		return getLocale() != null ? getLocale() : Session.get().getLocale();
	}

	private String getCurrentStyle()
	{
		return getStyle() != null ? getStyle() : Session.get().getStyle();
	}

	/**
	 * Initializes the cache for the existence of the minified resource.
	 */
	private void initMinifiedExists()
	{
		if (minifiedExists != null)
		{
			return;
		}

		String name = getMinifiedName();
		IResourceStreamLocator locator = Application.get()
				.getResourceSettings()
				.getResourceStreamLocator();
		String absolutePath = Packages.absolutePath(getScope(), name);
		IResourceStream stream = locator.locate(getScope(), absolutePath, getStyle(),
				getVariation(), getLocale(), null, true);
		minifiedExists = stream != null;
		if (!minifiedExists && log.isDebugEnabled())
		{
			log.debug("No minified version of '" + super.getName() +
					"' found, expected a file with the name '" + name + "', using full version");
		}
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
	public UrlAttributes getUrlAttributes()
	{
		Locale locale = getCurrentLocale();
		String style = getCurrentStyle();
		String variation = getVariation();

		UrlAttributes key = new UrlAttributes(locale, style, variation);

		if (urlAttributesCacheMap == null)
		{
			urlAttributesCacheMap = Generics.newConcurrentHashMap();
		}
		UrlAttributes value = urlAttributesCacheMap.get(key);
		if (value == null)
		{
			value = getUrlAttributes(locale, style, variation);
			urlAttributesCacheMap.put(key, value);
		}

		return value;
	}
}
