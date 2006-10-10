/*
 * $Id: DynamicImageResource.java,v 1.20 2006/03/01 15:29:47 joco01 Exp $
 * $Revision: 1.20 $ $Date: 2006/03/01 15:29:47 $
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
package wicket.markup.html;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import wicket.util.resource.IResourceStream;
import wicket.util.resource.ResourceStreamNotFoundException;
import wicket.util.time.Time;

/**
 * An WebResource subclass for dynamic resources (resources created
 * programmatically).
 * 
 * @author Jonathan Locke
 * @author Johan Compagner
 * @author Gili Tzabari
 */
public abstract class DynamicWebResource extends WebResource
{
	/**
	 * The resource state returned by the getResourceState() method. This state
	 * needs to be thread-safe and its methods must return the same values no
	 * matter how many times they are invoked. A ResourceState may assume
	 * getParameters() will remain unchanged during its lifetime.
	 * 
	 * @author jcompagner
	 */
	public static class ResourceState
	{
		protected Time lastModifiedTime;

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
			if (lastModifiedTime == null)
			{
				lastModifiedTime = Time.now();
			}
			return lastModifiedTime;
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

	/**
	 * The resource locale.
	 */
	private Locale locale;

	/**
	 * Creates a dynamic resource.
	 */
	public DynamicWebResource()
	{
		setCacheable(false);
	}

	/**
	 * Creates a dynamic resource from for the given locale
	 * 
	 * @param locale
	 *            The locale of this resource
	 */
	public DynamicWebResource(Locale locale)
	{
		this();
		this.locale = locale;
	}

	/**
	 * Returns the resource locale.
	 * 
	 * @return The locale of this resource
	 */
	public Locale getLocale()
	{
		return locale;
	}

	/**
	 * @return Gets the resource to attach to the component.
	 */
	@Override
	public IResourceStream getResourceStream()
	{
		return new IResourceStream()
		{
			private static final long serialVersionUID = 1L;

			private Locale locale = DynamicWebResource.this.getLocale();

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
				return data.getLength();
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
	 * Gets the byte array for our dynamic resource. If the subclass regenerates
	 * the data, it should set the lastModifiedTime too. This ensures that
	 * resource caching works correctly.
	 * 
	 * @return The byte array for this dynamic resource.
	 */
	protected abstract ResourceState getResourceState();
}
