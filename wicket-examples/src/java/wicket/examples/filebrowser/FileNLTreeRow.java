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

import wicket.RequestCycle;
import wicket.markup.ComponentTagAttributeModifier;
import wicket.markup.html.HtmlContainer;
import wicket.markup.html.basic.Label;
import wicket.markup.html.panel.Panel;
import wicket.markup.html.tree.NLTreeRowReplacementModel;
import wicket.markup.html.tree.OnClickTreeNodeLink;
import wicket.markup.html.tree.TreeNodeModel;

/**
 * Panel for displaying one tree row. This overrides the default
 * {@link wicket.markup.html.tree.TreeRow} used by {@link wicket.markup.html.tree.Tree}.
 *
 * @author Eelco Hillenius
 */
public final class FileNLTreeRow extends Panel
{
    /**
     * Construct.
     * @param componentName name of the component
     * @param tree
     * @param nodeModel the tree node for this row
     */
    public FileNLTreeRow(String componentName, final FileNLTreeCustomRows tree,
    		TreeNodeModel nodeModel)
    {
        super(componentName);
        HtmlContainer li = null;
        if(nodeModel != null)
        {
            File file = (File)nodeModel.getUserObject();
            li = new OnClickTreeNodeLink("li", tree, nodeModel){

                public void linkClicked(RequestCycle cycle, TreeNodeModel node)
                {
                	// link to tree
                	tree.linkClicked(cycle, node);
                }   
            };
            li.add(new Label("label", file.getName()));
        }
        else
        {
            // not a real node; just add some dummies
        	li = new HtmlContainer("li");
            li.add(new HtmlContainer("label"));
        }
        NLTreeRowReplacementModel replacementModel = new NLTreeRowReplacementModel(nodeModel);
        li.add(new ComponentTagAttributeModifier("class", true, replacementModel));
        add(li);
    }
}
