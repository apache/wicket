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

import wicket.markup.html.HtmlContainer;
import wicket.markup.html.basic.Label;
import wicket.markup.html.panel.Panel;

/**
 * Simple panel for displaying one tree row. This is a panel in order to provide users
 * of the tree component the possibility to use their own panel.
 *
 * @author Eelco Hillenius
 */
public final class TreeRow extends Panel
{
    /**
     * Construct.
     * @param componentName name of the component
     * @param node the tree node for this row
     */
    public TreeRow(String componentName, Tree tree, TreeNodeModel nodeModel)
    {
        super(componentName);
        if(nodeModel != null)
        {
            TreeNodeLink link = new TreeNodeLink("link", tree, nodeModel);
            link.add(new Label("label", String.valueOf(nodeModel.getUserObject())));
            add(link);
        }
        else
        {
            // not a real node; just add some dummies
            add(new HtmlContainer("link").add(new HtmlContainer("label")));
        }
    }
}
