///////////////////////////////////////////////////////////////////////////////////
//
// Created Jun 25, 2004
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

package library;

import com.voicetribe.wicket.Component;
import com.voicetribe.wicket.Container;
import com.voicetribe.wicket.RequestCycle;
import com.voicetribe.wicket.markup.html.HtmlPage;
import com.voicetribe.wicket.markup.html.border.Border;

/**
 * Ensures that user is authenticated in session.  If no user is signed in, a sign
 * in is forced by redirecting the browser to the SignIn page.  
 * <p>
 * This base class also creates a border for each page subclass, automatically adding 
 * children of the page to the border.  This accomplishes two important things: 
 * (1) subclasses do not have to repeat the code to create the border navigation and 
 * (2) since subclasses do not repeat this code, they are not hardwired to page 
 * navigation structure details
 *  
 * @author Jonathan Locke
 */
public class AuthenticatedHtmlPage extends HtmlPage
{
    /**
     * Constructor
     */
    public AuthenticatedHtmlPage()
    {
        // Create border and add it to the page
        border = new LibraryApplicationBorder("border");
        super.add(border);
    }
    
    /**
     * Adding children to instances of this class causes those children to
     * be added to the border child instead.  
     * @see com.voicetribe.wicket.Container#add(com.voicetribe.wicket.Component)
     */
    public Container add(final Component child)
    {
        // Add children of the page to the page's border component
        border.add(child);
        return this;
    }
    
    /**
     * @return Any authenticated user 
     */
    protected User getUser()
    {
        return Authenticator.forSession(getSession()).getUser();            
    }
    
    /**
     * @see com.voicetribe.wicket.Page#checkAccess(com.voicetribe.wicket.RequestCycle)
     */
    protected boolean checkAccess(RequestCycle cycle)
    {
        // Is user signed in?
        if (Authenticator.forSession(cycle.getSession()).isSignedIn())
        {
            // okay to proceed
            return true;
        }
        else
        {
            // Force sign in
            cycle.redirectToInterceptPage(SignIn.class);
            return false;
        }
    }
    
    private Border border;
}

///////////////////////////////// End of File /////////////////////////////////
