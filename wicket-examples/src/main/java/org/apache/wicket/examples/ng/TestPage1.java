package org.apache.wicket.examples.ng;

import org.apache.wicket.ng.Page;
import org.apache.wicket.ng.markup.html.link.Link;
import org.apache.wicket.ng.request.cycle.RequestCycle;

public class TestPage1 extends Page
{
	private static final long serialVersionUID = 1L;

	public TestPage1()
	{
		Link l1 = new Link("l1")
		{
			private static final long serialVersionUID = 1L;

			public void onLinkClicked()
			{
				System.out.println("link 1 clicked");
				getPageParameters().setNamedParameter("p1", "v1");
				getPageParameters().setIndexedParameter(0, "indexed1");
				getPageParameters().setIndexedParameter(1, "indexed2");
				getPageParameters().setIndexedParameter(2, "indexed3");

				// necessary on stateless page
				if (getPage().isPageStateless())
					RequestCycle.get().setResponsePage(getPage());
			}
		};
		l1.setBookmarkable(isPageStateless());
		l1.setLabel("Link 1 - Add Some Parameters");
		add(l1);

		Link l2 = new Link("l2")
		{
			private static final long serialVersionUID = 1L;

			public void onLinkClicked()
			{
				System.out.println("link 2 clicked");
				getPageParameters().removeNamedParameter("p1");
				getPageParameters().clearIndexedParameters();

				if (getPage().isPageStateless())
					// necessary on stateless page
					RequestCycle.get().setResponsePage(getPage());
			}
		};
		l2.setLabel("Link 2 - Remove The Parameters   (this link is bookmarkable listener interface!)");
		l2.setBookmarkable(true);
		add(l2);


		Link l3 = new Link("l3")
		{
			private static final long serialVersionUID = 1L;

			public void onLinkClicked()
			{
				System.out.println("link 3 clicked");
				RequestCycle.get().setResponsePage(new TestPage2());
			}
		};
		// l3.setBookmarkable(true);
		l3.setLabel("Link 3 - Go to Test Page 2 - Not mounted, Not bookmarkable");
		add(l3);


		Link l4 = new Link("l4")
		{
			private static final long serialVersionUID = 1L;

			public void onLinkClicked()
			{
				System.out.println("link 4 clicked");
				RequestCycle.get().setResponsePage(TestPage2.class, null);
			}
		};
		l4.setLabel("Link 4 - Go to Test Page 2 - Not mounted, Bookmarkable");
		add(l4);


		Link l5 = new Link("l5")
		{
			private static final long serialVersionUID = 1L;

			public void onLinkClicked()
			{
				System.out.println("link 5 clicked");
				TestPage3 page = new TestPage3(TestPage1.this);
				page.getPageParameters().setIndexedParameter(0, "i1");
				page.getPageParameters().setIndexedParameter(1, "i2");
				page.getPageParameters().setIndexedParameter(2, "i3");
				RequestCycle.get().setResponsePage(page);
			}
		};
		l5.setLabel("Link 5 - Go to Test Page 3 - Mounted");
		add(l5);
	}

	private boolean rendered = false;

	@Override
	public void renderPage()
	{
		super.renderPage();
		rendered = true;
	}

	@Override
	public boolean isPageStateless()
	{
		return false;
// return !rendered;
	}
}
