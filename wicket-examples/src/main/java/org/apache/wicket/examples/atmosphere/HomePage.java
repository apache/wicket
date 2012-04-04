package org.apache.wicket.examples.atmosphere;

import java.util.Date;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.atmosphere.Subscribe;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class HomePage extends WebPage
{
	private static final long serialVersionUID = 1L;

	private Component timeLabel;

	public HomePage(final PageParameters parameters)
	{
		add(new Label("version", getApplication().getFrameworkSettings().getVersion()));
		add(timeLabel = new Label("time", Model.of("start")).setOutputMarkupId(true));
		setVersioned(false);
	}

	@Subscribe
	public void test(AjaxRequestTarget target, Date event)
	{
		timeLabel.setDefaultModelObject(event.toString());
		target.add(timeLabel);
	}
}
