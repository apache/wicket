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
import wicket.markup.html.PackageResourceReference;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.html.image.Image;
import wicket.markup.html.link.Link;
import wicket.markup.html.tree.Tree;
import wicket.model.IModel;
import wicket.model.Model;

public abstract class DefaultAbstractTree extends AbstractTree {

	/** Reference to the css file. */
	private static final PackageResourceReference CSS = 
		new PackageResourceReference(DefaultAbstractTree.class, "tree.css");

	/** Minus sign image. */
	private static final ResourceReference MINUS = 
		new PackageResourceReference(DefaultAbstractTree.class, "minus.gif");

	/** Plus sign image. */
	private static final ResourceReference PLUS = 
		new PackageResourceReference(DefaultAbstractTree.class, "plus.gif");
	
	/** Blank image. */
	private static final ResourceReference BLANK = 
		new PackageResourceReference(Tree.class, "blank.gif");
	
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
	
	
	protected Image createJunctionImage(MarkupContainer parent, final String id, final TreeNode node)
	{		
		if (!node.isLeaf())
		{
			// we want the image to be dynamically, yet resolving to a static
			// image.
			return new Image(parent, id)
			{
				private static final long serialVersionUID = 1L;

				@Override
				protected ResourceReference getImageResourceReference()
				{
					if (isNodeExpanded(node))
					{
						return MINUS;
					}
					else
					{
						return PLUS;
					}
				}
			};
		}
		else
		{
			return new Image(parent, id, getBlank());
		}
	}
	
	protected MarkupContainer createIndentation(MarkupContainer parent, String id, final int level) 
	{
		WebMarkupContainer result = new WebMarkupContainer(parent, id) {
			@Override
			protected void onComponentTag(ComponentTag tag) 
			{
				super.onComponentTag(tag);
				Response response = RequestCycle.get().getResponse();
				for (int i = 0; i < level; ++i) 
				{
					response.write(getIndentationString());
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
	
	protected String getIndentationString() {
		return "&nbsp;&nbsp;&nbsp;";
	}
	
	protected PackageResourceReference getCSS() 
	{
		return CSS;
	}
	
	protected ResourceReference getMinus() 
	{
		return MINUS;
	}
	
	protected ResourceReference getPlus() 
	{
		return PLUS;
	}
	
	protected ResourceReference getBlank() 
	{
		return BLANK;
	}
}
