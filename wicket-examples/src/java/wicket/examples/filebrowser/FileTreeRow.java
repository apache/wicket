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

import java.io.File;

import wicket.markup.ComponentTagAttributeModifier;
import wicket.markup.html.HtmlContainer;
import wicket.markup.html.basic.Label;
import wicket.markup.html.panel.Panel;
import wicket.markup.html.tree.OnClickTreeNodeLink;
import wicket.markup.html.tree.Tree;
import wicket.markup.html.tree.TreeNodeLink;
import wicket.markup.html.tree.TreeNodeModel;
import wicket.markup.html.tree.TreeRowReplacementModel;

/**
 * Panel for displaying one tree row. This overrides the default
 * {@link wicket.markup.html.tree.TreeRow} used by {@link wicket.markup.html.tree.Tree}.
 *
 * @author Eelco Hillenius
 */
public final class FileTreeRow extends Panel
{
    /**
     * Construct.
     * @param componentName name of the component
     * @param node the tree node for this row
     */
    public FileTreeRow(String componentName, Tree tree, TreeNodeModel nodeModel)
    {
        super(componentName);
        HtmlContainer li = null;
        if(nodeModel != null)
        {
            TreeNodeLink link = new TreeNodeLink("link", tree, nodeModel);
            File file = (File)nodeModel.getUserObject();
            link.add(new Label("label", file.getName()));
            li = new OnClickTreeNodeLink("li", tree, nodeModel);
            li.add(link);
        }
        else
        {
            // not a real node; just add some dummies
        	li = new HtmlContainer("li");
            li.add(new HtmlContainer("link").add(new HtmlContainer("label")));
        }
        TreeRowReplacementModel replacementModel = new TreeRowReplacementModel(nodeModel);
        li.add(new ComponentTagAttributeModifier("class", true, replacementModel));
        add(li);
    }
}
