/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket.markup;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.Component;
import wicket.Container;
import wicket.Page;
import wicket.PageParameters;
import wicket.RequestCycle;
import wicket.markup.html.link.BookmarkablePageLink;
import wicket.util.value.ValueMap;

/**
 * The AutolinkComponentNameResolver is responsible to handle automatic link
 * resolution. Autolink components are automatically created by MarkupParser 
 * for anchor tags with no explicit wicket component. 
 * E.g. &lt;a href="Home.html"&gt;
 * <p>
 * For each such tag a BookmarkablePageLink will be automatically created.
 * <p>
 * It resolves the given URL by searching for a page class at
 * the relative URL specified by the href attribute of the tag. The href URL is
 * relative to the package containing the page where this component is contained.
 * 
 * @author Juergen Donnerstag
 */
public class AutolinkComponentResolver implements IComponentResolver
{
    /** Logging */
    private static Log log = LogFactory.getLog(AutolinkComponentResolver.class);
    
    /**
     * Automatically creates a BookmarkablePageLink component.
     * 
     * @see wicket.markup.IComponentResolver#resolve(RequestCycle, MarkupStream, ComponentTag, Container)
     * @param cycle The current RequestCycle 
     * @param markupStream The current markupStream
     * @param tag The current component tag while parsing the markup
     * @param container The container parsing its markup
     * @return true, if componentName was handle by the resolver. False, otherwise  
     */
	public boolean resolve(final Container container, final MarkupStream markupStream,
			final ComponentTag tag)
	{
	    // Must be marked as autolink tag
        if (tag.isAutolinkEnabled())
        {
            // Try to find the Page matching the href
    	    final String componentName = tag.getComponentName();
            final Component link = resolveAutomaticLink(container.getPage(), componentName, tag);

	        // Add the link to the container
			container.add(link);
			if (log.isDebugEnabled()) 
			{
				log.debug("Added autolink " + link);
			}

			// Render the Link
			link.render();
			
			// Tell the container, we handled the componentName
			return true;
		}
	
        // We were not able to handle the componentName
        return false;
	}
	
    /**
     * Resolves the given tag's automaticLinkPageClass and automaticLinkPageParameters
     * variables by parsing the tag component name and then searching for a page class at
     * the relative URL specified by the href attribute of the tag. The href URL is
     * relative to the package containing the page where this component is contained.
     * @param page The page where the link is
     * @param componentName the name of the component
     * @param tag the component tag
     * @return A BookmarkablePageLink to handle the href
     */
    private Component resolveAutomaticLink(final Page page, 
    		final String componentName, final ComponentTag tag)
    {
        final String originalHref = tag.getAttributes().getString("href");
        final int pos = originalHref.indexOf(".html");
        
        String classPath = originalHref.substring(0, pos);
        PageParameters pageParameters = null;
        
        // ".html?" => 6 chars
        if ((classPath.length() + 6) < originalHref.length())
        {
            String queryString = originalHref.substring(classPath.length() + 6);
            pageParameters = new PageParameters(new ValueMap(queryString, "&"));
        }
        
        classPath = classPath.replaceAll("/", ".");
        classPath = page.getClass().getPackage().getName() + "." + classPath;
        
        Class clazz = page.getApplicationSettings().getDefaultClassResolver().resolveClass(classPath);

        // Make the componentName (page-)unique
        final String id = componentName + page.getAutoIndex();
        
        return new BookmarkablePageLink(id, clazz, pageParameters);
    }
}
