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
package org.apache.wicket.util.resource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import org.apache.wicket.util.io.IOUtils;
import org.apache.wicket.util.io.Streams;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.time.Time;


/**
 * Base class for string resources.
 * 
 * @author Jonathan Locke
 */
public abstract class AbstractStringResourceStream extends AbstractResourceStream
	implements
		IStringResourceStream
{
	private static final long serialVersionUID = 1L;

	/** The content-type applied in case the resource stream's default constructor is used */
	public static final String DEFAULT_CONTENT_TYPE = "text";

	/** Charset name for resource */
	private String charsetName;

	/** MIME content type */
	private final String contentType;

	/** The last time this stylesheet was modified */
	private Time lastModified = null;

	/**
	 * Constructor.
	 */
	public AbstractStringResourceStream()
	{
		this(DEFAULT_CONTENT_TYPE);
	}

	/**
	 * Constructor.
	 * 
	 * @param contentType
	 *            The mime type of this resource, such as "image/jpeg" or "text/html"
	 */
	public AbstractStringResourceStream(final String contentType)
	{
		// TODO null for contentType is allowed? or should the default be applied instead?
		this.contentType = contentType;

		lastModified = Time.now();
	}

	/**
	 * @return This resource as a String.
	 */
	@Override
	public String asString()
	{
		Reader reader = null;
		try
		{
			if (charsetName == null)
			{
				reader = new InputStreamReader(getInputStream());
			}
			else
			{
				reader = new InputStreamReader(getInputStream(), getCharset());
			}
			return Streams.readString(reader);
		}
		catch (IOException e)
		{
			throw new RuntimeException("Unable to read resource as String", e);
		}
		catch (ResourceStreamNotFoundException e)
		{
			throw new RuntimeException("Unable to read resource as String", e);
		}
		finally
		{
			IOUtils.closeQuietly(reader);
			IOUtils.closeQuietly(this);
		}
	}

	/**
	 * @return Charset for resource
	 */
	protected Charset getCharset()
	{
		// java.nio.Charset is not serializable so we can only store the name
		return (charsetName != null) ? Charset.forName(charsetName) : null;
	}

	/**
	 * Sets the character set used for reading this resource.
	 * 
	 * @param charset
	 *            Charset for component
	 */
	@Override
	public void setCharset(final Charset charset)
	{
		// java.nio.Charset itself is not serializable so we can only store the name
		charsetName = (charset != null) ? charset.name() : null;
	}

	/**
	 * @see org.apache.wicket.util.resource.IResourceStream#close()
	 */
	@Override
	public void close() throws IOException
	{
	}

	/**
	 * @see org.apache.wicket.util.resource.IResourceStream#getContentType()
	 */
	@Override
	public String getContentType()
	{
		return contentType;
	}

	/**
	 * @see org.apache.wicket.util.resource.IResourceStream#getInputStream()
	 */
	@Override
	public InputStream getInputStream() throws ResourceStreamNotFoundException
	{
		final byte[] bytes;
		if (getCharset() != null)
		{
			try
			{
				bytes = getString().getBytes(getCharset().name());
			}
			catch (UnsupportedEncodingException e)
			{
				throw new ResourceStreamNotFoundException("Could not encode resource", e);
			}
		}
		else
		{
			bytes = getString().getBytes();
		}
		return new ByteArrayInputStream(bytes);
	}

	/**
	 * @see org.apache.wicket.util.watch.IModifiable#lastModifiedTime()
	 */
	@Override
	public Time lastModifiedTime()
	{
		return lastModified;
	}

	/**
	 * @param lastModified
	 *            The lastModified to set.
	 */
	public void setLastModified(final Time lastModified)
	{
		this.lastModified = lastModified;
	}

	/**
	 * @return The string resource
	 */
	protected abstract String getString();

	@Override
	public final Bytes length()
	{
		int lengthInBytes = Strings.lengthInBytes(getString(), getCharset());
		return Bytes.bytes(lengthInBytes);
	}
}
