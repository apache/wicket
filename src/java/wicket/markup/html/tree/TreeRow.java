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

import wicket.RequestCycle;
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
	/** reference to the tree component. */
	private final AbstractTree tree;

    /**
     * Construct.
     * @param componentName name of the component
     * @param tree reference to the holding tree component
     * @param nodeModel model for the current node
     */
    public TreeRow(String componentName, AbstractTree tree, TreeNodeModel nodeModel)
    {
        super(componentName);
        this.tree = tree;
        if(nodeModel != null)
        {
            TreeNodeLink link = new TreeNodeLink("link", tree, nodeModel){

                public void linkClicked(RequestCycle cycle, TreeNodeModel node)
                {
                	TreeRow.this.nodeLinkClicked(cycle, node);
                }   
            };
            link.add(new Label("label", String.valueOf(nodeModel.getUserObject())));
            add(link);
        }
        else
        {
            // not a real node; just add some dummies
            add(new HtmlContainer("link").add(new HtmlContainer("label")));
        }
    }

	/**
	 * Handler that is called when a node link is clicked; this implementation
	 * sets the expanded state based on the given node.
	 * Override this for custom behaviour.
	 * @param cycle the current request cycle
	 * @param node the tree node model
	 */
	protected void nodeLinkClicked(RequestCycle cycle, TreeNodeModel node)
	{
        tree.setExpandedState(node);
	}
}
