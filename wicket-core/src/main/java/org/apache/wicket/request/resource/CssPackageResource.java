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
package org.apache.wicket.request.resource;

import static org.apache.wicket.util.resource.ResourceUtils.MIN_POSTFIX_DEFAULT_AS_EXTENSION;

import java.util.Locale;

import org.apache.wicket.Application;
import org.apache.wicket.css.ICssCompressor;

/**
 * Package resource for css files. It strips comments and whitespace from css.
 */
public class CssPackageResource extends PackageResource
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 * 
	 * @param scope
	 *            This argument will be used to get the class loader for loading the package
	 *            resource, and to determine what package it is in
	 * @param name
	 *            The relative path to the resource
	 * @param locale
	 *            The locale of the resource
	 * @param style
	 *            The style of the resource
	 * @param variation
	 *            The component's variation (of the style)
	 */
	public CssPackageResource(Class<?> scope, String name, Locale locale, String style,
		String variation)
	{
		super(scope, name, locale, style, variation);

		// CSS resources can be compressed if there is configured ICssCompressor, and the
		// resource isn't already minified (the file already has .min. in its name).
		setCompress(!name.contains(MIN_POSTFIX_DEFAULT_AS_EXTENSION));
	}

	/**
	 * Gets the {@link ICssCompressor} to be used. By default returns the configured compressor on
	 * application level, but can be overridden by the user application to provide compressor
	 * specific to the resource.
	 * 
	 * @return the configured application level Css compressor. May be {@code null}.
	 */
	protected ICssCompressor getCompressor()
	{
		ICssCompressor compressor = null;
		if (Application.exists())
		{
			compressor = Application.get().getResourceSettings().getCssCompressor();
		}
		return compressor;
	}

}
