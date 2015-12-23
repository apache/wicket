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

import java.lang.ref.WeakReference;
import java.util.Locale;

import org.apache.wicket.MarkupContainer;

/**
 * Because Component has a reference to its parents, which eventually is the Page, keeping a "copy"
 * of a component is very expensive. ContainerInfo shall be used instead of MarkupContainer whenever
 * a small subset of the container's information is required.
 * 
 * @author Juergen Donnerstag
 */
public class ContainerInfo
{
	private final WeakReference<Class<?>> containerClassRef;
	private final Locale locale;
	private final String style;
	private final String variation;
	private final MarkupType markupType;

	/**
	 * Construct.
	 * 
	 * @param container
	 *            The container to create the information from
	 */
	public ContainerInfo(final MarkupContainer container)
	{
		this(container.getClass(), container.getLocale(), container.getStyle(),
			container.getVariation(), container.getMarkupType());
	}

	/**
	 * Construct.
	 * 
	 * @param containerClass
	 *            the real container class (could be a parent class)
	 * @param container
	 *            The container to create the information from
	 */
	public ContainerInfo(final Class<?> containerClass, final MarkupContainer container)
	{
		this(containerClass != null ? containerClass : container.getClass(), container.getLocale(), container.getStyle(),
			container.getVariation(), container.getMarkupType());
	}

	/**
	 * Construct.
	 * 
	 * @param containerClass
	 * @param locale
	 * @param style
	 * @param variation
	 * @param markupType
	 */
	public ContainerInfo(final Class<?> containerClass, final Locale locale, final String style,
		final String variation, final MarkupType markupType)
	{
		super();
		containerClassRef = new WeakReference<Class<?>>(containerClass);
		this.locale = locale;
		this.style = style;
		this.variation = variation;
		this.markupType = markupType;
	}

	/**
	 * 
	 * @return The container class
	 */
	public Class<?> getContainerClass()
	{
		return containerClassRef.get();
	}

	/**
	 * 
	 * @return The container markup type (== file extension)
	 */
	public String getFileExtension()
	{
		return markupType != null ? markupType.getExtension() : null;
	}

	/**
	 * 
	 * @return The container locale
	 */
	public Locale getLocale()
	{
		return locale;
	}

	/**
	 * 
	 * @return The container style
	 */
	public String getStyle()
	{
		return style;
	}

	/**
	 * 
	 * @return The containers variation
	 */
	public String getVariation()
	{
		return variation;
	}

	/**
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		Class<?> classRef = containerClassRef.get();
		return (classRef != null ? classRef.getName() : "null class") + ":" + locale + ":" + style +
			":" + markupType;
	}
}
