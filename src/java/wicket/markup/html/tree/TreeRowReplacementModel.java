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

import wicket.model.IModel;

/**
 * A replacement model for
 * {@link wicket.markup.ComponentTagAttributeModifier}s for tree row components.
 *
 * @see wicket.markup.ComponentTagAttributeModifier
 * @see wicket.markup.html.tree.Tree
 * @see wicket.markup.html.tree.TreeRow
 *
 * @author Eelco Hillenius
 */
public class TreeRowReplacementModel implements IModel
{
    /** model of one tree node. */
    private final TreeNodeModel treeNodeModel;

    /**
     * Construct.
     * @param treeNodeModel model of one tree node
     */
    public TreeRowReplacementModel(TreeNodeModel treeNodeModel)
    {
        this.treeNodeModel = treeNodeModel;
    }

    /**
     * Gets the class depending on some attributes of the current tree node.
     * @see wicket.model.IModel#getObject()
     */
    public final Object getObject()
    {
        return (treeNodeModel != null) ? getJunctionCSSClass(treeNodeModel) : null;
    }

    /**
     * @see wicket.model.IModel#setObject(java.lang.Object)
     */
    public final void setObject(Object object)
    {
        // nothing to do here
    }

    /**
     * Gets the css class name/ replacement value for the given node.
     * @param treeNodeModel model of one tree node
     * @return css class name/ replacement value
     */
    protected String getJunctionCSSClass(TreeNodeModel treeNodeModel)
    {
        final String cssClass;

        if (treeNodeModel.isLeaf())
        {
            if (treeNodeModel.hasSiblings())
            {
                cssClass = getCSSClassForLeafWithSiblings();
            }
            else
            {
                cssClass = getCSSClassForEndLeaf();
            }
        }
        else
        {
            if (treeNodeModel.hasSiblings())
            {
                if (treeNodeModel.isExpanded())
                {
                    cssClass = getCSSClassForExpandedJunctionWithSiblings();
                }
                else
                {
                    cssClass = getCSSClassForClosedJunctionWithSiblings();
                }
            }
            else
            {
                if (treeNodeModel.isExpanded())
                {
                    cssClass = getCSSClassForExpandedEndJunction();
                }
                else
                {
                    cssClass = getCSSClassForClosedEndJunction();
                }
            }
        }

        return cssClass;
    }

    /**
     * Gets the css class for a closed junction with no siblings.
     * @return the css class
     */
    private String getCSSClassForClosedEndJunction()
    {
        final String cssClass;
        cssClass = "tree-junction-closed-end";
        return cssClass;
    }

    /**
     * Gets the css class for an expanded junction with no siblings.
     * @return the css class
     */
    private String getCSSClassForExpandedEndJunction()
    {
        final String cssClass;
        cssClass = "tree-junction-expanded-end";
        return cssClass;
    }

    /**
     * Gets the css class for a closed junction with siblings.
     * @return the css class
     */
    private String getCSSClassForClosedJunctionWithSiblings()
    {
        final String cssClass;
        cssClass = "tree-junction-closed-siblings";
        return cssClass;
    }

    /**
     * Gets the css class for an expanded junction with siblings.
     * @return the css class
     */
    private String getCSSClassForExpandedJunctionWithSiblings()
    {
        final String cssClass;
        cssClass = "tree-junction-expanded-siblings";
        return cssClass;
    }

    /**
     * Gets the css class for a leaf with no siblings.
     * @return the css class
     */
    private String getCSSClassForEndLeaf()
    {
        final String cssClass;
        cssClass = "tree-leaf-end";
        return cssClass;
    }

    /**
     * Gets the css class for leaf with siblings.
     * @return the css class
     */
    protected String getCSSClassForLeafWithSiblings()
    {
        final String cssClass;
        cssClass = "tree-leaf-siblings";
        return cssClass;
    }
}
