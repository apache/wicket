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
package wicket.examples.library;

import wicket.Component;
import wicket.MarkupContainer;
import wicket.examples.WicketExamplePage;
import wicket.markup.html.border.Border;

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
public class AuthenticatedWebPage extends WicketExamplePage
{
    private Border border;

    /**
     * Adding children to instances of this class causes those children to
     * be added to the border child instead.
     * @see wicket.MarkupContainer#add(wicket.Component)
     */
    public MarkupContainer add(final Component child)
    {
        // Add children of the page to the page's border component
        if (border == null)
        {
            // Create border and add it to the page
            border = new LibraryApplicationBorder("border");
            super.add(border);   
        }
        border.add(child);
        return this;
    }

    /**
	 * @see wicket.MarkupContainer#autoAdd(wicket.Component)
	 */
	public boolean autoAdd(Component component)
	{
		return border.autoAdd(component);
	}
	
    /**
     * Removing children from instances of this class causes those children to
     * be removed from the border child instead.
     * @see wicket.MarkupContainer#removeAll()
     */
    public void removeAll()
    {
        border.removeAll();
    }

    /**
     * Replacing children on instances of this class causes those children
     * to be replaced on the border child instead.
     * @see wicket.MarkupContainer#replace(wicket.Component)
     */
    public MarkupContainer replace(Component child)
    {
        return border.replace(child);
    }
    
    /**
     * Get downcast session object
     * 
     * @return The session
     */
    public LibrarySession getLibrarySession()
    {
        return (LibrarySession)getSession();
    }

    /**
     * @see wicket.Page#checkAccess()
     */
    protected boolean checkAccess()
    {
        // Is user signed in?
        if (getLibrarySession().isSignedIn())
        {
            // okay to proceed
            return true;
        }
        
        // Force sign in
        redirectToInterceptPage(newPage(SignIn.class));
        return false;
    }
}


