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
package wicket.examples.navmenu;

import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import wicket.AttributeModifier;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.html.list.ListItem;
import wicket.markup.html.list.ListView;
import wicket.markup.html.panel.Panel;
import wicket.model.Model;

/**
 * This example list knows how to display sublists. It expects a list where
 * each element is either a string or another list.
 *
 * @author Eelco Hillenius
 */
final class UL extends Panel
{
	/** the level this view is on. */
	private final int level;

    /**
     * Constructor.
     * @param componentName The name of this component
     * @param list a list where each element is either a string or another list
     * @param level the level this view is on (from 0..n-1)
     */
    public UL(String componentName, List list, int level)
    {
        super(componentName);
        this.level = level;
        WebMarkupContainer ul = new WebMarkupContainer("ul");
        ul.add(new AttributeModifier("id", true, new Model(getLevelAsString())));
		Rows rows = new Rows("rows", list);
		ul.add(rows);
		add(ul);
    }

    /**
     * Gets the level of this view as a string that is usuable with CSS.
     * @return the level of this view as a string that is usuable with CSS
     */
    private String getLevelAsString()
    {
    	return "tabNavigation";
//    	if(level == 0)
//    	{
//    		return "primary";
//    	}
//    	else
//    	{
//    		return "secondary";
//    	}
    }

    /**
     * The list class.
     */
    private final class Rows extends ListView
    {
        /**
         * Construct.
         * @param name name of the component
         * @param list a list where each element is either a string or another list
         */
        public Rows(String name, List list)
        {
            super(name, list);
        }

        /**
         * @see wicket.markup.html.list.ListView#populateItem(wicket.markup.html.list.ListItem)
         */
        protected void populateItem(ListItem listItem)
        {
        	final int index = listItem.getIndex();
            Object modelObject = listItem.getModelObject();
            if(modelObject instanceof List)
            {
                // create a panel that renders the sub list
                List list = (List)modelObject;
				UL ul = new UL("row", list, level + 1);
                listItem.add(ul);
            }
            else
            {
            	DefaultMutableTreeNode node = (DefaultMutableTreeNode)modelObject;
				LI li = new LI("row", node, level, index);
                listItem.add(li);
            }
        }
    }
}
