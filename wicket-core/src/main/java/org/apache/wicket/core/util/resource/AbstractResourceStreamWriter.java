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
package org.apache.wicket.core.util.resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
import org.apache.wicket.util.time.Time;

/**
 * Base implementation of an IResourceStreamWriter so that you only have to override the
 * {@link IResourceStreamWriter#write(java.io.OutputStream)} Don't forget to overwrite the
 * {@link IResourceStream#length()} method if you do know the total length that will be generated.
 *
 * @see IResourceStreamWriter
 */
public abstract class AbstractResourceStreamWriter implements IResourceStreamWriter
{
	private static final long serialVersionUID = 1L;

	private Locale locale;

	private String variation;

	private String style;

	/**
	 * Default implementation to return -1. Do override this if you know the length up front.
	 */
	@Override
	public Bytes length()
	{
		return null;
	}

	@Override
	public Locale getLocale()
	{
		return locale;
	}

	@Override
	public void setLocale(final Locale locale)
	{
		this.locale = locale;
	}

	/**
	 * Just returns now.
	 */
	@Override
	public Time lastModifiedTime()
	{
		return Time.now();
	}

	/**
	 * this method should not be used as it is not required for resource writers
	 */
	@Override
	public final InputStream getInputStream() throws ResourceStreamNotFoundException
	{
		throw new IllegalStateException("getInputStream is not used with IResourceStreamWriter");
	}

	/**
	 * this method should not be used as it is not required for resource writers
	 * <p>
	 * resource write generate content directly, not using an input stream, so there's nothing to
	 * close later.
	 */
	@Override
	public final void close() throws IOException
	{
	}

	@Override
	public String getContentType()
	{
		return null;
	}

	@Override
	public String getStyle()
	{
		return style;
	}

	@Override
	public void setStyle(String style)
	{
		this.style = style;
	}

	@Override
	public String getVariation()
	{
		return variation;
	}

	@Override
	public void setVariation(String variation)
	{
		this.variation = variation;
	}

}