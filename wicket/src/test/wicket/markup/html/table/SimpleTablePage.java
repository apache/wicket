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
package wicket.markup.html.table;

import java.util.ArrayList;
import java.util.List;

import wicket.PageParameters;
import wicket.markup.html.HtmlPage;
import wicket.markup.html.basic.Label;
import wicket.markup.html.table.ListItem;
import wicket.markup.html.table.ListView;


/**
 * Dummy page used for resource testing.
 */
public class SimpleTablePage extends HtmlPage {

    /**
     * Construct.
     * @param parameters page parameters.
     */
    public SimpleTablePage(final PageParameters parameters) {
        super();
        List list = new ArrayList();
        list.add("one");
        list.add("two");
        list.add("three");
        add(new ListView("table", list)
        {
	        protected void populateItem(ListItem listItem)
	        {
	            String txt = (String)listItem.getModelObject();
	            listItem.add(new Label("txt", txt));
	        }
        });
    }
}
