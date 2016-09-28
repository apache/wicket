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

import java.util.Map;

import org.apache.wicket.resource.IPropertiesFactory;
import org.apache.wicket.resource.IPropertiesFactoryContext;
import org.apache.wicket.resource.IsoPropertiesFilePropertiesLoader;
import org.apache.wicket.resource.Properties;
import org.apache.wicket.resource.PropertiesFactory;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;

/**
 * 
 */
public class ComponentStringResourceLoaderTest extends WicketTestCase
{

	/**
	 * @throws Exception
	 */
	@Test
	public void renderHomePage_1() throws Exception
	{
		// The xml markup file and the search for an xml properties
		// file get in the way
		executeTest(TestPage_1.class, "TestPageExpectedResult_1.xml");
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void renderHomePage_2() throws Exception
	{
		// Avoid the conflict by limiting the search for properties files
		// to *.properties
		IPropertiesFactory myFac = new MyPropertiesFactory(tester.getApplication()
			.getResourceSettings());
		tester.getApplication().getResourceSettings().setPropertiesFactory(myFac);

		executeTest(TestPage_1.class, "TestPageExpectedResult_1.xml");
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void disabledCache() throws Exception
	{
		IPropertiesFactory myFac = new DisabledCachePropertiesFactory(tester.getApplication()
			.getResourceSettings());
		tester.getApplication().getResourceSettings().setPropertiesFactory(myFac);

		executeTest(TestPage_1.class, "TestPageExpectedResult_1.xml");

		myFac.clearCache();
	}

	/**
	 * 
	 */
	private class MyPropertiesFactory extends PropertiesFactory
	{
		/**
		 * Construct.
		 * 
		 * @param context
		 */
		public MyPropertiesFactory(IPropertiesFactoryContext context)
		{
			super(context);

			getPropertiesLoaders().clear();
			getPropertiesLoaders().add(new IsoPropertiesFilePropertiesLoader("properties"));
		}
	}

	/**
	 * 
	 */
	private class DisabledCachePropertiesFactory extends PropertiesFactory
	{
		/**
		 * Construct.
		 * 
		 * @param context
		 */
		public DisabledCachePropertiesFactory(IPropertiesFactoryContext context)
		{
			super(context);
		}

		/**
		 * @see org.apache.wicket.resource.PropertiesFactory#newPropertiesCache()
		 */
		@Override
		protected Map<String, Properties> newPropertiesCache()
		{
			return null;
		}
	}
}
