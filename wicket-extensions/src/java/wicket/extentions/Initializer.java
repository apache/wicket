/**
 * 
 */
package wicket.extentions;

import wicket.Application;
import wicket.IInitializer;
import wicket.extensions.markup.html.datepicker.DatePickerComponentInitializer;

/**
 * @author jcompagner
 *
 */
public class Initializer implements IInitializer
{

	/**
	 * @see wicket.IInitializer#init(wicket.Application)
	 */
	public void init(Application application)
	{
		new DatePickerComponentInitializer().init(application);
	}

}
