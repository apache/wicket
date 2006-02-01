package wicket.quickstart.partial.timer;

import wicket.quickstart.partial.AjaxRequestTarget;

public class AjaxSelfUpdatingTimerBehavior extends AjaxTimerBehavior
{

	public AjaxSelfUpdatingTimerBehavior(long millis)
	{
		super(millis);
	}

	protected final void onTimer(AjaxRequestTarget target)
	{
		target.addComponent(getComponent());
	}

}
