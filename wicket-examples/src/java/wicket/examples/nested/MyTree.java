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

import wicket.Resource;
import wicket.SharedResource;
import wicket.markup.html.image.Image;
import wicket.markup.html.image.resource.StaticImageResource;
import wicket.markup.html.tree.Tree;

/**
 * tree implementation.
 *
 * @author Eelco Hillenius
 */
public class MyTree extends Tree
{
	/** Log. */
	private static Log log = LogFactory.getLog(MyTree.class);

	/**
	 * Construct.
	 * 
	 * @param componentName
	 *            The name of this container
	 * @param model
	 *            the tree model
	 */
	public MyTree(String componentName, TreeModel model)
	{
		super(componentName, model);
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
			Image img = new Image(NODE_IMAGE_NAME, getImage("node.gif"));
			return img;
		}
		else
		{
			// we want the image to be dynamically, yet resolving to a static image.
			return new Image(NODE_IMAGE_NAME, (SharedResource)null)
			{
				protected Resource getImageResource()
				{
					if (isExpanded(node))
					{
						return getImage("folderopen.gif");
					}
					else
					{
						return getImage("folder.gif");
					}
				}
			};
		}
	}

	/**
	 * Gets the shared image resource with the given name from this package.
	 * @param name the name of the image resource; must match the name of the image in the
	 * package.
	 * @return the shared image resource
	 */
	private SharedResource getImage(final String name)
	{
		return new SharedResource(MyTree.class, name)
		{
			public Resource newResource()
			{
				return StaticImageResource.get(MyTree.class.getPackage(), name, null, null);
			}
		};
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