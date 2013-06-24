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
package org.apache.wicket.examples.compref;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Page;
import org.apache.wicket.examples.WicketExampleApplication;


/**
 * Application class for the component reference.
 * 
 * @author Eelco Hillenius
 */
public class ComponentReferenceApplication extends WicketExampleApplication
{
	private static final List<Person> personsDB;
	static
	{
		personsDB = new ArrayList<>();
		personsDB.add(new Person("Fritz", "Fritzel"));
		personsDB.add(new Person("Ghan", "Phariounimn"));
		personsDB.add(new Person("Jan", "Klaasen"));
		personsDB.add(new Person("Hank", "Plaindweller"));
	}

	/**
	 * @return persons db
	 */
	public static final List<Person> getPersons()
	{
		return personsDB;
	}

	/**
	 * Constructor.
	 */
	public ComponentReferenceApplication()
	{
	}

	/**
	 * @see org.apache.wicket.examples.WicketExampleApplication#init()
	 */
	@Override
	protected void init()
	{
		super.init();

		getResourceSettings().setThrowExceptionOnMissingResource(false);
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
