package wicket.ajax.markup.html;

import wicket.ajax.AjaxEventBehavior;
import wicket.ajax.AjaxRequestTarget;
import wicket.markup.html.WebMarkupContainer;
import wicket.model.IModel;

public abstract class AjaxLink extends WebMarkupContainer
{
	private static final long serialVersionUID = 1L;


	public AjaxLink(String id)
	{
		this(id, null);
	}

	public AjaxLink(String id, IModel model)
	{
		super(id, model);

		add(new AjaxEventBehavior("href")
		{

			private static final long serialVersionUID = 1L;

			protected void onEvent(AjaxRequestTarget target)
			{
				onClick(target);
			}

		});
	}

	protected abstract void onClick(AjaxRequestTarget target);

}
