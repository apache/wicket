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
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Enumeration;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Simple module that provides the treemodel based on a file system tree.
 *
 * @author Eelco Hillenius
 */
public final class FileModelProvider
{
    /** Log. */
    private static Log log = LogFactory.getLog(FileModelProvider.class);

    /** treemodel; only do this once for this example. */
    private static TreeModel model = null;

    /** cheap lock. */
    private static Object lock = new Object();

    /**
     * Construct.
     */
    public FileModelProvider()
    {
    }

    /**
     * Gets the tree model.
     * @return the tree model
     */
    public TreeModel getFileModel()
    {
        synchronized(lock)
        {
            if(model == null)
            {
                model = buildTree();
                debugTree(model);
            }
        }
        return model;
    }

    /**
     * Build the tree.
     * @return the tree
     */
    private TreeModel buildTree()
    {
        TreeModel model = buildTreeModel();
        //debugTree((DefaultTreeModel)model);
        return model;
    }

    /**
     * Build the tree model.
     * @return the tree model
     */
    private TreeModel buildTreeModel()
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
            Arrays.sort(c); // sort files
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
    private static void debugTree(TreeModel treeModel)
    {
    	if(log.isDebugEnabled())
    	{
	        DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) treeModel.getRoot();
	        Enumeration e = rootNode.preorderEnumeration();
	        log.debug("-- DUMPING TREE --");
	        while (e.hasMoreElements())
	        {
	            DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.nextElement();
	            String tabs = "|";
	
	            for (int i = 0; i < node.getLevel(); i++)
	            {
	                tabs += "-";
	            }
	
	            log.debug(tabs + node);
	        }
    	}
    }
}
