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

import java.util.Locale;

import org.apache.wicket.MarkupContainer;


/**
 * Wicket default implementation for the cache key used to reference the cached markup resource
 * stream.
 * 
 * @author Jonathan Locke
 * @author Juergen Donnerstag
 */
public class DefaultMarkupCacheKeyProvider implements IMarkupCacheKeyProvider
{
	/**
	 * Constructor.
	 */
	public DefaultMarkupCacheKeyProvider()
	{
	}

	/**
	 * Construct a proper key value for the cache
	 * 
	 * @param container
	 *            The container requesting the markup
	 * @param clazz
	 *            The clazz to get the key for
	 * @return Key that uniquely identifies any markup that might be associated with this markup
	 *         container.
	 */
	@Override
	public String getCacheKey(final MarkupContainer container, final Class<?> clazz)
	{
		final String classname = clazz.getName();
		final StringBuilder buffer = new StringBuilder(classname.length() + 64);
		buffer.append(classname);

		final String variation = container.getVariation();
		if (variation != null)
		{
			buffer.append('_').append(variation);
		}

		final String style = container.getStyle();
		if (style != null)
		{
			buffer.append('_').append(style);
		}

		final Locale locale = container.getLocale();
		if (locale != null)
		{
			buffer.append('_').append(locale.toString());
		}

		buffer.append('.').append(container.getMarkupType().getExtension());
		return buffer.toString();
	}
}
