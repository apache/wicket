package wicket.examples.ajax.prototype;

import wicket.examples.WicketExampleApplication;
import wicket.markup.html.ServerAndClientTimeFilter;

/**
 * Application object for the Prototype.js ajax demo page.
 */
public class PrototypeApplication extends WicketExampleApplication
{
	/**
	 * Constructor.
	 */
	public PrototypeApplication()
	{
		getExceptionSettings().setThrowExceptionOnMissingResource(false);
		getRequestCycleSettings().addResponseFilter(new ServerAndClientTimeFilter());
	}

	/**
	 * @see wicket.Application#getHomePage()
	 */
	public Class getHomePage()
	{
		return Index.class;
	}
}
