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

import linkomatic.Page3;

import com.voicetribe.wicket.Model;
import com.voicetribe.wicket.PageParameters;
import com.voicetribe.wicket.markup.ComponentTagAttributeModifier;
import com.voicetribe.wicket.markup.html.basic.Label;
import com.voicetribe.wicket.markup.html.link.ExternalPageLink;
import com.voicetribe.wicket.markup.html.table.ListItem;
import com.voicetribe.wicket.markup.html.table.ListView;

import displaytag.utils.ListObject;
import displaytag.utils.TestList;

/**
 * Table may not only print value. This example show how to decorate
 * table values.
 *
 * @author Juergen Donnerstag
 */
public class ExampleDecoratorLink extends Displaytag
{
    /**
     * Constructor.
     *
     * @param parameters Page parameters
     */
    public ExampleDecoratorLink(final PageParameters parameters)
    {
        // Test data
        List data = new TestList(10, false);

        // Add the table
        add(new ListView("rows", data)
        {
            public void populateItem(final ListItem listItem)
            {
                final ListObject value = (ListObject) listItem.getModelObject();

                // alternating row styles
                listItem.addAttributeModifier(new ComponentTagAttributeModifier("class",
                                                          new Model(listItem.isEvenIndex() ? "even" : "odd")));
                
                ExternalPageLink idLink = new ExternalPageLink("idLink", Page3.class);
                idLink.setParameter("id", value.getId());
                idLink.add(new Label("id", new Integer(value.getId())));
                listItem.add(idLink);

                ExternalPageLink emailLink = new ExternalPageLink("mailLink", Page3.class);
                emailLink.setParameter("action", "sendamail");
                emailLink.add(new Label("email", value.getEmail()));
                listItem.add(emailLink);

                ExternalPageLink statusLink = new ExternalPageLink("statusLink", Page3.class);
                statusLink.setParameter("id", value.getId());
                statusLink.add(new Label("status", value.getStatus()));
                listItem.add(statusLink);
            }
        });

        // Add table of existing comments
        add(new ListView("rows2", data)
        {
            public void populateItem(final ListItem listItem)
            {
                final ListObject value = (ListObject) listItem.getModelObject();

                listItem.addAttributeModifier(new ComponentTagAttributeModifier("class",
                                                          new Model(listItem.isEvenIndex() ? "even" : "odd")));
                
                ExternalPageLink idLink = new ExternalPageLink("idLink", Page3.class);
                idLink.setParameter("id", value.getId());
                idLink.add(new Label("id", new Integer(value.getId())));
                listItem.add(idLink);

                listItem.add(new Label("email", value.getEmail()));

                listItem.add(
                        new ExternalPageLink("view", Page3.class)
                        	.setParameter("id", value.getId())
                        	.setParameter("action", "view"));

                listItem.add(
                        new ExternalPageLink("edit", Page3.class)
                        	.setParameter("id", value.getId())
                        	.setParameter("action", "edit"));

                listItem.add(
                        new ExternalPageLink("delete", Page3.class)
                        	.setParameter("id", value.getId())
                        	.setParameter("action", "delete"));
            }
        });
    }
}