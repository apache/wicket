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
import com.voicetribe.wicket.Page;
import com.voicetribe.wicket.PageParameters;
import com.voicetribe.wicket.RequestCycle;
import com.voicetribe.wicket.markup.html.HtmlPage;
import com.voicetribe.wicket.markup.html.basic.Label;
import com.voicetribe.wicket.markup.html.link.Link;
import com.voicetribe.wicket.markup.html.table.Cell;
import com.voicetribe.wicket.markup.html.table.PagedTableNavigator;

import displaytag.export.Export;
import displaytag.export.XmlView;
import displaytag.utils.MyPagedTable;
import displaytag.utils.ReportList;
import displaytag.utils.ReportableListObject;

/**
 * Start page for different displaytag pages
 * 
 * @author Juergen Donnerstag
 */
public class ExamplePse extends HtmlPage
{
    /**
     * Constructor.
     * 
     * @param parameters Page parameters
     */
    public ExamplePse(final PageParameters parameters)
    {
        final ReportList data = new ReportList();

        // Add table of existing comments
        final MyPagedTable table = new MyPagedTable("rows", data)
        {
            private ReportableListObject previousValue = null;
            public boolean populateCell(final Cell cell, final Container tagClass)
            {
                final ReportableListObject value = (ReportableListObject) cell.getModelObject();

                if (previousValue == null)
                {
	                tagClass.add(new Label("city", value.getCity()));
	                tagClass.add(new Label("project", value.getProject()));
                } 
                else
                {
	                boolean equal = value.getCity().equals(previousValue.getCity());
	                tagClass.add(new Label("city", equal ? "" : value.getCity()));
	                
	                equal &= value.getProject().equals(previousValue.getProject());
	                tagClass.add(new Label("project", equal ? "" : value.getProject()));
                }

                tagClass.add(new Label("hours", new Double(value.getAmount())));
                tagClass.add(new Label("task", value.getTask()));
                
                previousValue = value;
                return true;
            }
        };
        
        table.setNumberOfCellsToDisplay(10);
        add(table);

        add(new PagedTableNavigator("pageTableNav", table));

        // Add export links
        add(new ExportLink("exportCsv", data));
        add(new ExportLink("exportExcel", data));
        add(new ExportLink("exportXml", data));
    }
    
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
            // This is very rudimentary only
            new Export().doExport(cycle, new XmlView(data, true, false, false), data);
            
            // rendering completed
            cycle.setPage((Page)null);
        }
    }
}
