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
package org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.dynamic;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.wicket.Application;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.core.util.resource.locator.IResourceStreamLocator;
import org.apache.wicket.markup.MarkupNotFoundException;
import org.apache.wicket.util.io.IClusterable;
import org.apache.wicket.util.io.Streams;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;

/**
 * A template read from an {@code .html} resource, found by the application's
 * {@link IResourceStreamLocator} the way a component's markup is: for a style, a variation and a
 * locale, so {@code Template_de.html} is picked for a German locale. As with a panel, the template
 * is the content of the file's {@code <wicket:panel>}, or the whole file if it has none, so a
 * license header outside of it is left out. Comments are removed if the
 * application {@link org.apache.wicket.settings.MarkupSettings#getStripComments() strips them}
 * from markup.
 */
final class ResourceTemplate implements IClusterable
{
	private static final long serialVersionUID = 1L;

	private static final String EXTENSION = "html";

	private static final Pattern PANEL = Pattern.compile(
		"<wicket:panel(?:\\s[^>]*)?>(.*?)</wicket:panel>", Pattern.DOTALL);

	private static final Pattern COMMENT = Pattern.compile("<!--(?!\\[if).*?-->", Pattern.DOTALL);

	private final Class<?> scope;

	private final Class<?> stopClass;

	private final String name;

	private ResourceTemplate(Class<?> scope, Class<?> stopClass, String name)
	{
		this.scope = scope;
		this.stopClass = stopClass;
		this.name = name;
	}

	/**
	 * @param scope
	 *            the class the template is named after
	 * @param stopClass
	 *            the superclass at which the search up the hierarchy of {@code scope} ends,
	 *            exclusive
	 * @return a template named after {@code scope} or, failing that, after its superclasses
	 */
	static ResourceTemplate forClass(Class<?> scope, Class<?> stopClass)
	{
		return new ResourceTemplate(Args.notNull(scope, "scope"), stopClass, null);
	}

	/**
	 * @param scope
	 *            the class whose package holds the template
	 * @param name
	 *            the name of the template, without the extension
	 * @return the template {@code name.html} in the package of {@code scope}
	 */
	static ResourceTemplate named(Class<?> scope, String name)
	{
		return new ResourceTemplate(Args.notNull(scope, "scope"), null,
			Args.notEmpty(name, "name"));
	}

	/**
	 * @return the template for the style, variation and locale
	 * @throws MarkupNotFoundException
	 *             if there is no such resource
	 */
	String load(String style, String variation, Locale locale)
	{
		IResourceStreamLocator locator = Application.get()
			.getResourceSettings()
			.getResourceStreamLocator();

		if (name != null)
		{
			String path = scope.getPackageName().replace('.', '/') + "/" + name;
			return read(locator.locate(scope, path, style, variation, locale, EXTENSION, false));
		}

		for (Class<?> clazz = scope; clazz != null && clazz != stopClass;
			clazz = clazz.getSuperclass())
		{
			IResourceStream stream = locator.locate(clazz, clazz.getName().replace('.', '/'),
				style, variation, locale, EXTENSION, false);
			if (stream != null)
			{
				return read(stream);
			}
		}
		return read(null);
	}

	/**
	 * @return a key identifying the template loaded for the style, variation and locale
	 */
	String getCacheKey(String style, String variation, Locale locale)
	{
		return scope.getName() + ':' + (name != null ? name : "") + '_' + variation + '_' +
			style + '_' + locale;
	}

	private String read(IResourceStream stream)
	{
		if (stream == null)
		{
			throw new MarkupNotFoundException("No template " + describe() + " found");
		}
		try (stream; InputStream in = stream.getInputStream())
		{
			String template = Streams.readString(in, getEncoding());
			Matcher panel = PANEL.matcher(template);
			if (panel.find())
			{
				template = panel.group(1);
			}
			if (Application.get().getMarkupSettings().getStripComments())
			{
				template = COMMENT.matcher(template).replaceAll("");
			}
			return template;
		}
		catch (IOException | ResourceStreamNotFoundException e)
		{
			throw new WicketRuntimeException("Reading the template " + describe() + " failed", e);
		}
	}

	private static String getEncoding()
	{
		String encoding = Application.get().getMarkupSettings().getDefaultMarkupEncoding();
		return encoding != null ? encoding : StandardCharsets.UTF_8.name();
	}

	private String describe()
	{
		return name != null ? "'" + name + "." + EXTENSION + "' next to " + scope.getName()
			: "named after " + scope.getName();
	}
}
