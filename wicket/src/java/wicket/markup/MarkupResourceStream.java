/*
 * $Id: MarkupResourceStream.java 5791 2006-05-20 00:32:57 +0000 (Sat, 20 May
 * 2006) joco01 $ $Revision$ $Date: 2006-05-20 00:32:57 +0000 (Sat, 20
 * May 2006) $
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
package wicket.markup;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import wicket.MarkupContainer;
import wicket.util.resource.IResourceStream;
import wicket.util.resource.ResourceStreamNotFoundException;
import wicket.util.time.Time;

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
	private final Class<? extends MarkupContainer> markupClass;

	/**
	 * Construct.
	 * 
	 * @param resourceStream
	 * @param containerInfo
	 * @param markupClass
	 */
	public MarkupResourceStream(final IResourceStream resourceStream,
			final ContainerInfo containerInfo, final Class<? extends MarkupContainer> markupClass)
	{
		this.resourceStream = resourceStream;
		this.containerInfo = containerInfo;
		this.markupClass = markupClass;

		if (resourceStream == null)
		{
			throw new IllegalArgumentException("Parameter 'resourceStream' must not be null");
		}
	}

	/**
	 * 
	 * @see wicket.util.resource.IResourceStream#close()
	 */
	public final void close() throws IOException
	{
		resourceStream.close();
	}

	/**
	 * 
	 * @see wicket.util.resource.IResourceStream#getContentType()
	 */
	public final String getContentType()
	{
		return resourceStream.getContentType();
	}

	/**
	 * 
	 * @see wicket.util.resource.IResourceStream#getInputStream()
	 */
	public final InputStream getInputStream() throws ResourceStreamNotFoundException
	{
		return resourceStream.getInputStream();
	}

	/**
	 * 
	 * @see wicket.util.resource.IResourceStream#getLocale()
	 */
	public final Locale getLocale()
	{
		return resourceStream.getLocale();
	}

	/**
	 * 
	 * @see wicket.util.watch.IModifiable#lastModifiedTime()
	 */
	public final Time lastModifiedTime()
	{
		return resourceStream.lastModifiedTime();
	}

	/**
	 * 
	 * @see wicket.util.resource.IResourceStream#length()
	 */
	public final long length()
	{
		return resourceStream.length();
	}

	/**
	 * 
	 * @see wicket.util.resource.IResourceStream#setLocale(java.util.Locale)
	 */
	public final void setLocale(final Locale locale)
	{
		resourceStream.setLocale(locale);
	}

	/**
	 * Get the actual component class the markup is directly associated with.
	 * Note: it not necessarily must be the container class.
	 * 
	 * @return The directly associated class
	 */
	public final Class<? extends MarkupContainer> getMarkupClass()
	{
		return markupClass;
	}

	/**
	 * Get the container infos associated with the markup
	 * 
	 * @return ContainerInfo
	 */
	public final ContainerInfo getContainerInfo()
	{
		return containerInfo;
	}

	/**
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public final String toString()
	{
		return resourceStream.toString();
	}
}
