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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.wicket.Component;
import org.apache.wicket.core.util.lang.WicketObjects;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.util.resource.IFixedLocationResourceStream;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
import org.apache.wicket.util.string.Strings;
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

	/** */
	public static final String WICKET_XHTML_DTD = "http://wicket.apache.org/dtds.data/wicket-xhtml1.4-strict.dtd";

	private static final Pattern DOCTYPE_REGEX = Pattern.compile("!DOCTYPE\\s+(.*)\\s*");

	/** The associated markup resource stream */
	private final IResourceStream resourceStream;

	/** Container info like Class, locale and style which were used to locate the resource */
	private final transient ContainerInfo containerInfo;

	/**
	 * The actual component class the markup is directly associated with. It might be super class of
	 * the component class
	 */
	private final String markupClassName;

	/** The key used to cache the markup resource stream */
	private String cacheKey;

	/** In case of the inherited markup, this is the base markup */
	private transient Markup baseMarkup;

	/** The encoding as found in &lt;?xml ... encoding="" ?&gt;. {@code null}, otherwise */
	private String encoding;

	/** Wicket namespace: see WICKET_XHTML_DTD */
	private String wicketNamespace;

	/** == wicket namespace name + ":id" */
	private String wicketId;

	/** HTML5 http://www.w3.org/TR/html5-diff/#doctype */
	private String doctype;

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
		this.resourceStream = Args.notNull(resourceStream, "resourceStream");
		this.containerInfo = containerInfo;
		markupClassName = markupClass == null ? null : markupClass.getName();

		setWicketNamespace(MarkupParser.WICKET);
	}

	@Override
	public String locationAsString()
	{
		if (resourceStream instanceof IFixedLocationResourceStream)
		{
			return ((IFixedLocationResourceStream)resourceStream).locationAsString();
		}
		return null;
	}

	@Override
	public void close() throws IOException
	{
		resourceStream.close();
	}

	@Override
	public String getContentType()
	{
		return resourceStream.getContentType();
	}

	@Override
	public InputStream getInputStream() throws ResourceStreamNotFoundException
	{
		return resourceStream.getInputStream();
	}

	@Override
	public Locale getLocale()
	{
		return resourceStream.getLocale();
	}

	@Override
	public Time lastModifiedTime()
	{
		return resourceStream.lastModifiedTime();
	}

	@Override
	public Bytes length()
	{
		return resourceStream.length();
	}

	@Override
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
		if (markupClassName == null)
		{
			throw new MarkupException("no associated markup class");
		}
		return WicketObjects.resolveClass(markupClassName);
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
		wicketId = (wicketNamespace + ":id").intern();

		if (!MarkupParser.WICKET.equals(wicketNamespace) && log.isDebugEnabled())
		{
			log.debug("You are using a non-standard namespace name: '{}'", wicketNamespace);
		}
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

	@Override
	public String getStyle()
	{
		return resourceStream.getStyle();
	}

	@Override
	public String getVariation()
	{
		return resourceStream.getVariation();
	}

	@Override
	public void setStyle(String style)
	{
		resourceStream.setStyle(style);
	}

	@Override
	public void setVariation(String variation)
	{
		resourceStream.setVariation(variation);
	}

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

	/**
	 * Gets doctype.
	 * 
	 * @return The doctype excluding 'DOCTYPE'
	 */
	public final String getDoctype()
	{
		if (doctype == null)
		{
			MarkupResourceStream baseMarkupResourceStream = getBaseMarkupResourceStream();
			if (baseMarkupResourceStream != null)
			{
				doctype = baseMarkupResourceStream.getDoctype();
			}
		}

		return doctype;
	}

	/**
	 * Sets doctype.
	 * 
	 * @param doctype
	 *            doctype
	 */
	public final void setDoctype(final CharSequence doctype)
	{
		if (Strings.isEmpty(doctype) == false)
		{
			String doc = doctype.toString().replaceAll("[\n\r]+", "");
			doc = doc.replaceAll("\\s+", " ");
			Matcher matcher = DOCTYPE_REGEX.matcher(doc);
			if (matcher.matches() == false)
			{
				throw new MarkupException("Invalid DOCTYPE: '" + doctype + "'");
			}
			this.doctype = matcher.group(1).trim();
		}
	}

	/**
	 * @see <a href="http://www.w3.org/TR/html5-diff/#doctype">DOCTYPE</a>
	 * @return True, if doctype == &lt;!DOCTYPE html&gt;
	 */
	public boolean isHtml5()
	{
		return "html".equalsIgnoreCase(getDoctype());
	}
}
