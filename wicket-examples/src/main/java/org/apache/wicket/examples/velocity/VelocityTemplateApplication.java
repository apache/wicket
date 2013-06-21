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
package org.apache.wicket.examples.velocity;

import java.util.ArrayList;
import java.util.List;

import org.apache.velocity.app.Velocity;
import org.apache.wicket.Page;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.markup.html.IPackageResourceGuard;
import org.apache.wicket.markup.html.SecurePackageResourceGuard;
import org.apache.wicket.protocol.http.WebApplication;

/**
 * Application class for velocity template example.
 * 
 * @author Eelco Hillenius
 */
public class VelocityTemplateApplication extends WebApplication
{
	private static List<Field> fields = new ArrayList<>();

	/** simple persons db. */
	private static List<Person> persons = new ArrayList<>();

	static
	{
		persons.add(new Person("Joe", "Down"));
		persons.add(new Person("Fritz", "Frizel"));
		persons.add(new Person("Flip", "Vlieger"));
		persons.add(new Person("George", "Forrest"));
		persons.add(new Person("Sue", "Hazel"));
		persons.add(new Person("Bush", "Gump"));
	}
	static
	{
		fields.add(new Field("firstName", 50));
		fields.add(new Field("lastName", 80));
	}

	/**
	 * @return Fields
	 */
	public static List<Field> getFields()
	{
		return fields;
	}

	/**
	 * Gets the dummy persons database.
	 * 
	 * @return the dummy persons database
	 */
	public static List<Person> getPersons()
	{
		return persons;
	}

	/**
	 * Constructor.
	 */
	public VelocityTemplateApplication()
	{
	}

	/**
	 * @return class
	 */
	@Override
	public Class<? extends Page> getHomePage()
	{
		return Home.class;
	}

	/**
	 * @see org.apache.wicket.protocol.http.WebApplication#init()
	 */
	@Override
	protected void init()
	{
		getDebugSettings().setDevelopmentUtilitiesEnabled(true);
		IPackageResourceGuard packageResourceGuard = getResourceSettings().getPackageResourceGuard();
		if (packageResourceGuard instanceof SecurePackageResourceGuard)
		{
			SecurePackageResourceGuard guard = (SecurePackageResourceGuard) packageResourceGuard;
			// allow velocity macros resources
			guard.addPattern("+*.vm");
		}

		// initialize velocity
		try
		{
			Velocity.init();
		}
		catch (Exception e)
		{
			throw new WicketRuntimeException(e);
		}
	}
}
