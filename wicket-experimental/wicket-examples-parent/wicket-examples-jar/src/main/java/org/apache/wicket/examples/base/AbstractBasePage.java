package org.apache.wicket.examples.base;

import org.apache.wicket.bootstrap.Bootstrap;
import org.apache.wicket.examples.base.navbar.TopNavbar;
import org.apache.wicket.examples.base.prettify.Prettify;
import org.apache.wicket.examples.basic.BasicExamplesPage;
import org.apache.wicket.examples.components.ComponentExamplesPage;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

public abstract class AbstractBasePage extends WebPage
{
	private static final long serialVersionUID = 1L;

	@Override
	protected void onConfigure()
	{
		if (!hasBeenRendered())
		{
			TopNavbar mainmenu = new TopNavbar("topnavbar");
			mainmenu.addMenuItem("Home", HomePage.class);
			mainmenu.addMenuItem("Basic", BasicExamplesPage.class);
			mainmenu.addMenuItem("Components", ComponentExamplesPage.class);
			add(mainmenu);
		}
	}

	@Override
	public void renderHead(IHeaderResponse response)
	{
		super.renderHead(response);

		Bootstrap.renderHead(response);
		Prettify.renderHead(response);

		response.render(CssHeaderItem.forReference(new CssResourceReference(AbstractBasePage.class,
			"docs.css")));
		response.render(CssHeaderItem.forCSS("body { padding-top: 60px; padding-bottom: 40px; }",
			"custom-wicket-examples"));

		response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(
			AbstractBasePage.class, "examples.js")));

	}
}
