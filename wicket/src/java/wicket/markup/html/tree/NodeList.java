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


import javax.swing.tree.TreeNode;

import wicket.RequestCycle;
import wicket.markup.MarkupStream;
import wicket.markup.html.HtmlContainer;

import java.io.Serializable;

/**
 * Container for tree node lists.
 * @author Eelco Hillenius
 */
public abstract class NodeList extends HtmlContainer
{
    /**
     * Construct.
     * @param componentName the component name
     * @param node the tree node
     */
    public NodeList(String componentName, TreeNode node)
    {
        super(componentName, (Serializable) node);
    }

    /**
     * Renders this container.
     * @param cycle The request cycle
     */
    protected void handleRender(final RequestCycle cycle)
    {
        // Ask parents for markup stream to use
        final MarkupStream markupStream = findMarkupStream();

        // Save position in markup stream
        final int markupStart = markupStream.getCurrentIndex();

        TreeNode currentNode = (TreeNode) getModelObject();
        final int childCount = currentNode.getChildCount();

        if (childCount > 0)
        {
            for (int i = 0; i < childCount; i++)
            {
                // Get name of component for cell i
                final String componentName = Integer.toString(i);

                // If this component does not already exist
                Node node = (Node) get(componentName);

                if (node == null)
                {
                    // Create node for index i of the childs
                    node = newNode(i);

                    // Add node to list
                    add(node);

                    // Let subclass populate it with components
                    populateNode(node);
                }

                // Rewind to start of markup for kids
                markupStream.setCurrentIndex(markupStart);

                // Render node
                node.render(cycle);
            }
        }
        else
        {
            markupStream.skipComponent();
        }
    }

    /**
     * Creates a new cell for the given cell index of this table.
     * @param index Cell index
     * @return The new cell
     */
    protected Node newNode(final int index)
    {
        TreeNode currentNode = (TreeNode) getModelObject();

        return null; //new Node(index, currentNode.getChildAt(index));
    }

    /**
     * Called to allow a subclass to populate a given cell of the table with components.
     * @param node The node
     */
    protected abstract void populateNode(Node node);
}
