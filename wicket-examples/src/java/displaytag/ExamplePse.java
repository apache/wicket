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

import com.voicetribe.wicket.PageParameters;
import com.voicetribe.wicket.markup.html.basic.Label;
import com.voicetribe.wicket.markup.html.table.ListItem;
import com.voicetribe.wicket.markup.html.table.PagedTableNavigator;
import com.voicetribe.wicket.markup.html.table.SortableTableHeader;
import com.voicetribe.wicket.markup.html.table.SortableTableHeaders;

import displaytag.export.CsvView;
import displaytag.export.ExcelView;
import displaytag.export.ExportLink;
import displaytag.export.XmlView;
import displaytag.utils.PagedTableWithAlternatingRowStyle;
import displaytag.utils.ReportList;
import displaytag.utils.ReportableListObject;

/**
 * Pageable + sortable + exportable + grouping table
 * 
 * @author Juergen Donnerstag
 */
public class ExamplePse extends Displaytag
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
            public void populateItem(final ListItem listItem)
            {
                super.populateItem(listItem);
                
                final ReportableListObject value = (ReportableListObject) listItem.getModelObject();

                // If first row of table, print anyway
                if (previousValue == null)
                {
                    listItem.add(new Label("city", value.getCity()));
                    listItem.add(new Label("project", value.getProject()));
                } 
                else
                {
	                boolean equal = value.getCity().equals(previousValue.getCity());
	                listItem.add(new Label("city", equal ? "" : value.getCity()));
	                
	                equal &= value.getProject().equals(previousValue.getProject());
	                listItem.add(new Label("project", equal ? "" : value.getProject()));
                }

                // Not included in grouping
                listItem.add(new Label("hours", new Double(value.getAmount())));
                listItem.add(new Label("task", value.getTask()));
                
                // remember the current value for the next row
                previousValue = value;
            }
        };

        add(table);

        // Add a table navigator
        add(new PagedTableNavigator("pageTableNav", table));

        // Add export links
        add(new ExportLink("exportCsv", data, new CsvView(data, true, false, false)));
        add(new ExportLink("exportExcel", data, new ExcelView(data, true, false, false)));
        add(new ExportLink("exportXml", data, new XmlView(data, true, false, false)));
    }
}
