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

import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.request.resource.SharedResourceReference;
import org.junit.Test;

/**
 * @author jcompagner
 */
public class SharedResourceUrlTest extends WicketTestCase
{
	/**
	 * @throws Exception
	 */
	@Test
	public void resourceReferenceUrl() throws Exception
	{
		ResourceReference rr = new SharedResourceReference("test");
		CharSequence url = tester.getRequestCycle().mapUrlFor(rr, null).toString();
		assertEquals("wicket/resource/org.apache.wicket.Application/test", url);

		rr = new PackageResourceReference(SharedResourceUrlTest.class, "test");
		url = tester.getRequestCycle().mapUrlFor(rr, null).toString();
		assertEquals("wicket/resource/org.apache.wicket.SharedResourceUrlTest/test", url);
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void resourceReferenceWithParamsUrl() throws Exception
	{
		ResourceReference rr = new SharedResourceReference("test");
		CharSequence url = tester.getRequestCycle()
			.mapUrlFor(rr, new PageParameters().set("param", "value"))
			.toString();
		assertEquals("wicket/resource/org.apache.wicket.Application/test?param=value", url);

		rr = new PackageResourceReference(SharedResourceUrlTest.class, "test");
		url = tester.getRequestCycle()
			.mapUrlFor(rr, new PageParameters().set("param", "value"))
			.toString();
		assertEquals("wicket/resource/org.apache.wicket.SharedResourceUrlTest/test?param=value",
			url);
	}

}
