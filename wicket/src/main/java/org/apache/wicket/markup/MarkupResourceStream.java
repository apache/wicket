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

import org.apache.wicket.Component;
import org.apache.wicket.util.lang.Classes;
import org.apache.wicket.util.resource.IFixedLocationResourceStream;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
import org.apache.wicket.util.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * An IResourceStream implementation with specific extensions for markup resource streams.
 * 
 * @author Juergen Donnerstag
 */
public class MarkupResourceStream implements IResourceStream, IFixedLocationResourceStream
{
	private static final long serialVersionUID = 1846489965076612828L;

	private static final Logger log = LoggerFactory.getLogger(MarkupResourceStream.class);

	/** The associated markup resource stream */
	private final IResourceStream resourceStream;

	/** Container info like Class, locale and style which were used to locate the resource */
	private final ContainerInfo containerInfo;

	/**
	 * The actual component class the markup is directly associated with. It might be super class of
	 * the component class
	 */
	private final String markupClassName;

	/** The key used to cache the markup resource stream */
	private String cacheKey;

	/** In case of the inherited markup, this is the base markup */
	private Markup baseMarkup;

	/** If found in the markup, the <?xml ...?> string */
	private String xmlDeclaration;

	/** The encoding as found in <?xml ... encoding="" ?>. Null, else */
	private String encoding;

	/**
	 * Wicket namespace: <html
	 * xmlns:wicket="http://wicket.apache.org/dtds.data/wicket-xhtml1.3-strict.dtd>
	 */
	private String wicketNamespace;

	/** == wicket namespace name + ":id" */
	private String wicketId;

	/**
	 * Construct.
	 * 
	 * @param resourceStream
	 */
	public MarkupResourceStream(final IResourceStream resourceStream)
	{
		this(resourceStream, null, null);
	}

	/**
	 * Construct.
	 * 
	 * @param resourceStream
	 * @param containerInfo
	 * @param markupClass
	 */
	public MarkupResourceStream(final IResourceStream resourceStream,
		final ContainerInfo containerInfo, final Class<?> markupClass)
	{
		this.resourceStream = resourceStream;
		this.containerInfo = containerInfo;
		markupClassName = markupClass == null ? null : markupClass.getName();

		if (resourceStream == null)
		{
			throw new IllegalArgumentException("Parameter 'resourceStream' must not be null");
		}

		setWicketNamespace(ComponentTag.DEFAULT_WICKET_NAMESPACE);
	}

	/**
	 * @see org.apache.wicket.util.resource.IFixedLocationResourceStream#locationAsString()
	 */
	public String locationAsString()
	{
		if (resourceStream instanceof IFixedLocationResourceStream)
		{
			return ((IFixedLocationResourceStream)resourceStream).locationAsString();
		}
		return null;
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
	 * Get the actual component class the markup is directly associated with. Note: it not
	 * necessarily must be the container class.
	 * 
	 * @return The directly associated class
	 */
	public Class<? extends Component> getMarkupClass()
	{
		return Classes.resolveClass(markupClassName);
	}

	/**
	 * Get the container info associated with the markup
	 * 
	 * @return ContainerInfo
	 */
	public ContainerInfo getContainerInfo()
	{
		return containerInfo;
	}

	/**
	 * Gets cacheKey.
	 * 
	 * @return cacheKey
	 */
	public final String getCacheKey()
	{
		return cacheKey;
	}

	/**
	 * Set the cache key
	 * 
	 * @param cacheKey
	 */
	public final void setCacheKey(final String cacheKey)
	{
		this.cacheKey = cacheKey;
	}

	/**
	 * Gets the resource that contains this markup
	 * 
	 * @return The resource where this markup came from
	 */
	public IResourceStream getResource()
	{
		return resourceStream;
	}

	/**
	 * Return the XML declaration string, in case if found in the markup.
	 * 
	 * @return Null, if not found.
	 */
	public String getXmlDeclaration()
	{
		return xmlDeclaration;
	}

	/**
	 * Gets the markup encoding. A markup encoding may be specified in a markup file with an XML
	 * encoding specifier of the form &lt;?xml ... encoding="..." ?&gt;.
	 * 
	 * @return Encoding, or null if not found.
	 */
	public String getEncoding()
	{
		return encoding;
	}

	/**
	 * Get the wicket namespace valid for this specific markup
	 * 
	 * @return wicket namespace
	 */
	public String getWicketNamespace()
	{
		return wicketNamespace;
	}

	/**
	 * 
	 * @return usually it is "wicket:id"
	 */
	final public String getWicketId()
	{
		return wicketId;
	}

	/**
	 * Sets encoding.
	 * 
	 * @param encoding
	 *            encoding
	 */
	final void setEncoding(final String encoding)
	{
		this.encoding = encoding;
	}

	/**
	 * Sets wicketNamespace.
	 * 
	 * @param wicketNamespace
	 *            wicketNamespace
	 */
	public final void setWicketNamespace(final String wicketNamespace)
	{
		this.wicketNamespace = wicketNamespace;
		wicketId = wicketNamespace + ":id";

		if (!ComponentTag.DEFAULT_WICKET_NAMESPACE.equals(wicketNamespace))
		{
			log.info("You are using a non-standard component name: " + wicketNamespace);
		}
	}

	/**
	 * Sets xmlDeclaration.
	 * 
	 * @param xmlDeclaration
	 *            xmlDeclaration
	 */
	final void setXmlDeclaration(final String xmlDeclaration)
	{
		this.xmlDeclaration = xmlDeclaration;
	}

	/**
	 * Get the resource stream containing the base markup (markup inheritance)
	 * 
	 * @return baseMarkupResource Null, if not base markup
	 */
	public MarkupResourceStream getBaseMarkupResourceStream()
	{
		if (baseMarkup == null)
		{
			return null;
		}
		return baseMarkup.getMarkupResourceStream();
	}

	/**
	 * In case of markup inheritance, the base markup.
	 * 
	 * @param baseMarkup
	 *            The base markup
	 */
	public void setBaseMarkup(Markup baseMarkup)
	{
		this.baseMarkup = baseMarkup;
	}

	/**
	 * In case of markup inheritance, the base markup resource.
	 * 
	 * @return The base markup
	 */
	public Markup getBaseMarkup()
	{
		return baseMarkup;
	}

	/**
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		if (resourceStream != null)
		{
			return resourceStream.toString();
		}
		else
		{
			return "(unknown resource)";
		}
	}
}
