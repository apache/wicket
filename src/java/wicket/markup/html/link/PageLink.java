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
package wicket.markup.html.link;

import wicket.Page;
import wicket.RenderException;
import wicket.RequestCycle;

/**
 * Links to a given page via an object implementing the IPageLink interface.
 * @author Jonathan Locke
 */
public class PageLink extends Link
{
    /** Serial Version ID */
	private static final long serialVersionUID = 8530958543148278216L;
	
	// The page source
    private final IPageLink pageLink;

    /**
     * Constructor.
     * @param componentName The name of this component
     * @param pageLink The page source object which will produce the page linked to when
     *            the hyperlink is clicked at a later time.
     */
    public PageLink(final String componentName, final IPageLink pageLink)
    {
        super(componentName);
        this.pageLink = pageLink;
    }

    /**
     * Constructor.
     * @param componentName Name of this component
     * @param c Page class
     */
    public PageLink(final String componentName, final Class c)
    {
        this(componentName, new IPageLink()
        {
            /** Serial Version ID */
			private static final long serialVersionUID = 319659497178801753L;

			public Page getPage()
            {
                try
                {
                    return (Page) c.newInstance();
                }
                catch (InstantiationException e)
                {
                    throw new RenderException("Cannot instantiate page class " + c, e);
                }
                catch (IllegalAccessException e)
                {
                    throw new RenderException("Cannot instantiate page class " + c, e);
                }
            }

            public Class getPageClass()
            {
                return c;
            }
        });

        // Ensure that c is a subclass of Page
        if (!Page.class.isAssignableFrom(c))
        {
            throw new IllegalArgumentException("Class " + c + " is not a subclass of Page");
        }
    }

    /**
     * @see wicket.markup.html.link.ILinkListener#linkClicked(wicket.RequestCycle)
     */
    public final void linkClicked(final RequestCycle cycle)
    {
        // Set page source's page as wicket.response page
        cycle.setPage(pageLink.getPage());
    }

    /**
     * @see wicket.markup.html.link.Link#linksTo(wicket.Page)
     */
    public boolean linksTo(final Page page)
    {
        return page.getClass() == pageLink.getPageClass();
    }
}

///////////////////////////////// End of File /////////////////////////////////
