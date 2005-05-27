/*
 * $Id$ $Revision$
 * $Date$
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
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

import wicket.ResourceReference;
import wicket.markup.html.StaticResourceReference;
import wicket.markup.html.image.Image;
import wicket.markup.html.tree.Tree;

/**
 * tree implementation.
 *
 * @author Eelco Hillenius
 */
public class MyTree extends Tree
{
	private static final ResourceReference folderOpen = new StaticResourceReference(MyTree.class, "folderopen.gif");
	private static final ResourceReference folder = new StaticResourceReference(MyTree.class, "folder.gif");
	private static final ResourceReference nodeImage = new StaticResourceReference(MyTree.class, "node.gif");
	
	/** Log. */
	private static Log log = LogFactory.getLog(MyTree.class);

	/**
	 * Construct.
	 * 
	 * @param id
	 *            The id of this component
	 * @param model
	 *            the tree model
	 */
	public MyTree(String id, TreeModel model)
	{
		super(id, model);
	}

	/**
	 * @see wicket.markup.html.tree.Tree#junctionLinkClicked(javax.swing.tree.DefaultMutableTreeNode)
	 */
	protected void junctionLinkClicked(DefaultMutableTreeNode node)
	{
		super.junctionLinkClicked(node);
		log.info("tree junction link was clicked, user object: " + node.getUserObject());
	}

	/**
	 * @see wicket.markup.html.tree.Tree#nodeLinkClicked(javax.swing.tree.DefaultMutableTreeNode)
	 */
	protected void nodeLinkClicked(DefaultMutableTreeNode node)
	{
		super.nodeLinkClicked(node);
		log.info("tree node link was clicked, user object: " + node.getUserObject());
	}

	/**
	 * @see wicket.markup.html.tree.Tree#getNodeImage(javax.swing.tree.DefaultMutableTreeNode)
	 */
	protected Image getNodeImage(final DefaultMutableTreeNode node)
	{
		if (node.isLeaf())
		{
			Image img = new Image(NODE_IMAGE_NAME, nodeImage);
			return img;
		}
		else
		{
			// we want the image to be dynamic, yet resolve to a static image.
			return new Image(NODE_IMAGE_NAME)
			{
				protected ResourceReference getImageResourceReference()
				{
					if (isExpanded(node))
					{
						return folderOpen;
					}
					else
					{
						return folder;
					}
				}
			};
		}
	}

	/**
	 * @see wicket.markup.html.tree.Tree#getNodeLabel(javax.swing.tree.DefaultMutableTreeNode)
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