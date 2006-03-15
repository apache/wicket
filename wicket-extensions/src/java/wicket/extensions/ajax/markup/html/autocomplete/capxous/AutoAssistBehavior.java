package wicket.extensions.ajax.markup.html.autocomplete.capxous;

import java.util.Iterator;

import wicket.IRequestTarget;
import wicket.RequestCycle;
import wicket.Response;

/**
 * This behavior builds on top of {@link AbstractAutoAssistBehavior} by
 * introducing the concept of a {@link IAutoAssistRenderer} to make response
 * writing easier.
 * 
 * @see IAutoAssistRenderer
 * 
 * @since 1.2
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public abstract class AutoAssistBehavior extends AbstractAutoAssistBehavior
{
	private static final long serialVersionUID = 1L;

	private final IAutoAssistRenderer renderer;

	/**
	 * Constructor
	 * 
	 * @param renderer
	 *            renderer that will be used to generate output
	 */
	public AutoAssistBehavior(IAutoAssistRenderer renderer)
	{
		if (renderer == null)
		{
			throw new IllegalArgumentException("renderer cannot be null");
		}
		this.renderer = renderer;
	}


	protected final void onRequest(final String val, RequestCycle requestCycle)
	{
		IRequestTarget target = new IRequestTarget()
		{

			public void respond(RequestCycle requestCycle)
			{
				Response r = requestCycle.getResponse();
				Iterator comps = getAssists(val);
				while (comps.hasNext())
				{
					final Object comp = comps.next();
					renderer.render(comp, r);
				}
			}

			public void cleanUp(RequestCycle requestCycle)
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
	 * Callback method that should return an iterator over all possible assist
	 * choice objects. These objects will be passed to the renderer to generate
	 * output. Usually it is enough to return an iterator over strings.
	 * 
	 * @param input
	 *            current input
	 * @return iterator ver all possible assist choice objects
	 */
	protected abstract Iterator getAssists(String input);
}
