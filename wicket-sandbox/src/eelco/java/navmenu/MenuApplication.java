/*
 * $Id$
 * $Revision$
 * $Date$
 * 
 * ==================================================================== Licensed
 * under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the
 * License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package navmenu;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

import wicket.ApplicationSettings;
import wicket.protocol.http.WebApplication;
import wicket.util.time.Duration;

/**
 * WicketServlet class for nested structure example.
 *
 * @author Eelco Hillenius
 */
public class MenuApplication extends WebApplication
{
    /**
     * Constructor.
     */
    public MenuApplication()
    {
        getPages().setHomePage(Home.class);
        ApplicationSettings settings = getSettings();
		Duration pollFreq = Duration.ONE_SECOND;
		settings.setResourcePollFrequency(pollFreq);
    }

    /**
     * @return the menu as a tree model
     */
    public static TreeModel getMenu()
    {
		// create tree
		TreeModel model = null;
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("ROOT"); // dummy
		DefaultMutableTreeNode a0 = new DefaultMutableTreeNode(
				new MenuItem("Home", Home.class, null));
		root.add(a0);
		DefaultMutableTreeNode a1 = new DefaultMutableTreeNode(
				new MenuItem("Images", Page1.class, null));
		root.add(a1);
		DefaultMutableTreeNode a2 = new DefaultMutableTreeNode(
				new MenuItem("Templates", Page2.class, null));
		root.add(a2);
		DefaultMutableTreeNode a3 = new DefaultMutableTreeNode(
				new MenuItem("Users", Page3.class, null));
		root.add(a3);
		DefaultMutableTreeNode a4 = new DefaultMutableTreeNode(
				new MenuItem("Preferences", Page4.class, null));
		root.add(a4);
		// TODO this is just one level. We should be able to handle more than that
		model = new DefaultTreeModel(root);
		return model;
    }
}
