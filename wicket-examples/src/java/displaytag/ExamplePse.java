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
import com.voicetribe.wicket.markup.html.table.SortableTableHeader;
import com.voicetribe.wicket.markup.html.table.SortableTableHeaders;

import displaytag.export.Export;
import displaytag.export.XmlView;
import displaytag.utils.PagedTableWithAlternatingRowStyle;
import displaytag.utils.ReportList;
import displaytag.utils.ReportableListObject;

/**
 * Pageable + sortable + exportable + grouping table
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
        // Test data
        final ReportList data = new ReportList();
        
        // Add the sortable header and define how to sort the different columns
        add(new SortableTableHeaders("header", data, "rows", true)
        {
	        protected Comparable getObjectToCompare(final SortableTableHeader header, final Object object)
	        {
	            final String name = header.getName();
	            if (name.equals("city"))
	            {
	                return ((ReportableListObject)object).getCity();
	            }
	            if (name.equals("project"))
	            {
	                return ((ReportableListObject)object).getProject();
	            }
	            
	            return "";
	        }
        });

        // Add the table
        final PagedTableWithAlternatingRowStyle table = new PagedTableWithAlternatingRowStyle("rows", data, 10)
        {
            // Groups: value must be equal
            private ReportableListObject previousValue = null;
            
            /**
             * @see displaytag.utils.PagedTableWithAlternatingRowStyle#populateCell(com.voicetribe.wicket.markup.html.table.Cell, com.voicetribe.wicket.Container)
             */
            public boolean populateCell(final Cell cell, final Container tagClass)
            {
                final ReportableListObject value = (ReportableListObject) cell.getModelObject();

                // If first row of table, print anyway
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

                // Not included in grouping
                tagClass.add(new Label("hours", new Double(value.getAmount())));
                tagClass.add(new Label("task", value.getTask()));
                
                // remember the current value for the next row
                previousValue = value;
                return true;
            }
        };

        add(table);

        // Add a table navigator
        add(new PagedTableNavigator("pageTableNav", table));

        // Add export links
        add(new ExportLink("exportCsv", data));
        add(new ExportLink("exportExcel", data));
        add(new ExportLink("exportXml", data));
    }
    
    /**
     * Simple extension to Link

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
            // This is very rudimentary only
            new Export().doExport(cycle, new XmlView(data, true, false, false), data);
            
            // rendering completed
            cycle.setPage((Page)null);
        }
    }
}
