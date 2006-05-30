/*
 * $Id: MyTree.java 5394 2006-04-16 06:36:52 -0700 (Sun, 16 Apr 2006)
 * jdonnerstag $ $Revision: 5394 $ $Date: 2005-10-02 01:14:57 +0200 (So, 02 Okt
 * 2005) $
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

import wicket.Component;
import wicket.MarkupContainer;
import wicket.markup.html.basic.Label;
import wicket.markup.html.link.Link;
import wicket.markup.html.panel.Panel;
import wicket.markup.html.tree.Tree;
import wicket.model.AbstractReadOnlyModel;
import wicket.model.IModel;

/**
 * Another customized tree implementation, this time with a custom node panel.
 * 
 * @author Eelco Hillenius
 */
public class AnotherTree extends Tree
{
	/**
	 * Custom node panel.
	 */
	public final class Node extends Panel
	{
		/**
		 * Construct.
		 * 
		 * @param parent
		 * @param panelId
		 *            The id of the component
		 * @param node
		 *            The tree node for this panel
		 */
		public Node(MarkupContainer parent, String panelId, final DefaultMutableTreeNode node)
		{
			super(parent, panelId);

			Object userObject = node.getUserObject();

			// create a link for expanding and collapsing the node
			final Link junctionLink = new Link(this, "junctionLink")
			{
				@Override
				public void onClick()
				{
					junctionLinkClicked(node);
				}
			};

			// we make this a proper model instead of just evaluating the
			// string, as we want to have the current value everytime
			// the label is rendered
			IModel junctionLabelModel = new AbstractReadOnlyModel()
			{
				@Override
				public Object getObject(Component component)
				{
					return (!node.isLeaf()) ? (isExpanded(node)) ? "^" : ">" : "";
				}
			};
			String junctionLabel = "";
			if (!node.isLeaf())
			{
				junctionLabel = (isExpanded(node)) ? "[-]" : "[+]";
			}
			new Label(junctionLink, "junctionLabel", junctionLabelModel);

			// create a link for selecting a node
			final Link nodeLink = new Link(this, "nodeLink")
			{
				@Override
				public void onClick()
				{
					nodeLinkClicked(node);
				}
			};
			String label = (userObject instanceof List) ? "" : String.valueOf(node.getUserObject());
			new Label(nodeLink, "label", label);
		}
	}

	/** Log. */
	private static Log log = LogFactory.getLog(AnotherTree.class);

	/**
	 * Construct.
	 * 
	 * @param parent
	 * @param id
	 *            The id of this component
	 * @param model
	 *            the tree model
	 */
	public AnotherTree(MarkupContainer parent, final String id, TreeModel model)
	{
		super(parent, id, model);
	}

	/**
	 * @see wicket.markup.html.tree.Tree#newNodePanel(MarkupContainer, java.lang.String,
	 *      javax.swing.tree.DefaultMutableTreeNode)
	 */
	@Override
	protected Component newNodePanel(MarkupContainer parent, String panelId,
			DefaultMutableTreeNode node)
	{
		return new Node(parent, panelId, node);
	}
}