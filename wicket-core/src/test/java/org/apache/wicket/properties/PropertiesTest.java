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
package org.apache.wicket.properties;

import java.util.Locale;

import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;

/**
 * 
 * @author Juergen Donnerstag
 */
public class PropertiesTest extends WicketTestCase
{
	@Override
	protected WebApplication newApplication()
	{
		return new MyApplication();
	}

	/**
	 */
	@Test
	public void test_1()
	{
		tester.getSession().setLocale(Locale.GERMANY);
		tester.getSession().setStyle("mystyle");

		TestPage page = new TestPage();

		assertEquals("MyApplication", page.getString("test1"));
		assertEquals("MyApplication_de", page.getString("test2"));
		assertEquals("MyApplication_mystyle", page.getString("test3"));
		assertEquals("MyApplication_mystyle_de", page.getString("test4"));

		tester.getSession().setLocale(Locale.ENGLISH);

		assertEquals("MyApplication_en", page.getString("test2"));
		assertEquals("MyApplication_mystyle_en", page.getString("test4"));
	}

	/**
	 */
	@Test
	public void test_2()
	{
		tester.getSession().setLocale(Locale.GERMANY);
		TestPage page = new TestPage()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public String getVariation()
			{
				return "mystyle";
			}
		};

		assertEquals("MyApplication", page.getString("test1"));
		assertEquals("MyApplication_de", page.getString("test2"));
		assertEquals("MyApplication_mystyle", page.getString("test3"));
		assertEquals("MyApplication_mystyle_de", page.getString("test4"));

		tester.getSession().setLocale(Locale.ENGLISH);

		assertEquals("MyApplication_en", page.getString("test2"));
		assertEquals("MyApplication_mystyle_en", page.getString("test4"));
	}
}
