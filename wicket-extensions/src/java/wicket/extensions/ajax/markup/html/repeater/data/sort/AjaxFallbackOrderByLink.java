package wicket.extensions.ajax.markup.html.repeater.data.sort;

import wicket.ajax.AjaxEventBehavior;
import wicket.ajax.AjaxRequestTarget;
import wicket.ajax.IAjaxCallDecorator;
import wicket.ajax.calldecorator.CancelEventIfNoAjaxDecorator;
import wicket.extensions.markup.html.repeater.data.sort.ISortStateLocator;
import wicket.extensions.markup.html.repeater.data.sort.OrderByLink;
import wicket.markup.html.WebMarkupContainer;

/**
 * Ajaxified {@link OrderByLink}
 * 
 * @see OrderByLink
 * 
 * @since 1.2.1
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public abstract class AjaxFallbackOrderByLink extends OrderByLink
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 * 
	 * @param parent
	 * @param id
	 * @param property
	 * @param stateLocator
	 * @param cssProvider
	 */
	public AjaxFallbackOrderByLink(WebMarkupContainer parent, String id, String property,
			ISortStateLocator stateLocator, ICssProvider cssProvider)
	{
		this(parent, id, property, stateLocator, cssProvider, null);
	}

	/**
	 * Constructor
	 * 
	 * @param parent
	 * @param id
	 * @param property
	 * @param stateLocator
	 */
	public AjaxFallbackOrderByLink(WebMarkupContainer parent, String id, String property,
			ISortStateLocator stateLocator)
	{
		this(parent, id, property, stateLocator, DefaultCssProvider.getInstance(), null);
	}


	/**
	 * Constructor
	 * 
	 * @param parent
	 * @param id
	 * @param property
	 * @param stateLocator
	 * @param decorator
	 */
	public AjaxFallbackOrderByLink(WebMarkupContainer parent, String id, String property,
			ISortStateLocator stateLocator, final IAjaxCallDecorator decorator)
	{
		this(parent, id, property, stateLocator, DefaultCssProvider.getInstance(), decorator);
	}

	/**
	 * Constructor
	 * 
	 * @param parent
	 * @param id
	 * @param property
	 * @param stateLocator
	 * @param cssProvider
	 * @param decorator
	 */
	public AjaxFallbackOrderByLink(WebMarkupContainer parent, String id, String property,
			ISortStateLocator stateLocator, ICssProvider cssProvider,
			final IAjaxCallDecorator decorator)
	{
		super(parent, id, property, stateLocator, cssProvider);

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

	/**
	 * Callback method when an ajax click occurs. All the behavior of changing
	 * the sort, etc is already performed bfore this is called so this method
	 * should primarily be used to configure the target.
	 * 
	 * @param target
	 */
	protected abstract void onAjaxClick(AjaxRequestTarget target);


}
