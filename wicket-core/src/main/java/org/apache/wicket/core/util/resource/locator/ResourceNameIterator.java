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
package org.apache.wicket.core.util.resource.locator;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.util.string.Strings;

/**
 * Contains the logic to locate a resource based on a path, style (see
 * {@link org.apache.wicket.Session}), variation, locale and extension strings. The full filename
 * will be built like:
 * &lt;path&gt;_&lt;variation&gt;_&lt;_&lt;style&gt;_&lt;locale&gt;.&lt;extension&gt;.
 * <p>
 * Resource matches will be attempted in the following order:
 * <ol>
 * <li>1. &lt;path&gt;_&lt;style&gt;_&lt;locale&gt;.&lt;extension&gt;</li>
 * <li>2. &lt;path&gt;_&lt;locale&gt;.&lt;extension&gt;</li>
 * <li>3. &lt;path&gt;_&lt;style&gt;.&lt;extension&gt;</li>
 * <li>4. &lt;path&gt;.&lt;extension&gt;</li>
 * </ol>
 * <p>
 * Locales may contain a language, a country and a region or variant. Combinations of these
 * components will be attempted in the following order:
 * <ol>
 * <li>locale.toString() see javadoc for Locale for more details</li>
 * <li>&lt;language&gt;_&lt;country&gt;</li>
 * <li>&lt;language&gt;</li>
 * </ol>
 *
 * @author Juergen Donnerstag
 */
public class ResourceNameIterator implements Iterator<String>
{
	// The base path without extension, style, locale etc.
	private final String path;

	// The extensions to search for the resource file
	private final Iterable<String> extensions;

	// The locale to search for the resource file
	private final Locale locale;

	// Do not test any combinations. Just return the full path based on the locale, style etc.
	// provided. Only iterate over the extensions provided.
	private final boolean strict;

	// The various iterators used to locate the resource file
	private final StyleAndVariationResourceNameIterator styleIterator;
	private LocaleResourceNameIterator localeIterator;
	private ExtensionResourceNameIterator extensionsIterator;

	/**
	 * Construct.
	 *
	 * @param path
	 *            The path of the resource. In case the parameter 'extensions' is null, the path
	 *            will be checked and if a filename extension is present, it'll be used instead.
	 * @param style
	 *            A theme or style (see {@link org.apache.wicket.Session})
	 * @param variation
	 *            The component's variation (of the style)
	 * @param locale
	 *            The Locale to apply
	 * @param extensions
	 *            the filename's extensions
	 * @param strict
	 *            If false, weaker combinations of style, locale, etc. are tested as well
	 */
	public ResourceNameIterator(final String path, final String style, final String variation,
		final Locale locale, final Iterable<String> extensions, final boolean strict)
	{
		this.locale = locale;

		boolean noext = extensions == null || !extensions.iterator().hasNext();

		if (noext && (path != null) && (path.indexOf('.') != -1))
		{
			String[] extns = Strings.split(Strings.afterLast(path, '.'), ',');
			this.extensions = Arrays.asList(extns);
			this.path = Strings.beforeLast(path, '.');
		}
		else
		{
			this.extensions = extensions;
			this.path = path;
		}

		styleIterator = newStyleAndVariationResourceNameIterator(style, variation);
		this.strict = strict;
	}

	/**
	 * Get the exact Locale which has been used for the latest resource path.
	 *
	 * @return current Locale
	 */
	public final Locale getLocale()
	{
		return localeIterator.getLocale();
	}

	/**
	 * Get the exact Style which has been used for the latest resource path.
	 *
	 * @return current Style
	 */
	public final String getStyle()
	{
		return styleIterator.getStyle();
	}

	/**
	 * Get the exact Variation which has been used for the latest resource path.
	 *
	 * @return current Variation
	 */
	public final String getVariation()
	{
		return styleIterator.getVariation();
	}

	/**
	 * Get the exact filename extension used for the latest resource path.
	 *
	 * @return current filename extension
	 */
	public final String getExtension()
	{
		return extensionsIterator.getExtension();
	}

	/**
	 * @see java.util.Iterator#hasNext()
	 */
	@Override
	public boolean hasNext()
	{
		// Most inner loop. Loop through all extensions provided
		if (extensionsIterator != null)
		{
			if (extensionsIterator.hasNext() == true)
			{
				return true;
			}

			// If there are no more extensions, than return to the next outer
			// loop (locale). Get the next value from that loop and start
			// over again with the first extension in the list.
			extensionsIterator = null;
		}

		// 2nd inner loop: Loop through all Locale combinations
		if (localeIterator != null)
		{
			while (localeIterator.hasNext())
			{
				localeIterator.next();

				extensionsIterator = newExtensionResourceNameIterator(extensions);
				if (extensionsIterator.hasNext() == true)
				{
					return true;
				}
			}
			localeIterator = null;
		}

		// Most outer loop: Loop through all combinations of styles and variations
		while (styleIterator.hasNext())
		{
			styleIterator.next();

			localeIterator = newLocaleResourceNameIterator(locale, strict);
			while (localeIterator.hasNext())
			{
				localeIterator.next();

				extensionsIterator = newExtensionResourceNameIterator(extensions);
				if (extensionsIterator.hasNext() == true)
				{
					return true;
				}
			}

			if (strict)
			{
				break;
			}
		}

		// No more combinations found. End of iteration.
		return false;
	}

	/**
	 * @see java.util.Iterator#next()
	 */
	@Override
	public String next()
	{
		if (extensionsIterator != null)
		{
			extensionsIterator.next();

			return toString();
		}
		throw new WicketRuntimeException(
			"Illegal call of next(). Iterator not properly initialized");
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return path + prepend(getVariation(), '_') + prepend(getStyle(), '_') +
			prepend(getLocale(), '_') + prepend(getExtension(), '.');
	}

	/**
	 *
	 * @param string
	 * @param prepend
	 * @return The string prepended with the char
	 */
	private String prepend(Object string, char prepend)
	{
		return (string != null) ? prepend + string.toString() : "";
	}

	/**
	 * @param locale
	 * @param strict
	 * @return New iterator
	 */
	protected LocaleResourceNameIterator newLocaleResourceNameIterator(final Locale locale,
		boolean strict)
	{
		return new LocaleResourceNameIterator(locale, strict);
	}

	/**
	 *
	 * @param style
	 * @param variation
	 * @return new iterator
	 */
	protected StyleAndVariationResourceNameIterator newStyleAndVariationResourceNameIterator(
		final String style, final String variation)
	{
		return new StyleAndVariationResourceNameIterator(style, variation);
	}

	/**
	 * @param extensions
	 * @return New iterator
	 */
	protected ExtensionResourceNameIterator newExtensionResourceNameIterator(final Iterable<String> extensions)
	{
		return new ExtensionResourceNameIterator(extensions);
	}

	/**
	 * @see java.util.Iterator#remove()
	 */
	@Override
	public void remove()
	{
		// ignore
	}
}
