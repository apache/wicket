package wicket.extensions.ajax.markup.html.autocomplete.capxous;

import java.util.Iterator;

import wicket.Component;
import wicket.IRequestTarget;
import wicket.RequestCycle;
import wicket.Response;

public abstract class AutoAssistBehavior extends AbstractAutoAssistBehavior
{
	private final IAutoAssistRenderer renderer;
	
	public AutoAssistBehavior(IAutoAssistRenderer renderer) {
		if (renderer==null) {
			throw new IllegalArgumentException("renderer cannot be null");
		}
		this.renderer=renderer;
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected void onRequest(final String val, RequestCycle requestCycle)
	{
		IRequestTarget target=new IRequestTarget() {

			public void respond(RequestCycle requestCycle)
			{
				Response r=requestCycle.getResponse();
				Iterator comps=getCompletions(val);
				while (comps.hasNext()) {
					final Object comp=comps.next();
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

	protected abstract Iterator getCompletions(String input);
}
