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

import java.util.Locale;

import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.resource.IPropertiesFactory;
import org.apache.wicket.resource.Properties;
import org.apache.wicket.util.resource.locator.ResourceNameIterator;
import org.apache.wicket.validation.IValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This is one of Wicket's default string resource loaders.
 * <p>
 * The validator string resource loader checks resource bundles attached to validators (eg
 * MinimumValidator.properties). The validator list is pulled from the form component in error.
 * <p>
 * This implementation is fully aware of both locale and style values when trying to obtain the
 * appropriate resources.
 * <p>
 * 
 * @author igor.vaynberg
 */
public class ValidatorStringResourceLoader implements IStringResourceLoader
{
	private static final Logger log = LoggerFactory.getLogger(ValidatorStringResourceLoader.class);

	/**
	 * Create and initialize the resource loader.
	 */
	public ValidatorStringResourceLoader()
	{
	}

	/**
	 * @see org.apache.wicket.resource.loader.IStringResourceLoader#loadStringResource(java.lang.Class,
	 *      java.lang.String, java.util.Locale, java.lang.String)
	 */
	public String loadStringResource(Class<?> clazz, final String key, final Locale locale,
		final String style)
	{
		// only care about IValidator subclasses
		if (clazz == null || !IValidator.class.isAssignableFrom(clazz))
		{
			return null;
		}

		IPropertiesFactory propertiesFactory = Application.get()
			.getResourceSettings()
			.getPropertiesFactory();

		while (true)
		{
			// figure out the base path for the class
			String path = clazz.getName().replace('.', '/');

			// iterate over all the combinations
			ResourceNameIterator iter = new ResourceNameIterator(path, style, locale, null);
			while (iter.hasNext())
			{
				String newPath = iter.next();

				final Properties props = propertiesFactory.load(clazz, newPath);
				if (props != null)
				{
					// Lookup the value
					String value = props.getString(key);
					if (value != null)
					{
						if (log.isDebugEnabled())
						{
							log.debug("Found resource from: " + props + "; key: " + key);
						}

						return value;
					}
				}
			}

			// Move to the next superclass
			clazz = clazz.getSuperclass();

			if (clazz == null || Object.class.equals(clazz))
			{
				// nothing more to search, done
				break;
			}
		}

		// not found
		return null;
	}

	/**
	 * 
	 * @see org.apache.wicket.resource.loader.IStringResourceLoader#loadStringResource(org.apache.wicket.Component,
	 *      java.lang.String)
	 */
	public String loadStringResource(final Component component, final String key)
	{
		if (component == null || !(component instanceof FormComponent))
		{
			return null;
		}

		FormComponent<?> fc = (FormComponent<?>)component;

		Locale locale = component.getLocale();
		String style = component.getStyle();

		for (IValidator<?> validator : fc.getValidators())
		{
			String resource = loadStringResource(validator.getClass(), key, locale, style);
			if (resource != null)
			{
				return resource;
			}
		}

		// not found
		return null;
	}

}
