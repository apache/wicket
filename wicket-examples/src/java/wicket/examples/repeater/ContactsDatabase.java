/*
 * $Id: ContactsDatabase.java 5394 2006-04-16 13:36:52 +0000 (Sun, 16 Apr 2006)
 * jdonnerstag $ $Revision$ $Date: 2006-04-16 13:36:52 +0000 (Sun, 16 Apr
 * 2006) $
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * simple database for contacts
 * 
 * @author igor
 * 
 */
public class ContactsDatabase
{
	private Map<Long, Contact> map = Collections.synchronizedMap(new HashMap<Long, Contact>());
	private List<Contact> fnameIdx = Collections.synchronizedList(new ArrayList<Contact>());
	private List<Contact> lnameIdx = Collections.synchronizedList(new ArrayList<Contact>());
	private List<Contact> fnameDescIdx = Collections.synchronizedList(new ArrayList<Contact>());
	private List<Contact> lnameDescIdx = Collections.synchronizedList(new ArrayList<Contact>());

	/**
	 * Constructor
	 * 
	 * @param count
	 *            number of contacts to generate at startup
	 */
	public ContactsDatabase(int count)
	{
		for (int i = 0; i < count; i++)
		{
			add(ContactGenerator.getInstance().generate());
		}
		updateIndecies();
	}

	/**
	 * find contact by id
	 * 
	 * @param id
	 * @return contact
	 */
	public Contact get(long id)
	{
		Contact c = (Contact)map.get(new Long(id));
		if (c == null)
		{
			throw new RuntimeException("contact with id [" + id + "] not found in the database");
		}
		return c;
	}

	protected void add(final Contact contact)
	{
		map.put(new Long(contact.getId()), contact);
		fnameIdx.add(contact);
		lnameIdx.add(contact);
		fnameDescIdx.add(contact);
		lnameDescIdx.add(contact);
	}

	/**
	 * select contacts and apply sort
	 * 
	 * @param first
	 * @param count
	 * @param sortProperty
	 * @param sortAsc
	 * @return list of contacts
	 */
	public List find(int first, int count, String sortProperty, boolean sortAsc)
	{
		List sublist = getIndex(sortProperty, sortAsc).subList(first, first + count);
		return sublist;
	}

	protected List getIndex(String prop, boolean asc)
	{
		if (prop == null)
		{
			return fnameIdx;
		}
		if (prop.equals("firstName"))
		{
			return (asc) ? fnameIdx : fnameDescIdx;
		}
		else if (prop.equals("lastName"))
		{
			return (asc) ? lnameIdx : lnameDescIdx;
		}
		throw new RuntimeException("uknown sort option [" + prop
				+ "]. valid options: [firstName] , [lastName]");
	}

	/**
	 * @return number of contacts in the database
	 */
	public int getCount()
	{
		return fnameIdx.size();
	}

	/**
	 * add contact to the database
	 * 
	 * @param contact
	 */
	public void save(final Contact contact)
	{
		if (contact.getId() == 0)
		{
			contact.setId(ContactGenerator.getInstance().generateId());
			add(contact);
			updateIndecies();
		}
		else
		{
			throw new IllegalArgumentException("contact [" + contact.getFirstName()
					+ "] is already persistent");
		}
	}

	/**
	 * delete contact from the database
	 * 
	 * @param contact
	 */
	public void delete(final Contact contact)
	{
		Contact c = (Contact)map.remove(new Long(contact.getId()));

		fnameIdx.remove(contact);
		lnameIdx.remove(contact);
		fnameDescIdx.remove(contact);
		lnameDescIdx.remove(contact);

		contact.setId(0);
	}

	private void updateIndecies()
	{
		Collections.sort(fnameIdx, new Comparator<Contact>()
		{
			public int compare(Contact arg0, Contact arg1)
			{
				return arg0.getFirstName().compareTo(arg1.getFirstName());
			}
		});

		Collections.sort(lnameIdx, new Comparator<Contact>()
		{
			public int compare(Contact arg0, Contact arg1)
			{
				return arg0.getLastName().compareTo(arg1.getLastName());
			}
		});

		Collections.sort(fnameDescIdx, new Comparator<Contact>()
		{
			public int compare(Contact arg0, Contact arg1)
			{
				return arg1.getFirstName().compareTo(arg0.getFirstName());
			}
		});

		Collections.sort(lnameDescIdx, new Comparator<Contact>()
		{
			public int compare(Contact arg0, Contact arg1)
			{
				return arg1.getLastName().compareTo(arg0.getLastName());
			}
		});

	}

}
