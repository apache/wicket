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
package org.apache.wicket.examples.spring.annot.web;

import org.apache.wicket.examples.spring.common.Contact;
import org.apache.wicket.examples.spring.common.ContactDao;
import org.apache.wicket.examples.spring.common.web.ContactDataProvider;
import org.apache.wicket.model.IModel;

public class ProxyDataProvider extends ContactDataProvider
{
	private ContactDao dao;

	public ProxyDataProvider(ContactDao dao)
	{
		this.dao = dao;
	}

	@Override
	protected ContactDao getContactDao()
	{
		return dao;
	}

	public IModel<Contact> model(Contact contact)
	{
		return new ProxyModel(contact, dao);
	}

}
