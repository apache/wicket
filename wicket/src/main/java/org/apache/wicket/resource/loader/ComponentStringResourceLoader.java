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
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.resource.IPropertiesFactory;
import org.apache.wicket.resource.Properties;
import org.apache.wicket.util.resource.locator.ResourceNameIterator;
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
	 * 
	 * @see org.apache.wicket.resource.loader.IStringResourceLoader#loadStringResource(java.lang.Class,
	 *      java.lang.String, java.util.Locale, java.lang.String)
	 */
	public String loadStringResource(Class<?> clazz, final String key, final Locale locale,
		final String style)
	{
		if (clazz == null)
		{
			return null;
		}

		// Load the properties associated with the path
		IPropertiesFactory propertiesFactory = Application.get()
			.getResourceSettings()
			.getPropertiesFactory();

		while (true)
		{
			// Create the base path
			String path = clazz.getName().replace('.', '/');

			// Iterator over all the combinations
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
	 * 
	 * @see org.apache.wicket.resource.loader.IStringResourceLoader#loadStringResource(org.apache.wicket.Component,
	 *      java.lang.String)
	 */
	public String loadStringResource(final Component component, final String key)
	{
		if (component == null)
		{
			return null;
		}

		// The return value
		String string = null;
		Locale locale = component.getLocale();
		String style = component.getStyle();

		// The key prefix is equal to the component path relative to the
		// current component on the top of the stack.
		String prefix = Strings.replaceAll(component.getPageRelativePath(), ":", ".").toString();

		// The reason why we need to create that stack is because we need to
		// walk it downwards starting with Page down to the Component
		List<Class<?>> searchStack = getComponentStack(component);

		// Walk the component hierarchy down from page to the component
		for (int i = searchStack.size() - 1; (i >= 0) && (string == null); i--)
		{
			Class<?> clazz = searchStack.get(i);

			// First, try the fully qualified resource name relative to the
			// component on the path from page down.
			if ((prefix != null) && (prefix.length() > 0))
			{
				string = loadStringResource(clazz, prefix + '.' + key, locale, style);

				if (string == null)
				{
					prefix = Strings.afterFirst(prefix, '.');
				}
			}

			// If not found, than check if a property with the 'key' provided by
			// the user can be found.
			if (string == null)
			{
				string = loadStringResource(clazz, key, locale, style);
			}
		}

		return string;
	}

	/**
	 * Traverse the component hierarchy up to the Page and add each component class to the list
	 * (stack) returned
	 * 
	 * @param component
	 *            The component to evaluate
	 * @return The stack of classes
	 */
	private List<Class<?>> getComponentStack(final Component component)
	{
		// Build the search stack
		final List<Class<?>> searchStack = new ArrayList<Class<?>>();
		searchStack.add(component.getClass());

		if (!(component instanceof Page))
		{
			// Add all the component on the way to the Page
			MarkupContainer container = component.getParent();
			while (container != null)
			{
				searchStack.add(container.getClass());
				if (container instanceof Page)
				{
					break;
				}

				container = container.getParent();
			}
		}
		return searchStack;
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
