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
package org.apache.wicket.markup.html.basic;

import org.apache.wicket.Application;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.resource.IPropertiesFactory;
import org.apache.wicket.resource.IsoPropertiesFilePropertiesLoader;
import org.apache.wicket.resource.PropertiesFactory;
import org.apache.wicket.resource.XmlFilePropertiesLoader;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 
 * @author Juergen Donnerstag
 */
public class XmlPageTest extends WicketTestCase
{
	private static final Logger log = LoggerFactory.getLogger(XmlPageTest.class);

	/**
	 * @throws Exception
	 */
	@Test
	public void renderHomePage_1() throws Exception
	{
		executeTest(XmlPage_1.class, "XmlPageExpectedResult_1.xml");
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void renderHomePage_2() throws Exception
	{
		IPropertiesFactory myFac = new MyPropertiesFactory(tester.getApplication());
		tester.getApplication().getResourceSettings().setPropertiesFactory(myFac);

		executeTest(XmlPage_2.class, "XmlPageExpectedResult_2.xml");
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void renderHomePage_3() throws Exception
	{
		IPropertiesFactory myFac = new MyPropertiesFactory(tester.getApplication());
		tester.getApplication().getResourceSettings().setPropertiesFactory(myFac);

		executeTest(XmlPage_3.class, "XmlPageExpectedResult_3.xml");
	}

	private static class MyPropertiesFactory extends PropertiesFactory
	{
		/**
		 * Construct.
		 * 
		 * @param application
		 */
		public MyPropertiesFactory(Application application)
		{
			super(application.getResourceSettings());

			getPropertiesLoaders().clear();
			getPropertiesLoaders().add(new IsoPropertiesFilePropertiesLoader("properties"));
			getPropertiesLoaders().add(new XmlFilePropertiesLoader("xmlProperties"));
		}
	}
}
