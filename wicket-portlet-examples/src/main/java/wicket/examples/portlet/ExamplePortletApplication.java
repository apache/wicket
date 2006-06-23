package wicket.examples.portlet;

import wicket.protocol.http.portlet.PortletApplication;

/**
 * Application class of the example.
 * 
 * @author Janne Hietam&auml;ki
 */
public class ExamplePortletApplication extends PortletApplication
{
	/**
	 * Gets the home page.
	 * 
	 * @return The home page
	 */
	public Class getHomePage()
	{
		return ExamplePortlet.class;
	}
}
