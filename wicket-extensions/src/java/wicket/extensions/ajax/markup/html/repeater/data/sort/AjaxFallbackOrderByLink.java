package wicket.extensions.ajax.markup.html.repeater.data.sort;

import wicket.ajax.AjaxEventBehavior;
import wicket.ajax.AjaxRequestTarget;
import wicket.ajax.IAjaxCallDecorator;
import wicket.ajax.calldecorator.CancelEventIfNoAjaxDecorator;
import wicket.extensions.markup.html.repeater.data.sort.ISortStateLocator;
import wicket.extensions.markup.html.repeater.data.sort.OrderByLink;

public abstract class AjaxFallbackOrderByLink extends OrderByLink
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AjaxFallbackOrderByLink(String id, String property, ISortStateLocator stateLocator,
			ICssProvider cssProvider)
	{
		this(id, property, stateLocator, cssProvider, null);
	}

	public AjaxFallbackOrderByLink(String id, String property, ISortStateLocator stateLocator)
	{
		this(id, property, stateLocator, DefaultCssProvider.getInstance(), null);
	}


	public AjaxFallbackOrderByLink(String id, String property, ISortStateLocator stateLocator,
			final IAjaxCallDecorator decorator)
	{
		this(id, property, stateLocator, DefaultCssProvider.getInstance(), decorator);
	}

	public AjaxFallbackOrderByLink(String id, String property, ISortStateLocator stateLocator,
			ICssProvider cssProvider, final IAjaxCallDecorator decorator)
	{
		super(id, property, stateLocator, cssProvider);

		add(new AjaxEventBehavior("onclick")
		{
			private static final long serialVersionUID = 1L;

			protected void onEvent(AjaxRequestTarget target)
			{
				onClick();
				onAjaxClick(target);
			}

			protected IAjaxCallDecorator getAjaxCallDecorator()
			{
				return new CancelEventIfNoAjaxDecorator(decorator);
			}

		});

	}

	protected abstract void onAjaxClick(AjaxRequestTarget target);


}
