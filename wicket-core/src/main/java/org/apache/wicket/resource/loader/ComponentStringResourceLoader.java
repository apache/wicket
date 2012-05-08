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
import java.util.List;
import java.util.Locale;

import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.core.util.resource.locator.IResourceNameIterator;
import org.apache.wicket.core.util.resource.locator.IResourceStreamLocator;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.repeater.AbstractRepeater;
import org.apache.wicket.resource.IPropertiesFactory;
import org.apache.wicket.resource.Properties;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.string.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This is one of Wicket's default string resource loaders.
 * <p>
 * The component based string resource loader attempts to find the resource from a bundle that
 * corresponds to the supplied component object or one of its parent containers.
 * <p>
 * The search order for resources is built around the containers that hold the component (if it is
 * not a page). Consider a Page that contains a Panel that contains a Label. If we pass the Label as
 * the component then resource loading will first look for the resource against the page, then
 * against the panel and finally against the label.
 * <p>
 * The above search order may seem slightly odd at first, but can be explained thus: Team A writes a
 * new component X and packages it as a reusable Wicket component along with all required resources.
 * Team B then creates a new container component Y that holds a instance of an X. However, Team B
 * wishes the text to be different to that which was provided with X so rather than needing to
 * change X, they include override values in the resources for Y. Finally, Team C makes use of
 * component Y in a page they are writing. Initially they are happy with the text for Y so they do
 * not include any override values in the resources for the page. However, after demonstrating to
 * the customer, the customer requests the text for Y to be different. Team C need only provide
 * override values against their page and thus do not need to change Y.
 * <p>
 * This implementation is fully aware of both locale and style values when trying to obtain the
 * appropriate resources.
 * <p>
 * In addition to the above search order, each key will be pre-pended with the relative path of the
 * current component related to the component that is being searched. E.g. assume a component
 * hierarchy like page1.form1.input1 and your are requesting a key named 'Required'. Wicket will
 * search the property in the following order:
 * 
 * <pre>
 *        page1.properties =&gt; form1.input1.Required
 *        page1.properties =&gt; Required
 *        form1.properties =&gt; input1.Required
 *        form1.properties =&gt; Required
 *        input1.properties =&gt; Required
 *        myApplication.properties =&gt; page1.form1.input1.Required
 *        myApplication.properties =&gt; Required
 * </pre>
 * 
 * Note that the latter two property files are only checked if the ClassStringResourceLoader has
 * been registered with Application as well, which is the default.
 * <p>
 * In addition to the above search order, each component that is being searched for a resource also
 * includes the resources from any parent classes that it inherits from. For example, PageA extends
 * CommonBasePage which in turn extends WebPage When a resource lookup is requested on PageA, the
 * resource bundle for PageA is first checked. If the resource is not found in this bundle then the
 * resource bundle for CommonBasePage is checked. This allows designers of base pages and components
 * to define default sets of string resources and then developers implementing subclasses to either
 * override or extend these in their own resource bundle.
 * <p>
 * This implementation can be subclassed to implement modified behavior. The new implementation must
 * be registered with the Application (ResourceSettings) though.
 * <p>
 * You may enable log debug messages for this class to fully understand the search order.
 * 
 * @author Chris Turner
 * @author Juergen Donnerstag
 */
public class ComponentStringResourceLoader implements IStringResourceLoader
{
	/** Log. */
	private static final Logger log = LoggerFactory.getLogger(ComponentStringResourceLoader.class);

	/**
	 * Create and initialize the resource loader.
	 */
	public ComponentStringResourceLoader()
	{
	}

	/**
	 * @see org.apache.wicket.resource.loader.IStringResourceLoader#loadStringResource(java.lang.Class,
	 *      java.lang.String, java.util.Locale, java.lang.String, java.lang.String)
	 */
	@Override
	public String loadStringResource(Class<?> clazz, final String key, final Locale locale,
		final String style, final String variation)
	{
		if (clazz == null)
		{
			return null;
		}

		if (log.isDebugEnabled())
		{
			log.debug("key: '" + key + "'; class: '" + clazz.getName() + "'; locale: '" + locale +
				"'; Style: '" + style + "'; Variation: '" + variation + '\'');
		}

		// Load the properties associated with the path
		IPropertiesFactory propertiesFactory = getPropertiesFactory();
		while (true)
		{
			// Create the base path
			String path = clazz.getName().replace('.', '/');

			// Iterator over all the combinations
			IResourceNameIterator iter = newResourceNameIterator(path, locale, style, variation);
			while (iter.hasNext())
			{
				String newPath = iter.next();

				Properties props = propertiesFactory.load(clazz, newPath);
				if (props != null)
				{
					// Lookup the value
					String value = props.getString(key);
					if (value != null)
					{
						return value;
					}
				}
			}

			// Didn't find the key yet, continue searching if possible
			if (isStopResourceSearch(clazz))
			{
				break;
			}

			// Move to the next superclass
			clazz = clazz.getSuperclass();

			if (clazz == null)
			{
				// nothing more to search, done
				break;
			}
		}

		// not found
		return null;
	}

