package wicket.quickstart.partial;

import wicket.Response;
import wicket.markup.MarkupStream;

public abstract class IndicatingAjaxLink extends AjaxLink
{

	public IndicatingAjaxLink(String id)
	{
		super(id);
	}

	private String getIndicatorId() {
		return getPageRelativePath()+"-indicator";
	}

	protected String getOnclickScript(String url)
	{
		return "wicketShow('"+getIndicatorId()+"'); wicketAjaxGet('" + url + "', function() { wicketHide('"+getIndicatorId()+"'); });";
	}
	
	protected void onRender(MarkupStream markupStream)
	{
		super.onRender(markupStream);
		Response r=getResponse();
		r.write("<span id=\"");
		r.write(getIndicatorId());
		r.write("\" style=\"display:none;\">");
		r.write("<img src=\"indicator.gif\"/>");
		r.write("</span>");
	}
}
