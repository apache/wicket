package org.apache.wicket.markupdriventree;

import org.apache.wicket.Auto;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;

public class BasePage extends WebPage {

	@Auto
	WebMarkupContainer a, b, c;
	
	public BasePage() 
	{
		super();

		a = new WebMarkupContainer("a");
		b = new WebMarkupContainer("b");
		c = new WebMarkupContainer("c");
	}
}
