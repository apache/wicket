package wicket.examples.repeater;

import java.util.Iterator;

import wicket.extensions.markup.html.repeater.data.IDataProvider;
import wicket.model.IModel;

/**
 * Implementation of IDataProvider that retrieves contacts from the contact
 * database.
 * 
 * @see wicket.extensions.markup.html.repeater.data.IDataProvider
 * @see wicket.extensions.markup.html.repeater.data.AbstractDataView
 * 
 * @author igor
 * 
 */
public class ContactDataProvider implements IDataProvider
{
	protected ContactsDatabase getContactsDB()
	{
		return DatabaseLocator.getDatabase();
	}

	/**
	 * retrieves contacts from database starting with index <code>first</code>
	 * and ending with <code>first+count</code>
	 * 
	 * @see wicket.extensions.markup.html.repeater.data.IDataProvider#iterator(int,
	 *      int)
	 */
	public Iterator iterator(int first, int count)
	{
		return getContactsDB().find(first, count, "firstName", true).iterator();
	}

	/**
	 * returns total number of contacts in the database
	 * 
	 * @see wicket.extensions.markup.html.repeater.data.IDataProvider#size()
	 */
	public int size()
	{
		return getContactsDB().getCount();
	}

	/**
	 * wraps retrieved contact pojo with a wicket model
	 * 
	 * @see wicket.extensions.markup.html.repeater.data.IDataProvider#model(java.lang.Object)
	 */
	public IModel model(Object object)
	{
		return new DetachableContactModel((Contact)object);
	}

}
