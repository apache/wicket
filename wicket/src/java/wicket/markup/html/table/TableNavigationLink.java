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
package wicket.markup.html.table;

import wicket.Page;
import wicket.markup.html.link.IPageLink;
import wicket.markup.html.link.PageLink;


/**
 * A link to a page of a table.
 * 
 * @author Jonathan Locke
 * @author Eelco Hillenius
 */
public final class TableNavigationLink extends PageLink
{ // TODO finalize javadoc
	/** Serial Version ID */
	private static final long serialVersionUID = 9064718260408332988L;

	/** The page of the table this link is for */
    private final int pageNumber;

    /** The table */
    private final Table table;

    /**
     * Constructor.
     * @param componentName The name of this component
     * @param table The table for this page link
     * @param pageNumber The page number in the table that this link links to
     */
    public TableNavigationLink(final String componentName, final Table table, final int pageNumber)
    {
        super(componentName, new IPageLink()
        {
			/** Serial Version ID */
			private static final long serialVersionUID = 7836120196635892372L;

			public Page getPage()
            {
                table.setCurrentPage(pageNumber);

                return table.getPage();
            }

            public Class getPageIdentity()
            {
                return table.getPage().getClass();
            }
        });

        this.pageNumber = (pageNumber < 0 ? 0 : pageNumber);
        this.table = table;
    }

    /**
     * Returns true if this table navigation link links to the given page.
     * @param page The page
     * @return True if this link links to the given page
     * @see wicket.markup.html.link.PageLink#linksTo(wicket.Page)
     */
    public boolean linksTo(final Page page)
    {
        return pageNumber == table.getCurrentPage();
    }

    /**
     * Get pageNumber.
     * @return pageNumber.
     */
    public final int getPageNumber()
    {
        return pageNumber;
    }

    /**
     * @return True if this page is the first page of the containing table
     */
    public boolean isFirst()
    {
        return pageNumber == 0;
    }

    /**
     * @return True if this page is the last page of the containing table
     */
    public boolean isLast()
    {
        return pageNumber == (table.size() - 1);
    }
}


