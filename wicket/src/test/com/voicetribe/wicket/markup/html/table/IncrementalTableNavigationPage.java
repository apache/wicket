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
public class IncrementalTableNavigationPage extends HtmlPage {

    /**
     * Construct.
     * @param parameters page parameters.
     */
    public IncrementalTableNavigationPage(final PageParameters parameters) {
        super();
        List list = new ArrayList();
        list.add("one");
        list.add("two");
        list.add("three");
        list.add("four");
        list.add("five");
        list.add("six");
        list.add("seven");
        list.add("eight");
        
        Table table = new Table("table", list, 2)
        {
            protected void populateItem(ListItem listItem)
            {
                String txt = (String)listItem.getModelObject();
                listItem.add(new Label("txt", txt));
            }
        };

        add(table);
        TableNavigationIncrementLink prev = new TableNavigationIncrementLink("prev", table, -1);
        add(prev);
        TableNavigationIncrementLink nextNext = new TableNavigationIncrementLink("nextNext", table, +2);
        add(nextNext);
    }
}
