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

import wicket.RequestCycle;

/**
 *
 * @author Juergen Donnerstag
 */
public class TableNavigationWithMargin extends TableNavigation
{ // TODO finalize javadoc
    /** Number of navigation cells left and/or right to keep the current
      * cell somewhere near the middle.
      */
    private int margin;

    /** Default separator between page numbers */
    private String separator = null;

    /**
     * Constructor.
     * @param componentName The name of this component
     * @param table The table to navigate
     */
    public TableNavigationWithMargin(final String componentName, final Table table)
    {
        super(componentName, table);
    }

    /**
     * TableNavigation itself (not table) may have pages.
     * @param cycle request cycle
     */
    protected void handleRender(final RequestCycle cycle)
    {
        // Set window based on table's current page
        this.setStartIndex(); 
        
        // default 
        super.handleRender(cycle);
    }

    /**
     * Render the cell. Add the separator if not last cell
     * @param listItem
     * @param cycle
     * @param lastItem
     */
    protected void renderItem(final ListItem listItem, final RequestCycle cycle, final boolean lastItem)
    {
        // default
        super.renderItem(listItem, cycle, lastItem);
        
        // add separator if not last page
        if ((separator != null) && !lastItem)
        {
            cycle.getResponse().write(separator);
        }
    }

    /**
     * Gets the first cell in the current page of this table. If the last page
     * would only be partially listed, then recalculate firstListItem to show
     * pageSizeInListItems cells up to the last one.
     */
    protected void setStartIndex()
    {
        int firstListItem = this.getStartIndex();
        int viewSize = this.getViewSize();
        int currentPage = table.getCurrentPage();
        
        if (currentPage < (firstListItem + this.margin))
        {
            firstListItem = currentPage - viewSize + margin;
        } 
        else if (currentPage >= (firstListItem + viewSize - this.margin))
        {
            firstListItem = currentPage - margin;
        }
        
        if ((firstListItem + viewSize) >= table.getPageCount())
        {
            firstListItem = table.getPageCount() - viewSize; 
        }
        
        if (firstListItem < 0)
        {
            firstListItem = 0;
        }
        
        this.setStartIndex(firstListItem);
    }

    /**
     * Gets the margin.
     * @return the margin
     */
    public int getMargin()
    {
        return margin;
    }
    
    /**
     * Sets the margin.
     * @param margin the margin
     */
    public void setMargin(int margin)
    {
        this.margin = margin;
    }
    
    /**
     * Gets the seperator.
     * @return the seperator
     */
    public String getSeparator()
    {
        return separator;
    }
    
    /**
     * Sets the seperator.
     * @param separator the seperator
     */
    public void setSeparator(String separator)
    {
        this.separator = separator;
    }
}


