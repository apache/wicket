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
package com.voicetribe.wicket.markup.html.table;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.voicetribe.wicket.PageParameters;
import com.voicetribe.wicket.markup.html.HtmlPage;
import com.voicetribe.wicket.markup.html.basic.Label;

/**
 * Dummy page used for resource testing.
 */
public class SortableTableHeadersPage extends HtmlPage {

    /**
     * Construct.
     * @param parameters page parameters.
     */
    public SortableTableHeadersPage(final PageParameters parameters) {
        super();
        
        List list = new ArrayList();
        addUser(list, 1, "Name-aaa", "mail-1");
        addUser(list, 2, "Name-bbb", "mail-2");
        addUser(list, 3, "Name-ccc", "mail-3");
        addUser(list, 4, "Name-ddd", "mail-4");
        addUser(list, 5, "Name-eee", "mail-5");
        addUser(list, 6, "Name-aba", "mail-6");
        addUser(list, 7, "Name-bab", "mail-7");
        addUser(list, 8, "Name-dca", "mail-8");
        addUser(list, 9, "Name-eaa", "mail-9");
        
        ListView table = new ListView("table", list)
        {
            protected void populateItem(ListItem listItem)
            {
                User user = (User)listItem.getModelObject();
                listItem.add(new Label("id", user, "id"));
                listItem.add(new Label("name", user, "name"));
                listItem.add(new Label("email", user, "email"));
            }
        };

        add(table);
        add(new SortableTableHeaders("header", table, true)
        {
            /*
             * If object does not support equals()
             */
	        protected int compareTo(SortableTableHeader header, Object o1, Object o2)
	        {
	            if (header.getName().equals("id"))
	            {
	                return ((User)o1).id - ((User)o2).id;
	            }
	            
	            return super.compareTo(header, o1, o2);
	        }

	        /**
	         * Define how to do sorting
	         * 
	         * @see com.voicetribe.wicket.markup.html.table.SortableTableHeaders#getObjectToCompare(com.voicetribe.wicket.markup.html.table.SortableTableHeader, java.lang.Object)
	         */
	        protected Comparable getObjectToCompare(final SortableTableHeader header, final Object object)
	        {
	            final String name = header.getName();
	            if (name.equals("name"))
	            {
	                return ((User)object).name;
	            }
	            if (name.equals("email"))
	            {
	                return ((User)object).email;
	            }
	            
	            return "";
	        }
        });
                
    }
    
    private void addUser(List data, int id, String name, String email)
    {
        User user = new User();
        user.id = id;
        user.name = name;
        user.email = email;
        data.add(user);
    }
    
    private class User implements Serializable
    {
        public int id;
        public String name;
        public String email;
    }
}
