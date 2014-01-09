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

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.util.time.Time;

/**
 * A IResourceStream that wraps another resource stream
 */
public class ResourceStreamWrapper implements IResourceStream
{
	private final IResourceStream delegate;

	public ResourceStreamWrapper(IResourceStream delegate)
	{
		this.delegate = Args.notNull(delegate, "delegate");
	}

	protected IResourceStream getDelegate()
	{
		return delegate;
	}

	@Override
	public String getContentType()
	{
		return delegate.getContentType();
	}

	@Override
	public Bytes length()
	{
		return delegate.length();
	}

	@Override
	public InputStream getInputStream() throws ResourceStreamNotFoundException
	{
		return delegate.getInputStream();
	}

	@Override
	public void close() throws IOException
	{
		delegate.close();
	}

	@Override
	public Locale getLocale()
	{
		return delegate.getLocale();
	}

	@Override
	public void setLocale(Locale locale)
	{
		delegate.setLocale(locale);
	}

	@Override
	public String getStyle()
	{
		return delegate.getStyle();
	}

	@Override
	public void setStyle(String style)
	{
		delegate.setStyle(style);
	}

	@Override
	public String getVariation()
	{
		return delegate.getVariation();
	}

	@Override
	public void setVariation(String variation)
	{
		delegate.setVariation(variation);
	}

	@Override
	public Time lastModifiedTime()
	{
		return delegate.lastModifiedTime();
	}
}
