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

import java.io.Serializable;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.markup.ComponentTag;
import wicket.markup.html.image.AbstractImage;
import wicket.markup.html.image.Image;
import wicket.markup.html.tree.IndentTree;
import wicket.util.file.Path;
import wicket.util.lang.Classes;
import wicket.util.resource.IResource;
import wicket.util.resource.ResourceLocator;

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
	 * @see wicket.markup.html.tree.IndentTree#getNodeImage(javax.swing.tree.DefaultMutableTreeNode)
	 */
	protected AbstractImage getNodeImage(final DefaultMutableTreeNode node)
	{
		if (node.isLeaf())
		{
			Image img = new Image(NODE_IMAGE_NAME, "node.gif");
			return img;
		}
		else
		{
			if (isExpanded(node))
			{
				Image img = new Image(NODE_IMAGE_NAME, "folderopen.gif");
				return img;
			}
			else
			{
				Image img = new Image(NODE_IMAGE_NAME, "folder.gif");
				return img;
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

	/**
	 * Image that loads from this package (instead of Image's page)
	 * without locale, style etc.
	 */
	private static final class LocalImage extends Image
	{
		/**
		 * Construct.
		 * @param name component name
		 * @param object model
		 */
		public LocalImage(String name, Serializable object)
		{
			super(name, object);
		}

	    /**
	     * @return Gets the image resource for the component.
	     */
	    protected IResource getResource()
	    {
	    	final String imageResource = getModelObjectAsString();
			final String path = Classes.packageName(MyTree.class) + "." + imageResource;
	        return ResourceLocator.locate
	        (
	            new Path(),
	            MyTree.class.getClassLoader(),
	            path,
	            null,
	            null,
	            null
	        );
	    }

	    /**
	     * @see wicket.Component#onComponentTag(ComponentTag)
	     */
	    protected void onComponentTag(final ComponentTag tag)
	    {
	        checkComponentTag(tag, "img");
	        final String url = getModelObjectAsString();
			tag.put("src", url.replaceAll("&", "&amp;"));
	    }
	}
}