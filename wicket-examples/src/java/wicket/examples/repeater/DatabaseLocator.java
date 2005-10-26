package wicket.examples.repeater;

import wicket.RequestCycle;

/**
 * service locator class for contacts database
 * 
 * @author igor
 * 
 */
public class DatabaseLocator
{
	/**
	 * @return contacts database
	 */
	public static ContactsDatabase getDatabase()
	{
		RepeaterApplication app = (RepeaterApplication)RequestCycle.get().getApplication();
		return app.getContactsDB();
	}
}
