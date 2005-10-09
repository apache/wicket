/*
 * $Id$
 * $Revision$
 * $Date$
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
import wicket.util.time.Time;

/**
 * Byte array resource class for static data.
 *
 * @author Johan Compagner
 */
public class ByteArrayResource extends WebResource
{
	private static final long serialVersionUID = 1L;

	/** the content type. */
	private final String contentType;

	/** binary data. */
	private final byte[] array;

	/** the locale. */
	private final Locale locale;

	/** the time that this resource was last modified; same as construction time. */
	private final Time lastModified = Time.now();

	/**
	 * Construct.
	 * @param contentType the content type
	 * @param array the binary contents
	 */
	public ByteArrayResource(String contentType, byte[] array)
	{
		this.contentType = contentType;
		this.array = array;
		this.locale = null;
	}

	/**
	 * Construct.
	 * @param contentType the content type
	 * @param array the binary contents
	 * @param locale the locale
	 */
	public ByteArrayResource(String contentType, byte[] array, Locale locale)
	{
		this.contentType = contentType;
		this.array = array;
		this.locale = locale;
	}

	/**
	 * @see wicket.Resource#getResourceStream()
	 */
	public IResourceStream getResourceStream()
	{
		return new IResourceStream()
		{
			private static final long serialVersionUID = 1L;

			/** Transient input stream to resource */
			private transient InputStream inputStream = null;
			
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
			}
	
			/**
			 * @see wicket.util.resource.IResourceStream#getContentType()
			 */
			public String getContentType()
			{
				return contentType;
			}
	
			/**
			 * @see wicket.util.resource.IResourceStream#getInputStream()
			 */
			public InputStream getInputStream() throws ResourceStreamNotFoundException
			{
				if (inputStream == null)
					inputStream = new ByteArrayInputStream(array);

				return inputStream;
			}
	
			/**
			 * @see wicket.util.watch.IModifiable#lastModifiedTime()
			 */
			public Time lastModifiedTime()
			{
				return lastModified;
			}
	
			public long length()
			{
				return array.length;
			}
			
			public Locale getLocale()
			{
				return locale;
			}
			
			public void setLocale(Locale locale)
			{
				// ignore  
			}
		};
	}
}
