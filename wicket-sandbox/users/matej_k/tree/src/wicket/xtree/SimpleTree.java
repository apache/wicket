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

public class SimpleTree extends DefaultAbstractTree {

	public SimpleTree(MarkupContainer parent, String id, TreeModel model, boolean rootLess) {
		super(parent, id, model, rootLess);
	}

	public SimpleTree(MarkupContainer parent, String id, IModel<TreeModel> model, boolean rootLess) {
		super(parent, id, model, rootLess);
	}

	public SimpleTree(MarkupContainer parent, String id, boolean rootLess) {
		super(parent, id, rootLess);
	}

	@Override
	protected void populateTreeItem(WebMarkupContainer<TreeNode> item, int level) 
	{
		final TreeNode node = item.getModelObject();
		
		createIndentation(item, "indent", level);
		
		createJunctionLink(item, "link", "image", node);
		
		WebMarkupContainer nodeLink = createNodeLink(item, "nodeLink", node);
		
		new Label(nodeLink, "label", new Model<String>() {
			@Override
			public String getObject() {				
				return renderNode(node);
			}
		});
		
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

	protected String renderNode(TreeNode node) 
	{
		return node.toString();
	}
}
