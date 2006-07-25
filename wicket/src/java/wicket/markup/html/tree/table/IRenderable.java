package wicket.markup.html.tree.table;

import java.io.Serializable;

import javax.swing.tree.TreeNode;

import wicket.Response;

/**
 * Interface for lightweight cell renderes. If you are concerned about server
 * state size, have larget trees with read-only cells, implementing this
 * interface and using it instead of e.g. Label can decrease the memory
 * footprint of tree table.
 * 
 * @author Matej Knopp
 */
public interface IRenderable extends Serializable
{

	/**
	 * Renders the content of the cell to the response.
	 * 
	 * @param node
	 *            The node for the row. Will be null for header
	 * 
	 * @param response
	 *            Response where the renderer is supposed to write the content.
	 */
	public void render(TreeNode node, Response response);

}
