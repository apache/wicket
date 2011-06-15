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
import org.apache.wicket.util.time.Time;

/**
 * TODO javadoc
 */
public class PackageResourceReference extends ResourceReference
{
	private static final long serialVersionUID = 1L;

	private static final String CSS_EXTENSION = "css";
	private static final String JAVASCRIPT_EXTENSION = "js";

	private transient ConcurrentMap<UrlAttributes, UrlAttributes> urlAttributesCacheMap;

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
	public IResource getResource()
	{
		final String extension = getExtension();

		if (CSS_EXTENSION.equals(extension))
		{
			return new CssPackageResource(getScope(), getName(), getLocale(), getStyle(),
				getVariation());
		}
		else if (JAVASCRIPT_EXTENSION.equals(extension))
		{
			return new JavaScriptPackageResource(getScope(), getName(), getLocale(), getStyle(),
				getVariation());
		}
		else
		{
			return new PackageResource(getScope(), getName(), getLocale(), getStyle(),
				getVariation());
		}
	}

	private StreamInfo lookupStream(IResourceStreamLocator locator, Locale locale, String style,
		String variation)
	{
		String absolutePath = Packages.absolutePath(getScope(), getName());
		// strict only if an attribute was specified
		boolean strict = getLocale() != null || getStyle() != null || getVariation() != null;
		IResourceStream stream = locator.locate(getScope(), absolutePath, style, variation, locale,
			null, strict);

		if (stream == null)
			return null;

		return new StreamInfo(stream);
	}

	private StreamInfo lookupStream(Locale locale, String style, String variation)
	{
		IResourceStreamLocator locator = Application.get()
			.getResourceSettings()
			.getResourceStreamLocator();

		StreamInfo info;

		info = lookupStream(locator, locale, style, variation);
		if (info == null)
		{
			info = lookupStream(locator, locale, style, null);
		}
		if (info == null)
		{
			info = lookupStream(locator, locale, null, variation);
		}
		if (info == null)
		{
			info = lookupStream(locator, null, style, variation);
		}
		if (info == null)
		{
			info = lookupStream(locator, locale, null, null);
		}
		if (info == null)
		{
			info = lookupStream(locator, null, style, null);
		}
		if (info == null)
		{
			info = lookupStream(locator, null, null, variation);
		}
		return info;
	}

	private UrlAttributes getUrlAttributes(Locale locale, String style, String variation)
	{
		StreamInfo info = lookupStream(locale, style, variation);

		if (info != null)
			return new UrlAttributes(info.locale, info.style, info.variation);

		return new UrlAttributes(null, null, null);
	}

	private Locale getCurrentLocale()
	{
		return getLocale() != null ? getLocale() : Session.get().getLocale();
	}

	private String getCurrentStyle()
	{
		return getStyle() != null ? getStyle() : Session.get().getStyle();
	}

	@Override
	public Time getLastModified()
	{
		StreamInfo info = lookupStream(getCurrentLocale(), getCurrentStyle(), getVariation());

		if (info == null)
			return null;

		return info.stream.lastModifiedTime();
	}

	@Override
	public UrlAttributes getUrlAttributes()
	{
		Locale locale = getLocale() != null ? getLocale() : Session.get().getLocale();
		String style = getStyle() != null ? getStyle() : Session.get().getStyle();
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

	private static class StreamInfo
	{
		public final IResourceStream stream;
		public final Locale locale;
		public final String style;
		public final String variation;

		public StreamInfo(IResourceStream stream)
		{
			this.stream = stream;
			locale = stream.getLocale();
			style = stream.getStyle();
			variation = stream.getVariation();
		}
	}
}
