///////////////////////////////////////////////////////////////////////////////////
//
// Created Jun 13, 2004
//
// Copyright 2004, Jonathan W. Locke
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//

package linkomatic;

import com.voicetribe.wicket.Model;
import com.voicetribe.wicket.Page;
import com.voicetribe.wicket.PageParameters;
import com.voicetribe.wicket.PropertyModel;
import com.voicetribe.wicket.RequestCycle;
import com.voicetribe.wicket.markup.html.HtmlPage;
import com.voicetribe.wicket.markup.html.basic.Label;
import com.voicetribe.wicket.markup.html.link.ExternalPageLink;
import com.voicetribe.wicket.markup.html.link.IPageLink;
import com.voicetribe.wicket.markup.html.link.ImageMap;
import com.voicetribe.wicket.markup.html.link.Link;
import com.voicetribe.wicket.markup.html.link.PageLink;

/**
 * Demonstrates different flavors of hyperlinks.
 * @author Jonathan Locke
 */
public class Home extends HtmlPage
{
    /**
     * Constructor
     * @param parameters Page parameters (ignored since this is the home page)
     */
    public Home(final PageParameters parameters)
    {
        // Action link counts link clicks
        final Link actionLink = new Link("actionLink")
        {
            public void linkClicked(final RequestCycle cycle)
            {
                linkClickCount++;               

                // Redirect back to result to avoid refresh updating the link count
                cycle.setRedirect(true);
            }
        };
        actionLink.add(new Label("linkClickCount",
                new PropertyModel(new Model(this), "linkClickCount")));
        add(actionLink);

        // Link to Page1 is a simple external page link 
        add(new ExternalPageLink("page1Link", Page1.class));
        
        // Link to Page2 is automaticLink, so no code
        
        // Link to Page3 is an external link which takes a parameter
        add(new ExternalPageLink("page3Link", Page3.class).setParameter("id", 3));
        
        // Link to BookDetails page
        add(new PageLink("bookDetailsLink", new IPageLink()
        {
            public Page getPage()
            {
                return new BookDetails(new Book("The Hobbit"));
            }

            public Class getPageClass()
            {
                return BookDetails.class;
            }
        }));
                
        // Delayed link to BookDetails page
        add(new PageLink("bookDetailsLink2", new IPageLink()
        {
            public Page getPage()
            {
                return new BookDetails(new Book("Inside The Matrix"));
            }

            public Class getPageClass()
            {
                return BookDetails.class;
            }        
        }));
        
        // Image map link example
        add(new ImageMap("imageMap")
            .addRectangleLink(0, 0, 100, 100, new ExternalPageLink("page1", Page1.class))
            .addCircleLink(160, 50, 35, new ExternalPageLink("page2", Page2.class))
            .addPolygonLink(new int[] { 212, 79, 241, 4, 279, 54, 212, 79 }, 
                           new ExternalPageLink("page3", Page3.class)));
        
        // Popup example
        add(new ExternalPageLink("popupLink", Page1.class).setPopupDimensions(100, 100));
     }
    
    /**
     * @return Returns the linkClickCount.
     */
    public int getLinkClickCount()
    {
        return linkClickCount;
    }

    /**
     * @param linkClickCount The linkClickCount to set.
     */
    public void setLinkClickCount(final int linkClickCount)
    {
        this.linkClickCount = linkClickCount;
    }
    
    private int linkClickCount = 0;
}

///////////////////////////////// End of File /////////////////////////////////
