package org.apache.wicket.examples.ng;

import org.apache.wicket.ng.Page;
import org.apache.wicket.ng.markup.html.form.Form;
import org.apache.wicket.ng.markup.html.link.Link;
import org.apache.wicket.ng.request.cycle.RequestCycle;

public class TestPage3 extends Page
{
	private static final long serialVersionUID = 1L;

	public TestPage3(final Page back)
	{
		Link b = new Link("back")
		{
			private static final long serialVersionUID = 1L;

			public void onLinkClicked()
			{
				RequestCycle.get().setResponsePage(back);
			}
		};
		b.setLabel("Go Back");
		add(b);

		Form form = new Form("form");
		add(form);
	}

}
