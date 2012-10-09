package org.apache.wicket.examples.components;

import org.apache.wicket.examples.base.AbstractBasePage;
import org.apache.wicket.examples.base.MarkdownArticleModel;
import org.apache.wicket.examples.base.components.AnchorLink;
import org.apache.wicket.examples.base.markdown.MarkdownLabel;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.repeater.RepeatingView;

public class ComponentExamplesPage extends AbstractBasePage
{
	private static final long serialVersionUID = 1L;

	private RepeatingView articles = new RepeatingView("articles");
	private RepeatingView menuitems = new RepeatingView("menuitems");

	public ComponentExamplesPage()
	{
		add(articles);
		add(menuitems);

		addArticle("Labels.md");
		addArticle("Links.md");
		addArticle("Lists.md");
		addArticle("Pagination.md");
		addArticle("Forms.md");
		addArticle("Feedback.md");
		addArticle("Internationalization.md");
		addArticle("Layout.md");
	}

	private void addArticle(String article)
	{
		String title = article.substring(0, article.length() - 3);

		MarkdownLabel markdownLabel = new MarkdownLabel(articles.newChildId(),
			new MarkdownArticleModel(ComponentExamplesPage.class, article));
		articles.add(markdownLabel);
		markdownLabel.setMarkupId(title);
		markdownLabel.setOutputMarkupId(true);

		WebMarkupContainer menuitem = new WebMarkupContainer(menuitems.newChildId());
		menuitems.add(menuitem);

		menuitem.add(new AnchorLink("menuitem", title, title));
	}

	@Override
	public void renderHead(IHeaderResponse response)
	{
		super.renderHead(response);
	}
}
