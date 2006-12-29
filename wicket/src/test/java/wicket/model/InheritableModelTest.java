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
package wicket.model;

import wicket.WicketTestCase;
import wicket.markup.html.WebMarkupContainer;
import wicket.util.lang.Person;

/**
 * @author jcompagner
 */
public class InheritableModelTest extends WicketTestCase
{

	/**
	 * Construct.
	 * @param name
	 */
	public InheritableModelTest(String name)
	{
		super(name);
	}

	/**
	 * 
	 */
	public void testInhertiableModelChange()
	{
		TestPage page = new TestPage();
		WebMarkupContainer <Person> parent = new WebMarkupContainer<Person>(page,"parent");
		WebMarkupContainer <String> child = new WebMarkupContainer<String>(parent,"name");
		
		Person person1 = new Person();
		person1.setName("john");
		parent.setModel(new CompoundPropertyModel<Person>(person1));
		
		assertEquals("john", child.getModel().getObject());
		assertEquals("john", child.getModelObject());
		
		Person person2 = new Person();
		person2.setName("igor");
		parent.setModel(new CompoundPropertyModel<Person>(person2));
		
		assertEquals("igor", child.getModel().getObject());
		assertEquals("igor", child.getModelObject());
		
	}
}
