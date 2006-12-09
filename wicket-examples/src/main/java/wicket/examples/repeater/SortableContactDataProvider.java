/*
 * $Id: SortableContactDataProvider.java 5394 2006-04-16 13:36:52 +0000 (Sun, 16
 * Apr 2006) jdonnerstag $ $Revision$ $Date: 2006-04-16 13:36:52 +0000
 * (Sun, 16 Apr 2006) $
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

import wicket.extensions.markup.html.repeater.util.SortParam;
import wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import wicket.model.IModel;

/**
 * implementation of IDataProvider for contacts that keeps track of sort
 * information
 * 
 * @author igor
 * 
 */
public class SortableContactDataProvider extends SortableDataProvider<Contact>
{
	/**
	 * constructor
	 */
	public SortableContactDataProvider()
	{
		// set default sort
		setSort("firstName", true);
	}

	protected ContactsDatabase getContactsDB()
	{
		return DatabaseLocator.getDatabase();
	}

	/**
	 * @see wicket.extensions.markup.html.repeater.data.IDataProvider#iterator(int,
	 *      int)
	 */
	public Iterator<Contact> iterator(int first, int count)
	{
		SortParam sp = getSort();
		return getContactsDB().find(first, count, sp.getProperty(), sp.isAscending()).iterator();
	}

	/**
	 * @see wicket.extensions.markup.html.repeater.data.IDataProvider#size()
	 */
	public int size()
	{
		return getContactsDB().getCount();
	}

	/**
	 * @see wicket.extensions.markup.html.repeater.data.IDataProvider#model(java.lang.Object)
	 */
	public IModel<Contact> model(Contact object)
	{
		return new DetachableContactModel(object);
	}

}
