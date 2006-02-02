package wicket.ajax;

import wicket.markup.ComponentTag;
import wicket.util.string.Strings;

public abstract class AjaxEventBehavior extends AjaxBehavior
{
	private static final long serialVersionUID = 1L;

	private String event;
	
	public AjaxEventBehavior(String event) {
		if (Strings.isEmpty(event))
		{
			throw new IllegalArgumentException("argument [event] cannot be null or empty");
		}
		
		onCheckEvent(event);
		
		this.event=event;
		
	}

	protected void onComponentTag(ComponentTag tag)
	{
		super.onComponentTag(tag);
		String handler=getEventHandler();
		
		if (event.equalsIgnoreCase("href")) {
			handler="javascript:"+handler;
		}
		
		tag.put(event, handler);
	}

	protected String getEventHandler() {
		return buildAjaxCall();
	}
	
	protected void onCheckEvent(String event)
	{
		
	}
	
	protected final String getEvent() {
		return event;
	}

	protected final void respond(AjaxRequestTarget target)
	{
		onEvent(target);
	}

	protected abstract void onEvent(AjaxRequestTarget target);
}
