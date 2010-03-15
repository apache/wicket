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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.wicket.Application;
import org.apache.wicket.Session;
import org.apache.wicket.util.lang.Packages;
import org.apache.wicket.util.resource.locator.IResourceStreamLocator;

public class PackageResourceReference extends ResourceReference
{

	public PackageResourceReference(Class<?> scope, String name, Locale locale, String style,
		String variation)
	{
		super(scope, name, locale, style, variation);
	}

	public PackageResourceReference(Class<?> scope, String name)
	{
		super(scope, name);
	}

	public PackageResourceReference(String name)
	{
		super(name);
	}

	@Override
	public IResource getResource()
	{
		return new PackageResource(getScope(), getName(), getLocale(), getStyle(), getVariation());
	}

	private UrlAttributes testResource(IResourceStreamLocator locator, Locale locale, String style,
		String variation)
	{
		String absolutePath = Packages.absolutePath(getScope(), getName());
		if (locator.locate(getScope(), absolutePath, style, variation, locale, null, true) != null)
		{
			return new UrlAttributes(locale, style, variation);
		}
		else
		{
			return null;
		}
	}

	private UrlAttributes getUrlAttributes(Locale locale, String style, String variation)
	{
		IResourceStreamLocator locator = Application.get()
			.getResourceSettings()
			.getResourceStreamLocator();

		UrlAttributes res;

		res = testResource(locator, locale, style, variation);
		if (res == null)
		{
			res = testResource(locator, locale, style, null);
		}
		if (res == null)
		{
			res = testResource(locator, locale, null, variation);
		}
		if (res == null)
		{
			res = testResource(locator, null, style, variation);
		}
		if (res == null)
		{
			res = testResource(locator, locale, null, null);
		}
		if (res == null)
		{
			res = testResource(locator, null, style, null);
		}
		if (res == null)
		{
			res = testResource(locator, null, null, variation);
		}
		if (res == null)
		{
			res = new UrlAttributes(null, null, null);
		}
		return res;
	}

	private transient ConcurrentMap<UrlAttributes, UrlAttributes> urlAttributesCacheMap;

	@Override
	public UrlAttributes getUrlAttributes()
	{
		Locale locale = getLocale() != null ? getLocale() : Session.get().getLocale();
		String style = getStyle() != null ? getStyle() : Session.get().getStyle();
		String variation = getVariation();

		UrlAttributes key = new UrlAttributes(locale, style, variation);

		if (urlAttributesCacheMap == null)
		{
			urlAttributesCacheMap = new ConcurrentHashMap<UrlAttributes, UrlAttributes>();
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
