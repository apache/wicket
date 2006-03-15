package wicket.extensions.ajax.markup.html;

import wicket.Component;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.html.border.Border;
import wicket.model.Model;

public class AjaxCollapsableBorder extends Border
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final WebMarkupContainer header;

	public AjaxCollapsableBorder(String id)
	{
		super(id, new Model());
		setCollapsed(true);
		setTransparentResolver(true);
		header = new WebMarkupContainer("header");
		add(header);
	}

	protected void onBeginRequest()
	{
		visitChildren(new IVisitor()
		{

			public Object component(Component component)
			{
				System.out.println(component.getId());
				if (component != header||true)
				{
					
					component.setVisible(!isCollapsed());
				}
				return IVisitor.CONTINUE_TRAVERSAL_BUT_DONT_GO_DEEPER;
			}


		});
	}

	public final void setCollapsed(boolean collapsed)
	{

		setModelObject(Boolean.TRUE);
	}


	public final boolean isCollapsed()
	{
		return Boolean.TRUE.equals(getModelObject());
	}


}
