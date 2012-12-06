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
package org.apache.wicket.request.resource;

import org.apache.wicket.Application;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.request.resource.IResource.Attributes;
import org.apache.wicket.response.ByteArrayResponse;
import org.junit.Test;

/**
 * Testcases for minified aware resources.
 * There is a logic in PackageResourceReference to use a minified version of a resource
 * if there is such.
 * 
 * @author papegaaij
 */
public class MinifiedAwareResourceReferenceTest extends WicketTestCase
{

	private String renderResource(ResourceReference reference)
	{
		ByteArrayResponse byteResponse = new ByteArrayResponse();
		Attributes mockAttributes = new Attributes(tester.getRequestCycle().getRequest(),
			byteResponse);
		reference.getResource().respond(mockAttributes);
		return new String(byteResponse.getBytes());
	}

	/**
	 * Tests is a pre-minified resource is detected
	 */
	@Test
	public void minifiedResourceAvailable()
	{
		Application.get().getResourceSettings().setUseMinifiedResources(true);
		ResourceReference reference = new JavaScriptResourceReference(
			MinifiedAwareResourceReferenceTest.class, "b.js");
		assertEquals("b.min.js", reference.getName());
		String fileContent = renderResource(reference);
		assertEquals("//minified-b", fileContent);
	}

	/**
	 * Tests fallback to normal resource when pre-minified is not available
	 */
	@Test
	public void noMinifiedResourceAvailable()
	{
		Application.get().getResourceSettings().setUseMinifiedResources(true);
		ResourceReference reference = new JavaScriptResourceReference(
			MinifiedAwareResourceReferenceTest.class, "a.js");
		assertEquals("a.js", reference.getName());
		String fileContent = renderResource(reference);
		assertEquals("//a", fileContent);
	}
}
