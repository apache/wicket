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
package org.apache.wicket.resource.loader;

import org.apache.wicket.Application;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.resource.IPropertiesFactory;
import org.apache.wicket.resource.PropertiesFactory;

/**
 * 
 */
public class ComponentStringResourceLoaderTest extends WicketTestCase
{
	/**
	 * Construct.
	 * 
	 * @param name
	 */
	public ComponentStringResourceLoaderTest(String name)
	{
		super(name);
	}

	/**
	 * @throws Exception
	 */
	public void testRenderHomePage_1() throws Exception
	{
		try
		{
			// The xml markup file and the search for an xml properties
			// file get in the way
			executeTest(TestPage_1.class, "TestPageExpectedResult_1.xml");
		}
		catch (WicketRuntimeException ex)
		{
		}
	}

	/**
	 * @throws Exception
	 */
	public void testRenderHomePage_2() throws Exception
	{
		// Avoid the conflict by limiting the search for properties files
		// to *.properties
		IPropertiesFactory myFac = new MyPropertiesFactory(tester.getApplication());
		tester.getApplication().getResourceSettings().setPropertiesFactory(myFac);

		executeTest(TestPage_1.class, "TestPageExpectedResult_1.xml");
	}

	/**
	 * 
	 */
	private class MyPropertiesFactory extends PropertiesFactory
	{
		/**
		 * Construct.
		 * 
		 * @param application
		 */
		public MyPropertiesFactory(Application application)
		{
			super(application);

			getPropertiesLoaders().clear();
			getPropertiesLoaders().add(new PropertiesFilePropertiesLoader());
		}
	}
}
