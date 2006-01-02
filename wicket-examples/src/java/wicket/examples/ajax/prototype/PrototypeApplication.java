package wicket.examples.ajax.prototype;

import wicket.examples.WicketExampleApplication;
import wicket.markup.html.ServerAndClientTimeFilter;
import wicket.util.crypt.SunJceCrypt;

/**
 * Application object for the Prototype.js ajax demo page.
 */
public class PrototypeApplication extends WicketExampleApplication
{
	/**
	 * Constructor.
	 */
	public PrototypeApplication() {
		getPages().setHomePage(Index.class);
		getSettings().setThrowExceptionOnMissingResource(false);

		getSettings().setCryptClass(SunJceCrypt.class);

		addResponseFilter(new ServerAndClientTimeFilter());
	}
}
