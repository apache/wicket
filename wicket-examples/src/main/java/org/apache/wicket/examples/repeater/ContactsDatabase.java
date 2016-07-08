/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.wicket.examples.repeater;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;

/**
 * simple database for contacts
 * 
 * @author igor
 * 
 */
public class ContactsDatabase
{
	private final Map<Long, Contact> map = Collections.synchronizedMap(new HashMap<>());
	private final List<Contact> fnameIdx = Collections.synchronizedList(new ArrayList<>());
	private final List<Contact> lnameIdx = Collections.synchronizedList(new ArrayList<>());
	private final List<Contact> fnameDescIdx = Collections.synchronizedList(new ArrayList<>());
	private final List<Contact> lnameDescIdx = Collections.synchronizedList(new ArrayList<>());

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
		Contact c = map.get(id);
		if (c == null)
		{
			throw new RuntimeException("contact with id [" + id + "] not found in the database");
		}
		return c;
	}

	protected void add(final Contact contact)
	{
		map.put(contact.getId(), contact);
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
	 * @param sort
	 * @return list of contacts
	 */
	public List<Contact> find(long first, long count, SortParam sort)
	{
		return getIndex(sort).subList((int)first, (int)(first + count));
	}

	public List<Contact> getIndex(SortParam sort)
	{
		if (sort == null)
		{
			return fnameIdx;
		}

		if (sort.getProperty().equals("firstName"))
		{
			return sort.isAscending() ? fnameIdx : fnameDescIdx;
		}
		else if (sort.getProperty().equals("lastName"))
		{
			return sort.isAscending() ? lnameIdx : lnameDescIdx;
		}
		throw new RuntimeException("unknown sort option [" + sort +
			"]. valid fields: [firstName], [lastName]");
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
			throw new IllegalArgumentException("contact [" + contact.getFirstName() +
				"] is already persistent");
		}
	}

	/**
	 * delete contact from the database
	 * 
	 * @param contact
	 */
	public void delete(final Contact contact)
	{
		map.remove(contact.getId());

		fnameIdx.remove(contact);
		lnameIdx.remove(contact);
		fnameDescIdx.remove(contact);
		lnameDescIdx.remove(contact);

		contact.setId(0);
	}

	private void updateIndecies()
	{
		Collections.sort(fnameIdx, (arg0, arg1) -> (arg0).getFirstName().compareTo((arg1).getFirstName()));

		Collections.sort(lnameIdx, (arg0, arg1) -> (arg0).getLastName().compareTo((arg1).getLastName()));

		Collections.sort(fnameDescIdx, (arg0, arg1) -> (arg1).getFirstName().compareTo((arg0).getFirstName()));

		Collections.sort(lnameDescIdx, (arg0, arg1) -> (arg1).getLastName().compareTo((arg0).getLastName()));

	}

}
