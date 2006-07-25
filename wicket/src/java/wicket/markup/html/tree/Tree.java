package wicket.markup.html.tree;

import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

import wicket.Component;
import wicket.MarkupContainer;
import wicket.behavior.AbstractBehavior;
import wicket.markup.ComponentTag;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.html.basic.Label;
import wicket.model.IModel;
import wicket.model.Model;

/**
 * A complete tree implementation where three item consists of junction link,
 * icon and label.
 * 
 * @author Matej Knopp
 */
public class Tree extends DefaultAbstractTree
{
	private static final long serialVersionUID = 1L;

	/**
	 * Tree constructor.
	 * 
	 * @param parent
	 *            The parent component
	 * @param id
	 *            The component id
	 */
	public Tree(MarkupContainer parent, String id)
	{
		super(parent, id);
	}

	/**
	 * Tree constructor.
	 * 
	 * @param parent
	 *            The parent component
	 * @param id
	 *            The component id
	 * @param model
	 *            The tree model
	 */
	public Tree(MarkupContainer parent, String id, IModel<TreeModel> model)
	{
		super(parent, id, model);
	}

	/**
	 * Tree constructor.
	 * 
	 * @param parent
	 *            The parent component
	 * @param id
	 *            The component id
	 * @param model
	 *            The tree model
	 */
	public Tree(MarkupContainer parent, String id, TreeModel model)
	{
		super(parent, id, model);
	}

	/**
	 * Populates the tree item. It creates all necesary components for the tree
	 * to work properly.
	 */
	@Override
	protected void populateTreeItem(WebMarkupContainer<TreeNode> item, int level)
	{
		final TreeNode node = item.getModelObject();

		newIndentation(item, "indent", item.getModelObject(), level);

		newJunctionLink(item, "link", "image", node);

		MarkupContainer nodeLink = newNodeLink(item, "nodeLink", node);

		newNodeIcon(nodeLink, "icon", node);

		new Label(nodeLink, "label", new Model<String>()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public String getObject()
			{
				return renderNode(node);
			}
		});

		// do distinguish between selected and unselected rows we add an
		// behavior
		// that modifies row css class.
		item.add(new AbstractBehavior()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onComponentTag(Component component, ComponentTag tag)
			{
				super.onComponentTag(component, tag);
				if (getTreeState().isNodeSelected(node))
				{
					tag.put("class", "row-selected");
				}
				else
				{
					tag.put("class", "row");
				}
			}
		});
	}

	/**
	 * This method is called for every node to get it's string representation.
	 * 
	 * @param node
	 *            The tree node to get the string representation of
	 * @return The string representation
	 */
	protected String renderNode(TreeNode node)
	{
		return node.toString();
	}
}
