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
package org.apache.wicket.markup;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import org.apache.wicket.util.lang.Classes;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
import org.apache.wicket.util.time.Time;


/**
 * An IResourceStream implementation with specific extensions for markup
 * resource streams.
 * 
 * @author Juergen Donnerstag
 */
public class MarkupResourceStream implements IResourceStream
{
	private static final long serialVersionUID = 1846489965076612828L;

	/** The associated markup resource stream */
	private final IResourceStream resourceStream;

	/**
	 * Container info like Class, locale and style which were used to locate the
	 * resource
	 */
	private final ContainerInfo containerInfo;

	/**
	 * The actual component class the markup is directly associated with. It
	 * might be super class of the component class
	 */
	private final String markupClassName;

	/** The key used to cache the markup resource stream */
	private String cacheKey;

	/**
	 * Construct.
	 * 
	 * @param resourceStream
	 */
	public MarkupResourceStream(final IResourceStream resourceStream)
	{
		this.resourceStream = resourceStream;
		this.containerInfo = null;
		this.markupClassName = null;

		if (resourceStream == null)
		{
			throw new IllegalArgumentException("Parameter 'resourceStream' must not be null");
		}
	}

	/**
	 * Construct.
	 * 
	 * @param resourceStream
	 * @param containerInfo
	 * @param markupClass
	 */
	public MarkupResourceStream(final IResourceStream resourceStream,
			final ContainerInfo containerInfo, final Class markupClass)
	{
		this.resourceStream = resourceStream;
		this.containerInfo = containerInfo;
		this.markupClassName = markupClass == null ? null : markupClass.getName();

		if (resourceStream == null)
		{
			throw new IllegalArgumentException("Parameter 'resourceStream' must not be null");
		}
	}

	/**
	 * 
	 * @see org.apache.wicket.util.resource.IResourceStream#close()
	 */
	public void close() throws IOException
	{
		resourceStream.close();
	}

	/**
	 * 
	 * @see org.apache.wicket.util.resource.IResourceStream#getContentType()
	 */
	public String getContentType()
	{
		return resourceStream.getContentType();
	}

	/**
	 * 
	 * @see org.apache.wicket.util.resource.IResourceStream#getInputStream()
	 */
	public InputStream getInputStream() throws ResourceStreamNotFoundException
	{
		return resourceStream.getInputStream();
	}

	/**
	 * 
	 * @see org.apache.wicket.util.resource.IResourceStream#getLocale()
	 */
	public Locale getLocale()
	{
		return resourceStream.getLocale();
	}

	/**
	 * 
	 * @see org.apache.wicket.util.watch.IModifiable#lastModifiedTime()
	 */
	public Time lastModifiedTime()
	{
		return resourceStream.lastModifiedTime();
	}

	/**
	 * 
	 * @see org.apache.wicket.util.resource.IResourceStream#length()
	 */
	public long length()
	{
		return resourceStream.length();
	}

	/**
	 * 
	 * @see org.apache.wicket.util.resource.IResourceStream#setLocale(java.util.Locale)
	 */
	public void setLocale(Locale locale)
	{
		resourceStream.setLocale(locale);
	}

	/**
	 * Get the actual component class the markup is directly associated with.
	 * Note: it not necessarily must be the container class.
	 * 
	 * @return The directly associated class
	 */
	public Class getMarkupClass()
	{
		return Classes.resolveClass(markupClassName);
	}

	/**
	 * Get the container infos associated with the markup
	 * 
	 * @return ContainerInfo
	 */
	public ContainerInfo getContainerInfo()
	{
		return containerInfo;
	}

	/**
	 * Gets cacheKey.
	 * @return cacheKey
	 */
	public final String getCacheKey()
	{
		return cacheKey;
	}

	/**
	 * Set the cache key
	 * @param cacheKey
	 */
	public final void setCacheKey(final String cacheKey)
	{
		this.cacheKey = cacheKey;
	}

	/**
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return resourceStream.toString();
	}
}
