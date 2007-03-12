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
package wicket.resource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import wicket.markup.html.DynamicWebResource;
import wicket.markup.html.WebResource;
import wicket.util.resource.IResourceStream;
import wicket.util.resource.ResourceStreamNotFoundException;
import wicket.util.time.Duration;
import wicket.util.time.Time;

/**
 * Byte array resource class that supports dynamic (database or on the fly
 * generated) data.
 * 
 * @author Johan Compagner
 * 
 * @deprecated use {@link DynamicWebResource} now
 */
public abstract class DynamicByteArrayResource extends WebResource
{
	/**
	 * This is a ResourceState subclasses should return in the getResourceState
	 * method. This resource state should be thread safe. So it shouldn't be
	 * altered after construction. Even with syncronize blocks this is still not
	 * safe because the call getContentType() can not be sync together with the
	 * call getData() they will happen after each other.
	 * 
	 * @author jcompagner
	 */
	public static class ResourceState
	{
		/**
		 * @return The Byte array for this resource
		 */
		public byte[] getData()
		{
			return null;
		}

		/**
		 * @return The content type of this resource
		 */
		public String getContentType()
		{
			return null;
		}

		/**
		 * @return The last modified time of this resource
		 */
		public Time lastModifiedTime()
		{
			return Time.now();
		}

		/**
		 * @return The length of the data
		 */
		public int getLength()
		{
			byte[] data = getData();
			return data != null ? data.length : 0;
		}
	}

	/** the locale. */
	private Locale locale;

	/**
	 * Creates a dynamic resource
	 */
	public DynamicByteArrayResource()
	{
		super();
		setCacheable(false);
	}

	/**
	 * Creates a dynamic resource
	 * 
	 * @param locale
	 *            The locale of this resource
	 */
	public DynamicByteArrayResource(Locale locale)
	{
		this.locale = locale;
	}

	/**
	 * Creates a dynamic resource
	 * 
	 * @param locale
	 *            The locale of this resource
	 * @param cacheTimeout
	 *            The cache duration timeout
	 */
	public DynamicByteArrayResource(Locale locale, Duration cacheTimeout)
	{
		this.locale = locale;
	}

	/**
	 * @return Gets the image resource to attach to the component.
	 */
	public final IResourceStream getResourceStream()
	{
		return new IResourceStream()
		{
			private static final long serialVersionUID = 1L;

			private Locale locale = DynamicByteArrayResource.this.locale;

			/** Transient input stream to resource */
			private transient InputStream inputStream = null;

			/**
			 * Transient ResourceState of the resources, will always be deleted
			 * in the close
			 */
			private transient ResourceState data = null;

			/**
			 * @see wicket.util.resource.IResourceStream#close()
			 */
			public void close() throws IOException
			{
				if (inputStream != null)
				{
					inputStream.close();
					inputStream = null;
				}
				data = null;
			}

			/**
			 * @see wicket.util.resource.IResourceStream#getContentType()
			 */
			public String getContentType()
			{
				checkLoadData();
				return data.getContentType();
			}

			/**
			 * @see wicket.util.resource.IResourceStream#getInputStream()
			 */
			public InputStream getInputStream() throws ResourceStreamNotFoundException
			{
				checkLoadData();
				if (inputStream == null)
				{
					inputStream = new ByteArrayInputStream(data.getData());
				}
				return inputStream;
			}

			/**
			 * @see wicket.util.watch.IModifiable#lastModifiedTime()
			 */
			public Time lastModifiedTime()
			{
				checkLoadData();
				return data.lastModifiedTime();
			}

			/**
			 * @see wicket.util.resource.IResourceStream#length()
			 */
			public long length()
			{
				checkLoadData();
				return (data != null) ? data.getLength() : 0;
			}

			/**
			 * @see wicket.util.resource.IResourceStream#getLocale()
			 */
			public Locale getLocale()
			{
				return locale;
			}

			/**
			 * @see wicket.util.resource.IResourceStream#setLocale(java.util.Locale)
			 */
			public void setLocale(Locale loc)
			{
				locale = loc;
			}

			/**
			 * Check whether the data was loaded yet. If not, load it now.
			 */
			private void checkLoadData()
			{
				if (data == null)
				{
					data = getResourceState();
				}
			}
		};
	}

	/**
	 * @see wicket.Resource#invalidate()
	 */
	public void invalidate()
	{
		super.invalidate();
	}

	/**
	 * Gets the byte array for our dynamic resource. If the subclass regenerates
	 * the data, it should set the lastModifiedTime too. This ensures that image
	 * caching works correctly.
	 * 
	 * @return The byte array for this dynamic resource.
	 */
	protected abstract ResourceState getResourceState();
}