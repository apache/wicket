package wicket.examples.ajax.builtin;

import java.util.TimeZone;

import wicket.ajax.AjaxSelfUpdatingTimerBehavior;

/**
 * A simple clock example page
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public class ClockPage extends BasePage
{
	/**
	 * Constructor
	 */
	public ClockPage()
	{
		// add the clock component
		Clock clock = new Clock("clock", TimeZone.getTimeZone("America/Los_Angeles"));
		add(clock);

		// add the ajax behavior which will keep updating the component every 5
		// seconds
		clock.add(new AjaxSelfUpdatingTimerBehavior(5000));


	}
}
