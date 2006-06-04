package wicket.extensions.ajax.markup.html.autocomplete;

import java.util.Iterator;

import wicket.IRequestTarget;
import wicket.RequestCycle;
import wicket.Response;

/**
 * This behavior builds on top of {@link AbstractAutoCompleteBehavior} by
 * introducing the concept of a {@link IAutoCompleteRenderer} to make response
 * writing easier.
 * 
 * @see IAutoCompleteRenderer
 * 
 * @since 1.2
 * 
 * @author Igor Vaynberg (ivaynberg)
 * @author Janne Hietam&auml;ki (jannehietamaki)
 */
public abstract class AutoCompleteBehavior extends AbstractAutoCompleteBehavior
{
	private static final long serialVersionUID = 1L;

	private final IAutoCompleteRenderer renderer;

	/**
	 * Constructor
	 * 
	 * @param renderer
	 *            renderer that will be used to generate output
	 */
	public AutoCompleteBehavior(IAutoCompleteRenderer renderer)
	{
		if (renderer == null)
		{
			throw new IllegalArgumentException("renderer cannot be null");
		}
		this.renderer = renderer;
	}


	@Override
	protected final void onRequest(final String val, RequestCycle requestCycle)
	{
		IRequestTarget target = new IRequestTarget()
		{

			public void respond(RequestCycle requestCycle)
			{
				Response r = requestCycle.getResponse();
				Iterator comps = getChoices(val);
				renderer.renderHeader(r);
				while (comps.hasNext())
				{
					final Object comp = comps.next();
					renderer.render(comp, r, val);
				}
				renderer.renderFooter(r);
			}

			public void detach(RequestCycle requestCycle)
			{
			}

			public Object getLock(RequestCycle requestCycle)
			{
				return requestCycle.getSession();
			}

		};
		requestCycle.setRequestTarget(target);
	}

	/**
	 * Callback method that should return an iterator over all possiblet
	 * choice objects. These objects will be passed to the renderer to generate
	 * output. Usually it is enough to return an iterator over strings.
	 * 
	 * @param input
	 *            current input
	 * @return iterator ver all possible choice objects
	 */
	protected abstract Iterator getChoices(String input);
}
