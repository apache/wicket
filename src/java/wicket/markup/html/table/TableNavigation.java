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
{ // TODO finalize javadoc
	/** Serial Version ID. */
	private static final long serialVersionUID = 8591577491410447609L;

    /** The table this navigation is navigating. */
    protected Table table;

    /**
     * Constructor.
     * @param componentName The name of this component
     * @param table The table to navigate
     */
    public TableNavigation(final String componentName, final Table table)
    {
        super(componentName, new Model(null));
        this.table = table;
        
        this.setStartIndex(0);
    }

    /**
     * Adds a {@link TableNavigationLink}to the cell. Override this to add custom
     * components for your navigation. Use (TableNavigationLink)cell.getModel() to get the
     * current link.
     * @param listItem the list item to populate
     * @see wicket.markup.html.table.Table#populateItem(wicket.markup.html.table.ListItem)
     */
    protected void populateItem(final ListItem listItem)
    {
        // Get link
        final int page = ((Integer) listItem.getModelObject()).intValue();
        final TableNavigationLink link = new TableNavigationLink("pageLink", table, page);

        // Add pagenumber label (1..n) to the navigation link
        link.add(new Label("pageNumber", String.valueOf(page + 1)));

        // Add the navigation link to the cell
        listItem.add(link);
    }
   
    /**
     * Creates a new listItem  for the given listItem index of this listView.
     * 
     * @param index ListItem index
     * @return The new ListItem
     */
    protected ListItem newItem(final int index)
    {
        return new ListItem(index, new Model(new Integer(index)));
    }

    /**
     * Gets the table that is used to get the number of pages.
     * @return the table that is used to get the number of pages
     */
    public Table getTable()
    {
        return table;
    }

    /**
     * Sets the table that is used to get the number of pages.
     * @param table the table that is used to get the number of pages
     */
    public void setTable(Table table)
    {
        this.table = table;
    }

    /**
     * @see wicket.markup.html.table.ListView#getViewSize()
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


