/*
 * $Id$
 * $Revision$ $Date$
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
package wicket.extensions.markup.html.tree;

import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.Application;
import wicket.MarkupContainer;
import wicket.ResourceReference;
import wicket.markup.html.PackageResourceReference;
import wicket.markup.html.image.Image;

/**
 * tree implementation.
 * 
 * @author Eelco Hillenius
 */
public class MyTree extends Tree
{
	private static final ResourceReference folder = new PackageResourceReference(Application.get(),
			MyTree.class, "folder.gif");

	private static final ResourceReference folderOpen = new PackageResourceReference(Application
			.get(), MyTree.class, "folderopen.gif");
	/** Log. */
	private static final Log log = LogFactory.getLog(MyTree.class);
	private static final ResourceReference nodeImage = new PackageResourceReference(Application
			.get(), MyTree.class, "node.gif");

	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 * 
	 * @param parent
	 * @param id
	 *            The id of this component
	 * @param model
	 *            the tree model
	 */
	public MyTree(MarkupContainer parent, String id, TreeModel model)
	{
		super(parent, id, model);
	}

	/**
	 * @see wicket.extensions.markup.html.tree.Tree#getNodeImage(MarkupContainer, javax.swing.tree.DefaultMutableTreeNode)
	 */
	@Override
	protected Image getNodeImage(MarkupContainer parent, final DefaultMutableTreeNode node)
	{
		if (node.isLeaf())
		{
			Image img = new Image(parent, NODE_IMAGE_NAME, nodeImage);
			return img;
		}
		else
		{
			// we want the image to be dynamic, yet resolve to a static image.
			return new Image(parent, NODE_IMAGE_NAME)
			{
				private static final long serialVersionUID = 1L;

				@Override
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
	 * @see wicket.extensions.markup.html.tree.Tree#getNodeLabel(javax.swing.tree.DefaultMutableTreeNode)
	 */
	@Override
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

	/**
	 * @see wicket.extensions.markup.html.tree.Tree#junctionLinkClicked(javax.swing.tree.DefaultMutableTreeNode)
	 */
	@Override
	protected void junctionLinkClicked(DefaultMutableTreeNode node)
	{
		super.junctionLinkClicked(node);
		log.info("tree junction link was clicked, user object: " + node.getUserObject());
	}

	/**
	 * @see wicket.extensions.markup.html.tree.Tree#nodeLinkClicked(javax.swing.tree.DefaultMutableTreeNode)
	 */
	@Override
	protected void nodeLinkClicked(DefaultMutableTreeNode node)
	{
		super.nodeLinkClicked(node);
		log.info("tree node link was clicked, user object: " + node.getUserObject());
	}
}