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
package org.apache.wicket.resource.loader;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.settings.ResourceSettings;
import org.apache.wicket.util.string.AppendingStringBuffer;

/**
 * Creates a nested string resource loader which resolves nested keys.<br>
 * <br>
 * Example:
 * 
 * <pre>
 * <code>
 * List<IStringResourceLoader> loaders = getResourceSettings().getStringResourceLoaders();
 * // Add more loaders here
 * NestedStringResourceLoader element = new NestedStringResourceLoader(loaders,Pattern.compile("#\\(([^ ]*?)\\)"));
 * loaders.clear();
 * loaders.add(element);
 * </code>
 * </pre>
 * 
 * @author Sven Meier
 * @author Tobias Soloschenko
 *
 */
public class NestedStringResourceLoader implements IStringResourceLoader
{
	private final Pattern pattern;

	private final List<IStringResourceLoader> loaders;

	private final ResourceSettings resourceSettings;

	/**
	 * Creates a nested string resource loader
	 * 
	 * @param loaders
	 *            the loaders to be added in a chain
	 * @param pattern
	 *            the pattern for nested keys. Example for <b>#(key)</b> is the pattern:
	 *            <b>Pattern.compile("#\\(([^ ]*?)\\)");</b>
	 */
	public NestedStringResourceLoader(List<IStringResourceLoader> loaders, Pattern pattern)
	{
		this.loaders = new ArrayList<>(loaders);
		this.pattern = pattern;
		this.resourceSettings = Application.get().getResourceSettings();
	}

	@Override
	public String loadStringResource(Component component, String key, Locale locale, String style,
		String variation)
	{
		return loadNestedStringResource(component, key, locale, style, variation);
	}

	@Override
	public String loadStringResource(Class<?> clazz, String key, Locale locale, String style,
		String variation)
	{
		return loadNestedStringResource(clazz, key, locale, style, variation);
	}

	/**
	 * loads nested string resources
	 * 
	 * @param scope
	 *            the scope to find the key
	 * @param key
	 *            the actual key
	 * @param locale
	 *            the locale
	 * @param style
	 *            the style
	 * @param variation
	 *            the variation
	 * @return the load string
	 */
	private String loadNestedStringResource(Object scope, String key, Locale locale, String style,
		String variation)
	{
		Class<?> clazz = null;
		Component component = null;
		if (scope instanceof Component)
		{
			component = (Component)scope;
		}
		else
		{
			clazz = (Class<?>)scope;
		}

		Iterator<IStringResourceLoader> iter = loaders.iterator();
		String value = null;
		while (iter.hasNext() && (value == null))
		{
			IStringResourceLoader loader = iter.next();
			value = component != null
				? loader.loadStringResource(component, key, locale, style, variation)
				: loader.loadStringResource(clazz, key, locale, style, variation);
		}

		if (value == null)
		{
			return handleMissingKey(key, locale, style, component, value);
		}
		
		StringBuffer output = new StringBuffer();
		Matcher matcher = pattern.matcher(value);
		// Search for other nested keys to replace
		while (matcher.find())
		{
			String nestedKey = matcher.group(1);
			String replacedPlaceHolder = component != null
				? loadNestedStringResource(component, nestedKey, locale, style, variation)
				: loadNestedStringResource(clazz, nestedKey, locale, style, variation);

			replacedPlaceHolder = handleMissingKey(nestedKey, locale, style, component,
				replacedPlaceHolder);
			matcher.appendReplacement(output, replacedPlaceHolder);
		}
		matcher.appendTail(output);
		return output.toString();
	}

	/**
	 * Handles a missing key
	 * 
	 * @param nestedKey
	 *            the key which is going to be handled
	 * @param locale
	 *            the actual locale
	 * @param style
	 *            the style
	 * @param component
	 *            the component
	 * 
	 * @param replacedPlaceHolder
	 * @return the replacedPlaceholder
	 */
	private String handleMissingKey(String nestedKey, Locale locale, String style,
		Component component, String replacedPlaceHolder)
	{
		if (replacedPlaceHolder == null)
		{
			if (resourceSettings.getThrowExceptionOnMissingResource())
			{
				AppendingStringBuffer message = new AppendingStringBuffer(
					"Unable to find property: '");
				message.append(nestedKey);
				message.append('\'');

				if (component != null)
				{
					message.append(" for component: ");
					message.append(component.getPageRelativePath());
					message.append(" [class=").append(component.getClass().getName()).append(']');
				}
				message.append(". Locale: ").append(locale).append(", style: ").append(style);

				throw new MissingResourceException(message.toString(),
					(component != null ? component.getClass().getName() : ""), nestedKey);
			}
			else
			{
				replacedPlaceHolder = "[Warning: Property for '" + nestedKey + "' not found]";
			}
		}
		return replacedPlaceHolder;
	}
}
