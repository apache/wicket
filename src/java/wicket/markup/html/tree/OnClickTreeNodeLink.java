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

import wicket.markup.ComponentTag;

/**
 * Special link for working with trees. Using these links enables working with server-side
 * trees without back-button issues.
 *
 * @author Eelco Hillenius
 */
public class OnClickTreeNodeLink extends AbstractTreeNodeLink
{ // TODO finalize javadoc
    /**
     * Construct.
     * @param componentName name of component
     * @param tree tree component
     * @param node current node (subject)
     */
    public OnClickTreeNodeLink(final String componentName,
            final Tree tree, final TreeNodeModel node)
    {
    	super(componentName, tree, node);
    }

    /**
     * @see wicket.Component#handleComponentTag(ComponentTag)
     */
    protected final void handleComponentTag(final ComponentTag tag)
    {
        // Add simple javascript on click handler that links to this
        // link's linkClicked method
        String url = getURL();
        url = url.replaceAll("&", "&amp;");
		tag.put("onclick", "location.href='"+ url + "';");
    }
}