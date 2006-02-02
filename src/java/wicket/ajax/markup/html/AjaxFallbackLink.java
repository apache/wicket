package wicket.ajax.markup.html;

import wicket.ajax.AjaxEventBehavior;
import wicket.ajax.AjaxRequestTarget;
import wicket.markup.html.link.Link;
import wicket.model.IModel;

public abstract class AjaxFallbackLink extends Link
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AjaxFallbackLink(String id)
	{
		this(id, null);
	}


	public AjaxFallbackLink(String id, IModel object)
	{
		super(id, object);

		add(new AjaxEventBehavior("onclick")
		{

			private static final long serialVersionUID = 1L;

			protected void onEvent(AjaxRequestTarget target)
			{
				onClick(target);
			}
			
			protected String getEventHandler()
			{
				return super.getEventHandler()+"return false;";
			}

		});
	}

	public final void onClick()
	{
		onClick(null);
	}

	
	protected abstract void onClick(AjaxRequestTarget target);



}
