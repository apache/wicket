/*
 * $Id$
 * $Revision$
 * $Date$
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.examples.navmenu;

import wicket.ApplicationSettings;
import wicket.extensions.markup.html.navmenu.MenuItem;
import wicket.extensions.markup.html.navmenu.MenuModel;
import wicket.protocol.http.WebApplication;

/**
 * WicketServlet class for nested structure example.
 *
 * @author Eelco Hillenius
 */
public class NavMenuApplication extends WebApplication
{
    /**
     * Constructor.
     */
    public NavMenuApplication()
    {
        getPages().setHomePage(Home.class);
		ApplicationSettings settings = getSettings();
		settings.configure("development");
    }

    /**
     * @return the menu as a tree model
     */
    public static MenuModel getMenu()
    {
		// create tree
		MenuModel model = null;
		MenuItem root = new MenuItem(); // dummy
		MenuItem a0 = new MenuItem("Home", Home.class, null);
		root.add(a0);
		MenuItem a1 = new MenuItem("Images", Page1.class, null);
		root.add(a1);
		MenuItem a2 = new MenuItem("Templates", Page2.class, null);
		MenuItem a2a = new MenuItem("Ognl", Page2a.class, null);
		a2.add(a2a);
		MenuItem a2b = new MenuItem("Velocity", Page2b.class, null);
		a2.add(a2b);
		root.add(a2);
		MenuItem a3 = new MenuItem("Users", Page3.class, null);
		MenuItem a3a = new MenuItem("Truus", Page3a.class, null);
		a3.add(a3a);
		MenuItem a3b = new MenuItem("Mien", Page3b.class, null);
		a3.add(a3b);
		root.add(a3);
		MenuItem a4 = new MenuItem("Preferences", Page4.class, null);
		root.add(a4);
		model = new MenuModel(root);
		return model;
    }
}
