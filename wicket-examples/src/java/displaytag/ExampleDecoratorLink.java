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

import com.voicetribe.wicket.PageParameters;
import com.voicetribe.wicket.markup.html.HtmlPage;
import com.voicetribe.wicket.markup.html.basic.Label;
import com.voicetribe.wicket.markup.html.link.ExternalPageLink;
import com.voicetribe.wicket.markup.html.style.CascadingStyleSheetStyle;
import com.voicetribe.wicket.markup.html.table.Cell;
import com.voicetribe.wicket.markup.html.table.Table;

import displaytag.utils.ListObject;
import displaytag.utils.TestList;

/**
 * Start page for different displaytag pages
 * 
 * @author Juergen Donnerstag
 */
public class ExampleDecoratorLink extends HtmlPage
{
    /**
     * Constructor.
     * 
     * @param parameters Page parameters
     */
    public ExampleDecoratorLink(final PageParameters parameters)
    {
        List data = new TestList(10, false);
        
        // Add table of existing comments
        add(new Table("rows", data)
        {
            public void populateCell(final Cell cell)
            {
                final ListObject value = (ListObject) cell.getModelObject();
            
                final CascadingStyleSheetStyle tagClass = new CascadingStyleSheetStyle("class", cell.isEvenIndex() ? "even" : "odd");
                tagClass.setEnable(true);
                cell.add(tagClass);

                ExternalPageLink idLink = new ExternalPageLink("idLink", Page3.class);
                idLink.setParameter("id", value.getId());
                idLink.add(new Label("id", new Integer(value.getId())));
                tagClass.add(idLink);
                
                ExternalPageLink emailLink = new ExternalPageLink("mailLink", Page3.class);
                emailLink.setParameter("action", "sendamail");
                emailLink.add(new Label("email", value.getEmail()));
                tagClass.add(emailLink);
        
                ExternalPageLink statusLink = new ExternalPageLink("statusLink", Page3.class);
                statusLink.setParameter("id", value.getId());
                statusLink.add(new Label("status", value.getStatus()));
                tagClass.add(statusLink);
            }
        });
        
        // Add table of existing comments
        add(new Table("rows2", data)
        {
            public void populateCell(final Cell cell)
            {
                final ListObject value = (ListObject) cell.getModelObject();
            
                final CascadingStyleSheetStyle tagClass = new CascadingStyleSheetStyle("class", cell.isEvenIndex() ? "even" : "odd");
                tagClass.setEnable(true);
                cell.add(tagClass);

                ExternalPageLink idLink = new ExternalPageLink("idLink", Page3.class);
                idLink.setParameter("id", value.getId());
                idLink.add(new Label("id", new Integer(value.getId())));
                tagClass.add(idLink);
                
                tagClass.add(new Label("email", value.getEmail()));
        
                tagClass.add(
                        new ExternalPageLink("view", Page3.class)
                        	.setParameter("id", value.getId())
                        	.setParameter("action", "view"));
                
                tagClass.add(
                        new ExternalPageLink("edit", Page3.class)
                        	.setParameter("id", value.getId())
                        	.setParameter("action", "edit"));
                
                tagClass.add(
                        new ExternalPageLink("delete", Page3.class)
                        	.setParameter("id", value.getId())
                        	.setParameter("action", "delete"));
            }
        });
    }
}