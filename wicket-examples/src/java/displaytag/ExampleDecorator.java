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

import java.text.DecimalFormat;
import java.util.List;

import com.voicetribe.util.time.Time;
import com.voicetribe.wicket.PageParameters;
import com.voicetribe.wicket.markup.html.basic.Label;
import com.voicetribe.wicket.markup.html.table.ListItem;

import displaytag.utils.ListObject;
import displaytag.utils.TableWithAlternatingRowStyle;
import displaytag.utils.TestList;

/**
 * Examples on how to format table data
 * 
 * @author Juergen Donnerstag
 */
public class ExampleDecorator extends Displaytag
{
    /**
     * Constructor.
     * 
     * @param parameters Page parameters
     */
    public ExampleDecorator(final PageParameters parameters)
    {
        // Test data
        List data = new TestList(10, false);
        
        // Add table 
        add(new TableWithAlternatingRowStyle("rows", data)
        {
            public void populateItem(final ListItem listItem)
            {
                final ListObject value = (ListObject) listItem.getModelObject();

                listItem.add(new Label("id", new Integer(value.getId())));
                listItem.add(new Label("email", value.getEmail()));
                listItem.add(new Label("status", value.getStatus()));
                listItem.add(new Label("date", Time.valueOf(value.getDate()).toString("yyyy-MM-dd")));
                
                final DecimalFormat format = new DecimalFormat("$ #,##0.00");
                listItem.add(new Label("money", format.format(value.getMoney())));
            }
        });
        
        // Add table 
        add(new TableWithAlternatingRowStyle("rows2", data)
        {
            public void populateItem(final ListItem listItem)
            {
                final ListObject value = (ListObject) listItem.getModelObject();

                listItem.add(new Label("id", listItem.getModel(), "id"));
                listItem.add(new Label("email", listItem.getModel(), "email"));
                listItem.add(new Label("status", listItem.getModel(), "status"));
                listItem.add(new Label("date", Time.valueOf(value.getDate()).toString("yyyy-MM-dd HH:mm:ss")));
            }
        });
     }
}