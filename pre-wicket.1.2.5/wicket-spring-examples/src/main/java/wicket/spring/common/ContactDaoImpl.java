/*
 * $Id$ $Revision$
 * $Date$
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket.spring.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * a dao implementation with an auto-generated embedded database. in a true
 * application this dao would interface with a real database, but because we
 * want to keep dependencies to a minimum we generate our own database here.
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public class ContactDaoImpl implements ContactDao {
	private Map map = Collections.synchronizedMap(new HashMap());

	private List fnameIdx = Collections.synchronizedList(new ArrayList());

	private List lnameIdx = Collections.synchronizedList(new ArrayList());

	private List fnameDescIdx = Collections.synchronizedList(new ArrayList());

	private List lnameDescIdx = Collections.synchronizedList(new ArrayList());

	/**
	 * Constructor
	 * 
	 * @param count
	 *            number of contacts to generate at startup
	 */
	public ContactDaoImpl() {
		for (int i = 0; i < 35; i++) {
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
	public Contact get(long id) {
		Contact c = (Contact) map.get(new Long(id));
		if (c == null)
			throw new RuntimeException("contact with id [" + id
					+ "] not found in the database");
		return c;
	}

	protected void add(final Contact contact) {
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
	public Iterator find(QueryParam qp) {
		List sublist = getIndex(qp.getSort(), qp.isSortAsc()).subList(
				qp.getFirst(), qp.getFirst() + qp.getCount());
		return sublist.iterator();
	}

	protected List getIndex(String prop, boolean asc) {
		if (prop == null)
			return fnameIdx;
		if (prop.equals("firstName")) {
			return (asc) ? fnameIdx : fnameDescIdx;
		} else if (prop.equals("lastName")) {
			return (asc) ? lnameIdx : lnameDescIdx;
		}
		throw new RuntimeException("uknown sort option [" + prop
				+ "]. valid options: [firstName] , [lastName]");
	}

	/**
	 * @return number of contacts in the database
	 */
	public int count() {
		return fnameIdx.size();
	}

	/**
	 * add contact to the database
	 * 
	 * @param contact
	 */
	public void save(final Contact contact) {
		if (contact.getId() == 0) {
			contact.setId(ContactGenerator.getInstance().generateId());
			add(contact);
			updateIndecies();
		} else {
			throw new IllegalArgumentException("contact ["
					+ contact.getFirstName() + "] is already persistent");
		}
	}

	/**
	 * delete contact from the database
	 * 
	 * @param contact
	 */
	public void delete(final Contact contact) {
		map.remove(new Long(contact.getId()));

		fnameIdx.remove(contact);
		lnameIdx.remove(contact);
		fnameDescIdx.remove(contact);
		lnameDescIdx.remove(contact);

		contact.setId(0);
	}

	private void updateIndecies() {
		Collections.sort(fnameIdx, new Comparator() {
			public int compare(Object arg0, Object arg1) {
				return ((Contact) arg0).getFirstName().compareTo(
						((Contact) arg1).getFirstName());
			}
		});

		Collections.sort(lnameIdx, new Comparator() {
			public int compare(Object arg0, Object arg1) {
				return ((Contact) arg0).getLastName().compareTo(
						((Contact) arg1).getLastName());
			}
		});

		Collections.sort(fnameDescIdx, new Comparator() {
			public int compare(Object arg0, Object arg1) {
				return ((Contact) arg1).getFirstName().compareTo(
						((Contact) arg0).getFirstName());
			}
		});

		Collections.sort(lnameDescIdx, new Comparator() {
			public int compare(Object arg0, Object arg1) {
				return ((Contact) arg1).getLastName().compareTo(
						((Contact) arg0).getLastName());
			}
		});

	}

}
