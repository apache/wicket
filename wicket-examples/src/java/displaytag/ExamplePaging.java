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

import java.util.List;

import com.voicetribe.wicket.Container;
import com.voicetribe.wicket.PageParameters;
import com.voicetribe.wicket.markup.html.HtmlPage;
import com.voicetribe.wicket.markup.html.basic.Label;
import com.voicetribe.wicket.markup.html.table.Cell;
import com.voicetribe.wicket.markup.html.table.PagedTableNavigator;

import displaytag.utils.ListObject;
import displaytag.utils.MyPagedTable;
import displaytag.utils.TestList;

/**
 * Start page for different displaytag pages
 * 
 * @author Juergen Donnerstag
 */
public class ExamplePaging extends HtmlPage
{
    /**
     * Constructor.
     * 
     * @param parameters Page parameters
     */
    public ExamplePaging(final PageParameters parameters)
    {
        final List data = new TestList(60, false);
        
        // Add table of existing comments
        final MyPagedTable table = new MyPagedTable("rows", data, 10)
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
    }
}