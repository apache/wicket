package wicket.xtree;

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
 * A complete tree implementation where three item consists of junction link, icon and label.
 * @author Matej Knopp
 */
public class Tree extends DefaultAbstractTree {

	/**
	 * Tree constructor.
	 */
	public Tree(MarkupContainer parent, String id, TreeModel model) {
		super(parent, id, model);
	}

	/**
	 * Tree constructor.
	 */
	public Tree(MarkupContainer parent, String id, IModel<TreeModel> model) {
		super(parent, id, model);
	}

	/**
	 * Tree constructor.
	 */
	public Tree(MarkupContainer parent, String id) {
		super(parent, id);
	}

	/**
	 * Populates the tree item. It creates all necesary components for the tree to
	 * work properly.
	 */
	@Override
	protected void populateTreeItem(WebMarkupContainer<TreeNode> item, int level) 
	{
		final TreeNode node = item.getModelObject();
		
		createIndentation(item, "indent", item.getModelObject(), level);
		
		createJunctionLink(item, "link", "image", node);
		
		WebMarkupContainer nodeLink = createNodeLink(item, "nodeLink", node);
		
		createNodeIcon(nodeLink, "icon", node);
		
		new Label(nodeLink, "label", new Model<String>() {
			@Override
			public String getObject() {				
				return renderNode(node);
			}
		});
		
		// do distinguish between selected and unselected rows we add an behavior
		// that modifies row css class.
		item.add(new AbstractBehavior() {
			@Override
			public void onComponentTag(Component component, ComponentTag tag) {
				super.onComponentTag(component, tag);
				if (getTreeState().isNodeSelected(node))
					tag.put("class", "row-selected");
				else
					tag.put("class", "row");
			}
		});
	}

	/**
	 * This method is called for every node to get it's string representation.
	 */
	protected String renderNode(TreeNode node) 
	{
		return node.toString();
	}
}
