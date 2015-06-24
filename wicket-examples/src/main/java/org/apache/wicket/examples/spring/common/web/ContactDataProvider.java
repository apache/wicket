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
package org.apache.wicket.examples.spring.common.web;

import java.util.Iterator;

import org.apache.wicket.examples.spring.common.Contact;
import org.apache.wicket.examples.spring.common.ContactDao;
import org.apache.wicket.examples.spring.common.QueryParam;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.repeater.data.SortOrder;

/**
 * Base class for contact data provider implementations. This class implements everything except
 * retrieval of the dao object, this way we can isolate that for our examples.
 * 
 * @author Igor Vaynberg (ivaynerg)
 * 
 */
public abstract class ContactDataProvider extends SortableDataProvider<Contact, String>
{

	public ContactDataProvider()
	{
		setSort("firstName", SortOrder.ASCENDING);
	}

	protected abstract ContactDao getContactDao();

	@Override
	public final Iterator<Contact> iterator(long first, long count)
	{
		QueryParam qp = new QueryParam(first, count, getSort());
		return getContactDao().find(qp);
	}

	@Override
	public final long size()
	{
		return getContactDao().count();
	}
}
