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
package wicket.examples.filebrowser;

import java.util.List;

import wicket.markup.ComponentTagAttributeModifier;
import wicket.markup.html.panel.Panel;
import wicket.markup.html.table.ListItem;
import wicket.markup.html.table.ListView;
import wicket.markup.html.tree.ListTreeRowReplacementModel;
import wicket.markup.html.tree.TreeNodeModel;

/**
 * This example list knows how to display sublists. It expects a list where
 * each element is either a string or another list.
 *
 * @author Eelco Hillenius
 */
public final class CustomListTreeRows extends Panel
{
    /** the holding tree component. */
    private final CustomListTree tree;

    /**
     * Constructor.
     * @param componentName The name of this component
     * @param list a list where each element is either a string or another list
     * @param tree the holding tree component
     */
    public CustomListTreeRows(final String componentName, List list, CustomListTree tree)
    {
        super(componentName);
        this.tree = tree;
        add(new Rows("rows", list));
    }

    /**
     * The list class.
     */
    private class Rows extends ListView
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
         * @see wicket.markup.html.table.ListView#populateItem(wicket.markup.html.table.ListItem)
         */
        protected void populateItem(ListItem listItem)
        {
            Object modelObject = listItem.getModelObject();
            if(modelObject instanceof List)
            {
                Panel row = getTreeRowPanel("row", null);
                row.setVisible(false);
                listItem.add(row);
                CustomListTreeRows nested = new CustomListTreeRows("nested", (List)modelObject, tree);
                listItem.add(nested);
            }
            else
            {
                TreeNodeModel nodeModel = (TreeNodeModel)modelObject;
                Panel row = getTreeRowPanel("row", nodeModel);
                listItem.add(row);
                CustomListTreeRows nested = new CustomListTreeRows("nested", null, tree);
                nested.setVisible(false);
                listItem.add(nested);
            }
        }

        /**
         * Gets the row panel.
         * @param componentName name of the component
         * @param nodeModel the node model
         * @return the new panel for one row
         */
        protected Panel getTreeRowPanel(String componentName, TreeNodeModel nodeModel)
        {
            ListTreeRowReplacementModel replacementModel =
                new ListTreeRowReplacementModel(nodeModel);
            Panel rowPanel = new CustomListTreeRow(componentName,
            		CustomListTreeRows.this.tree, nodeModel);
            rowPanel.add(new ComponentTagAttributeModifier(
                    "class", true, replacementModel));
            return rowPanel;
        } 
    }
}
