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
package wicket.examples.displaytag;

import java.util.ArrayList;
import java.util.List;

import wicket.RequestCycle;
import wicket.examples.displaytag.utils.ListObject;
import wicket.examples.displaytag.utils.PagedTableWithAlternatingRowStyle;
import wicket.markup.ComponentTag;
import wicket.markup.MarkupStream;
import wicket.markup.html.basic.Label;
import wicket.markup.html.panel.Panel;
import wicket.markup.html.table.ListItem;
import wicket.markup.html.table.SortableTableHeader;
import wicket.markup.html.table.SortableTableHeaders;
import wicket.markup.html.table.TableNavigation;
import wicket.markup.html.table.TableNavigationIncrementLink;
import wicket.markup.html.table.TableNavigationLink;



/**
 * Sortable + pageable table example
 * 
 * @author Juergen Donnerstag
 */
public class SortablePageableDisplaytagTableComponent extends Panel
{
    // Model data
    final private List data;

    /**
     * Constructor.
     * 
     * @param parameters Page parameters
     */
    public SortablePageableDisplaytagTableComponent(final String componentName, final List data)
    {
        super(componentName);
        
        // Get an internal copy of the model data
        this.data = new ArrayList();
        this.data.addAll(data);

        // Add a table 
        final PagedTableWithAlternatingRowStyle table = new PagedTableWithAlternatingRowStyle("rows", data, 10)
        {
            public void populateItem(final ListItem listItem)
            {
                super.populateItem(listItem);
                
                final ListObject value = (ListObject) listItem.getModelObject();

                listItem.add(new Label("id", new Integer(value.getId())));
                listItem.add(new Label("name", value.getName()));
                listItem.add(new Label("email", value.getEmail()));
                listItem.add(new Label("status", value.getStatus()));
                listItem.add(new Label("comments", value.getDescription()));
            }
        };
        add(table);

        // Add a sortable header to the table
        add(new SortableTableHeaders("header", table, true)
        {
	        protected int compareTo(SortableTableHeader header, Object o1, Object o2)
	        {
	            if (header.getName().equals("id"))
	            {
	                return ((ListObject)o1).getId() - ((ListObject)o2).getId();
	            }
	            
	            return super.compareTo(header, o1, o2);
	        }

	        protected Comparable getObjectToCompare(final SortableTableHeader header, final Object object)
	        {
	            final String name = header.getName();
	            if (name.equals("name"))
	            {
	                return ((ListObject)object).getName();
	            }
	            if (name.equals("email"))
	            {
	                return ((ListObject)object).getEmail();
	            }
	            if (name.equals("status"))
	            {
	                return ((ListObject)object).getStatus();
	            }
	            if (name.equals("comment"))
	            {
	                return ((ListObject)object).getDescription();
	            }
	            
	            return "";
	        }
        });

        // Add a headline
        add(new Label("headline", null)
        {
            protected void handleBody(final RequestCycle cycle, final MarkupStream markupStream,
                    final ComponentTag openTag)
            {
                int firstCell = table.getCurrentPage() * table.getRowsPerPage();
                
                String text = 
                    String.valueOf(data.size()) 
                    + " items found, displaying "
                    + String.valueOf(firstCell + 1)
                    + " to "
                    + String.valueOf(firstCell + table.getRowsPerPage())
                    + ".";
                
                replaceBody(cycle, markupStream, openTag, text);
            }
        });

        final TableNavigation tableNavigation = new TableNavigation("navigation", table /* , 5, 2 */);
        add(tableNavigation);

        // Add some navigation links
        add(new TableNavigationLink("first", table, 0));
        add(new TableNavigationIncrementLink("prev", table, -1));
        add(new TableNavigationIncrementLink("next", table, 1));
        add(new TableNavigationLink("last", table, table.getPageCount() - 1));
    }
}