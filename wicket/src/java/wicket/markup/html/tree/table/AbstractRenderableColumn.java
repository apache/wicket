package wicket.markup.html.tree.table;

import javax.swing.tree.TreeNode;

import wicket.Component;
import wicket.MarkupContainer;
import wicket.Response;
import wicket.util.string.Strings;

/**
 * Convenience class for creating non-interactive lightweight (IRenderable based)
 * columns.
 * 
 * @author Matej Knopp
 */
public abstract class AbstractRenderableColumn extends AbstractColumn {
	
	private boolean escapeContent = true;
	private boolean contentAsTooltip = false;

	/**
	 * Creates the column
	 * 
	 * @param location
	 *			Specifies how the column should be aligned and what his size should be 			
	 * 
	 * @param header
	 * 			Header caption
	 */
	public AbstractRenderableColumn(ColumnLocation location, String header) 
	{
		super(location, header);
	}

	/**
	 * @see IColumn#createCell(TreeTable, TreeNode, int)
	 */
	public IRenderable createCell(TreeNode node, int level) 
	{
		return new IRenderable() 
		{
			public void render(TreeNode node, Response response) 
			{				
				String content = getNodeValue(node);
				
				// escape if necessary
				if (isEscapeContent())
				{
					content = Strings.escapeMarkup(content).toString();
				}
				
				response.write("<span class=\"text\"");
				if (isContentAsTooltip())
				{
					response.write(" title=\"" + content + "\"");
				}
				response.write(">");
				response.write(content);
				response.write("</span>");
			}			
		};
	}
	
	/**
	 * @see IColumn#createCell(MarkupContainer, String, TreeNode, int)
	 */
	public Component createCell(MarkupContainer<?> parent, String id, TreeNode node, int level) 
	{
		return null;
	}

	/**
	 * Returns the string value for this column.
	 * 
	 * @param node
	 * 			Determines the position in tree
	 * 
	 */
	public abstract String getNodeValue(TreeNode node);
	
	/**
	 * Sets whether the special html characters of content should be escaped. 
	 */
	public void setEscapeContent(boolean escapeContent) 
	{
		this.escapeContent = escapeContent;
	}

	/**
	 * Returns whether the special html characters of content will be escaped.
	 */
	public boolean isEscapeContent() 
	{
		return escapeContent;
	}

	/**
	 * Sets whether the content should also be visible as tooltip (html title attribute)
	 * of the cell.
	 */
	public void setContentAsTooltip(boolean contentAsTooltip) {
		this.contentAsTooltip = contentAsTooltip;
	}

	/**
	 * Returns whether the content should also be visible as tooltip of the cell.
	 */
	public boolean isContentAsTooltip() {
		return contentAsTooltip;
	}
}
