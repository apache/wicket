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

import com.voicetribe.wicket.PageParameters;
import com.voicetribe.wicket.markup.html.basic.Label;
import com.voicetribe.wicket.markup.html.table.ListItem;
import com.voicetribe.wicket.markup.html.table.PagedTableNavigator;
import com.voicetribe.wicket.markup.html.table.Table;
import com.voicetribe.wicket.markup.html.table.TableNavigation;
import com.voicetribe.wicket.markup.html.table.TableNavigationWithMargin;

import displaytag.utils.ListObject;
import displaytag.utils.PagedTableWithAlternatingRowStyle;
import displaytag.utils.TestList;

/**
 * Table with paging
 * 
 * @author Juergen Donnerstag
 */
public class ExamplePaging extends Displaytag
{
    /**
     * Constructor.
     * 
     * @param parameters Page parameters
     */
    public ExamplePaging(final PageParameters parameters)
    {
        // Test data
        final List data = new TestList(60, false);
        
        // Add pageable table with alternating row styles
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
        add(new PagedTableNavigator("pageTableNav", table));

/*
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
                    + String.valueOf(table.getFirstCell() + table.getWindowSize())
                    + ".";
                
                replaceBody(cycle, markupStream, openTag, text);
            }
        });
        
        add(new TableNavigationLink("first", table, 0));
        add(new TableNavigationIncrementLink("prev", table, -1));
        add(new TableNavigationIncrementLink("next", table, 1));
        add(new TableNavigationLink("last", table, table.pageCount() - 1));
*/        
        
        // Add pageable table with alternating row styles
        List data2 = new ArrayList();
        data2.addAll(data.subList(0, 10));
        final PagedTableWithAlternatingRowStyle table2 = new PagedTableWithAlternatingRowStyle("rows2", data2, 20)
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
        add(table2);
        
        add(new PagedTableNavigator("pageTableNav2", table2));
        
        // Add pageable table with alternating row styles
        final PagedTableWithAlternatingRowStyle table3 = new PagedTableWithAlternatingRowStyle("rows3", data, 10)
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
        add(table3);
        
        PagedTableNavigator nav3 = new PagedTableNavigator("pageTableNav3", table3)
        {
            protected TableNavigation newTableNavigation(final Table table)
            {
                TableNavigationWithMargin nav = new TableNavigationWithMargin("navigation", table);
                nav.setMargin(2);
                nav.setViewSize(5);
                nav.setSeparator(", ");
                return nav;
            }
        };
        
        add(nav3);
        
        // Empty table
        List data4 = new ArrayList();
        final PagedTableWithAlternatingRowStyle table4 = new PagedTableWithAlternatingRowStyle("rows4", data4, 10)
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
        add(table4);
        
        PagedTableNavigator nav4 = new PagedTableNavigator("pageTableNav4", table4)
        {
            protected TableNavigation newTableNavigation(final Table table)
            {
                TableNavigationWithMargin nav = new TableNavigationWithMargin("navigation", table);
                nav.setMargin(2);
                if (nav.getViewSize() > 5)
                {
                    nav.setViewSize(5);
                }
                
                nav.setSeparator(", ");
                return nav;
            }
        };
        
        add(nav4);
    }
}