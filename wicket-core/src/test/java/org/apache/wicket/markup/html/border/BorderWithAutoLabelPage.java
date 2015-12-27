package org.apache.wicket.markup.html.border;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.TextField;

public class BorderWithAutoLabelPage extends WebPage
{
	@Override
	protected void onInitialize()
	{
		super.onInitialize();
		BorderComponent1 component1 = new BorderComponent1("border");
		WebMarkupContainer container = new WebMarkupContainer("container");
		
		component1.add(new TextField<>("text"));
		container.add(component1);
		
		add(container);
	}

}
