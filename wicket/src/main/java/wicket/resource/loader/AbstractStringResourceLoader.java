/*
 * $Id: ComponentStringResourceLoader.java,v 1.5 2005/01/19 08:07:57
 * jonathanlocke Exp $ $Revision$ $Date: 2006-04-17 20:02:21 +0000 (Mon,
 * 17 Apr 2006) $
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
package wicket.resource.loader;

import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.Application;
import wicket.Component;
import wicket.MarkupContainer;
import wicket.Page;
import wicket.markup.html.WebComponent;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.html.WebPage;
import wicket.resource.Properties;
import wicket.util.value.ValueMap;

/**
 * This abstract string resource loader provides two helper functions to
 * retrieve the message associated with a key, a locale and a style. The simple
 * one uses the <code>Class</code> provided and its parent classes to find the
 * associated message. The more complex one uses in addition the component
 * provided and its parent containers.
 * <p>
 * The component based string resource loader attempts to find the resource from
 * a bundle that corresponds to the supplied component object or one of its
 * parent containers. Generally the component will be an instance of
 * <code>Page</code>, but it may also be an instance of any reusable
 * component that is packaged along with its own resource files. If the
 * component is not an instance of <code>Page</code> then it must be a
 * component that has already been added to a page.
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
 * In addition to the above search order, each key will be pre-pended with the
 * relative path of the current component related to the component that is being
 * searched. E.g. assume a component hierarchy like page1.form1.input1 and your
 * are requesting a key named 'RequiredValidator'. Wicket will search the
 * property in the following order:
 * 
 * <pre>
 *       page1.properties =&gt; form1.input1.RequiredValidator
 *       page1.properties =&gt; RequiredValidator
 *       form1.properties =&gt; input1.RequiredValidator
 *       form1.properties =&gt; RequiredValidator
 *       input1.properties =&gt; RequiredValidator
 *       myApplication.properties =&gt; page1.form1.input1.RequiredValidator
 *       myApplication.properties =&gt; RequiredValidator
 * </pre>
 * 
 * <p>
 * In addition to the above search order, each component that is being searched
 * for a resource also includes the resources from any parent classes that it
 * inherits from. For example, PageA extends CommonBasePage which in turn
 * extends WebPage. When a resource lookup is requested on PageA, the resource
 * bundle for PageA is first checked. If the resource is not found in this
 * bundle then the resource bundle for CommonBasePage is checked. This allows
 * designers of base pages and components to define default sets of string
 * resources and then developers implementing subclasses to either override or
 * extend these in their own resource bundle.
 * <p>
 * You may enable log debug messages for this class to fully understand the
 * search order.
 * 
 * @author Chris Turner
 * @author Juergen Donnerstag
 */
public abstract class AbstractStringResourceLoader implements IStringResourceLoader
{
	/** Log. */
	private static final Log log = LogFactory.getLog(AbstractStringResourceLoader.class);

	/** Wickets application object */
	protected final Application application;

	/**
	 * Create and initialise the resource loader.
	 * 
	 * @param application
	 *            Wickets application object
	 */
	public AbstractStringResourceLoader(final Application application)
	{
		this.application = application;
	}

	/**
	 * Get the string resource for the given combination of class, key, locale
	 * and style. The information is obtained from a resource bundle associated
	 * with the provided Class (or one of its super classes).
	 * 
	 * @param clazz
	 *            The Class to find resources to be loaded
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
	public String loadStringResource(Class clazz, final String key, final Locale locale,
			final String style)
	{
		if (clazz == null)
		{
			return null;
		}

		String value = null;
		while (true)
		{
			// Get (or load) the properties associated with clazz, locale and
			// style
			final Properties props = getProperties(clazz, locale, style);
			if (props != null)
			{
				ValueMap strings = props.getAll();

				// Lookup value
				if (log.isDebugEnabled())
				{
					log.debug("Try to load resource from: " + props + "; key: " + key);
				}
				value = strings.getString(key);
				if (value != null)
				{
					if (log.isDebugEnabled())
					{
						log.debug("Found resource from: " + props + "; key: " + key);
					}

					break;
				}
			}

			if (isStopResourceSearch(clazz))
			{
				break;
			}

			// Move to the next superclass
			clazz = clazz.getSuperclass();
		}

		// Return the resource value (may be null if resource was not found)
		return value;
	}

	/**
	 * Get (or load) the properties associated with clazz, locale and style.
	 * 
	 * @param clazz
	 *            The class to find resources to be loaded
	 * @param locale
	 *            The locale identifying the resource set to select the strings
	 *            from
	 * @param style
	 *            The (optional) style identifying the resource set to select
	 *            the strings from (see {@link wicket.Session})
	 * @return The string resource value or null if resource not found
	 */
	protected Properties getProperties(final Class clazz, final Locale locale, final String style)
	{
		return application.getResourceSettings().getPropertiesFactory().get(clazz, style, locale);
	}

	/**
	 * Check the supplied class to see if it is one that we shouldn't bother
	 * further searches up the class hierarchy for properties.
	 * 
	 * @param clazz
	 *            The class to check
	 * @return Whether to stop the search
	 */
	protected boolean isStopResourceSearch(final Class clazz)
	{
		if (clazz == null || clazz.equals(Object.class) || clazz.equals(Application.class))
		{
			return true;
		}

		// Stop at all html markup base classes
		if (clazz.equals(WebPage.class) || clazz.equals(WebMarkupContainer.class)
				|| clazz.equals(WebComponent.class))
		{
			return true;
		}

		// Stop at all wicket base classes
		return clazz.equals(Page.class) || clazz.equals(MarkupContainer.class)
				|| clazz.equals(Component.class);
	}
}