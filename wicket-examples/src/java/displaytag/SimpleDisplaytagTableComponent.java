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
import com.voicetribe.wicket.markup.html.basic.Label;
import com.voicetribe.wicket.markup.html.panel.Panel;
import com.voicetribe.wicket.markup.html.table.Cell;

import displaytag.utils.ListObject;
import displaytag.utils.MyTable;

/**
 * Start page for different displaytag pages
 * 
 * @author Juergen Donnerstag
 */
public class SimpleDisplaytagTableComponent extends Panel
{
    final private List data;
    final private MyTable table;
    
    /**
     * Constructor.
     * 
     * @param parameters Page parameters
     */
    public SimpleDisplaytagTableComponent(final String componentName, final List data)
    {
        super(componentName);
        
        this.data = data;
        
        // Add table of existing comments
        table = new MyTable("rows", data)
        {
            public boolean populateCell(final Cell cell, final Container tagClass)
            {
                final ListObject value = (ListObject) cell.getModelObject();

                tagClass.add(new Label("id", new Integer(value.getId())));
                tagClass.add(new Label("email", value.getEmail()));
                tagClass.add(new Label("status", value.getStatus()));
                
                return true;
            }
        };
        
        add(table);
    }
    
    public void setNumberOfCellsToDisplay(int size)
    {
        table.setNumberOfCellsToDisplay(size);
    }
    
    public void setStartIndex(int startIndex)
    {
        table.setStartIndex(startIndex);
    }
}