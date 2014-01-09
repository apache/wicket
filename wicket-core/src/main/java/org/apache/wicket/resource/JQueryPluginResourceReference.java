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
package org.apache.wicket.resource;

import java.util.List;
import java.util.Locale;

import org.apache.wicket.Application;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

/**
 * Base class for JavaScript resources that are JQuery plugins. This class already defines a
 * dependency on JQuery.
 * 
 * @author papegaaij
 */
public class JQueryPluginResourceReference extends JavaScriptResourceReference
{
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new {@code JQueryPluginResourceReference}
	 * 
	 * @param scope
	 *            mandatory parameter
	 * @param name
	 *            mandatory parameter
	 */
	public JQueryPluginResourceReference(Class<?> scope, String name)
	{
		super(scope, name);
	}

	/**
	 * Creates a new {@code JQueryPluginResourceReference}
	 * 
	 * @param scope
	 *            mandatory parameter
	 * @param name
	 *            mandatory parameter
	 * @param locale
	 *            resource locale
	 * @param style
	 *            resource style
	 * @param variation
	 *            resource variation
	 */
	public JQueryPluginResourceReference(Class<?> scope, String name, Locale locale, String style,
		String variation)
	{
		super(scope, name, locale, style, variation);
	}

	@Override
	public List<HeaderItem> getDependencies()
	{
		final ResourceReference backingLibraryReference;
		if (Application.exists())
		{
			backingLibraryReference = Application.get()
				.getJavaScriptLibrarySettings()
				.getJQueryReference();
		}
		else
		{
			backingLibraryReference = JQueryResourceReference.get();
		}
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(JavaScriptHeaderItem.forReference(backingLibraryReference));
		return dependencies;
	}
}
