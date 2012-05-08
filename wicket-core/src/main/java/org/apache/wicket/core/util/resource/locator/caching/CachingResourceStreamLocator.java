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
package org.apache.wicket.core.util.resource.locator.caching;

import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.wicket.core.util.resource.UrlResourceStream;
import org.apache.wicket.core.util.resource.locator.IResourceNameIterator;
import org.apache.wicket.core.util.resource.locator.IResourceStreamLocator;
import org.apache.wicket.request.resource.ResourceReference.Key;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.resource.FileResourceStream;
import org.apache.wicket.util.resource.IResourceStream;


/**
 * Locating resources can take a significant amount of time, especially since there are often
 * several CSS, JavaScript and image resources on any given page. To facilitate localization and
 * styling, Wicket will usually make several attempts at locating each resource (i.e. first with an
 * "en_US" suffix, then "en", and so on); multiply these attempts by the number of resources on the
 * page and this starts to add up.
 * <p>
 * This locator mitigates this problem by caching (indefinitely) references to
 * {@link UrlResourceStream} and {@link FileResourceStream} objects as they are found, and
 * {@link NullResourceStreamReference} for all which are missing so they are not looked up again and
 * again.
 */
public class CachingResourceStreamLocator implements IResourceStreamLocator
{
	private final ConcurrentMap<Key, IResourceStreamReference> cache;

	private final IResourceStreamLocator delegate;

	/**
	 * Construct.
	 * 
	 * @param resourceStreamLocator
	 *            the delegate
	 */
	public CachingResourceStreamLocator(final IResourceStreamLocator resourceStreamLocator)
	{
		Args.notNull(resourceStreamLocator, "resourceStreamLocator");

		delegate = resourceStreamLocator;

		cache = new ConcurrentHashMap<Key, IResourceStreamReference>();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Checks for {@link IResourceStreamReference} in the cache and returns <code>null</code> if the
	 * result is {@link NullResourceStreamReference#INSTANCE}, or {@link FileResourceStream} /
	 * {@link UrlResourceStream} if there is an entry in the cache. Otherwise asks the delegate to
	 * find one and puts it in the cache.
	 */
	@Override
	public IResourceStream locate(Class<?> clazz, String path)
	{
		Key key = new Key(clazz.getName(), path, null, null, null);
		IResourceStreamReference resourceStreamReference = cache.get(key);

		final IResourceStream result;
		if (resourceStreamReference == null)
		{
			result = delegate.locate(clazz, path);

			updateCache(key, result);
		}
		else
		{
			result = resourceStreamReference.getReference();
		}

		return result;
	}

	private void updateCache(Key key, IResourceStream stream)
	{
		if (null == stream)
		{
			cache.put(key, NullResourceStreamReference.INSTANCE);
		}
		else if (stream instanceof FileResourceStream)
		{
			FileResourceStream fileResourceStream = (FileResourceStream)stream;
			cache.put(key, new FileResourceStreamReference(fileResourceStream));
		}
		else if (stream instanceof UrlResourceStream)
		{
			UrlResourceStream urlResourceStream = (UrlResourceStream)stream;
			cache.put(key, new UrlResourceStreamReference(urlResourceStream));
		}
	}

	@Override
	public IResourceStream locate(Class<?> scope, String path, String style, String variation,
		Locale locale, String extension, boolean strict)
	{
		Key key = new Key(scope.getName(), path, locale, style, variation);
		IResourceStreamReference resourceStreamReference = cache.get(key);

		final IResourceStream result;
		if (resourceStreamReference == null)
		{
			result = delegate.locate(scope, path, style, variation, locale, extension, strict);

			updateCache(key, result);
		}
		else
		{
			result = resourceStreamReference.getReference();
		}

		return result;
	}

	@Override
	public IResourceNameIterator newResourceNameIterator(String path, Locale locale, String style,
		String variation, String extension, boolean strict)
	{
		return delegate.newResourceNameIterator(path, locale, style, variation, extension, strict);
	}
}
