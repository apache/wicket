/*
 * $Id: AutoLinkResolver.java,v 1.2 2005/02/10 18:01:32 jonathanlocke
 * Exp $ $Revision$ $Date$
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
package wicket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.markup.ComponentTag;
import wicket.markup.MarkupStream;
import wicket.markup.html.link.BookmarkablePageLink;
import wicket.markup.html.link.ExternalLink;
import wicket.util.value.ValueMap;

/**
 * The AutoLinkResolver is responsible to handle automatic link
 * resolution. Autolink components are automatically created by MarkupParser for
 * anchor tags with no explicit wicket component. E.g. &lt;a
 * href="Home.html"&gt;
 * <p>
 * For each such tag a BookmarkablePageLink will be automatically created, with
 * one exception. An ExternalLink is created is if the URL is absolute (starts
 * with "/") and does not reference a valid Page class.
 * <p>
 * It resolves the given URL by searching for a page class, either relative or
 * absolute, specified by the href attribute of the tag. If relative the href
 * URL must be relative to the package containing the associated page. An
 * exception is thrown if no Page class was found.
 * <p>
 * 
 * @see wicket.markup.parser.filter.WicketLinkTagHandler
 * @author Juergen Donnerstag
 */
public class AutoLinkResolver implements IComponentResolver
{
	/** Logging */
	private static Log log = LogFactory.getLog(AutoLinkResolver.class);

	/**
	 * Automatically creates a BookmarkablePageLink component.
	 * 
	 * @see wicket.IComponentResolver#resolve(MarkupContainer,
	 *      MarkupStream, ComponentTag)
	 * @param markupStream
	 *            The current markupStream
	 * @param tag
	 *            The current component tag while parsing the markup
	 * @param container
	 *            The container parsing its markup
	 * @return true, if componentId was handle by the resolver. False,
	 *         otherwise
	 */
	public boolean resolve(final MarkupContainer container, final MarkupStream markupStream,
			final ComponentTag tag)
	{
		// Must be marked as autolink tag
		if (tag.isAutolinkEnabled())
		{
			// Try to find the Page matching the href
			final Component link = resolveAutomaticLink(container, tag.getId(), tag);

			// Add the link to the container
			container.autoAdd(link);
			if (log.isDebugEnabled())
			{
				log.debug("Added autolink " + link);
			}

			// Tell the container, we resolved the id
			return true;
		}

		// We were not able to resolve the id
		return false;
	}

	/**
	 * Resolves the given tag's page class and page parameters by parsing the
	 * tag component name and then searching for a page class at the absolute or
	 * relative URL specified by the href attribute of the tag.
	 * 
	 * @param container
	 *            The container where the link is
	 * @param componentId
	 *            the name of the component
	 * @param tag
	 *            the component tag
	 * @return A BookmarkablePageLink to handle the href
	 */
	private Component resolveAutomaticLink(final MarkupContainer container,
			final String componentId, final ComponentTag tag)
	{
		final Page page = container.getPage();
		final String originalHref = tag.getAttributes().getString("href");
		final int pos = originalHref.indexOf(".html");

		String classPath = originalHref.substring(0, pos);
		PageParameters pageParameters = null;

		// ".html?" => 6 chars
		if ((classPath.length() + 6) < originalHref.length())
		{
			final String queryString = originalHref.substring(classPath.length() + 6);
			pageParameters = new PageParameters(new ValueMap(queryString, "&"));
		}

		// Make the componentId (page-)unique
		final String id = componentId + page.getAutoIndex();

		// The component name on the tag changed
		tag.setId(id);

		// Obviously a href like href="myPkg.MyLabel.html" will do as well.
		// Wicket will not throw an exception. It accepts it.
		classPath = classPath.replaceAll("/", ".");

		if (!classPath.startsWith("."))
		{
			// Href is relative. Resolve the url given relative to the current
			// page
			final String className = page.getClass().getPackage().getName() + "." + classPath;
			final Class clazz = page.getApplicationSettings().getDefaultClassResolver()
					.resolveClass(className);

			return new BookmarkablePageLink(id, clazz, pageParameters);
		}
		else
		{
			// href is absolute. If class with the same absolute path exists,
			// use it. Else don't change the href.
			final String className = classPath.substring(1);
			try
			{
				final Class clazz = page.getApplicationSettings().getDefaultClassResolver()
						.resolveClass(className);

				return new BookmarkablePageLink(id, clazz, pageParameters);
			}
			catch (WicketRuntimeException ex)
			{
				; // fall through
			}
		}

		// Don't change the href. Did not find a proper Wicket page
		return new ExternalLink(id, originalHref);
	}
}
