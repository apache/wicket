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
import wicket.markup.ComponentTag;
import wicket.markup.MarkupStream;
import wicket.markup.html.HtmlContainer;
import wicket.markup.html.link.ILinkListener;
import wicket.markup.html.link.Link;


/**
 * Special link for working with trees. Using these links enables working with server-side
 * trees without back-button issues.
 *
 * @author Eelco Hillenius
 */
public abstract class TreeNodeLink extends HtmlContainer implements ILinkListener
{
    /** force static block to execute (registers ILinkListener). */
    private static final Class LINK = Link.class;

    /** tree component. */
    private final Tree tree;

    /** node. */
    private final Node node;

    /** object id. */
    Object id;

    final int creatorHash;

    /**
     * Construct.
     * @param componentName name of component
     * @param tree tree component
     * @param node current node (subject)
     * @param creator the object that creates this instance (needed for internal id
     *            keeping)
     */
    public TreeNodeLink(final String componentName, final Tree tree, final Node node, Object creator)
    {
        super(componentName);
        this.tree = tree;
        this.node = node;
        this.creatorHash = creator.hashCode();
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
    public abstract void linkClicked(final RequestCycle cycle, final Node node);

    /**
     * @param cycle Request cycle
     * @return The URL that this link links to
     */
    String getURL(final RequestCycle cycle)
    {
        return cycle.urlFor(tree, ILinkListener.class) + "&linkId=" + id;
    }

    /**
     * Get node.
     * @return node.
     */
    protected final Node getNode()
    {
        return node;
    }

    /**
     * Get tree.
     * @return tree.
     */
    protected final Tree getTree()
    {
        return tree;
    }

    final void setId(Object id)
    {
        this.id = id;
    }

    /**
     * Get id.
     * @return id
     */
    final Object getId()
    {
        return id;
    }

    /**
     * @see wicket.Component#handleBody(RequestCycle, MarkupStream,
     *      ComponentTag)
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
        tag.put("href", getURL(cycle));
    }
}

///////////////////////////////// End of File /////////////////////////////////
