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

import java.util.Locale;

import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.util.time.Time;


/**
 * @see org.apache.wicket.util.resource.IResourceStream
 * 
 * @author Jonathan Locke
 */
public abstract class AbstractResourceStream implements IResourceStream
{
	private static final long serialVersionUID = 1L;

	private Locale locale;
	private String style;
	private String variation;

	/**
	 * @see org.apache.wicket.util.resource.IResourceStream#getLocale()
	 */
	public Locale getLocale()
	{
		return locale;
	}

	/**
	 * @see org.apache.wicket.util.resource.IResourceStream#setLocale(java.util.Locale)
	 */
	public void setLocale(Locale locale)
	{
		this.locale = locale;
	}

	/**
	 * @see org.apache.wicket.util.resource.IResourceStream#getStyle()
	 */
	public String getStyle()
	{
		return style;
	}

	/**
	 * @see org.apache.wicket.util.resource.IResourceStream#getVariation()
	 */
	public String getVariation()
	{
		return variation;
	}

	/**
	 * @see org.apache.wicket.util.resource.IResourceStream#setStyle(java.lang.String)
	 */
	public void setStyle(String style)
	{
		this.style = style;
	}

	/**
	 * @see org.apache.wicket.util.resource.IResourceStream#setVariation(java.lang.String)
	 */
	public void setVariation(String variation)
	{
		this.variation = variation;
	}

	/**
	 * @see org.apache.wicket.util.resource.IResourceStream#length()
	 */
	public Bytes length()
	{
		return null;
	}

	/**
	 * @see org.apache.wicket.util.resource.IResourceStream#getContentType()
	 */
	public String getContentType()
	{
		return null;
	}

	/**
	 * @see org.apache.wicket.util.watch.IModifiable#lastModifiedTime()
	 */
	public Time lastModifiedTime()
	{
		return null;
	}
}
