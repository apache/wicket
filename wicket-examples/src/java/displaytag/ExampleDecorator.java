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
import com.voicetribe.wicket.Container;
import com.voicetribe.wicket.PageParameters;
import com.voicetribe.wicket.markup.html.HtmlPage;
import com.voicetribe.wicket.markup.html.basic.Label;
import com.voicetribe.wicket.markup.html.table.Cell;

import displaytag.utils.ListObject;
import displaytag.utils.TableWithAlternatingRowStyle;
import displaytag.utils.TestList;

/**
 * Examples on how to format table data
 * 
 * @author Juergen Donnerstag
 */
public class ExampleDecorator extends HtmlPage
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
            public boolean populateCell(final Cell cell, final Container tagClass)
            {
                final ListObject value = (ListObject) cell.getModelObject();

                tagClass.add(new Label("id", new Integer(value.getId())));
                tagClass.add(new Label("email", value.getEmail()));
                tagClass.add(new Label("status", value.getStatus()));
                tagClass.add(new Label("date", Time.valueOf(value.getDate()).toString("yyyy-MM-dd")));
                
                final DecimalFormat format = new DecimalFormat("$ #,##0.00");
                tagClass.add(new Label("money", format.format(value.getMoney())));
                
                return true;
            }
        });
        
        // Add table 
        add(new TableWithAlternatingRowStyle("rows2", data)
        {
            public boolean populateCell(final Cell cell, final Container tagClass)
            {
                final ListObject value = (ListObject) cell.getModelObject();

                tagClass.add(new Label("id", cell.getModel(), "id"));
                tagClass.add(new Label("email", cell.getModel(), "email"));
                tagClass.add(new Label("status", cell.getModel(), "status"));
                tagClass.add(new Label("date", Time.valueOf(value.getDate()).toString("yyyy-MM-dd HH:mm:ss")));
                
                return true;
            }
        });
     }
}