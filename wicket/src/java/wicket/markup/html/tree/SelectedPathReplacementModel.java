/*
 * $Id: AbstractTree.java 2527 2005-08-16 15:33:01 -0700 (Tue, 16 Aug 2005)
 * eelco12 $ $Revision: 2527 $ $Date: 2005-08-16 15:33:01 -0700 (Tue, 16 Aug
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
package wicket.markup.html.tree;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import wicket.Component;
import wicket.model.AbstractReadOnlyModel;

/**
 * Replacement model that looks up whether the current row is the active one.
 * 
 * @author Eelco Hillenius
 */
public final class SelectedPathReplacementModel extends AbstractReadOnlyModel<String>
{
	private static final long serialVersionUID = 1L;

	/** the tree node. */
	private final DefaultMutableTreeNode node;

	/** The tree. */
	private final Tree tree;

	/**
	 * Construct.
	 * 
	 * @param tree
	 *            The tree
	 * 
	 * @param node
	 *            The tree node
	 */
	public SelectedPathReplacementModel(Tree tree, DefaultMutableTreeNode node)
	{
		this.node = node;
		this.tree = tree;
	}

	/**
	 * @see wicket.model.IModel#getObject(Component)
	 */
	@Override
	public String getObject(final Component component)
	{
		TreePath path = new TreePath(node.getPath());
		TreePath selectedPath = tree.getTreeState().getSelectedPath();
		if (selectedPath != null)
		{
			boolean equals = tree.equals(path, selectedPath);

			if (equals)
			{
				return "treerow-selected";
			}
		}
		return "treerow";
	}
}