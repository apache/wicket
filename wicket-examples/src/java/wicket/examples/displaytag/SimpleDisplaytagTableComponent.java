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

import java.util.List;

import wicket.examples.displaytag.utils.ListObject;
import wicket.examples.displaytag.utils.TableWithAlternatingRowStyle;
import wicket.markup.html.basic.Label;
import wicket.markup.html.list.ListItem;
import wicket.markup.html.list.ListView;
import wicket.markup.html.panel.Panel;

/**
 * Display a simple table
 * 
 * @author Juergen Donnerstag
 */
public class SimpleDisplaytagTableComponent extends Panel
{
    private ListView table;
    
    /**
     * Constructor.
     * 
     * @param id Name of component
     * @param list List of data to display
     */
    public SimpleDisplaytagTableComponent(final String id, final List list)
    {
        super(id);
        
        // Add table with alternating row styles
        table = new TableWithAlternatingRowStyle("rows", list)
        {
            public void populateItem(final ListItem listItem)
            {
                final ListObject value = (ListObject) listItem.getModelObject();

                listItem.add(new Label("id", Integer.toString(value.getId())));
                listItem.add(new Label("email", value.getEmail()));
                listItem.add(new Label("status", value.getStatus()));
            }
        };
        
        add(table);
    }

    /**
     * @return The starting index for the table
     */
    public int getStartIndex()
    {
        return table.getStartIndex();
    }
    
    /**
     * @param startIndex The new start index
     * @return The listview
     */
    public ListView setStartIndex(int startIndex)
    {
        return table.setStartIndex(startIndex);
    }
    
    /**
     * @param size New view size
     * @return The list view
     */
    public ListView setViewSize(int size)
    {
        return table.setViewSize(size);
    }
}
