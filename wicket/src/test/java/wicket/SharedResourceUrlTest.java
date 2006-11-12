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
package wicket;

import wicket.protocol.http.WebRequestCycle;
import wicket.util.value.ValueMap;

/**
 * @author jcompagner
 */
public class SharedResourceUrlTest extends WicketTestCase
{

	/**
	 * Construct.
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
		assertEquals("/WicketTester$DummyWebApplication/WicketTester$DummyWebApplication/resources/wicket.Application/test", url);

		rr = new ResourceReference(SharedResourceUrlTest.class,"test");
		url = cycle.urlFor(rr);
		assertEquals("/WicketTester$DummyWebApplication/WicketTester$DummyWebApplication/resources/wicket.SharedResourceUrlTest/test", url);
}
	
	/**
	 * @throws Exception
	 */
	public void testResourceReferenceWithParamsUrl() throws Exception
	{
		tester.setupRequestAndResponse();
		WebRequestCycle cycle = tester.createRequestCycle();
		
		ResourceReference rr = new ResourceReference("test");
		CharSequence url = cycle.urlFor(rr,new ValueMap("param=value",""));
		assertEquals("/WicketTester$DummyWebApplication/WicketTester$DummyWebApplication/resources/wicket.Application/test?param=value", url);

		rr = new ResourceReference(SharedResourceUrlTest.class,"test");
		url = cycle.urlFor(rr,new ValueMap("param=value",""));
		assertEquals("/WicketTester$DummyWebApplication/WicketTester$DummyWebApplication/resources/wicket.SharedResourceUrlTest/test?param=value", url);
	}	

}
