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
package navmenu;

import javax.swing.tree.DefaultMutableTreeNode;

import wicket.AttributeModifier;
import wicket.markup.html.basic.Label;
import wicket.markup.html.link.BookmarkablePageLink;
import wicket.markup.html.panel.Panel;
import wicket.model.Model;

/**
 * This example list knows how to display sublists. It expects a list where
 * each element is either a string or another list.
 *
 * @author Eelco Hillenius
 */
final class LI extends Panel
{
	/** the level this view is on. */
	private final int level;

    /**
     * Constructor.
     * @param componentName The name of this component
     * @param node tree node
     * @param level the level this view is on (from 0..n-1)
     * @param index the sibling index
     */
    public LI(final String componentName, final DefaultMutableTreeNode node,
    		final int level, final int index)
    {
        super(componentName);
        this.level = level;
        // add the row (with the LI element attached, and the label with the
        // row's actual value to display
        MenuItem menuItem = (MenuItem)node.getUserObject();
        final String label = menuItem.getLabel();
        final BookmarkablePageLink pageLink = new BookmarkablePageLink(
        		"link", menuItem.getPageClass(), menuItem.getPageParameters());
        pageLink.setAutoEnable(false);
        pageLink.add(new Label("label", label));
        add(pageLink);
        // TODO this works for one level, but what about nesting?
		add(new AttributeModifier("class", true, new Model()
		{
			public Object getObject()
			{
				return (pageLink.linksTo(getPage())) ? "selectedTab" : null;
			}
		}));
    }
}
