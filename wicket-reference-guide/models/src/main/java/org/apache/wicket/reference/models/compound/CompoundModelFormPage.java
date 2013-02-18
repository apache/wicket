package org.apache.wicket.reference.models.compound;

import org.apache.wicket.markup.html.WebPage;

public class CompoundModelFormPage extends WebPage
{
	public CompoundModelFormPage()
	{
		add(new CompoundModelPanel("compound"));
		add(new CompoundModelBindPanel("bind"));
	}
}
