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
 * An incremental link to a page of a table. Let say your table navigation
 * looks like
 * <pre>
 * [first / << / <] 1 | 2 | 3 [> / >> /last]
 * </pre><p>
 * and "&lt;" meaning the previous and "&lt;&lt;" goto the "current page
 * - 5". Than this kind of incremental paged table link can easily be constructed.
 * 
 * @author Juergen Donnerstag
 */
public class TableNavigationIncrementLink extends PageLink
{ // TODO finalize javadoc
    /** The page of the table this link is for */
    private final int increment;

    /** The table */
    private final Table table;

    /**
     * Constructor.
     * @param componentName The name of this component
     * @param table The table for this page link
     * @param increment increment by
     */
    public TableNavigationIncrementLink(final String componentName, final Table table, final int increment)
    {
        super(componentName, new IPageLink()
        {
            public Page getPage()
            {
                int idx = table.getCurrentPage() + increment;
                if (idx < 0)
                {
                    idx = 0;
                }
                else if (idx > (table.getList().size() - 1))
                {
                    idx = table.getList().size() - 1;
                }
                
                table.setCurrentPage(idx);
                return table.getPage();
            }

            public Class getPageClass()
            {
                return table.getPage().getClass();
            }
        });

        this.increment = increment;
        this.table = table;
    }

    /**
     * Returns true if this table navigation link links to the given page.
     * 
     * @param page The page
     * @return True if this link links to the given page
     * @see wicket.markup.html.link.PageLink#linksTo(wicket.Page)
     */
    public boolean linksTo(final Page page)
    {
        int currentPage = table.getCurrentPage();
        if (((increment < 0) && (currentPage <= 0)) || 
            ((increment > 0) && (currentPage >= (table.getPageCount() - 1))))
        {
            return true;
        }
        
        return false;
    }

    /**
     * @return True if this page is the first page of the containing table
     */
    public boolean isFirst()
    {
        return table.getCurrentPage() <= 0;
    }

    /**
     * @return True if this page is the last page of the containing table
     */
    public boolean isLast()
    {
        return table.getCurrentPage() >= (table.getPageCount() - 1);
    }
}


