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

import org.apache.wicket.Page;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.response.filter.ServerAndClientTimeFilter;

/**
 * application class for repeater examples application
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public class RepeaterApplication extends WebApplication
{
	private final ContactsDatabase contactsDB = new ContactsDatabase(50);


	/**
	 * Constructor.
	 */
	public RepeaterApplication()
	{
	}

	/**
	 * @see org.apache.wicket.protocol.http.WebApplication#init()
	 */
	@Override
	protected void init()
	{
		getDebugSettings().setDevelopmentUtilitiesEnabled(true);

		getRequestCycleSettings().addResponseFilter(new ServerAndClientTimeFilter());

		getMarkupSettings().setStripWicketTags(true);
	}

	/**
	 * @return contacts database
	 */
	public ContactsDatabase getContactsDB()
	{
		return contactsDB;
	}

	/**
	 * @see org.apache.wicket.Application#getHomePage()
	 */
	@Override
	public Class<? extends Page> getHomePage()
	{
		return Index.class;
	}


}
