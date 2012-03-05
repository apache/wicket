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
package org.apache.wicket;

import java.util.Locale;

import org.apache.wicket.protocol.http.WebRequestCycle;
import org.apache.wicket.util.value.ValueMap;

/**
 * @author jcompagner
 */
public class SharedResourceUrlTest extends WicketTestCase
{

	/**
	 * Construct.
	 * 
	 * @param name
	 */
	public SharedResourceUrlTest(String name)
	{
		super(name);
	}

	/**
	 * @throws Exception
	 */
	public void testResourceReferenceUrl() throws Exception
	{
		tester.setupRequestAndResponse();
		WebRequestCycle cycle = tester.createRequestCycle();

		ResourceReference rr = new ResourceReference("test");
		CharSequence url = cycle.urlFor(rr);
		assertEquals("resources/org.apache.wicket.Application/test", url.toString());

		rr = new ResourceReference(SharedResourceUrlTest.class, "test");
		url = cycle.urlFor(rr);
		assertEquals("resources/org.apache.wicket.SharedResourceUrlTest/test", url.toString());
	}

	/**
	 * @throws Exception
	 */
	public void testResourceReferenceWithParamsUrl() throws Exception
	{
		tester.setupRequestAndResponse();
		WebRequestCycle cycle = tester.createRequestCycle();

		ResourceReference rr = new ResourceReference("test");
		CharSequence url = cycle.urlFor(rr, new ValueMap("param=value", ""));
		assertEquals("resources/org.apache.wicket.Application/test?param=value", url.toString());

		rr = new ResourceReference(SharedResourceUrlTest.class, "test");
		url = cycle.urlFor(rr, new ValueMap("param=value", ""));
		assertEquals("resources/org.apache.wicket.SharedResourceUrlTest/test?param=value",
			url.toString());
	}

	public void testResourceReferenceUrl_SessionLocale() throws Exception
	{
		tester.setupRequestAndResponse();
		WebRequestCycle cycle = tester.createRequestCycle();

		Session.get().setLocale(Locale.GERMANY);
		ResourceReference rr = new ResourceReference(Application.class, "test.css", true, false);
		CharSequence url = cycle.urlFor(rr);
		assertEquals("resources/org.apache.wicket.Application/test_de_DE.css", url.toString());

		Session.get().setLocale(Locale.US);
		url = cycle.urlFor(rr);
		assertEquals("resources/org.apache.wicket.Application/test_en_US.css", url.toString());
	}

	/**
	 * @throws Exception
	 */
	public void testResourceReferenceUrl_SessionStyle() throws Exception
	{
		tester.setupRequestAndResponse();
		WebRequestCycle cycle = tester.createRequestCycle();

		ResourceReference rr = new ResourceReference(Application.class, "test", false, true);
		CharSequence url = cycle.urlFor(rr);
		assertEquals("resources/org.apache.wicket.Application/test", url.toString());

		Session.get().setStyle("foo");
		url = cycle.urlFor(rr);
		assertEquals("resources/org.apache.wicket.Application/test_foo", url.toString());

		Session.get().setStyle("bar");
		url = cycle.urlFor(rr);
		assertEquals("resources/org.apache.wicket.Application/test_bar", url.toString());
	}

	/**
	 * @throws Exception
	 */
	public void testResourceReferenceUrl_SessionLocaleAndStyle() throws Exception
	{
		tester.setupRequestAndResponse();
		WebRequestCycle cycle = tester.createRequestCycle();

		ResourceReference rr = new ResourceReference(Application.class, "test.css", true, true);
		Session.get().setLocale(Locale.GERMANY);
		CharSequence url = cycle.urlFor(rr);
		assertEquals("resources/org.apache.wicket.Application/test_de_DE.css", url.toString());

		Session.get().setStyle("foo");
		url = cycle.urlFor(rr);
		assertEquals("resources/org.apache.wicket.Application/test_foo_de_DE.css", url.toString());

		Session.get().setStyle("bar");
		Session.get().setLocale(Locale.US);
		url = cycle.urlFor(rr);
		assertEquals("resources/org.apache.wicket.Application/test_bar_en_US.css", url.toString());
	}

	/**
	 * @throws Exception
	 */
	public void testResourceReferenceUrl_NoSessionLocaleAndStyle() throws Exception
	{
		tester.setupRequestAndResponse();
		WebRequestCycle cycle = tester.createRequestCycle();

		ResourceReference rr = new ResourceReference(Application.class, "test", false, false);
		Session.get().setLocale(Locale.GERMANY);
		CharSequence url = cycle.urlFor(rr);
		assertEquals("resources/org.apache.wicket.Application/test", url.toString());

		Session.get().setStyle("foo");
		url = cycle.urlFor(rr);
		assertEquals("resources/org.apache.wicket.Application/test", url.toString());

		Session.get().setStyle("bar");
		Session.get().setLocale(Locale.US);
		url = cycle.urlFor(rr);
		assertEquals("resources/org.apache.wicket.Application/test", url.toString());
	}
}
