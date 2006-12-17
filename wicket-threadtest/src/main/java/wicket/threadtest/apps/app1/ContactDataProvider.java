/*
 * $Id: ContactDataProvider.java 5394 2006-04-16 06:36:52 -0700 (Sun, 16 Apr 2006) jdonnerstag $
 * $Revision: 5394 $ $Date: 2006-04-16 06:36:52 -0700 (Sun, 16 Apr 2006) $
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
package wicket.threadtest.apps.app1;

import java.util.Iterator;

import wicket.markup.repeater.data.DataViewBase;
import wicket.markup.repeater.data.IDataProvider;
import wicket.model.IModel;

/**
 * Implementation of IDataProvider that retrieves contacts from the contact
 * database.
 * 
 * @see IDataProvider
 * @see DataViewBase
 * 
 * @author igor
 * 
 */
public class ContactDataProvider implements IDataProvider<Contact> {
	/*
	 * (non-Javadoc)
	 * 
	 * @see wicket.model.IDetachable#detach()
	 */
	public void detach() {

	}

	/**
	 * retrieves contacts from database starting with index <code>first</code>
	 * and ending with <code>first+count</code>
	 * 
	 * @see IDataProvider#iterator(int,int)
	 */
	@SuppressWarnings("unchecked")
	public Iterator<Contact> iterator(int first, int count) {
		return getContactsDB().find(first, count, "firstName", true).iterator();
	}

	/**
	 * wraps retrieved contact pojo with a wicket model
	 * 
	 * @see IDataProvider#model(java.lang.Object)
	 */
	public IModel<Contact> model(Contact object) {
		return new DetachableContactModel(object);
	}

	/**
	 * returns total number of contacts in the database
	 * 
	 * @see IDataProvider#size()
	 */
	public int size() {
		return getContactsDB().getCount();
	}

	protected ContactsDatabase getContactsDB() {
		return DatabaseLocator.getDatabase();
	}

}