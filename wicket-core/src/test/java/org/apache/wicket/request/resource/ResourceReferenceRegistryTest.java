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

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;

/**
 * Tests for {@link ResourceReferenceRegistry}
 */
public class ResourceReferenceRegistryTest extends WicketTestCase
{

	/**
	 * https://issues.apache.org/jira/browse/WICKET-3890
	 */
	@Test
	public void addRemove()
	{
		ResourceReferenceRegistry registry = new ResourceReferenceRegistry();
		assertEquals(0, registry.getSize());

		ResourceReference reference = new PackageResourceReference(
			ResourceReferenceRegistryTest.class, "test");
		registry.registerResourceReference(reference);
		assertEquals(1, registry.getSize());

		registry.unregisterResourceReference(reference.getKey());
		assertEquals(0, registry.getSize());
	}

	/**
	 * {@link SharedResourceReference} cannot be added in {@link ResourceReferenceRegistry}. It is
	 * just a shortcut to find already registered {@link IResource} or to create a new
	 * {@link PackageResource} and register it
	 */
	@Test
	public void addSharedResourceReference()
	{
		ResourceReferenceRegistry registry = new ResourceReferenceRegistry();
		assertEquals(0, registry.getSize());

		ResourceReference reference = new SharedResourceReference(
			ResourceReferenceRegistryTest.class, "test");
		registry.registerResourceReference(reference);
		assertEquals(0, registry.getSize());
	}

	@Test
	public void setNullResourceReferenceFactoryStillUsesTheDefault()
	{
		ResourceReferenceRegistry registry = new ResourceReferenceRegistry();
		registry.setResourceReferenceFactory(null);
		ResourceReference.Key key = new ResourceReference.Key(ResourceReferenceRegistryTest.class.getName(),
				"a.css", null, null, null);
		ResourceReference reference = registry.createDefaultResourceReference(key);
		assertThat(reference, is(instanceOf(ResourceReference.class)));
		assertThat(reference.getResource(), is(instanceOf(CssPackageResource.class)));
	}

	@Test
	public void createLessResourceReference()
	{
		ResourceReferenceRegistry registry = new ResourceReferenceRegistry();
		registry.setResourceReferenceFactory(new LessResourceReferenceTest.LessResourceReferenceFactory());
		ResourceReference.Key key = new ResourceReference.Key(ResourceReferenceRegistryTest.class.getName(),
				"LessResourceReference.less", null, null, null);
		ResourceReference reference = registry.createDefaultResourceReference(key);
		assertThat(reference, is(instanceOf(LessResourceReferenceTest.LessResourceReference.class)));
		assertThat(reference.getResource(), is(instanceOf(LessResourceReferenceTest.LessPackageResource.class)));
	}
}
