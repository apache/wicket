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
import com.voicetribe.wicket.Model;
import com.voicetribe.wicket.PageParameters;
import com.voicetribe.wicket.RequestCycle;
import com.voicetribe.wicket.markup.html.HtmlPage;
import com.voicetribe.wicket.markup.html.basic.Label;
import com.voicetribe.wicket.markup.html.table.Cell;
import com.voicetribe.wicket.markup.html.table.Table;

import displaytag.utils.MyTable;
import displaytag.utils.ReportList;
import displaytag.utils.ReportableListObject;

/**
 * Start page for different displaytag pages
 * 
 * @author Juergen Donnerstag
 */
public class ExampleCallbacks extends HtmlPage
{
    /**
     * Constructor.
     * 
     * @param parameters Page parameters
     */
    public ExampleCallbacks(final PageParameters parameters)
    {
        final ReportList data = new ReportList();

        add(new Table("border", new Model(data))
        {
            private int firstCell = 0;
            private SubtotalTable subtable = null;
            
            public boolean populateCell(final Cell cell)
            {
                subtable = new SubtotalTable("rows", data);
                subtable.setStartIndex(firstCell);
                
                cell.add(subtable);
                cell.add(new Label("name", subtable, "group1"));
                cell.add(new Label("value", subtable, "subtotal"));
                
                return true;
            }

            protected boolean renderCell(final Cell cell, final RequestCycle cycle, final boolean lastPage)
            {
                super.renderCell(cell, cycle, lastPage);
                firstCell = subtable.getLastIndex();
                return (firstCell >= (subtable.getNumberOfCellsToDisplay() - 1) ? false : true);
            }
        });
    }

    private class SubtotalTable extends MyTable
    {
        private ReportableListObject previousValue = null;
        private double subtotal = 0;
        private String city;
        
        public SubtotalTable(final String componentName, final List data)
        {
            super(componentName, data);
        }

        public double getSubtotal()
        {
            return subtotal;
        }

        public String getGroup1()
        {
            return city;
        }
        
        public boolean populateCell(final Cell cell, final Container tagClass)
        {
            final ReportableListObject value = (ReportableListObject) cell.getModelObject();

            boolean equal = false;
            if (previousValue != null)
            {
                equal = value.getCity().equals(previousValue.getCity());
                if (!equal)
                {
                    return false;
                }
            }

            if (previousValue == null)
            {
                city = value.getCity();
                tagClass.add(new Label("city", value.getCity()));
                tagClass.add(new Label("project", value.getProject()));
            } 
            else
            {
                tagClass.add(new Label("city", equal ? "" : value.getCity()));
                
                equal &= value.getProject().equals(previousValue.getProject());
                tagClass.add(new Label("project", equal ? "" : value.getProject()));
            }

            tagClass.add(new Label("hours", new Double(value.getAmount())));
            tagClass.add(new Label("task", value.getTask()));
            
            subtotal += value.getAmount();
            previousValue = value;
            
            return true;
        }
    }
}
