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

import com.voicetribe.wicket.Model;
import com.voicetribe.wicket.PageParameters;
import com.voicetribe.wicket.markup.ComponentTagAttributeModifier;
import com.voicetribe.wicket.markup.html.HtmlContainer;
import com.voicetribe.wicket.markup.html.basic.Label;
import com.voicetribe.wicket.markup.html.link.ExternalPageLink;
import com.voicetribe.wicket.markup.html.table.ListItem;

import displaytag.utils.ListObject;
import displaytag.utils.TableWithAlternatingRowStyle;
import displaytag.utils.TestList;

/**
 * A single table with different styles
 *
 * @author Juergen Donnerstag
 */
public class ExampleStyles extends Displaytag
{
    /**
     * Constructor.
     *
     * @param parameters Page parameters
     */
    public ExampleStyles(final PageParameters parameters)
    {
        // my model object
        List data = new TestList(10, false);

        // Add Links to handle the different styles
        add(new ExternalPageLink("isis", this.getClass()).setParameter("class", "isis").setAutoEnable(false));
        add(new ExternalPageLink("its", this.getClass()).setParameter("class", "its").setAutoEnable(false));
        add(new ExternalPageLink("mars", this.getClass()).setParameter("class", "mars").setAutoEnable(false));
        add(new ExternalPageLink("simple", this.getClass()).setParameter("class", "simple").setAutoEnable(false));
        add(new ExternalPageLink("report", this.getClass()).setParameter("class", "report").setAutoEnable(false));
        add(new ExternalPageLink("mark", this.getClass()).setParameter("class", "mark").setAutoEnable(false));

        // Apply style on current tag (based on URL parameter)
        HtmlContainer htmlTable = new HtmlContainer("htmlTable");
        htmlTable.addAttributeModifier(
                new ComponentTagAttributeModifier(
                        "class", 
                        new Model(parameters.getString("class"))));
        
        add(htmlTable);
        
        // Add table 
        htmlTable.add(new TableWithAlternatingRowStyle("rows", data)
        {
            public void populateItem(final ListItem listItem)
            {
                final ListObject value = (ListObject) listItem.getModelObject();

                listItem.add(new Label("id", new Integer(value.getId())));
                listItem.add(new Label("name", value.getName()));
                listItem.add(new Label("email", value.getEmail()));
                listItem.add(new Label("status", value.getStatus()));
                listItem.add(new Label("comments", value.getDescription()));
            }
        });
    }
}