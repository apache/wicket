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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.voicetribe.wicket.Container;
import com.voicetribe.wicket.PageParameters;
import com.voicetribe.wicket.markup.html.HtmlPage;
import com.voicetribe.wicket.markup.html.basic.Label;
import com.voicetribe.wicket.markup.html.table.Cell;

import displaytag.utils.ListObject;
import displaytag.utils.MyTable;
import displaytag.utils.TestList;

/**
 * Start page for different displaytag pages
 * 
 * @author Juergen Donnerstag
 */
public class ExampleNoColumns extends HtmlPage
{
    /**
     * Constructor.
     * 
     * @param parameters Page parameters
     */
    public ExampleNoColumns(final PageParameters parameters)
    {
        List data = new TestList(10, false);
        
        // Add table of existing comments
        add(new MyTable("entries", data)
        {
            public boolean populateCell(final Cell cell, final Container tagClass)
            {
                final ListObject value = (ListObject) cell.getModelObject();

                Map attrs = new HashMap();
                attrs.put("id", "class");
                tagClass.add(new Label("entry", value.getName()));
                
                return true;
            }
        });
    }

}