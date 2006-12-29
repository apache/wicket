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
package wicket.markup.html;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.util.Locale;
import java.util.zip.GZIPOutputStream;

import wicket.Application;
import wicket.SharedResources;
import wicket.markup.html.resources.CompressedResourceReference;
import wicket.protocol.http.WebRequest;
import wicket.protocol.http.WebRequestCycle;
import wicket.protocol.http.WebResponse;
import wicket.util.io.Streams;
import wicket.util.resource.IResourceStream;
import wicket.util.resource.ResourceStreamNotFoundException;
import wicket.util.time.Time;

/**
 * Identical to PackageResource, but supports gzip compression of data
 * 
 * See {@link PackageResource} and {@link CompressedResourceReference}
 * 
 * @author Janne Hietam&auml;ki
 */
public final class CompressedPackageResource extends PackageResource
{
	private static final long serialVersionUID = 1L;

	private CompressingResourceStream resourceStream;

	protected CompressedPackageResource(Class scope, String path, Locale locale, String style)
	{
		super(scope, path, locale, style);
		resourceStream = new CompressingResourceStream();
	}

	/**
	 * Gets the resource for a given set of criteria. Only one resource will be
	 * loaded for the same criteria.
	 * 
	 * @param scope
	 *            This argument will be used to get the class loader for loading
	 *            the package resource, and to determine what package it is in.
	 *            Typically this is the class in which you call this method
	 * @param path
	 *            The path to the resource
	 * @param locale
	 *            The locale of the resource
	 * @param style
	 *            The style of the resource (see {@link wicket.Session})
	 * @return The resource
	 * @throws PackageResourceBlockedException
	 *             when the target resource is not accepted by
	 *             {@link IPackageResourceGuard the package resource guard}.
	 */
	public static PackageResource get(final Class scope, final String path, final Locale locale,
			final String style)
	{
		final SharedResources sharedResources = Application.get().getSharedResources();

		PackageResource resource = (PackageResource)sharedResources.get(scope, path, locale, style,
				true);
		if (resource == null)
		{
			resource = new CompressedPackageResource(scope, path, locale, style);
			sharedResources.add(scope, path, locale, style, resource);
		}
		return resource;
	}

	/**
	 * 
	 * @see wicket.markup.html.PackageResource#getResourceStream()
	 */
	@Override
	public IResourceStream getResourceStream()
	{
		return resourceStream;
	}

	/**
	 * IResourceStream implementation which compresses the data with gzip if the
	 * requests header Accept-Encoding contains string gzip
	 */
	private class CompressingResourceStream implements IResourceStream
	{
		private static final long serialVersionUID = 1L;

		/** Cache for compressed data */
		private SoftReference<byte[]> cache = new SoftReference<byte[]>(null);

		/** Timestamp of the cache */
		private Time timeStamp = null;

		/**
		 * 
		 * @return byte[]
		 */
		private byte[] getCompressedContent()
		{
			IResourceStream stream = CompressedPackageResource.super.getResourceStream();
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

				ByteArrayOutputStream out = new ByteArrayOutputStream();
				GZIPOutputStream zout = new GZIPOutputStream(out);
				Streams.copy(stream.getInputStream(), zout);
				zout.close();
				stream.close();
				ret = out.toByteArray();
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

		/**
		 * 
		 * @see wicket.util.resource.IResourceStream#close()
		 */
		public void close() throws IOException
		{

		}

		/**
		 * 
		 * @see wicket.util.resource.IResourceStream#getContentType()
		 */
		public String getContentType()
		{
			return CompressedPackageResource.super.getResourceStream().getContentType();
		}

		/**
		 * 
		 * @see wicket.util.resource.IResourceStream#getInputStream()
		 */
		public InputStream getInputStream() throws ResourceStreamNotFoundException
		{
			if (supportsCompression())
			{
				return new ByteArrayInputStream(getCompressedContent());
			}
			else
			{
				return CompressedPackageResource.super.getResourceStream().getInputStream();
			}
		}

		/**
		 * 
		 * @see wicket.util.resource.IResourceStream#getLocale()
		 */
		public Locale getLocale()
		{
			return CompressedPackageResource.super.getResourceStream().getLocale();
		}

		/**
		 * 
		 * @see wicket.util.resource.IResourceStream#length()
		 */
		public long length()
		{
			if (supportsCompression())
			{
				return getCompressedContent().length;
			}
			else
			{
				return CompressedPackageResource.super.getResourceStream().length();
			}
		}

		/**
		 * 
		 * @see wicket.util.resource.IResourceStream#setLocale(java.util.Locale)
		 */
		public void setLocale(Locale locale)
		{
			CompressedPackageResource.super.getResourceStream().setLocale(locale);
		}

		/**
		 * 
		 * @see wicket.util.watch.IModifiable#lastModifiedTime()
		 */
		public Time lastModifiedTime()
		{
			return CompressedPackageResource.super.getResourceStream().lastModifiedTime();
		}
	}

	/**
	 * 
	 * @return boolean
	 */
	private boolean supportsCompression()
	{
		WebRequest request = WebRequestCycle.get().getWebRequest();
		String s = request.getHttpServletRequest().getHeader("Accept-Encoding");
		if (s == null)
		{
			return false;
		}
		else
		{
			return s.indexOf("gzip") >= 0;
		}
	}

	/**
	 * 
	 * @see wicket.markup.html.WebResource#setHeaders(wicket.protocol.http.WebResponse)
	 */
	@Override
	protected void setHeaders(WebResponse response)
	{
		super.setHeaders(response);
		if (supportsCompression())
		{
			response.setHeader("Content-Encoding", "gzip");
		}
	}
}