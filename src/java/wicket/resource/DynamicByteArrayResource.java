/*
 * $Id$ $Revision$ $Date$
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.resource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import wicket.markup.html.WebResource;
import wicket.util.resource.IResourceStream;
import wicket.util.resource.ResourceStreamNotFoundException;
import wicket.util.time.Duration;
import wicket.util.time.Time;

/**
 * Byte array resource class that supports dynamic (database or on the fly generated) data.
 * 
 * @author Johan Compagner
 */
public abstract class DynamicByteArrayResource extends WebResource
{
	/** The time this image resource was last modified */
	private Time lastModifiedTime;

	/** The maximum duration a resource can be idle before its cache is flushed */
	private Duration cacheTimeout = Duration.NONE;

	/** the locale. */
	private Locale locale;

	/**
	 * Creates a dynamic resource 
	 */
	public DynamicByteArrayResource()
	{
	}

	/**
	 * Creates a dynamic resource from for the given locale
	 * 
	 * @param locale
	 * 			The locale of this resource 
	 */
	public DynamicByteArrayResource(Locale locale)
	{
		this.locale = locale;
	}

	/**
	 * @return Gets the image resource to attach to the component.
	 */
	public IResourceStream getResourceStream()
	{
		return new IResourceStream()
		{
			private static final long serialVersionUID = 1L;

			/** Transient input stream to resource */
			private transient InputStream inputStream = null;

			/**
			 * Transient byte array of the resources, will always be deleted in
			 * the close
			 */
			private transient byte[] data = null;

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
				return DynamicByteArrayResource.this.getContentType();
			}

			/**
			 * @see wicket.util.resource.IResourceStream#getInputStream()
			 */
			public InputStream getInputStream() throws ResourceStreamNotFoundException
			{
				checkLoadData();
				if (inputStream == null)
				{
					inputStream = new ByteArrayInputStream(data);
				}
				return inputStream;
			}

			/**
			 * @see wicket.util.watch.IModifiable#lastModifiedTime()
			 */
			public Time lastModifiedTime()
			{
				checkLoadData();
				return DynamicByteArrayResource.this.lastModifiedTime();
			}

			/**
			 * @see wicket.util.resource.IResourceStream#length()
			 */
			public long length()
			{
				checkLoadData();
				return (data != null) ? data.length : 0;
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
				DynamicByteArrayResource.this.locale = loc;
			}

			/**
			 * Check whether the data was loaded yet. If not, load it now.
			 */
			private void checkLoadData()
			{
				if (data == null)
				{
					data = getData();
				}
			}
		};
	}

	/**
	 * Gets the last time this resource was modified.
	 * @return The last time this resource was modified
	 */
	public final Time lastModifiedTime()
	{
		return lastModifiedTime;
	}

	/**
	 * Sets the last time this resource was modified.
	 *
	 * @param lastModifiedTime the last time this resource was modified
	 */
	public final void setLastModifiedTime(Time lastModifiedTime)
	{
		this.lastModifiedTime = lastModifiedTime;
	}


	/**
	 * Gets the content type.
	 * 
	 * @return The content type of the byte array
	 */
	public abstract String getContentType();

	/**
	 * Set the maximum duration the resource can be idle before its cache is
	 * flushed. The cache might get flushed sooner if the JVM is low on memory.
	 * 
	 * @param value
	 *            The cache timout
	 */
	public final void setCacheTimeout(Duration value)
	{
		cacheTimeout = value;
	}

	/**
	 * Gets the maximum duration the resource can be idle before its cache is
	 * flushed.
	 * 
	 * @return The cache timeout
	 */
	public final Duration getCacheTimeout()
	{
		return cacheTimeout;
	}

	/**
	 * Gets the byte array for our dynamic resource. If the subclass regenerates
	 * the data, it should set the lastModifiedTime too. This ensures that image
	 * caching works correctly.
	 * 
	 * @return The byte array for this dynamic resource.
	 */
	protected abstract byte[] getData();
}
