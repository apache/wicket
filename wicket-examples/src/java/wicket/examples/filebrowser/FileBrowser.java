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
import java.io.Serializable;

import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.PageParameters;
import wicket.RequestCycle;
import wicket.examples.util.NavigationPanel;
import wicket.markup.ComponentTag;
import wicket.markup.MarkupStream;
import wicket.markup.html.HtmlComponent;
import wicket.markup.html.HtmlPage;
import wicket.markup.html.basic.Label;
import wicket.markup.html.tree.Node;
import wicket.markup.html.tree.Tree;
import wicket.markup.html.tree.TreeNodeLink;
import wicket.markup.html.tree.TreeStateCache;

/**
 * Tree example that uses the user-home dirs to populate the tree.
 *
 * @author Eelco Hillenius
 */
public class FileBrowser extends HtmlPage
{
    /** Log. */
    private static Log log = LogFactory.getLog(FileBrowser.class);

    /** tree component. */
    private FileTree fileTree = null;

    /** module that provides the file model. */
    private FileModelProvider fileModelProvider = new FileModelProvider();

    /**
     * Constructor.
     * @param parameters Page parameters
     */
    public FileBrowser(final PageParameters parameters)
    {
        add(new NavigationPanel("mainNavigation", "Filebrowser example"));
        TreeModel model = fileModelProvider.getFileModel();
        fileTree = new FileTree("fileTree", model);
        add(fileTree);
    }

    /**
     * Tree for files/ directories.
     */
    class FileTree extends Tree
    {
        /**
         * Construct.
         * @param componentName
         * @param model
         */
        public FileTree(String componentName, TreeModel model)
        {
            super(componentName, model);
        }

        /**
         * @see wicket.markup.html.tree.Tree#populateNode(wicket.markup.html.tree.Node)
         */
        protected void populateNode(final Node node)
        {
            final Serializable userObject = node.getUserObject();
            File file = (File) userObject;
            TreeNodeLink expandCollapsLink = new TreeNodeLink("expandCollapsLink",
                    fileTree, node, this)
                {
                    public void linkClicked(RequestCycle cycle, Node node)
                    {
                        TreeStateCache state = fileTree.getTreeState();
                        TreePath selection = state.findTreePath(userObject);

                        fileTree.setExpandedState(selection,
                            (!node.isExpanded())); // inverse
                    }
                };

            expandCollapsLink.add(new SimpleImage("junctionImg",
                    getJunctionImageName(node)));
            expandCollapsLink.add(new SimpleImage("nodeImg",
                    getNodeImageName(node)));
            node.add(expandCollapsLink);

            TreeNodeLink selectLink = new TreeNodeLink("selectLink", fileTree,
                    node, this)
                {
                    public void linkClicked(RequestCycle cycle, Node node)
                    {
                        TreeStateCache state = fileTree.getTreeState();
                        TreePath selection = state.findTreePath(userObject);

                        state.setSelectedPath(selection);
                    }
                };

            selectLink.add(new Label("fileName", file.getName()));
            node.add(selectLink);
        }

        /**
         * Get image name for junction.
         * @param node the current node
         * @return image name
         */
        protected String getJunctionImageName(Node node)
        {
            final String img;

            if (node.isRoot())
            {
                img = "filebrowser/cross.gif";
            }
            else if (node.isLeaf())
            {
                if (node.hasSiblings())
                {
                    img = "filebrowser/cross.gif";
                }
                else
                {
                    img = "filebrowser/end.gif";
                }
            }
            else
            {
                if (node.hasSiblings())
                {
                    if (node.isExpanded())
                    {
                        img = "filebrowser/mcross.gif";
                    }
                    else
                    {
                        img = "filebrowser/pcross.gif";
                    }
                }
                else
                {
                    if (node.isExpanded())
                    {
                        img = "filebrowser/mend.gif";
                    }
                    else
                    {
                        img = "filebrowser/pcross.gif";
                    }
                }
            }

            return img;
        }

        /**
         * Get image name for node.
         * @param node the current node
         * @return image name
         */
        protected String getNodeImageName(Node node)
        {
            final String img;

            if (node.isRoot())
            {
                img = "filebrowser/folderopen.gif";
            }
            else if (node.isLeaf())
            {
                // just a dummy for now
                img = "filebrowser/node.gif";
            }
            else
            {
                if (node.isExpanded())
                {
                    img = "filebrowser/folderopen.gif";
                }
                else
                {
                    img = "filebrowser/folder.gif";
                }
            }

            return img;
        }
    }

    /**
     * Component that writes the given content as-is. This is a *hack*, as getting a load
     * of wicket.examples.images as resources is just too inefficient, but we still want
     * to set them dynamicaly. Another option would be to have components for all possible
     * wicket.examples.images, and just set the needed wicket.examples.images visible. Not
     * nice either.
     */
    private static class SimpleImage extends HtmlComponent
    {
        /**
         * Construct.
         * @param name component name
         * @param src body
         */
        public SimpleImage(String name, String src)
        {
            super(name, src);
        }

        /**
         * @see wicket.Component#handleComponentTag(RequestCycle, ComponentTag)
         */
        protected void handleComponentTag(RequestCycle cycle, ComponentTag tag)
        {
            checkTag(tag, "img");
            super.handleComponentTag(cycle, tag);
            tag.put("src", (String) getModelObject());
        }

        /**
         * @see wicket.Component#handleBody(RequestCycle, MarkupStream, ComponentTag)
         */
        protected void handleBody(RequestCycle cycle,
            MarkupStream markupStream, ComponentTag openTag)
        {
        }
    }
}
