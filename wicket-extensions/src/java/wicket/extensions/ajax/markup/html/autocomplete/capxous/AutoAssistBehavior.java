package wicket.extensions.ajax.markup.html.autocomplete.capxous;

import java.io.OutputStream;
import java.util.Iterator;

import wicket.IRequestTarget;
import wicket.RequestCycle;
import wicket.Response;

public abstract class AutoAssistBehavior extends AbstractAutoAssistBehavior
{

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
				Iterator completions=getCompletionsForPrefix(val);
				while (completions.hasNext()) {
					final Object completion=completions.next();
					final String text=getCompletionText(completion);
					
					r.write("<div onSelect=\"this.txtBox.value='");
					r.write(text);
					r.write("';\")>");
					renderCompletion(completion, r);
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
	
	}
	
	protected abstract Iterator getCompletionsForPrefix(String prefix);
	
	protected abstract String getCompletionText(Object o);
	
	protected abstract void renderCompletion(Object o, Response r);

}
