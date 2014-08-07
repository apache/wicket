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
package org.apache.wicket.model;

import java.io.Serializable;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;

/**
 * Test for https://issues.apache.org/jira/browse/WICKET-4941
 */
public class CompoundPropertyModelTwoLevelsDeepPage extends WebPage
{
	public CompoundPropertyModelTwoLevelsDeepPage()
	{
		Person person = new Person("Andrew", new Address("100 Main St."));

		WebMarkupContainer personContainer = new WebMarkupContainer("person", new CompoundPropertyModel<Person>(person));
		add(personContainer);
		personContainer.add(new Label("name"));

		WebMarkupContainer addressContainer = new WebMarkupContainer("address");
		personContainer.add(addressContainer);

		addressContainer.add(new Label("address.street1"));
	}

	public static class Person implements Serializable
	{
		private String name;
		private Address address;

		public Person(String name, Address address)
		{
			this.name = name;
			this.address = address;
		}

		public String getName()
		{
			return name;
		}

		public Address getAddress()
		{
			return address;
		}
	}

	public static class Address implements Serializable
	{
		private String street1;

		public Address(String street1)
		{
			this.street1 = street1;
		}

		public String getStreet1()
		{
			return street1;
		}
	}
}
