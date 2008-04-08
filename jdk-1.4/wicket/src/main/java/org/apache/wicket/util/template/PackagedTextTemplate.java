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
package org.apache.wicket.util.template;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.wicket.Application;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.util.io.Streams;
import org.apache.wicket.util.lang.Packages;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
import org.apache.wicket.util.resource.locator.IResourceStreamLocator;
import org.apache.wicket.util.resource.locator.ResourceStreamLocator;
import org.apache.wicket.util.string.JavascriptStripper;
import org.apache.wicket.util.string.interpolator.MapVariableInterpolator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A <code>String</code> resource that can be appended to.
 * 
 * @author Eelco Hillenius
 * @since 1.2.6
 */
// TODO cache templates application scoped with a watch
public class PackagedTextTemplate extends TextTemplate
{
	private static final class CachedTextTemplate implements Serializable
	{
		private static final long serialVersionUID = 1L;

		CachedTextTemplate(String text)
		{
		}
	}

	private static final class CachedTextTemplateKey implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private final String className;

		CachedTextTemplateKey(Class clazz, String path)
		{
			className = clazz.getName();
		}

	}

	private static final class TextTemplateCache implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private final Map<CachedTextTemplateKey, CachedTextTemplate> cache = new ConcurrentHashMap<CachedTextTemplateKey, CachedTextTemplate>();

		CachedTextTemplate get(CachedTextTemplateKey key)
		{
			return cache.get(key);
		}

		void put(CachedTextTemplateKey key, CachedTextTemplate value)
		{
			cache.put(key, value);
		}
	}

	/** log. */
	private static final Logger log = LoggerFactory.getLogger(PackagedTextTemplate.class);

	private static final long serialVersionUID = 1L;

	/** class loader stream locator. */
	private static final IResourceStreamLocator streamLocator = new ResourceStreamLocator();

	private static final MetaDataKey<TextTemplateCache> TEXT_TEMPLATE_CACHE_KEY = new MetaDataKey<TextTemplateCache>(
		TextTemplateCache.class)
	{
		private static final long serialVersionUID = 1L;
	};

	/** contents */
	private final StringBuffer buffer = new StringBuffer();

	/**
	 * Constructor.
	 * 
	 * @param clazz
	 *            the <code>Class</code> to be used for retrieving the classloader for loading the
	 *            <code>PackagedTextTemplate</code>
	 * @param fileName
	 *            the name of the file, relative to the <code>clazz</code> position
	 */
	public PackagedTextTemplate(final Class clazz, final String fileName)
	{
		this(clazz, fileName, "text");
	}

	/**
	 * Constructor.
	 * 
	 * @param clazz
	 *            the <code>Class</code> to be used for retrieving the classloader for loading the
	 *            <code>PackagedTextTemplate</code>
	 * @param fileName
	 *            the name of the file, relative to the <code>clazz</code> position
	 * @param contentType
	 *            the mime type of this resource, such as "<code>image/jpeg</code>" or "<code>text/html</code>"
	 */
	public PackagedTextTemplate(final Class clazz, final String fileName, final String contentType)
	{
		this(clazz, fileName, contentType, null);
	}

	/**
	 * Constructor.
	 * 
	 * @param clazz
	 *            the <code>Class</code> to be used for retrieving the classloader for loading the
	 *            <code>PackagedTextTemplate</code>
	 * @param fileName
	 *            the name of the file, relative to the <code>clazz</code> position
	 * @param contentType
	 *            the mime type of this resource, such as "<code>image/jpeg</code>" or "<code>text/html</code>"
	 * @param encoding
	 *            the file's encoding, for example, "<code>UTF-8</code>"
	 */
	public PackagedTextTemplate(final Class clazz, final String fileName, final String contentType,
		final String encoding)
	{
		super(contentType);

		String path = Packages.absolutePath(clazz, fileName);

		Application app = Application.get();
		TextTemplateCache cache = (TextTemplateCache)app.getMetaData(TEXT_TEMPLATE_CACHE_KEY);
		// TODO implement cache

		IResourceStream stream = streamLocator.locate(clazz, path);

		if (stream == null)
		{
			throw new IllegalArgumentException("resource " + fileName + " not found for scope " +
				clazz + " (path = " + path + ")");
		}

		try
		{
			if (encoding != null)
			{
				buffer.append(Streams.readString(stream.getInputStream(), encoding));
			}
			else
			{
				buffer.append(Streams.readString(stream.getInputStream()));
			}
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
		catch (ResourceStreamNotFoundException e)
		{
			throw new RuntimeException(e);
		}
		finally
		{
			try
			{
				stream.close();
			}
			catch (IOException e)
			{
				log.error(e.getMessage(), e);
			}
		}
	}

	/**
	 * @see org.apache.wicket.util.resource.AbstractStringResourceStream#getString()
	 */
	@Override
	public String getString()
	{
		if (Application.get().getResourceSettings().getStripJavascriptCommentsAndWhitespace())
		{
			return JavascriptStripper.stripCommentsAndWhitespace(buffer.toString());
		}
		else
		{
			// don't strip the comments
			return buffer.toString();
		}
	}

	/**
	 * Interpolates a <code>Map</code> of variables with the content and replaces the content with
	 * the result. Variables are denoted in the <code>String</code> by the
	 * <code>syntax ${variableName}</code>. The contents will be altered by replacing each
	 * variable of the form <code>${variableName}</code> with the value returned by
	 * <code>variables.getValue("variableName")</code>.
	 * <p>
	 * WARNING: there is no going back to the original contents after the interpolation is done. If
	 * you need to do different interpolations on the same original contents, use the method
	 * {@link #asString(Map)} instead.
	 * </p>
	 * 
	 * @param variables
	 *            a <code>Map</code> of variables to interpolate
	 * @return this for chaining
	 */
	@Override
	public final TextTemplate interpolate(Map variables)
	{
		if (variables != null)
		{
			String result = new MapVariableInterpolator(buffer.toString(), variables).toString();
			buffer.delete(0, buffer.length());
			buffer.append(result);
		}
		return this;
	}

	/**
	 * @see org.apache.wicket.util.resource.IResourceStream#length()
	 */
	@Override
	public final long length()
	{
		return buffer.length();
	}
}
