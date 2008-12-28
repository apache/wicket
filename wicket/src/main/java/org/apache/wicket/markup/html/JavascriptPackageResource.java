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
package org.apache.wicket.markup.html;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.util.Locale;

import org.apache.wicket.Application;
import org.apache.wicket.util.io.Streams;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
import org.apache.wicket.util.string.JavascriptStripper;
import org.apache.wicket.util.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Package resource for javascript files. It strips comments and whitespace from javascript and
 * gzips the content. The stripped and gzipped version is cached.
 * 
 * @author Matej Knopp
 */
public class JavascriptPackageResource extends CompressedPackageResource
{
	private static final long serialVersionUID = 1L;;

	private static final Logger log = LoggerFactory.getLogger(JavascriptPackageResource.class);

	/**
	 * Resource Stream that caches the stripped content.
	 * 
	 * @author Matej Knopp
	 */
	protected abstract class FilteringResourceStream implements IResourceStream
	{
		private static final long serialVersionUID = 1L;

		/** Cache for compressed data */
		private SoftReference<byte[]> cache = new SoftReference<byte[]>(null);

		/** Timestamp of the cache */
		private Time timeStamp = null;

		/**
		 * @see org.apache.wicket.util.resource.IResourceStream#close()
		 */
		public void close() throws IOException
		{
		}

		/**
		 * @see org.apache.wicket.util.resource.IResourceStream#getContentType()
		 */
		public String getContentType()
		{
			return getOriginalResourceStream().getContentType();
		}

		/**
		 * @see org.apache.wicket.util.resource.IResourceStream#getInputStream()
		 */
		public InputStream getInputStream() throws ResourceStreamNotFoundException
		{
			return new ByteArrayInputStream(getFilteredContent());
		}

		/**
		 * @see org.apache.wicket.util.resource.IResourceStream#getLocale()
		 */
		public Locale getLocale()
		{
			return getOriginalResourceStream().getLocale();
		}

		/**
		 * @see org.apache.wicket.util.watch.IModifiable#lastModifiedTime()
		 */
		public Time lastModifiedTime()
		{
			return getOriginalResourceStream().lastModifiedTime();
		}

		/**
		 * @see org.apache.wicket.util.resource.IResourceStream#length()
		 */
		public long length()
		{
			return getFilteredContent().length;
		}

		/**
		 * @see org.apache.wicket.util.resource.IResourceStream#setLocale(java.util.Locale)
		 */
		public void setLocale(Locale locale)
		{
			getOriginalResourceStream().setLocale(locale);
		}

		/**
		 * @return compressed content
		 */
		private byte[] getFilteredContent()
		{
			IResourceStream stream = getOriginalResourceStream();
			try
			{
				byte ret[] = cache.get();
				if (ret != null && timeStamp != null)
				{
					if (timeStamp.equals(stream.lastModifiedTime()))
					{
						return ret;
					}
				}

				int length = (int)stream.length();
				ByteArrayOutputStream out = new ByteArrayOutputStream(length > 0 ? length : 0);
				Streams.copy(stream.getInputStream(), out);
				stream.close();
				ret = filterContent(out.toByteArray());
				timeStamp = stream.lastModifiedTime();
				cache = new SoftReference<byte[]>(ret);
				return ret;
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
			catch (ResourceStreamNotFoundException e)
			{
				throw new RuntimeException(e);
			}
		}

		protected abstract byte[] filterContent(byte[] input);

		protected abstract IResourceStream getOriginalResourceStream();
	}

	/**
	 * Create a new PackageResource
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
	 * @return The resource
	 */
	protected static PackageResource newPackageResource(final Class<?> scope, final String path,
		final Locale locale, final String style)
	{
		return new JavascriptPackageResource(scope, path, locale, style);
	}

	/**
	 * Creates a new javascript package resource.
	 * 
	 * @param scope
	 * @param path
	 * @param locale
	 * @param style
	 */
	protected JavascriptPackageResource(Class<?> scope, String path, Locale locale, String style)
	{
		super(scope, path, locale, style);
	}

	/**
	 * @see org.apache.wicket.markup.html.CompressedPackageResource#newResourceStream()
	 */
	@Override
	protected IResourceStream newResourceStream()
	{
		final FilteringResourceStream filteringStream = new FilteringResourceStream()
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected byte[] filterContent(byte[] input)
			{
				try
				{
					if (Application.get()
						.getResourceSettings()
						.getStripJavascriptCommentsAndWhitespace())
					{
						String s = new String(input, "UTF-8");
						return JavascriptStripper.stripCommentsAndWhitespace(s).getBytes("UTF-8");
					}
					else
					{
						// don't strip the comments, just return original input
						return input;
					}
				}
				catch (Exception e)
				{
					log.error("Error while filtering content", e);
					return input;
				}
			}

			@Override
			protected IResourceStream getOriginalResourceStream()
			{
				return getPackageResourceStream();
			}
		};

		return new CompressingResourceStream()
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected IResourceStream getOriginalResourceStream()
			{
				return filteringStream;
			}
		};
	}

}
