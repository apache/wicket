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
package displaytag;

import java.util.ArrayList;
import java.util.List;

import com.voicetribe.wicket.Container;
import com.voicetribe.wicket.RequestCycle;
import com.voicetribe.wicket.markup.ComponentTag;
import com.voicetribe.wicket.markup.MarkupStream;
import com.voicetribe.wicket.markup.html.basic.Label;
import com.voicetribe.wicket.markup.html.panel.Panel;
import com.voicetribe.wicket.markup.html.table.Cell;
import com.voicetribe.wicket.markup.html.table.SortableTableHeader;
import com.voicetribe.wicket.markup.html.table.SortableTableHeaders;
import com.voicetribe.wicket.markup.html.table.TableNavigation;
import com.voicetribe.wicket.markup.html.table.TableNavigationIncrementLink;
import com.voicetribe.wicket.markup.html.table.TableNavigationLink;

import displaytag.utils.ListObject;
import displaytag.utils.MyPagedTable;

/**
 * Start page for different displaytag pages
 * 
 * @author Juergen Donnerstag
 */
public class SortablePageableDisplaytagTableComponent extends Panel
{
    final private List data;
    /**
     * Constructor.
     * 
     * @param parameters Page parameters
     */
    public SortablePageableDisplaytagTableComponent(final String componentName, final List data)
    {
        super(componentName);
        
        this.data = new ArrayList();
        this.data.addAll(data);
/*        
        SortableTableHeaderGroup headerGroup = 
            new SortableTableHeaderGroup(this, data, "rows");
        
        add(new SimpleHrefComponent("id", headerGroup)
        {
            protected int compareTo(Object o1, Object o2)
            {
                return ((ListObject)o1).getId() - ((ListObject)o2).getId();
            }
        });
        
        add(new SimpleHrefComponent("name", headerGroup)
        {
            protected Comparable getObjectToCompare(Object object)
            {
                return ((ListObject)object).getName();
            }
        });

        add(new SimpleHrefComponent("email", headerGroup)
        {
            protected Comparable getObjectToCompare(Object object)
            {
                return ((ListObject)object).getEmail();
            }
        });
        
        add(new SimpleHrefComponent("status", headerGroup)
        {
            protected Comparable getObjectToCompare(Object object)
            {
                return ((ListObject)object).getStatus();
            }
        });
        
        add(new SimpleHrefComponent("comment", headerGroup)
        {
            protected Comparable getObjectToCompare(Object object)
            {
                return ((ListObject)object).getDescription();
            }
        });
*/
        add(new SortableTableHeaders("header", data, "rows", true)
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

        // Add table of existing comments
        final MyPagedTable table = new MyPagedTable("rows", data)
        {
            public boolean populateCell(final Cell cell, final Container tagClass)
            {
                final ListObject value = (ListObject) cell.getModelObject();

                tagClass.add(new Label("id", new Integer(value.getId())));
                tagClass.add(new Label("name", value.getName()));
                tagClass.add(new Label("email", value.getEmail()));
                tagClass.add(new Label("status", value.getStatus()));
                tagClass.add(new Label("comments", value.getDescription()));
                
                return true;
            }
        };
        
        table.setNumberOfCellsToDisplay(10);
        add(table);

        final TableNavigation tableNavigation = new TableNavigation("navigation", table, 5, 2);
        add(tableNavigation);
            
        add(new Label("headline", null)
        {
            protected void handleBody(final RequestCycle cycle, final MarkupStream markupStream,
                    final ComponentTag openTag)
            {
                String text = 
                    String.valueOf(data.size()) 
                    + " items found, displaying "
                    + String.valueOf(table.getFirstCell() + 1)
                    + " to "
                    + String.valueOf(table.getFirstCell() + table.getNumberOfCellsToDisplay())
                    + ".";
                
                replaceBody(cycle, markupStream, openTag, text);
            }
        });
        
        add(new TableNavigationLink("first", table, 0));
        add(new TableNavigationIncrementLink("prev", table, -1));
        add(new TableNavigationIncrementLink("next", table, 1));
        add(new TableNavigationLink("last", table, table.pageCount() - 1));
    }
}