	/**
	 * @see IResourceStreamLocator#newResourceNameIterator(String, Locale, String, String, String,
	 *      boolean)
	 * 
	 * @param path
	 * @param locale
	 * @param style
	 * @param variation
	 * @return resource name iterator
	 */
	protected IResourceNameIterator newResourceNameIterator(final String path, final Locale locale,
		final String style, final String variation)
	{
		return Application.get()
			.getResourceSettings()
			.getResourceStreamLocator()
			.newResourceNameIterator(path, locale, style, variation, null, false);
	}

	/**
	 * Get the properties file factory which loads the properties based on locale and style from
	 * *.properties and *.xml files
	 * 
	 * @return properties factory
	 */
	protected IPropertiesFactory getPropertiesFactory()
	{
		return Application.get().getResourceSettings().getPropertiesFactory();
	}

	/**
	 * @see org.apache.wicket.resource.loader.IStringResourceLoader#loadStringResource(org.apache.wicket.Component,
	 *      java.lang.String, java.util.Locale, java.lang.String, java.lang.String)
	 */
	@Override
	public String loadStringResource(final Component component, final String key,
		final Locale locale, final String style, final String variation)
	{
		if (component == null)
		{
			return null;
		}

		if (log.isDebugEnabled())
		{
			log.debug("component: '" + component.toString(false) + "'; key: '" + key + '\'');
		}

		// The return value
		String string = null;

		// The key prefix is equal to the component path relative to the
		// current component on the top of the stack.
		String prefix = getResourcePath(component);

		// walk downwards starting with page going down to component
		for (Component current : getComponentTrail(component))
		{
			// get current component class
			final Class<?> clazz = current.getClass();

			// first, try the fully qualified resource name relative to the
			// component on the path from page down.
			if (Strings.isEmpty(prefix) == false)
			{
				// lookup fully qualified path
				string = loadStringResource(clazz, prefix + '.' + key, locale, style, variation);

				// return string if we found it
				if (string != null)
				{
					return string;
				}

				// shorten resource key prefix when going downwards (skip for repeaters)
				if ((current instanceof AbstractRepeater) == false)
				{
					prefix = Strings.afterFirst(prefix, '.');
				}
			}
			// If not found, than check if a property with the 'key' provided by
			// the user can be found.
			string = loadStringResource(clazz, key, locale, style, variation);

			// return string if we found it
			if (string != null)
			{
				return string;
			}
		}

		return string;
	}

	/**
	 * get path for resource lookup
	 * 
	 * @param component
	 * @return path
	 */
	protected String getResourcePath(final Component component)
	{
		Component current = Args.notNull(component, "component");

		final StringBuilder buffer = new StringBuilder();

		while (current.getParent() != null)
		{
			final boolean skip = current.getParent() instanceof AbstractRepeater;

			if (skip == false)
			{
				if (buffer.length() > 0)
				{
					buffer.insert(0, '.');
				}
				buffer.insert(0, current.getId());
			}
			current = current.getParent();
		}
		return buffer.toString();
	}

	/**
	 * return the trail of components from page to specified component
	 * 
	 * @param component
	 *            The component to retrieve path for
	 * @return The list of components starting from top going down to component
	 */
	private List<Component> getComponentTrail(Component component)
	{
		final List<Component> path = new ArrayList<Component>();

		while (component != null)
		{
			path.add(0, component);
			component = component.getParent();
		}
		return path;
	}

	/**
	 * Check the supplied class to see if it is one that we shouldn't bother further searches up the
	 * class hierarchy for properties.
	 * 
	 * @param clazz
	 *            The class to check
	 * @return Whether to stop the search
	 */
	protected boolean isStopResourceSearch(final Class<?> clazz)
	{
		if ((clazz == null) || clazz.equals(Object.class) || clazz.equals(Application.class))
		{
			return true;
		}

		// Stop at all html markup base classes
		if (clazz.equals(WebPage.class) || clazz.equals(WebMarkupContainer.class) ||
			clazz.equals(WebComponent.class))
		{
			return true;
		}

		// Stop at all wicket base classes
		return clazz.equals(Page.class) || clazz.equals(MarkupContainer.class) ||
			clazz.equals(Component.class);
	}
}
