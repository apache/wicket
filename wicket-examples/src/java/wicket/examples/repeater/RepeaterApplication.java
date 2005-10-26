package wicket.examples.repeater;

import wicket.protocol.http.WebApplication;
import wicket.util.time.Duration;

/**
 * application class for repeater examples application
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public class RepeaterApplication extends WebApplication
{
	private ContactsDatabase contactsDB = new ContactsDatabase(50);


	/**
	 * Constructor.
	 */
	public RepeaterApplication()
	{
		getPages().setHomePage(Index.class);
		getSettings().setResourcePollFrequency(Duration.ONE_SECOND);
	}


	/**
	 * @return contacts database
	 */
	public ContactsDatabase getContactsDB()
	{
		return contactsDB;
	}


}
