///////////////////////////////////////////////////////////////////////////////////
//
// Created Jun 13, 2004
//
// Copyright 2004, Jonathan W. Locke
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//

package filebrowser;

import java.io.File;
import java.io.Serializable;
import java.util.Enumeration;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.voicetribe.wicket.Model;
import com.voicetribe.wicket.PageParameters;
import com.voicetribe.wicket.RequestCycle;
import com.voicetribe.wicket.markup.html.HtmlPage;
import com.voicetribe.wicket.markup.html.basic.Label;
import com.voicetribe.wicket.markup.html.image.Image;
import com.voicetribe.wicket.markup.html.tree.Filler;
import com.voicetribe.wicket.markup.html.tree.Node;
import com.voicetribe.wicket.markup.html.tree.Tree;
import com.voicetribe.wicket.markup.html.tree.TreeNodeLink;
import com.voicetribe.wicket.markup.html.tree.TreeStateCache;

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
	private void addChildDirsRecursively(
		String currentPath,
		DefaultMutableTreeNode currentNode)
	{
		if (log.isDebugEnabled())
			log.debug("scan path " + currentPath);

		File d = new File(currentPath);
		String[] c = d.list(); // get list of directories
		if (c != null)
		{
			for (int i = 0; i < c.length; i++)
			{ // for all directories
				File dchild = new File(d, c[i]);
				DefaultMutableTreeNode childNode = new DefaultMutableTreeNode();
				childNode.setUserObject(dchild);
				currentNode.add(childNode); // add child to the current node		

				if (log.isDebugEnabled())
					log.debug("add " + childNode + " to " + currentNode);

				addChildDirsRecursively(
					(currentPath + "/" + c[i]), childNode);
			}
		}
	}

	/**
	 * Debug tree to logger.
	 * @param treeModel tree model
	 */
	private void debugTree(DefaultTreeModel treeModel)
	{

		DefaultMutableTreeNode node = (DefaultMutableTreeNode)treeModel.getRoot();
		Enumeration enum = node.breadthFirstEnumeration();
		enum = node.preorderEnumeration();
		log.info("-- DUMPING TREE --");
		while (enum.hasMoreElements())
		{
			DefaultMutableTreeNode nd = 
				(DefaultMutableTreeNode)enum.nextElement();
			String tabs = "|";
			for (int i = 0; i < nd.getLevel(); i++)
				tabs += "-";
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
	     * @see com.voicetribe.wicket.markup.html.tree.Tree#populateNode(com.voicetribe.wicket.markup.html.tree.Node)
	     */
	    protected void populateNode(final Node node)
	    {
	       final Serializable userObject = node.getUserObject();
	       if(userObject == null)
	       {
	           throw new RuntimeException("userObject == null");
	       }
	       File file = (File)userObject;
           TreeNodeLink expandCollapsLink = new TreeNodeLink(
                    "expandCollapsLink", fileTree, node)
            {
                public void linkClicked(RequestCycle cycle, Node node)
                {
                    TreeStateCache state = fileTree.getTreeState();
                    TreePath selection = state.findTreePath(userObject);
                    fileTree.setExpandedState(selection, (!node.isExpanded())); // inverse
                }
            };
           expandCollapsLink.add(new Image("junctionImg", new Model(node)
            {
                public Object getObject()
                {
                    return getJunctionImageName((Node) super.getObject());
                }
            }));
           expandCollapsLink.add(new Image("nodeImg", new Model(node)
            {
                public Object getObject()
                {
                    return getNodeImageName((Node) super.getObject());
                }
            }));
           node.add(expandCollapsLink);
           TreeNodeLink selectLink = new TreeNodeLink("selectLink", fileTree, node)
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
	     * @see com.voicetribe.wicket.markup.html.tree.Tree#populateFiller(com.voicetribe.wicket.markup.html.tree.Filler)
	     */
	    protected void populateFiller(Filler filler)
	    {
	        filler.add(new Image("fillImg", "vert.gif"));
	    }

	    /**
	     * Get image name for junction.
	     * @param node the current node
	     * @return image name
	     */
	    protected String getJunctionImageName(Node node)
	    {
	        final String img;
	        if(node.isRoot())
	        {
	            img = "cross.gif";
	        }
	        else if(node.isLeaf())
	        {
	            if(node.hasSiblings())
	            {
	                img = "cross.gif";
	            }
	            else
	            {
	                img = "end.gif";
	            }
	        }
	        else
	        {
	            if(node.hasSiblings())
	            {
	                if(node.isExpanded())
	                {
	                    img = "mcross.gif";
	                }
	                else
	                {
	                    img = "pcross.gif";
	                }
	            }
	            else
	            {
	                if(node.isExpanded())
	                {
	                    img = "mend.gif";
	                }
	                else
	                {
	                    img = "pcross.gif";
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
	        if(node.isRoot())
	        {
	            img = "folderopen.gif";
	        }
	        else if(node.isLeaf())
	        {
	            // just a dummy for now
	            img = "node.gif";
	        }
	        else
	        {
                if(node.isExpanded())
                {
                    img = "folderopen.gif";
                }
                else
                {
                    img = "folder.gif";
                }
	        }
	        return img;
	    }
	}
}

