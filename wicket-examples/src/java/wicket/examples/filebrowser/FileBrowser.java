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
import wicket.markup.html.tree.Filler;
import wicket.markup.html.tree.Node;
import wicket.markup.html.tree.Tree;
import wicket.markup.html.tree.TreeNodeLink;
import wicket.markup.html.tree.TreeStateCache;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import java.io.File;
import java.io.FilenameFilter;
import java.io.Serializable;

import java.util.Enumeration;

/**
 * Tree example that uses the user-home dirs to populate the tree.
 * @author Eelco Hillenius
 */
public class FileBrowser extends HtmlPage
{
    /** Log. */
    private static Log log = LogFactory.getLog(FileBrowser.class);

    /** tree component. */
    private FileTree fileTree = null;

    /**
     * Constructor.
     * @param parameters Page parameters
     */
    public FileBrowser(final PageParameters parameters)
    {
        add(new NavigationPanel("mainNavigation", "Filebrowser example"));
        TreeModel model = buildTree();
        fileTree = new FileTree("fileTree", model);
        add(fileTree);
    }

    /**
     * Build the tree.
     * @return the tree
     */
    protected TreeModel buildTree()
    {
        TreeModel model = buildTreeModel();
        //debugTree((DefaultTreeModel)model);
        return model;
    }

    /**
     * Build the tree model.
     * @return the tree model
     */
    protected TreeModel buildTreeModel()
    {
        TreeModel model = null;

        // build directory tree, starting with root dir
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode();
        String userHomeDir = System.getProperty("user.dir");
        File d = new File(userHomeDir);

        rootNode.setUserObject(d);

        String currentPath = userHomeDir;

        addChildDirsRecursively(currentPath, rootNode);
        model = new DefaultTreeModel(rootNode);

        return model;
    }

    /**
     * Add childs recursively.
     * @param currentPath current path
     * @param currentNode current node
     */
    private void addChildDirsRecursively(String currentPath,
        DefaultMutableTreeNode currentNode)
    {
        if (log.isDebugEnabled())
        {
            log.debug("scan path " + currentPath);
        }

        File d = new File(currentPath);
        String[] c = d.list(new FilenameFilter(){

            public boolean accept(File dir, String name)
            {
                return dir.isDirectory();
            }
            
        }); // get list of directories

        if (c != null)
        {
            for (int i = 0; i < c.length; i++)
            { // for all directories

                File dchild = new File(d, c[i]);
                DefaultMutableTreeNode childNode = new DefaultMutableTreeNode();

                childNode.setUserObject(dchild);
                currentNode.add(childNode); // add child to the current node		

                if (log.isDebugEnabled())
                {
                    log.debug("add " + childNode + " to " + currentNode);
                }

                addChildDirsRecursively((currentPath + "/" + c[i]), childNode);
            }
        }
    }

    /**
     * Debug tree to logger.
     * @param treeModel tree model
     */
    private void debugTree(DefaultTreeModel treeModel)
    {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) treeModel
            .getRoot();
        Enumeration e = node.breadthFirstEnumeration();

        e = node.preorderEnumeration();
        log.info("-- DUMPING TREE --");

        while (e.hasMoreElements())
        {
            DefaultMutableTreeNode nd = (DefaultMutableTreeNode) e
                .nextElement();
            String tabs = "|";

            for (int i = 0; i < nd.getLevel(); i++)
            {
                tabs += "-";
            }

            log.info(tabs + nd);
        }
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
         * @see wicket.markup.html.tree.Tree#populateFiller(wicket.markup.html.tree.Filler)
         */
        protected void populateFiller(Filler filler)
        {
            filler.add(new SimpleImage("fillImg", "filebrowser/vert.gif"));
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
