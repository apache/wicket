package wicket.extensions.ajax.markup.html.autocomplete.capxous;

import java.util.Iterator;

import wicket.IRequestTarget;
import wicket.RequestCycle;
import wicket.Response;

public abstract class StringResponseAutoAssistBehavior extends AbstractAutoAssistBehavior
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected void onRequest(final String val, RequestCycle requestCycle)
	{
		final Response response=requestCycle.getResponse();
		
		final IRequestTarget target=new IRequestTarget() {

			public void respond(RequestCycle requestCycle)
			{
				final Response r=requestCycle.getResponse();
				final Iterator it=getCompletionsForPrefix(val);
				while (it.hasNext()) {
					
					final String option=it.next().toString();
					
					r.write("<div onSelect=\"this.txtBox.value='");
					r.write(option);
					r.write("';\")>");
					r.write(option);
					r.write("</div>");
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
	
	protected abstract Iterator getCompletionsForPrefix(String prefix);

	
}
