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
import java.util.Enumeration;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.voicetribe.wicket.PageParameters;
import com.voicetribe.wicket.markup.html.HtmlPage;
import com.voicetribe.wicket.markup.html.basic.Label;

/**
 * Tree example that uses the user-home dirs to populate the tree.
 * @author Eelco Hillenius
 */
public class FileBrowser extends HtmlPage
{
    /** Log. */
    private static Log log = LogFactory.getLog(FileBrowser.class);

    /**
     * Constructor.
     * @param parameters Page parameters
     */
    public FileBrowser(final PageParameters parameters)
    {
        add(new Label("message", "Hello world!"));
        buildTree();
    }

    protected void buildTree()
    {
        TreeModel model = buildTreeModel();
        debugTree((DefaultTreeModel)model);
    }

	protected TreeModel buildTreeModel() 
	{
		long tsBegin = System.currentTimeMillis();
		
		TreeModel model = null;
		// build directory tree, starting with root dir
		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode();
		String userHomeDir = System.getProperty("user.dir");
        File d = new File(userHomeDir);
		rootNode.setUserObject(d);
		String currentPath = userHomeDir;

		addChildDirsRecursively(currentPath, rootNode);
		model = new DefaultTreeModel(rootNode);
		
		long tsEnd = System.currentTimeMillis();
		if(log.isDebugEnabled())
		{
			log.debug("build resource directory tree in " + (tsEnd - tsBegin) + " miliseconds");	
		}
		return model;
	}

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


	private void debugTree(DefaultTreeModel m)
	{

		DefaultMutableTreeNode node = (DefaultMutableTreeNode)m.getRoot();
		Enumeration enum = node.breadthFirstEnumeration();
		enum = node.preorderEnumeration();
		log.info("-- DUMPING DIRECTORY TREE --");
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
}

