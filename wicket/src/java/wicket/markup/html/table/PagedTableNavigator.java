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
import wicket.markup.ComponentTag;
import wicket.markup.MarkupStream;
import wicket.markup.html.basic.Label;
import wicket.markup.html.panel.Panel;



/**
 * A Wicket component, including markup, to draw and maintain a complete
 * page navigator, meant to be easily added to any table.
 *  
 * @author Juergen Donnerstag
 */
public class PagedTableNavigator extends Panel 
{
    private final TableNavigation tableNavigation;
    
    /**
     * Constructor.
     * @param componentName
     * @param table
     */
    public PagedTableNavigator(final String componentName, final Table table)
    {
        super(componentName);
        
        this.tableNavigation = newTableNavigation(table);
        add(tableNavigation);
            
        // model = null; the model will be auto-generated during handleBody
        add(new Label("headline", null)
        {
            // Dynamically - at runtime - create the text
            protected void handleBody(final RequestCycle cycle, final MarkupStream markupStream,
                    final ComponentTag openTag)
            {
                String text = getHeadlineText(table); 
                replaceBody(cycle, markupStream, openTag, text);
            }
        });
        
        add(new TableNavigationLink("first", table, 0));
        add(new TableNavigationIncrementLink("prev", table, -1));
        add(new TableNavigationIncrementLink("next", table, 1));
        add(new TableNavigationLink("last", table, table.getPageCount() - 1));
    }

    /**
     * Create a new TableNavigation. May be subclassed to make us of
     * specialized TableNavigation.
     * 
     * @param table
     * @return table navigation object
     */
    protected TableNavigation newTableNavigation(final Table table)
    {
        return new TableNavigation("navigation", table);
    }
    
    /**
     * Subclasses may override it to provide their own text.
     * 
     * @param table
     * @return head line text
     */
    protected String getHeadlineText(final Table table)
    {
        int firstCell = table.getCurrentPage() * table.getRowsPerPage();
        StringBuffer buf = new StringBuffer(80);
        buf.append(String.valueOf(table.getList().size()))
           .append(" items found, displaying ")
           .append(String.valueOf(firstCell + 1))
           .append(" to ")
           .append(String.valueOf(firstCell + table.getRowsPerPage()))
           .append(".");
        
        return buf.toString();
    }
}