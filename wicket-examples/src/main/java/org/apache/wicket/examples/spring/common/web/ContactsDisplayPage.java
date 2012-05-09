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

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.examples.spring.common.Contact;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.model.Model;


/**
 * Base class for the contact display page.
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public abstract class ContactsDisplayPage extends BasePage
{
	/**
	 * Construct.
	 */
	public ContactsDisplayPage()
	{
		List<IColumn<Contact, String>> cols = new ArrayList<IColumn<Contact, String>>(4);
		cols.add(new PropertyColumn<Contact, String>(new Model<String>("first name"), "firstName", "firstName"));
		cols.add(new PropertyColumn<Contact, String>(new Model<String>("last name"), "lastName", "lastName"));
		cols.add(new PropertyColumn<Contact, String>(new Model<String>("home phone"), "homePhone"));
		cols.add(new PropertyColumn<Contact, String>(new Model<String>("cell phone"), "cellPhone"));

		add(new DefaultDataTable<Contact, String>("contacts", cols, getDataProvider(), 5));
	}

	protected abstract ContactDataProvider getDataProvider();
}
