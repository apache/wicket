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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import wicket.PageParameters;
import wicket.examples.displaytag.utils.ReportList;
import wicket.examples.displaytag.utils.ReportableListObject;
import wicket.examples.displaytag.utils.TableWithAlternatingRowStyle;
import wicket.markup.html.basic.Label;
import wicket.markup.html.list.ListItem;
import wicket.markup.html.list.ListView;



/**
 * Table with subtotals calculated and printed into the table on the fly
 * 
 * @author Juergen Donnerstag
 */
public class ExampleSubtotals extends Displaytag
{
    /**
     * Constructor.
     * 
     * @param parameters Page parameters
     */
    public ExampleSubtotals(final PageParameters parameters)
    {
        // Test data
        final ReportList data = new ReportList();
        final Map groups = new LinkedHashMap(); // Keep the insertion order
        
        ReportableListObject previousValue = (ReportableListObject) data.get(0);
        groups.put(previousValue.getCity(), new Integer(0));
        int startIdx = 0;
        for (int i=1; i < data.size(); i++)
        {
            final ReportableListObject value = (ReportableListObject) data.get(i);

            boolean equal = value.getCity().equals(previousValue.getCity());
            if (!equal)
            {
                groups.put(previousValue.getCity(), new Integer(i - startIdx));
                groups.put(value.getCity(), new Integer(0));
                previousValue = value;
            }
        }

        groups.put(previousValue.getCity(), new Integer(data.size() - startIdx));
        
        // add the table
        List groupList = new ArrayList();
        groupList.addAll(groups.keySet());
        add(new ListView("border", groupList)
        {
            private int startIndex = 0;
            
            public void populateItem(final ListItem listItem)
            {
                SubtotalTable subtable = new SubtotalTable("rows", data);
                subtable.setStartIndex(startIndex);
                String group = listItem.getModelObjectAsString();
                int size = ((Integer)groups.get(group)).intValue();
                subtable.setViewSize(size);
                startIndex = size;
                
                listItem.add(subtable);
                listItem.add(new Label("name", subtable, "group1"));
                listItem.add(new Label("value", subtable, "subtotal"));
            }
        });
    }

    /**
     * A subtotal + grouping table prints the tables rows and adds a bar 
     * and the subtotal at the bottom. 
     * 
     * @author Juergen Donnerstag
     */
    private class SubtotalTable extends TableWithAlternatingRowStyle
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
        
        public void populateItem(final ListItem listItem)
        {
            final ReportableListObject value = (ReportableListObject) listItem.getModelObject();

            if (previousValue == null)
            {
                city = value.getCity();
                listItem.add(new Label("city", value.getCity()));
                listItem.add(new Label("project", value.getProject()));
            } 
            else
            {
                listItem.add(new Label("city", ""));
                
                boolean equal = value.getProject().equals(previousValue.getProject());
                listItem.add(new Label("project", equal ? "" : value.getProject()));
            }

            listItem.add(new Label("hours", new Double(value.getAmount())));
            listItem.add(new Label("task", value.getTask()));
            
            subtotal += value.getAmount();
            previousValue = value;
        }
    }
}
