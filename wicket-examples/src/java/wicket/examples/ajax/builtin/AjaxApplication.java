package wicket.examples.ajax.builtin;

import wicket.examples.WicketExampleApplication;
import wicket.markup.html.ServerAndClientTimeFilter;

/**
 * Application object for the wicked ajax examples
 */
public class AjaxApplication extends WicketExampleApplication
{
	/**
	 * Constructor.
	 */
	public AjaxApplication()
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
