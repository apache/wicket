package wicket.ajax;

import wicket.markup.ComponentTag;


public abstract class AjaxTimerBehavior extends AjaxBehavior
{
	private long millis;

	public AjaxTimerBehavior(long millis)
	{
		this.millis = millis;
	}

	protected String getBodyOnloadContribution()
	{
		return getJsTimeoutCall(millis);
	}

	protected final String getJsTimeoutCall(long millis)
	{
		return "setTimeout(function() { " + buildAjaxCall() + " }, " + millis + ");";
	}


	protected void respond(AjaxRequestTarget target)
	{
		onTimer(target);
		target.addJavascript(getJsTimeoutCall(millis));
	}

	protected abstract void onTimer(AjaxRequestTarget target);

}
