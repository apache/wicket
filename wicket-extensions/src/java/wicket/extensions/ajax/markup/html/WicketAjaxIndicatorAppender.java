package wicket.extensions.ajax.markup.html;

import wicket.Component;
import wicket.RequestCycle;
import wicket.Response;
import wicket.ajax.AbstractDefaultAjaxBehavior;
import wicket.ajax.IAjaxIndicatorAware;
import wicket.behavior.AbstractBehavior;

/**
 * A behavior that adds a span with wicket's default indicator gif to the end of
 * the component's markup. This span can be used as an ajax busy indicator. For
 * an example usage see {@link IndicatingAjaxLink}
 * <p>
 * Instances of this behavior must not be shared between components.
 * 
 * @see IndicatingAjaxLink
 * @see IAjaxIndicatorAware
 * 
 * @since 1.2
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public class WicketAjaxIndicatorAppender extends AbstractBehavior
{
	/**
	 * Component instance this behavior is bound to
	 */
	private Component component;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 */
	public WicketAjaxIndicatorAppender()
	{

	}

	/**
	 * @see wicket.behavior.AbstractBehavior#onRendered(wicket.Component)
	 */
	public void onRendered(Component component)
	{
		final Response r = component.getResponse();
		r.write("<span style=\"display:none;\" class=\"");
		r.write(getSpanClass());
		r.write("\" ");
		r.write("id=\"");
		r.write(getMarkupId());
		r.write("\">");
		r.write("<img src=\"");
		r.write(getIndicatorUrl());
		r.write("\"/></span>");
	}

	/**
	 * @return url of the animated indicator image
	 */
	protected CharSequence getIndicatorUrl()
	{
		return RequestCycle.get().urlFor(AbstractDefaultAjaxBehavior.INDICATOR);
	}

	/**
	 * @return css class name of the generated outer span
	 */
	protected String getSpanClass()
	{
		return "wicket-ajax-indicator";
	}

	/**
	 * Returns the markup id attribute of the outer most span of this indicator.
	 * This is the id of the span that should be hidden or show to hide or show
	 * the indicator.
	 * 
	 * @return markup id of outer most span
	 */
	public String getMarkupId()
	{
		return component.getMarkupId() + "--ajax-indicator";
	}

	/**
	 * @see wicket.behavior.AbstractBehavior#bind(wicket.Component)
	 */
	public final void bind(Component component)
	{
		this.component = component;
	}

}
