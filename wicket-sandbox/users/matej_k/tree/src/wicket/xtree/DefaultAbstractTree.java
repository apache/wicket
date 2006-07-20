package wicket.xtree;

import java.io.Serializable;

import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

import wicket.MarkupContainer;
import wicket.RequestCycle;
import wicket.ResourceReference;
import wicket.Response;
import wicket.ajax.AjaxRequestTarget;
import wicket.ajax.markup.html.AjaxFallbackLink;
import wicket.ajax.markup.html.AjaxLink;
import wicket.behavior.HeaderContributor;
import wicket.markup.ComponentTag;
import wicket.markup.MarkupStream;
import wicket.markup.html.PackageResourceReference;
import wicket.markup.html.WebComponent;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.html.image.Image;
import wicket.markup.html.link.Link;
import wicket.model.IModel;
import wicket.model.Model;

public abstract class DefaultAbstractTree extends AbstractTree {

	/** Reference to the css file. */
	private static final PackageResourceReference CSS = 
		new PackageResourceReference(DefaultAbstractTree.class, "tree.css");

	private static final PackageResourceReference ITEM = 
		new PackageResourceReference(DefaultAbstractTree.class, "item.gif");
	
	private static final PackageResourceReference FOLDER_OPEN = 
		new PackageResourceReference(DefaultAbstractTree.class, "folder-open.gif");
	
	private static final PackageResourceReference FOLDER_CLOSED = 
		new PackageResourceReference(DefaultAbstractTree.class, "folder-closed.gif");
	
	public DefaultAbstractTree(MarkupContainer parent, String id, TreeModel model, boolean rootLess) 
	{
		super(parent, id, new Model<TreeModel>(model), rootLess);
		init();
	}

	public DefaultAbstractTree(MarkupContainer parent, String id, IModel<TreeModel> model, boolean rootLess) 
	{
		super(parent, id, model, rootLess);
		init();
	}

	
	public DefaultAbstractTree(MarkupContainer parent, String id, boolean rootLess) 
	{		
		super(parent, id, rootLess);
		init();
	}
	
	private void init() 
	{
		PackageResourceReference css = getCSS();
		add(HeaderContributor.forCss(css.getScope(), css.getName()));
	}
		
	public enum LinkType 
	{
		REGULAR,
		AJAX,
		AJAX_FALLBACK
	};
	
	private LinkType linkType = LinkType.AJAX;
	
	public void setLinkType(LinkType linkType) 
	{
		if (this.linkType != linkType) 
		{
			this.linkType = linkType;
			invalidateAll();
		}
	}
		
	public LinkType getLinkType() 
	{
		return linkType;
	}
	
	protected interface LinkCallback extends Serializable 
	{
		public void onClick(AjaxRequestTarget target);
	};

	protected WebMarkupContainer createLink(MarkupContainer parent, String id, final LinkCallback callback) 
	{
		if (getLinkType() == LinkType.REGULAR)
		{
			return new Link(parent, id) 
			{
				@Override
				public void onClick() {
					callback.onClick(null);
				}
			};
		}
		else if (getLinkType() == LinkType.AJAX)
		{
			return new AjaxLink(parent, id)
			{
				@Override
				public void onClick(AjaxRequestTarget target) {
					callback.onClick(target);
				}
			};
		}
		else
		{
			return new AjaxFallbackLink(parent, id)
			{
				@Override
				public void onClick(AjaxRequestTarget target) {
					callback.onClick(target);
				}
			};
		}
	}
	
	protected WebComponent createNodeIcon(MarkupContainer parent, String id, final TreeNode node)
	{
		return new Image(parent, id)
		{
			@Override
			protected ResourceReference getImageResourceReference() {
				return getNodeIcon(node);
			}
		};
	}
	
	protected ResourceReference getNodeIcon(TreeNode node)
	{
		if (node.isLeaf() == true)
		{
			return getItem();
		}
		else
		{
			if (isNodeExpanded(node))
				return getFolderOpen();
			else
				return getFolderClosed();
		}
	}
	
	protected void createJunctionLink(MarkupContainer parent, final String id, 
			                          final String imageId, final TreeNode node)
	{
		final MarkupContainer junctionLink;

		if (node.isLeaf() == false)
		{
			junctionLink = createLink(parent, id, new LinkCallback() 
			{
				public void onClick(AjaxRequestTarget target)
				{					
					if (isNodeExpanded(node))
					{
						getTreeState().collapseNode(node);
					}
					else
					{
						getTreeState().expandNode(node);
					}
					updateTree(target);					
				}
			});
		}		
		else
		{
			junctionLink = new WebMarkupContainer(parent, id)
			{
				@Override
				protected void onComponentTag(ComponentTag tag) {
					super.onComponentTag(tag);
					tag.put("onclick", "return false");
				}
			};
		}
			
		if (imageId != null)
			createJunctionImage(junctionLink, imageId, node);
	}
	
	/**
	 * Returns whether node is last child of it's parent.
	 * @return
	 */
	private boolean isNodeLast(TreeNode node) {
		TreeNode parent = node.getParent();
		if (parent == null)
		{
			return true;
		}
		else
		{
			return parent.getChildAt(parent.getChildCount() - 1).equals(node);
		}
	}
	
	protected WebMarkupContainer createJunctionImage(MarkupContainer parent, final String id, final TreeNode node)
	{		
		return (WebMarkupContainer) new WebMarkupContainer(parent, id) 
		{			
			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);
			
				final String cssClassInner;
				if (node.isLeaf() == false)
				{
					cssClassInner = isNodeExpanded(node) ? "minus" : "plus";							
				}
				else
				{
					cssClassInner = "corner"; 
				}
				
				final String cssClassOuter = isNodeLast(node) ? "junction-last" : "junction"; 				
				
				Response response = RequestCycle.get().getResponse();
				response.write("<span class=\""+cssClassOuter+"\"><span class=\""+cssClassInner+"\"></span></span>");
			}
		}.setRenderBodyOnly(true);
	}
	
	protected MarkupContainer createIndentation(MarkupContainer parent, String id, final TreeNode node, final int level) 
	{
		WebMarkupContainer result = new WebMarkupContainer(parent, id) {
			@Override
			protected void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag) {
				Response response = RequestCycle.get().getResponse();
				TreeNode parent = node.getParent();
				
				CharSequence urls[] = new CharSequence[level];				
				for (int i = 0; i < level; ++i) 
				{
					if (isNodeLast(parent))
						urls[i] = "indent-blank";
					else
						urls[i] = "indent-line"; 
					
					parent = parent.getParent();
				}
				
				for (int i = level - 1; i >= 0; --i)
				{
					response.write("<span class=\""+urls[i]+"\"></span>");

				}
			}
		};
		result.setRenderBodyOnly(true);
		return result;
	}
	
	protected WebMarkupContainer createNodeLink(MarkupContainer parent, String id, final TreeNode node)
	{
		return createLink(parent, id, new LinkCallback() 
		{
			public void onClick(AjaxRequestTarget target) {
				getTreeState().selectNode(node, !getTreeState().isNodeSelected(node));
				updateTree(target);
			}
		});
	}
		
	protected PackageResourceReference getCSS() 
	{
		return CSS;
	}	
	
	protected ResourceReference getItem()
	{
		return ITEM;
	}
	
	protected ResourceReference getFolderOpen()
	{
		return FOLDER_OPEN;
	}
	
	protected ResourceReference getFolderClosed() 
	{
		return FOLDER_CLOSED;
	}
}
