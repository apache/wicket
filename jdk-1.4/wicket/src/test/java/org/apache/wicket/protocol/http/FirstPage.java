package org.apache.wicket.protocol.http;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.link.IPageLink;
import org.apache.wicket.markup.html.link.PageLink;

public class FirstPage extends WebPage
{

	public FirstPage()
	{
		add(new PageLink("link", new IPageLink()
		{

			private static final long serialVersionUID = 1L;

			SecondPage page = new SecondPage(FirstPage.this);

			public Page getPage()
			{
				return page;
			}

			public Class getPageIdentity()
			{
				return SecondPage.class;
			}
		}));
	}
}
