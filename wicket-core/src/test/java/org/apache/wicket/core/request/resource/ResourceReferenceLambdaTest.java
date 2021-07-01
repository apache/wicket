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
package org.apache.wicket.core.request.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.tester.WicketTestCase;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.Test;

class ResourceReferenceLambdaTest extends WicketTestCase
{

	private final String output = "lambda resource";

	@Override
	protected WicketTester newWicketTester(WebApplication app) 
	{
		WicketTester wicketTester = super.newWicketTester(app);

		IResource res = (attributes) ->
			attributes.getResponse().write(output);
		
		ResourceReference resRef = ResourceReference.of("lambdares", () -> res);
		
		app.mountResource("/test", resRef);
				
		return wicketTester;
	}
	
	@Test
	void lambdaBasedResurceReference() throws Exception
	{
		tester.executeUrl("./test");
		
		assertEquals(output, tester.getLastResponseAsString());
	}
}
