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
package wicket.markup.html.tree;

import java.util.List;

import wicket.markup.html.panel.Panel;
import wicket.markup.html.table.ListItem;
import wicket.markup.html.table.ListView;

/**
 * This example list knows how to display sublists. It expects a list where
 * each element is either a string or another list.
 *
 * @author Eelco Hillenius
 */
public final class TreeRows extends Panel
{
    /** the holding tree component. */
    private final Tree tree;

    /**
     * Constructor.
     * @param componentName The name of this component
     * @param list a list where each element is either a string or another list
     * @param tree the holding tree component
     */
    public TreeRows(final String componentName, List list, Tree tree)
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
                TreeRow row = new TreeRow("row", tree, null);
                row.setVisible(false);
                listItem.add(row);
                TreeRows nested = new TreeRows("nested", (List)modelObject, tree);
                listItem.add(nested);
            }
            else
            {
                NodeModel nodeModel = (NodeModel)modelObject;
                TreeRow row = new TreeRow("row", tree, nodeModel);
                listItem.add(row);
                TreeRows nested = new TreeRows("nested", null, tree);
                nested.setVisible(false);
                listItem.add(nested);
            }
        }
    }
}
