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
package org.apache.wicket.markup.html.link;

import org.apache.wicket.request.resource.CharSequenceResource;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;


/**
 * Tests {@link ResourceLink}
 */
public class ResourceLinkTest extends WicketTestCase
{

	IResource resource = new CharSequenceResource("text", "DATA");
	
	/**
	 * Test resource
	 */
	@Test
	public void reference()
	{
		ResourceReference reference = new ResourceReference(ResourceLinkTest.class, "resource") {
			@Override
			public IResource getResource() {
				return resource;
			}
		};
		PageWithResourceLink page = new PageWithResourceLink(reference);
		
		tester.startPage(page);

		assertTrue(page.isPageStateless());
	}

	/**
	 * Test resource
	 */
	@Test
	public void resource()
	{
		PageWithResourceLink page = new PageWithResourceLink(resource);
		
		tester.startPage(page);

		assertFalse(page.isPageStateless());
	}
}
