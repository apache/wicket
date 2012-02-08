package org.apache.wicket.resource.filtering;

import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebPage;

class HeaderFilteringPage extends WebPage
{
	HeaderFilteringPage()
	{
		add(new HeaderResponseFilteredResponseContainer("headerJS", "headerJS"));
	}

	@Override
	public void renderHead(IHeaderResponse response)
	{
		super.renderHead(response);
		response.renderJavaScript("someJS()", "js");
	}
}