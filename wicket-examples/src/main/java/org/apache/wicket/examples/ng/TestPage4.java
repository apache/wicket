package org.apache.wicket.examples.ng;

import org.apache.wicket.ng.Page;
import org.apache.wicket.ng.markup.html.Label;
import org.apache.wicket.ng.request.component.PageParameters;

public class TestPage4 extends Page
{

	private static final long serialVersionUID = 1L;

	public TestPage4(PageParameters parameters)
	{
		super(parameters);

		add(new Label("label", parameters.getNamedParameter("color").toString("empty")));


	}

	/**
	 * @see org.apache.wicket.ng.Page#isPageStateless()
	 */
	@Override
	public boolean isPageStateless()
	{
		return true;
	}
}
