package wicket.quickstart.partial;

import wicket.RequestCycle;
import wicket.markup.ComponentTag;
import wicket.markup.html.WebMarkupContainer;

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

		tag.put("onclick", getOnclickScript(url));
		tag.put("href", "#");
	}

	protected String getOnclickScript(String url)
	{
		return "wicketAjaxGet('" + url + "');";
	}

	static
	{
		RequestCycle.registerRequestListenerInterface(IAjaxListener.class);
	}

}
