package wicket.examples.repeater;

import wicket.examples.WicketExamplePage;

/**
 * Base class for all pages in the QuickStart application. Any page which
 * subclasses this page can get session properties from QuickStartSession via
 * getQuickStartSession().
 */
public abstract class ExamplePage extends WicketExamplePage
{
	/**
	 * Get downcast session object for easy access by subclasses
	 * 
	 * @return The session
	 */
	public ContactsDatabase getContactsDB()
	{
		return ((RepeaterApplication)getApplication()).getContactsDB();
	}
}