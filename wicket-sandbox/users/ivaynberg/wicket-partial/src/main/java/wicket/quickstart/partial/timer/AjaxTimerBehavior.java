package wicket.quickstart.partial.timer;

import wicket.RequestCycle;
import wicket.Response;
import wicket.behavior.AjaxHandler;
import wicket.markup.html.internal.HtmlHeaderContainer;
import wicket.quickstart.partial.AjaxBehavior;
import wicket.quickstart.partial.AjaxRequestTarget;

public abstract class AjaxTimerBehavior extends AjaxBehavior
{
	private long millis;

	public AjaxTimerBehavior(long millis)
	{
		this.millis = millis;
	}

	protected void renderHeadContribution(HtmlHeaderContainer container)
	{
		Response r = container.getResponse();
		r.write("<script>");
		r.write(getAjaxTimeoutCall(millis));
		r.write("</script>");
	}

	protected final String getAjaxTimeoutCall(long millis)
	{
		return "setTimeout(function() { " + getAjaxCall() + " }, " + millis + ");";
	}


	protected void respond(AjaxRequestTarget target)
	{
		onTimer(target);
		target.addJavascript(getAjaxTimeoutCall(millis));
	}

	protected abstract void onTimer(AjaxRequestTarget target);

}
