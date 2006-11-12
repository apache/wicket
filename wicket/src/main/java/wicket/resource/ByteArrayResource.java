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

import wicket.markup.html.WebResource;
import wicket.util.resource.IResourceStream;
import wicket.util.resource.ResourceStreamNotFoundException;
import wicket.util.time.Time;

/**
 * This class can be used to easy make a Resource from a predefined byte array.
 * If your data comes from a database then a DynamicWebResource is a better
 * choice. Only use this class if you have to have the byte array in memory.
 * Like a pdf that is generated on the fly.
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
	 * Creates a Resource from the given byte array with its content type
	 * 
	 * @param contentType
	 *            The Content type of the array.
	 * @param array
	 *            The binary content
	 */
	public ByteArrayResource(String contentType, byte[] array)
	{
		this.contentType = contentType;
		this.array = array;
		this.locale = null;
	}

	/**
	 * Creates a Resource from the given byte array with its content type and
	 * the locale for which it is valid.
	 * 
	 * @param contentType
	 *            The Content type of the array.
	 * @param array
	 *            The binary content.
	 * @param locale
	 *            The locale of this resource
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
	@Override
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
				{
					inputStream = new ByteArrayInputStream(array);
				}

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