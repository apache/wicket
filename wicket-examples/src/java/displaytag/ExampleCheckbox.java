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

import com.voicetribe.wicket.PageParameters;
import com.voicetribe.wicket.markup.html.basic.Label;
import com.voicetribe.wicket.markup.html.form.CheckBox;
import com.voicetribe.wicket.markup.html.form.TextField;
import com.voicetribe.wicket.markup.html.table.ListItem;

import displaytag.utils.ListObject;
import displaytag.utils.TableWithAlternatingRowStyle;
import displaytag.utils.TestList;

/**
 * A table with checkboxes and input fields. I'm not sure displaytag can do!
 * 
 * @author Juergen Donnerstag
 */
public class ExampleCheckbox extends Displaytag
{
    /**
     * Constructor.
     * 
     * @param parameters Page parameters
     */
    public ExampleCheckbox(final PageParameters parameters)
    {
        // test data
        List data = new TestList(6, false);
        
        // Add table of existing comments
        add(new TableWithAlternatingRowStyle("rows", data)
        {
            public void populateItem(final ListItem listItem)
            {
                final ListObject value = (ListObject) listItem.getModelObject();

                listItem.add(new Label("id", new Integer(value.getId())));
                listItem.add(new Label("name", value.getName()));
                listItem.add(new CheckBox("activ", new Boolean(value.isActiv())));
                listItem.add(new TextField("comment", value.getStatus()));
            }
        });
    }
}