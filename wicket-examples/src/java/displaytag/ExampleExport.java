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

import com.voicetribe.util.time.Time;
import com.voicetribe.wicket.Container;
import com.voicetribe.wicket.Page;
import com.voicetribe.wicket.PageParameters;
import com.voicetribe.wicket.RequestCycle;
import com.voicetribe.wicket.markup.html.HtmlPage;
import com.voicetribe.wicket.markup.html.basic.Label;
import com.voicetribe.wicket.markup.html.link.Link;
import com.voicetribe.wicket.markup.html.table.Cell;

import displaytag.export.Export;
import displaytag.export.XmlView;
import displaytag.utils.ListObject;
import displaytag.utils.TableWithAlternatingRowStyle;
import displaytag.utils.TestList;

/**
 * How to support exporting table data
 * 
 * @author Juergen Donnerstag
 */
public class ExampleExport extends HtmlPage
{
    /**
     * Constructor.
     * 
     * @param parameters Page parameters
     */
    public ExampleExport(final PageParameters parameters)
    {
        // Test data
        final List data = new TestList(6, false);
        
        // Add the table
        TableWithAlternatingRowStyle table = new TableWithAlternatingRowStyle("rows", data)
        {
            public boolean populateCell(final Cell cell, final Container tagClass)
            {
                final ListObject value = (ListObject) cell.getModelObject();

                tagClass.add(new Label("id", new Integer(value.getId())));
                tagClass.add(new Label("email", value.getEmail()));
                tagClass.add(new Label("status", value.getStatus()));
                tagClass.add(new Label("date", Time.valueOf(value.getDate()).toDateString()));
                
                return true;
            }
        };
        
        add(table);
        
        // Add the export links
        add(new ExportLink("exportCsv", data));
        add(new ExportLink("exportExcel", data));
        add(new ExportLink("exportXml", data));
    }
    
    /**
     * Define action if Link is selected
     * 
     * @author Juergen Donnerstag
     */
    private class ExportLink extends Link
    {
        final private List data;
        
        public ExportLink(final String componentName, final List data)
	    {
            super(componentName);
            this.data = data;
	    }
        
        public void linkClicked(final RequestCycle cycle)
        {
            new Export().doExport(cycle, new XmlView(data, true, false, false), data);
            
            // rendering completed
            cycle.setPage((Page)null);
        }
    }
}