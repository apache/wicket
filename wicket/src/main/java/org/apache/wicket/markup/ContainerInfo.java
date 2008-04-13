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

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;

/**
 * Because a Component has reference to its parents, which eventually is the Page, and because the
 * Page contains a reference to the Session, keeping a "copy" of a component is very expensive.
 * ContainerInfo shall be used instead of MarkupContainer whenever a small subset of the container's
 * information is required.
 * 
 * @author Juergen Donnerstag
 */
public class ContainerInfo
{
	private final WeakReference<Class< ? extends Component>> containerClassRef;
	private final Locale locale;
	private final String style;
	private final String variation;
	private final String fileExtension;

	/**
	 * Construct.
	 * 
	 * @param container
	 *            The container to create the information from
	 */
	public ContainerInfo(final MarkupContainer< ? > container)
	{
		this(container.getClass(), container.getLocale(), container.getStyle(), null,
			container.getMarkupType());
	}

	/**
	 * Construct.
	 * 
	 * @param containerClass
	 * @param locale
	 * @param style
	 * @param variation
	 * @param fileExtension
	 */
	public ContainerInfo(final Class< ? extends Component> containerClass, final Locale locale,
		final String style, final String variation, final String fileExtension)
	{
		super();
		containerClassRef = new WeakReference<Class< ? extends Component>>(containerClass);
		this.locale = locale;
		this.style = style;
		this.variation = variation;
		this.fileExtension = fileExtension;
	}

	/**
	 * 
	 * @return The container class
	 */
	public Class< ? extends Component> getContainerClass()
	{
		return containerClassRef.get();
	}

	/**
	 * 
	 * @return The container markup type (== file extension)
	 */
	public String getFileExtension()
	{
		return fileExtension;
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
		return containerClassRef.get().getName() + ":" + locale + ":" + style + ":" + fileExtension;
	}
}
