package org.apache.wicket.protocol.http;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.link.IPageLink;
import org.apache.wicket.markup.html.link.PageLink;

public class SecondPage extends WebPage
{

	public SecondPage(final FirstPage page)
	{
		add(new PageLink("link", new IPageLink()
		{

			private static final long serialVersionUID = 1L;

			public Page getPage()
			{
				return page;
			}

			public Class getPageIdentity()
			{
				return FirstPage.class;
			}
		}));
	}
}
