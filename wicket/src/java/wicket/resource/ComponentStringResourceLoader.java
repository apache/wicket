/*
 * $Id: ComponentStringResourceLoader.java,v 1.5 2005/01/19 08:07:57
 * jonathanlocke Exp $ $Revision$ $Date$
 *
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.resource;

import java.util.Locale;
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.Component;
import wicket.MarkupContainer;
import wicket.Page;
import wicket.util.string.Strings;
import wicket.util.value.ValueMap;

/**
 * This string resource loader attempts to find the resource from a bundle that
 * corresponds the the supplied component object or one of its parent
 * containers. Generally the component will be an instance of <code>Page</code>,
 * but it may also be an instance of any reusable component that is packaged
 * along with its own resource files. If the component is not an instance of
 * <code>Page</code> then it must be a component that has already been added
 * to a page.
 * <p>
 * The search order for resources is built around the containers that hold the
 * component (if it is not a page). Consider a Page that contains a Panel that
 * contains a Label. If we pass the Label as the component then resource loading
 * will first look for the resource against the page, then against the panel and
 * finally against the label.
 * <p>
 * The above search order may seem slightly odd at first, but can be explained
 * thus: Team A writes a new component X and packages it as a reusable Wicket
 * component along with all required resources. Team B then creates a new
 * container component Y that holds a instance of an X. However, Team B wishes
 * the text to be different to that which was provided with X so rather than
 * needing to change X, they include override values in the resources for Y.
 * Finally, Team C makes use of component Y in a page they are writing.
 * Initially they are happy with the text for Y so they do not include any
 * override values in the resources for the page. However, after demonstrating
 * to the customer, the customer requests the text for Y to be different. Team C
 * need only provide override values against their page and thus do not need to
 * change Y.
 * <p>
 * This implementation is fully aware of both locale and style values when
 * trying to obtain the appropriate resources.
 * <p>
 * In addition to the above search order, each key will be pre-pended with
 * the relative path of the current component related to the component that is 
 * being searched.
 * <p>
 * In addition to the above search order, each component that is being searched
 * for a resource also includes the resources from any parent classes that it
 * inherits from. For example, PageA extends CommonBasePage which in turn extends
 * WebPage. When a resource lookup is requested on PageA, the resource bundle for
 * PageA is first checked. If the resource is not found in this bundle then the
 * resource bundle for CommonBasePage is checked. This allows designers of base
 * pages and components to define default sets of string resources and then
 * developers implementing subclasses to either override or extend these in their
 * own resource bundle.
 * <p>
 * You may enable log debug messages for this class to fully understand the
 * search order.
 * 
 * @author Chris Turner
 */
public class ComponentStringResourceLoader 
	extends AbstractStringResourceLoader 
	implements IStringResourceLoader
{
	/** Log. */
	private static final Log log = LogFactory.getLog(ComponentStringResourceLoader.class);

	/**
	 * Create and initialise the resource loader.
	 */
	public ComponentStringResourceLoader()
	{
	}

	/**
	 * Get the string resource for the given combination of key, locale and
	 * style. The information is obtained from a resource bundle associated with
	 * the provided component instance (or one of its parent containers). The
	 * supplied component may be null, which indicates that this loader should
	 * be skipped and a value of null will be returned. If the supplied
	 * component is not an instance of <code>Page</code> and has not been
	 * previously added to a <code>Page</code> then an exception will be
	 * thrown.
	 *
	 * @param component
	 *            The component to use to find resources to be loaded
	 * @param key
	 *            The key to obtain the string for
	 * @param locale
	 *            The locale identifying the resource set to select the strings
	 *            from
	 * @param style
	 *            The (optional) style identifying the resource set to select
	 *            the strings from (see {@link wicket.Session})
	 * @return The string resource value or null if resource not found
	 */
	public final String loadStringResource(final Component component, final String key,
			final Locale locale, final String style)
	{
		// Check rules
		if (component != null && component.getPage() != null)
		{
			// Build search stack
			Stack searchStack = new Stack();
			searchStack.push(component);
			
			String prefixKey = component.getId() + "." + key;
			if (!(component instanceof Page))
			{
				MarkupContainer c = component.getParent();
				while (true)
				{
					searchStack.push(c);
					if (c instanceof Page)
					{
						break;
					}
					
					prefixKey = c.getId() + "." + prefixKey;
					c = c.getParent();
				}
			}

			// Iterate through search stack
			String value = null;
			while (!searchStack.isEmpty())
			{
				Component c = (Component)searchStack.pop();
				Class cc = c.getClass();

				while (value == null) 
				{
					// Locate previously loaded resources from the cache
					final String id = createCacheId(cc, style, locale);
					ValueMap strings = getResourceCache(id);
					if (strings == null)
					{
						// No resources previously loaded, attempt to load them
						strings = loadResources(cc, style, locale, id);
					}

					// Lookup value with prefix (relative path) prepended
					if (log.isDebugEnabled())
					{
						log.debug("Try to load resource from: " + id + "; key: " + prefixKey);
					}
					value = strings.getString(prefixKey);
				    if (value != null)
				    {
						if (log.isDebugEnabled())
						{
							log.debug("Found resource from: " + id + "; key: " + prefixKey);
						}
				    	break;
				    }

					// Lookup value
					if (log.isDebugEnabled())
					{
						log.debug("Try to load resource from: " + id + "; key: " + key);
					}
					value = strings.getString(key);
					if (value != null)
					{
						if (log.isDebugEnabled())
						{
							log.debug("Found resource from: " + id + "; key: " + key);
						}

						break;
					}
				     
					// Move to next superclass
					cc = cc.getSuperclass();
					if (isStopResourceSearch(cc))
					{
						break;
					}
				}
				if (value != null)
				{
					break;
				}
				
				prefixKey = Strings.afterFirst(prefixKey, '.');
			}

			// Return the resource value (may be null if resource was not found)
			return value;
		}
		return null;
	}
}