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

import java.io.Serializable;

import javax.swing.tree.TreePath;

import wicket.RequestCycle;
import wicket.markup.ComponentTag;
import wicket.markup.MarkupStream;
import wicket.markup.html.link.AbstractLink;
import wicket.markup.html.link.ILinkListener;
import wicket.markup.html.link.Link;


/**
 * Special link for working with trees. Using these links enables working with server-side
 * trees without back-button issues.
 *
 * @author Eelco Hillenius
 */
public class TreeNodeLink extends AbstractLink
{
	/** the request parameter for the link id; value == 'lid'. */
	public static final String REQUEST_PARAMETER_LINK_ID = "lid";

    /** force static block to execute (registers ILinkListener). */
    private static final Class LINK = Link.class;

    /** tree component. */
    private final AbstractTree tree;

    /** node. */
    private final TreeNodeModel node;

    /** object id. */
    private final Serializable id;

    /**
     * Construct.
     * @param componentName name of component
     * @param tree tree component
     * @param node current node (subject)
     */
    public TreeNodeLink(final String componentName,
            final AbstractTree tree, final TreeNodeModel node)
    {
        super(componentName);
        this.tree = tree;
        this.node = node;

        // get the user object. WARNING: do not call node.getUserObject, as we want the
        // wrapped user object in case it was made unique
        Object userObject = node.getTreeNode().getUserObject();

        // links can change, but the target user object should be the same, so
        // if a new link is added that actually points to the same userObject, it will
        // replace the old one thus allowing the old link to be GC-ed.
        // Te id is a combination of the 
        String linkId = String.valueOf(userObject.hashCode());
        id = linkId;

        // add the link to the tree. By adding it to the tree instead of one of the tree's nested components,
        // we have it decoupled and thus reachable for as long as the tree exists
        tree.addLink(this);
    }

    /**
     * Called when a link is clicked.
     * @see ILinkListener
     * @param cycle The cycle object
     */
    public final void linkClicked(final RequestCycle cycle)
    {
        linkClicked(cycle, node);
    }

    /**
     * Called when a link is clicked.
     * @see ILinkListener
     * @param cycle The cycle object
     * @param node
     */
    public void linkClicked(RequestCycle cycle, TreeNodeModel node)
    {
        // get the user object. WARNING: do not call node.getUserObject, as we want the
        // wrapped user object in case it was made unique
        Object userObject = node.getTreeNode().getUserObject();

        TreeStateCache state = tree.getTreeState();
        TreePath selection = state.findTreePath(userObject);
        tree.setExpandedState(selection, (!node.isExpanded())); // inverse
    }

    /**
     * @param cycle Request cycle
     * @return The URL that this link links to
     */
    protected String getURL(final RequestCycle cycle)
    {
        return cycle.urlFor(tree, ILinkListener.class)
        	+ "&" + REQUEST_PARAMETER_LINK_ID + "=" + id;
    }

    /**
     * Gets the node model.
     * @return the node model
     */
    public TreeNodeModel getNode()
    {
        return node;
    }

    /**
     * Gets the holding tree component.
     * @return the holding tree component.
     */
    protected final AbstractTree getTree()
    {
        return tree;
    }

    /**
     * Gets the link's unique id.
     * @return the link's unique id
     */
    final Serializable getId()
    {
        return id;
    }

    /**
     * @see wicket.Component#handleBody(RequestCycle, MarkupStream, ComponentTag)
     */
    protected final void handleBody(final RequestCycle cycle, final MarkupStream markupStream,
            final ComponentTag openTag)
    {
        // Render the body of the link
        renderBody(cycle, markupStream, openTag);
    }

    /**
     * @see wicket.Component#handleComponentTag(RequestCycle, ComponentTag)
     */
    protected final void handleComponentTag(final RequestCycle cycle, final ComponentTag tag)
    {
        // Can only attach links to anchor tags
        checkTag(tag, "a");

        // Default handling for tag
        super.handleComponentTag(cycle, tag);

        // Set href to link to this link's linkClicked method
        String url = getURL(cycle);
		tag.put("href", url);
    }
}