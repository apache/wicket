package org.apache.wicket.examples.base;

import org.apache.wicket.bootstrap.Bootstrap;
import org.apache.wicket.examples.base.annot.ExamplePage;
import org.apache.wicket.examples.base.navbar.TopNavbar;
import org.apache.wicket.examples.base.prettify.Prettify;
import org.apache.wicket.examples.basic.BasicExamplesPage;
import org.apache.wicket.examples.components.ComponentExamplesPage;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.TransparentWebMarkupContainer;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

public abstract class AbstractBasePage extends WebPage
{
	private static final long serialVersionUID = 1L;

	private final WebMarkupContainer body;

	public AbstractBasePage()
	{
		add(body = new TransparentWebMarkupContainer("body"));
	}

	protected WebMarkupContainer getBody()
	{
		return body;
	}

	@Override
	protected void onInitialize()
	{
		super.onInitialize();
		add(new Label("title", PropertyModel.of(this, "title")).setEscapeModelStrings(false));
	}

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

		Bootstrap.renderHeadResponsive(response);
		Prettify.renderHead(response);

		response.render(CssHeaderItem.forReference(new CssResourceReference(AbstractBasePage.class,
			"docs.css")));

		response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(
			AbstractBasePage.class, "examples.js")));
	}

	/**
	 * Gets the title for rendering in the {@code head} section of the page using the
	 * {@link ExamplePage} annotation.
	 * 
	 * @return the title tag value
	 */
	public String getTitle()
	{
		ExamplePage examplePage = getClass().getAnnotation(ExamplePage.class);
		if (examplePage != null)
			return examplePage.title() + " &mdash; Apache Wicket Examples";
		return "Apache Wicket Examples";
	}
}
