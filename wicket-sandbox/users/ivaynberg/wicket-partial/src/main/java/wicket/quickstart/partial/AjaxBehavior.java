package wicket.quickstart.partial;

import wicket.Component;
import wicket.RequestCycle;
import wicket.behavior.AjaxHandler;
import wicket.markup.ComponentTag;

public abstract class AjaxBehavior extends AjaxHandler
{

	protected final String getImplementationId()
	{
		return "wicket-default";
	}

	protected final String getAjaxCall()
	{
		return getAjaxCall(null);
	}

	protected final String getAjaxCall(String params)
	{
		return "wicketAjaxGet('"
				+ getCallbackUrl() + ((params != null) ? "&" + params : "") + "');";
	}

	public String getCssId()
	{
		Component c = getComponent();
		if (c.getMarkupAttributes().containsKey("id"))
		{
			return c.getMarkupAttributes().getString("id");
		}
		else
		{
			return c.getPageRelativePath();
		}
	}

	public void onComponentTag(ComponentTag tag)
	{
		super.onComponentTag(tag);
		tag.put("id", getCssId());
	}

	protected void respond()
	{
		// TODO PASS THE BEHAV INTO THE TARGET SO IT CAN USE getCssId() ?
		AjaxRequestTarget target = new AjaxRequestTarget();
		RequestCycle.get().setRequestTarget(target);
		respond(target);
	}

	protected abstract void respond(AjaxRequestTarget target);

}
