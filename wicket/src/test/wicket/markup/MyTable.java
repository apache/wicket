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
package wicket.markup;

import java.util.ArrayList;
import java.util.List;

import wicket.Page;
import wicket.markup.html.basic.Label;
import wicket.markup.html.list.ListItem;
import wicket.markup.html.list.ListView;
import wicket.model.Model;

/**
 * Dummy component used for ComponentCreateTagTest
 * 
 * @author Juergen Donnerstag
 */
public class MyTable extends ListView
{
    private int rows = 10;
    
    /**
     * Construct.
     * @param componentName
     */
    public MyTable(final String componentName)
    {
        super(componentName, new Model(null));
    }

    protected void populateItem(ListItem listItem)
    {
        String txt = (String)listItem.getModelObject();
        listItem.add(new Label("txt", txt));
    }

    /**
     * Sets the number of rows per page.
     * @param rows
     */
    public void setRowsPerPage(final int rows)
    {
        this.rows = rows;
        
        List list = new ArrayList();

        for (int i=0; i < rows; i++)
        {
            list.add("row: " + String.valueOf(i));
        }
        
        this.setModelObject(list);
        
        if (this.findParent(Page.class) != null)
        {
            this.invalidateModel();
        }
    }
}
