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
package wicket.examples.nested;

import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.Component;
import wicket.markup.html.image.AbstractImage;
import wicket.markup.html.image.Image;
import wicket.markup.html.tree.IndentTree;

/** indent tree implementation. */
public class MyTree extends IndentTree
{
	/** Log. */
	private static Log log = LogFactory.getLog(MyTree.class);

	/** node image. */
	private static final Image IMG_NODE = new Image(NODE_IMAGE_NAME, "node.gif");

	/** folder image. */
	private static final Image IMG_FOLDER = new Image(NODE_IMAGE_NAME, "folder.gif");

	/** open folder image. */
	private static final Image IMG_FOLDER_OPEN = new Image(NODE_IMAGE_NAME, "folderopen.gif");

	// set scope of images
	static
	{
		IMG_NODE.setSharing(Component.APPLICATION_SHARED);
		IMG_FOLDER.setSharing(Component.APPLICATION_SHARED);
		IMG_FOLDER_OPEN.setSharing(Component.APPLICATION_SHARED);
	}

	/**
	 * Construct.
	 * @param componentName The name of this container
	 * @param model the tree model
	 */
	public MyTree(String componentName, TreeModel model)
	{
		super(componentName, model);
	}

	/**
	 * @see wicket.markup.html.tree.IndentTree#junctionLinkClicked(javax.swing.tree.DefaultMutableTreeNode)
	 */
	protected void junctionLinkClicked(DefaultMutableTreeNode node)
	{
		super.junctionLinkClicked(node);
		log.info("tree junction link was clicked, user object: " + node.getUserObject());
	}

	/**
	 * @see wicket.markup.html.tree.IndentTree#nodeLinkClicked(javax.swing.tree.DefaultMutableTreeNode)
	 */
	protected void nodeLinkClicked(DefaultMutableTreeNode node)
	{
		super.nodeLinkClicked(node);
		log.info("tree node link was clicked, user object: " + node.getUserObject());
	}

	protected AbstractImage getNodeImage(final DefaultMutableTreeNode node)
	{
		if (node.isLeaf())
		{
			return IMG_NODE;
		}
		else
		{
			if (isExpanded(node))
			{
				return IMG_FOLDER_OPEN;
			}
			else
			{
				return IMG_FOLDER;
			}
		}
	}

	/**
	 * @see wicket.markup.html.tree.IndentTree#getNodeLabel(javax.swing.tree.DefaultMutableTreeNode)
	 */
	protected String getNodeLabel(DefaultMutableTreeNode node)
	{
		Object userObject = node.getUserObject();
		if (userObject instanceof List)
		{
			return "<sub>";
		}
		else
		{
			return String.valueOf(node.getUserObject());
		}
	}
}