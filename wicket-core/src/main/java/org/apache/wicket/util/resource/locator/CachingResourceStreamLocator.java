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
package org.apache.wicket.util.resource.locator;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.wicket.request.resource.ResourceReference.Key;
import org.apache.wicket.util.file.File;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.resource.FileResourceStream;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.UrlResourceStream;


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
	/**
	 * Lightweight reference to the cached {@link IResourceStream}
	 */
	private static interface IResourceStreamReference
	{
		String getReference();
	}

	/**
	 * A singleton reference that is used for resource streams which do not exists. I.e. if there is
	 * a key in the cache which value is NullResourceStreamReference.INSTANCE then there is no need
	 * to lookup again for this key anymore.
	 */
	private static class NullResourceStreamReference implements IResourceStreamReference
	{
		private final static NullResourceStreamReference INSTANCE = new NullResourceStreamReference();

		public String getReference()
		{
			return null;
		}
	}

	/**
	 * A reference which can be used to recreate {@link FileResourceStream}
	 */
	private static class FileResourceStreamReference implements IResourceStreamReference
	{
		private final String fileName;

		private FileResourceStreamReference(final String fileName)
		{
			this.fileName = fileName;
		}

		public String getReference()
		{
			return fileName;
		}
	}

	/**
	 * A reference which may be used to recreate {@link UrlResourceStream}
	 */
	private static class UrlResourceStreamReference implements IResourceStreamReference
	{
		private final String url;

		private UrlResourceStreamReference(final String url)
		{
			this.url = url;
		}

		public String getReference()
		{
			return url;
		}
	}

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
	public IResourceStream locate(Class<?> clazz, String path)
	{
		Key key = new Key(clazz.getName(), path, null, null, null);
		IResourceStream resourceStream = getCopyFromCache(key);

		if (resourceStream == null)
		{
			resourceStream = delegate.locate(clazz, path);

			updateCache(key, resourceStream);
		}

		return resourceStream;
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
			String absolutePath = fileResourceStream.getFile().getAbsolutePath();
			cache.put(key, new FileResourceStreamReference(absolutePath));
		}
		else if (stream instanceof UrlResourceStream)
		{
			UrlResourceStream urlResourceStream = (UrlResourceStream)stream;
			String url = urlResourceStream.getURL().toExternalForm();
			cache.put(key, new UrlResourceStreamReference(url));
		}
	}

	/**
	 * Make a copy before returning an item from the cache as resource streams are not thread-safe.
	 * 
	 * @param key
	 *            the cache key
	 * @return the cached File or Url resource stream
	 */
	private IResourceStream getCopyFromCache(Key key)
	{
		final IResourceStreamReference orig = cache.get(key);
		if (NullResourceStreamReference.INSTANCE == orig)
		{
			return null;
		}

		if (orig instanceof UrlResourceStreamReference)
		{
			UrlResourceStreamReference resourceStreamReference = (UrlResourceStreamReference)orig;
			String url = resourceStreamReference.getReference();
			try
			{
				return new UrlResourceStream(new URL(url));
			}
			catch (MalformedURLException e)
			{
				return null;
			}
		}

		if (orig instanceof FileResourceStreamReference)
		{
			FileResourceStreamReference resourceStreamReference = (FileResourceStreamReference)orig;
			String absolutePath = resourceStreamReference.getReference();
			return new FileResourceStream(new File(absolutePath));
		}

		return null;
	}

	public IResourceStream locate(Class<?> scope, String path, String style, String variation,
		Locale locale, String extension, boolean strict)
	{
		Key key = new Key(scope.getName(), path, locale, style, variation);
		IResourceStream resourceStream = getCopyFromCache(key);

		if (resourceStream == null)
		{
			resourceStream = delegate.locate(scope, path, style, variation, locale, extension,
				strict);

			updateCache(key, resourceStream);
		}

		return resourceStream;
	}

	public ResourceNameIterator newResourceNameIterator(String path, Locale locale, String style,
		String variation, String extension, boolean strict)
	{
		return delegate.newResourceNameIterator(path, locale, style, variation, extension, strict);
	}
}
