/*
 * $Id$ $Revision:
 * 1.7 $ $Date$
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
package wicket.markup.html.tree;

import java.io.Serializable;
import java.util.Enumeration;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.VariableHeightLayoutCache;

/**
 * Holder and handler for tree state.
 * 
 * @author Eelco Hillenius
 */
public final class TreeState extends VariableHeightLayoutCache
	implements Serializable, TreeModelListener
{
	/** currently selected path. */
	private TreePath selectedPath;

	/**
	 * Expands the selected path and set selection to currently selected path.
	 * @param selection the new selection.
	 */
	public void setSelectedPath(TreePath selection)
	{
		setExpandedState(selection, true);
		this.selectedPath = selection;
	}

	/**
	 * Gets the currently selected path.
	 * @return the currently selected path
	 */
	public TreePath getSelectedPath()
	{
		if ((selectedPath == null) && isRootVisible())
		{
			selectedPath = new TreePath(getModel().getRoot());
		}

		return selectedPath;
	}

	/**
	 * Returns an <code>Enumerator</code> that increments over the visible
	 * paths starting at the root. The ordering of the enumeration is based on
	 * how the paths are displayed.
	 * @return an <code>Enumerator</code> that increments over the visible
	 *         paths
	 */
	public Enumeration getVisiblePathsFromRoot()
	{
		TreeNode root = (TreeNode)(getModel().getRoot());
		TreePath rootPath = new TreePath(root);

		return getVisiblePathsFrom(rootPath);
	}
}
