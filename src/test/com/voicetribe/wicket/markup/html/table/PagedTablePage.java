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

import java.util.ArrayList;
import java.util.List;

import com.voicetribe.wicket.PageParameters;
import com.voicetribe.wicket.markup.html.HtmlPage;
import com.voicetribe.wicket.markup.html.basic.Label;

/**
 * Dummy page used for resource testing.
 */
public class PagedTablePage extends HtmlPage {

    /**
     * Construct.
     * @param parameters page parameters.
     */
    public PagedTablePage(final PageParameters parameters) {
        super();
        List list = new ArrayList();
        list.add("one");
        list.add("two");
        list.add("three");
        SimplePagedTable simplePagedTable = new SimplePagedTable("table", list);
        add(simplePagedTable);
        TableNavigation navigation = new TableNavigation("navigation", simplePagedTable);
        add(navigation);
    }

    /** simple table. */
    class SimplePagedTable extends Table
    {

        /**
         * Construct.
         * @param name
         * @param object
         */
        public SimplePagedTable(String name, List object)
        {
            super(name, object, 2);
        }

        /**
         * @see com.voicetribe.wicket.markup.html.table.Table#populateCell(com.voicetribe.wicket.markup.html.table.Cell)
         */
        protected void populateCell(Cell cell)
        {
            String txt = (String)cell.getModelObject();
            cell.add(new Label("txt", txt));
        }
        
    }
}
