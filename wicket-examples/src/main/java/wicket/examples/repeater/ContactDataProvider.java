/*
 * $Id$
 * $Revision$ $Date$
 * 
 * ==================================================================== Licensed
 * under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the
 * License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.examples.repeater;

import java.util.Iterator;

import wicket.extensions.markup.html.repeater.data.IDataProvider;
import wicket.model.IModel;

/**
 * Implementation of IDataProvider that retrieves contacts from the contact
 * database.
 * 
 * @see wicket.extensions.markup.html.repeater.data.IDataProvider
 * @see wicket.extensions.markup.html.repeater.data.DataViewBase
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

	/**
	 * @see wicket.model.IDetachable#detach()
	 */
	public void detach()
	{
	}

}
