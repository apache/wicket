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

import com.voicetribe.wicket.markup.html.basic.Label;
import com.voicetribe.wicket.markup.html.panel.Panel;
import com.voicetribe.wicket.markup.html.table.ListItem;
import com.voicetribe.wicket.markup.html.table.ListView;

import displaytag.utils.ListObject;
import displaytag.utils.TableWithAlternatingRowStyle;

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
     * @param parameters Page parameters
     */
    public SimpleDisplaytagTableComponent(final String componentName, final List data)
    {
        super(componentName);
        
        // Add table with alternating row styles
        table = new TableWithAlternatingRowStyle("rows", data)
        {
            public void populateItem(final ListItem listItem)
            {
                final ListObject value = (ListObject) listItem.getModelObject();

                listItem.add(new Label("id", new Integer(value.getId())));
                listItem.add(new Label("email", value.getEmail()));
                listItem.add(new Label("status", value.getStatus()));
            }
        };
        
        add(table);
    }

    public int getStartIndex()
    {
        return table.getStartIndex();
    }
    
    public ListView setStartIndex(int startIndex)
    {
        return table.setStartIndex(startIndex);
    }
    
    public ListView setViewSize(int size)
    {
        return table.setViewSize(size);
    }
}