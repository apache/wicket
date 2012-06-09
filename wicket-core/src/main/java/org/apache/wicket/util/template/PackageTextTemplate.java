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
import java.util.Locale;
import java.util.Map;

import org.apache.wicket.Application;
import org.apache.wicket.util.io.Streams;
import org.apache.wicket.util.lang.Packages;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
import org.apache.wicket.core.util.resource.locator.ResourceStreamLocator;
import org.apache.wicket.util.string.interpolator.MapVariableInterpolator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A <code>String</code> resource that can be appended to.
 * 
 * @author Eelco Hillenius
 * @since 1.2.6
 */
public class PackageTextTemplate extends TextTemplate
{
	/** log. */
	private static final Logger log = LoggerFactory.getLogger(PackageTextTemplate.class);

	private static final long serialVersionUID = 1L;

	/** The content type used if not provided in the constructor */
	public static final String DEFAULT_CONTENT_TYPE = "text";

	/** The encoding used if not provided in the constructor */
	public static final String DEFAULT_ENCODING = null;

	/** contents */
	private final StringBuilder buffer = new StringBuilder();

	/**
	 * Constructor.
	 * 
	 * @param clazz
	 *            the <code>Class</code> to be used for retrieving the classloader for loading the
	 *            <code>PackagedTextTemplate</code>
	 * @param fileName
	 *            the name of the file, relative to the <code>clazz</code> position
	 */
	public PackageTextTemplate(final Class<?> clazz, final String fileName)
	{
		this(clazz, fileName, DEFAULT_CONTENT_TYPE);
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
	 *            the mime type of this resource, such as "<code>image/jpeg</code>" or "
	 *            <code>text/html</code>"
	 */
	public PackageTextTemplate(final Class<?> clazz, final String fileName, final String contentType)
	{
		this(clazz, fileName, contentType, DEFAULT_ENCODING);
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
	 *            the mime type of this resource, such as "<code>image/jpeg</code>" or "
	 *            <code>text/html</code>"
	 * @param encoding
	 *            the file's encoding, for example, "<code>UTF-8</code>"
	 */
	public PackageTextTemplate(final Class<?> clazz, final String fileName,
		final String contentType, final String encoding)
	{
		this(clazz, fileName, null, null, null, contentType, encoding);
	}

	/**
	 * Constructor.
	 *
	 * @param clazz
	 *            the <code>Class</code> to be used for retrieving the classloader for loading the
	 *            <code>PackagedTextTemplate</code>
	 * @param fileName
	 *            the name of the file, relative to the <code>clazz</code> position
	 * @param style
	 *            Any resource style, such as a skin style (see {@link org.apache.wicket.Session})
	 * @param variation
	 *            The template's variation (of the style)
	 * @param locale
	 *            The locale of the resource to load
	 * @param contentType
	 *            the mime type of this resource, such as "<code>image/jpeg</code>" or "
	 *            <code>text/html</code>"
	 * @param encoding
	 *            the file's encoding, for example, "<code>UTF-8</code>"
	 */
	public PackageTextTemplate(final Class<?> clazz, final String fileName, final String style, final String variation,
		final Locale locale, final String contentType, final String encoding)
	{
		super(contentType);

		String path = Packages.absolutePath(clazz, fileName);

		Application app = Application.get();

		// first try default class loading locator to find the resource
		IResourceStream stream = app.getResourceSettings()
			.getResourceStreamLocator()
			.locate(clazz, path, style, variation, locale, null, false);

		if (stream == null)
		{
			// if the default locator didn't find the resource then fallback
			stream = new ResourceStreamLocator().locate(clazz, path, style, variation, locale, null, false);
		}

		if (stream == null)
		{
			throw new IllegalArgumentException("resource " + fileName + " not found for scope " +
				clazz + " (path = " + path + ")");
		}

		setLastModified(stream.lastModifiedTime());

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
		return buffer.toString();
	}

	/**
	 * Interpolates a <code>Map</code> of variables with the content and replaces the content with
	 * the result. Variables are denoted in the <code>String</code> by the
	 * <code>syntax ${variableName}</code>. The contents will be altered by replacing each variable
	 * of the form <code>${variableName}</code> with the value returned by
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
	public final TextTemplate interpolate(Map<String, ?> variables)
	{
		if (variables != null)
		{
			String result = new MapVariableInterpolator(buffer.toString(), variables).toString();
			buffer.delete(0, buffer.length());
			buffer.append(result);
		}
		return this;
	}


}
