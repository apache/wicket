package wicket.ajax;

import wicket.markup.ComponentTag;


public class AjaxSelfUpdatingTimerBehavior extends AjaxTimerBehavior
{
	private static final long serialVersionUID = 1L;

	public AjaxSelfUpdatingTimerBehavior(long millis)
	{
		super(millis);
	}

	protected void onComponentTag(ComponentTag tag)
	{
		// make sure this component is render with an id so its markup can be
		// found and replaced
		super.onComponentTag(tag);
		if (!tag.getAttributes().containsKey("id"))
		{
			tag.put("id", getComponent().getMarkupId());
		}
	}

	protected final void onTimer(AjaxRequestTarget target)
	{
		target.addComponent(getComponent());
	}


}
