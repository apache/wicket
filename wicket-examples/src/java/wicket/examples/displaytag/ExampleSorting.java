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

import java.util.List;

import wicket.PageParameters;
import wicket.examples.displaytag.utils.ListObject;
import wicket.examples.displaytag.utils.TableWithAlternatingRowStyle;
import wicket.examples.displaytag.utils.TestList;
import wicket.markup.html.basic.Label;
import wicket.markup.html.list.ListItem;
import wicket.markup.html.list.ListView;
import wicket.markup.html.list.SortableListViewHeader;
import wicket.markup.html.list.SortableListViewHeaders;



/**
 * A sorted table example
 * 
 * @author Juergen Donnerstag
 */
public class ExampleSorting extends Displaytag
{
    /**
     * Constructor.
     * 
     * @param parameters Page parameters
     */
    public ExampleSorting(final PageParameters parameters)
    {
        // Test data
        final List data = new TestList(6, false);
        
/* This is without magic  
        SortableTableHeaderGroup headerGroup = 
            new SortableTableHeaderGroup(this, data, "rows");
        
        add(new SimpleHrefComponent("id", headerGroup)
        {
            protected int compareTo(Object o1, Object o2)
            {
                return ((ListObject)o1).getId() - ((ListObject)o2).getId();
            }
        });
        
        add(new SimpleHrefComponent("name", headerGroup)
        {
            protected int compareTo(Object o1, Object o2)
            {
                return ((ListObject)o1).getName().compareTo(((ListObject)o2).getName());
            }
        });

        add(new SimpleHrefComponent("email", headerGroup)
        {
            protected int compareTo(Object o1, Object o2)
            {
                return ((ListObject)o1).getEmail().compareTo(((ListObject)o2).getEmail());
            }
        });
        
        add(new SimpleHrefComponent("status", headerGroup)
        {
            protected int compareTo(Object o1, Object o2)
            {
                return ((ListObject)o1).getStatus().compareTo(((ListObject)o2).getStatus());
            }
        });
        
        add(new SimpleHrefComponent("comment", headerGroup)
        {
            protected int compareTo(Object o1, Object o2)
            {
                return ((ListObject)o1).getDescription().compareTo(((ListObject)o2).getDescription());
            }
        });
*/        
        // Add table 
        ListView table = new TableWithAlternatingRowStyle("rows", data)
        {
            public void populateItem(final ListItem listItem)
            {
                final ListObject value = (ListObject) listItem.getModelObject();

                listItem.add(new Label("id", new Integer(value.getId())));
                listItem.add(new Label("name", value.getName()));
                listItem.add(new Label("email", value.getEmail()));
                listItem.add(new Label("status", value.getStatus()));
                listItem.add(new Label("comment", value.getDescription()));
            }
        };

        add(table);
        
        // And this is with a little bit of magic
        add(new SortableListViewHeaders("header", table)
        {
            /*
             * If object does not support equals()
             */
	        protected int compareTo(SortableListViewHeader header, Object o1, Object o2)
	        {
	            if (header.getName().equals("id"))
	            {
	                return ((ListObject)o1).getId() - ((ListObject)o2).getId();
	            }
	            
	            return super.compareTo(header, o1, o2);
	        }

	        /**
	         * Define how to do sorting
	         * 
	         * @see wicket.markup.html.table.SortableTableHeaders#getObjectToCompare(wicket.markup.html.table.SortableTableHeader, java.lang.Object)
	         */
	        protected Comparable getObjectToCompare(final SortableListViewHeader header, final Object object)
	        {
	            final String name = header.getName();
	            if (name.equals("name"))
	            {
	                return ((ListObject)object).getName();
	            }
	            if (name.equals("email"))
	            {
	                return ((ListObject)object).getEmail();
	            }
	            if (name.equals("status"))
	            {
	                return ((ListObject)object).getStatus();
	            }
	            if (name.equals("comment"))
	            {
	                return ((ListObject)object).getDescription();
	            }
	            
	            return "";
	        }
        });
        
    }
}