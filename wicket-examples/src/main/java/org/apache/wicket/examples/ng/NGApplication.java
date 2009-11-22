package org.apache.wicket.examples.ng;

import org.apache.wicket.ng.markup.html.link.ILinkListener;
import org.apache.wicket.ng.protocol.http.WebApplication;
import org.apache.wicket.ng.request.component.RequestablePage;
import org.apache.wicket.ng.request.listener.RequestListenerInterface;
import org.apache.wicket.ng.request.mapper.MountedMapper;

public class NGApplication extends WebApplication
{

	public NGApplication()
	{
		super();
	}

	@Override
	public void init()
	{
		mount(new MountedMapper("first-test-page", TestPage1.class));
		mount(new MountedMapper("third-test-page", TestPage3.class));
		mount(new MountedMapper("/page4/${color}/display", TestPage4.class));

		// load the interface
		RequestListenerInterface i = ILinkListener.INTERFACE;
	}

	@Override
	public Class<? extends RequestablePage> getHomePage()
	{
		return TestPage1.class;
	}

}
