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

import java.text.DecimalFormat;
import java.util.List;

import wicket.PageParameters;
import wicket.examples.displaytag.utils.ListObject;
import wicket.examples.displaytag.utils.TableWithAlternatingRowStyle;
import wicket.examples.displaytag.utils.TestList;
import wicket.markup.html.basic.Label;
import wicket.markup.html.list.ListItem;
import wicket.model.PropertyModel;
import wicket.util.time.Time;

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

                listItem.add(new Label("id", Integer.toString(value.getId())));
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

                listItem.add(new Label("id", new PropertyModel(listItem.getModel(), "id")));
                listItem.add(new Label("email", new PropertyModel(listItem.getModel(), "email")));
                listItem.add(new Label("status", new PropertyModel(listItem.getModel(), "status")));
                listItem.add(new Label("date", Time.valueOf(value.getDate()).toString("yyyy-MM-dd HH:mm:ss")));
            }
        });
     }
}