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

import wicket.markup.html.tree.IndentTree;

/** indent tree implementation. */
public class MyTree extends IndentTree
{
	/** Log. */
	private static Log log = LogFactory.getLog(MyTree.class);

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

	/**
	 * @see wicket.markup.html.tree.IndentTree#getJunctionImageName(javax.swing.tree.DefaultMutableTreeNode)
	 */
	protected String getJunctionImageName(DefaultMutableTreeNode node)
	{
		final String img;

		if (!node.isLeaf())
		{
			if (isExpanded(node))
			{
				img = "nested/minus.gif";
			}
			else
			{
				img = "nested/plus.gif";
			}
		}
		else
		{
			img = "nested/blank.gif";
		}

		return img;
	}

	/**
	 * @see wicket.markup.html.tree.IndentTree#getNodeImageName(javax.swing.tree.DefaultMutableTreeNode)
	 */
	protected String getNodeImageName(DefaultMutableTreeNode node)
	{
		final String img;

		if (node.isLeaf())
		{
			img = "nested/node.gif";
		}
		else
		{
			if (isExpanded(node))
			{
				img = "nested/folderopen.gif";
			}
			else
			{
				img = "nested/folder.gif";
			}
		}

		return img;
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