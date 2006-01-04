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
	public PrototypeApplication() {
		getRequiredPageSettings().setHomePage(Index.class);
		getSettings().setThrowExceptionOnMissingResource(false);

		getRequestCycleSettings().addResponseFilter(new ServerAndClientTimeFilter());
	}
}
