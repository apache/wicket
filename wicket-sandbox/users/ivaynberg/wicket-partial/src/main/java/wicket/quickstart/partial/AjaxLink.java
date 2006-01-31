package wicket.quickstart.partial;

import wicket.RequestCycle;
import wicket.markup.ComponentTag;
import wicket.markup.html.WebMarkupContainer;

// TODO add a generic onclick webmarkup container
public abstract class AjaxLink extends WebMarkupContainer implements IAjaxListener
{

	public AjaxLink(String id)
	{
		super(id);
	}

	protected void onComponentTag(ComponentTag tag)
	{
		super.onComponentTag(tag);
		String url = getPage().urlFor(this, IAjaxListener.class);

		// tag.put("onclick", getOnclickScript(url));
		// tag.put("href", "#");
		tag.put("href", "javascript:" + getOnclickScript(url));
	}

	protected String getOnclickScript(String url)
	{
		return "wicketAjaxGet('" + url + "');";
	}

	public void onAjaxRequest()
	{
		AjaxRequestTarget target = new AjaxRequestTarget();
		getRequestCycle().setRequestTarget(target);

		onClick(target);
	}

	protected abstract void onClick(AjaxRequestTarget target);

	static
	{
		RequestCycle.registerRequestListenerInterface(IAjaxListener.class);
	}

}
