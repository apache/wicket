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

import wicket.Model;
import wicket.PageParameters;
import wicket.examples.displaytag.utils.ListObject;
import wicket.examples.displaytag.utils.TableWithAlternatingRowStyle;
import wicket.examples.displaytag.utils.TestList;
import wicket.markup.ComponentTagAttributeModifier;
import wicket.markup.html.HtmlContainer;
import wicket.markup.html.basic.Label;
import wicket.markup.html.link.ExternalPageLink;
import wicket.markup.html.table.ListItem;



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