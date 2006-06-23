package wicket.examples.portlet;

import wicket.Page;
import wicket.protocol.http.portlet.PortletApplication;


/**
 * @author Janne Hietam&auml;ki
 *
 */
public class ExamplePortletApplication extends PortletApplication 
{

	/*
	 * 
	 * @see wicket.Application#getHomePage()
	 */
	@Override
	public Class<? extends Page> getHomePage()
	{
		return ExamplePortlet.class;
	}
}
