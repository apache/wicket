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

import com.voicetribe.wicket.Container;
import com.voicetribe.wicket.PageParameters;
import com.voicetribe.wicket.markup.html.HtmlPage;
import com.voicetribe.wicket.markup.html.basic.Label;
import com.voicetribe.wicket.markup.html.table.Cell;

import displaytag.utils.TableWithAlternatingRowStyle;
import displaytag.utils.ReportList;
import displaytag.utils.ReportableListObject;

/**
 * A table supporting grouping
 * 
 * @author Juergen Donnerstag
 */
public class ExampleGrouping extends HtmlPage
{
    /**
     * Constructor.
     * 
     * @param parameters Page parameters
     */
    public ExampleGrouping(final PageParameters parameters)
    {
        // Test data
        ReportList data = new ReportList();

        // Add table of existing comments
        add(new TableWithAlternatingRowStyle("rows", data)
        {
            // Remember the value from the previous row
            private ReportableListObject previousValue = null;
            
            public boolean populateCell(final Cell cell, final Container tagClass)
            {
                final ReportableListObject value = (ReportableListObject) cell.getModelObject();

                // If first row, print anyway
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

                // These values are not grouped
                tagClass.add(new Label("hours", new Double(value.getAmount())));
                tagClass.add(new Label("task", value.getTask()));
                
                // Remember current value
                previousValue = value;
                return true;
            }
        });
    }
}
