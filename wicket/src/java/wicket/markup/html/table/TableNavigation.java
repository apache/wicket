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

import wicket.markup.html.basic.Label;
import wicket.model.Model;


/**
 * A navigation for a table that holds links to other pages of the table.
 * <p>
 * For each row (one page of the list of pages) a {@link TableNavigationLink} will be
 * added that contains a {@link Label}with the page number of that link (1..n).
 * 
 * <pre>
 * 
 * 
 * 
 *      &lt;td id=&quot;wicket-navigation&quot;&gt;
 *          &lt;a id=&quot;wicket-pageLink&quot; href=&quot;SearchCDPage.html&quot;&gt;
 *             &lt;span id=&quot;wicket-pageNumber&quot;/&gt;
 *          &lt;/a&gt;
 *      &lt;/td&gt;
 * 
 * 
 *  
 * </pre>
 * 
 * thus renders like:
 * 
 * <pre>
 * 
 * 
 * 
 *      1 |  2 |  3 |  4 |  5 |  6 |  7 |  8 |  9 |
 * 
 * 
 *  
 * </pre>
 * 
 * </p>
 * <p>
 * Override method populateItem to customize the rendering of the navigation. For
 * instance:
 * 
 * <pre>
 * 
 * protected void populateItem(ListItem listItem)
 * {
 *     final int page = ((Integer) listItem.getModelObject()).intValue();
 *     final TableNavigationLink link = new TableNavigationLink(&quot;pageLink&quot;, table, page);
 *     if (page &gt; 0)
 *     {
 *         listItem.add(new Label(&quot;separator&quot;, &quot;|&quot;));
 *     }
 *     else
 *     {
 *         listItem.add(new Label(&quot;separator&quot;, &quot;&quot;));
 *     }
 *     link.add(new Label(&quot;pageNumber&quot;, String.valueOf(page + 1)));
 *     link.add(new Label(&quot;pageLabel&quot;, &quot;page&quot;));
 *     listItem.add(link);
 * }
 * </pre>
 * 
 * With:
 * 
 * <pre>
 * 
 * 
 * 
 *      &lt;td id=&quot;wicket-navigation&quot;&gt;
 *          &lt;span id=&quot;wicket-separator&quot;/&gt;
 *          &lt;a id=&quot;wicket-pageLink&quot; href=&quot;#&quot;&gt;
 *            &lt;span id=&quot;wicket-pageLabel&quot;/&gt;&lt;span id=&quot;wicket-pageNumber&quot;/&gt;
 *          &lt;/a&gt;
 *      &lt;/td&gt;
 * 
 * 
 *  
 * </pre>
 * 
 * renders like:
 * 
 * <pre>
 * page1 | page2 | page3 | page4 | page5 | page6 | page7 | page8 | page9
 * </pre>
 * 
 * </p>
 * In addition
 *
 * @author Jonathan Locke
 * @author Eelco Hillenius
 * @author Juergen Donnerstag
 */
public class TableNavigation extends ListView
{
	/** Serial Version ID. */
	private static final long serialVersionUID = 8591577491410447609L;

    /** The table this navigation is navigating. */
    protected Table table;

    /**
     * Constructor.
     * @param componentName The name of the component
     * @param table The underlying table to navigate
     */
    public TableNavigation(final String componentName, final Table table)
    {
        super(componentName, new Model(null));

        this.table = table;
        this.setStartIndex(0);
    }

    /**
     * Populate the current cell with a page link (TableNavigationLink) enclosing
     * the page number the link is pointing to. Subclasses may provide there own
     * implementation adding more sophisticated page links.
     * 
     * @param listItem the list item to populate
     * @see wicket.markup.html.table.Table#populateItem(wicket.markup.html.table.ListItem)
     */
    protected void populateItem(final ListItem listItem)
    {
        // Get the index of page this link shall point to
        final int pageIndex = ((Integer) listItem.getModelObject()).intValue();
        
        // Add a page link pointing to the page
        final TableNavigationLink link = new TableNavigationLink("pageLink", table, pageIndex);
        listItem.add(link);

        // Add a label (the page number) to the list which is enclosed by the link
        link.add(new Label("pageNumber", String.valueOf(pageIndex + 1)));
    }
   
    /**
     * Provide the ListItem for the index given.<p>
     * TableNavigation actually does not have an underlying model like most
     * other ListViews. It doesn't have to, because the model is simply
     * the index of the table's page. Thus we create a model based on the
     * table's page index.
     * 
     * @param index ListItem index
     * @return The new ListItem
     */
    protected ListItem newItem(final int index)
    {
        return new ListItem(index, new Model(new Integer(index)));
    }

    /**
     * Get the table which the navigation bar is navigating.
     * 
     * @return the table that is used to get the number of pages
     */
    public Table getTable()
    {
        return table;
    }

    /**
     * Set the table which the navigation bar is navigating.
     * 
     * @param table the table that is used to get the number of pages
     */
    public void setTable(Table table)
    {
        this.table = table;
    }

    /**
     * Get the number of page links per "window".
     * 
     * @see wicket.markup.html.table.ListView#setViewSize(int)
     * @return The overall number of page links (= number of table pages). 
     *      0, if no underlying table is available.
     */
    public int getViewSize()
    {
        if(table != null)
        {
            return Math.min(table.getPageCount(), this.viewSize);
        }
        else
        {
            return 0;
        }
    }
}
