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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.wicket.request.resource.CssPackageResource;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.PackageResource;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.request.resource.ResourceReferenceRegistry;
import org.apache.wicket.request.resource.SharedResourceReference;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link ResourceReferenceRegistry}
 */
class ResourceReferenceRegistryTest extends WicketTestCase
{

	/**
	 * https://issues.apache.org/jira/browse/WICKET-3890
	 */
	@Test
	void addRemove()
	{
		RRR registry = new RRR();
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
	void addSharedResourceReference()
	{
		RRR registry = new RRR();
		assertEquals(0, registry.getSize());

		ResourceReference reference = new SharedResourceReference(
			ResourceReferenceRegistryTest.class, "test");
		registry.registerResourceReference(reference);
		assertEquals(0, registry.getSize());
	}

	@Test
	void setNullResourceReferenceFactoryStillUsesTheDefault()
	{
		RRR registry = new RRR();
		registry.setResourceReferenceFactory(null);
		ResourceReference.Key key = new ResourceReference.Key(ResourceReferenceRegistryTest.class.getName(),
				"a.css", null, null, null);
		ResourceReference reference = registry.createDefaultResourceReference(key);
		assertThat(reference, instanceOf(ResourceReference.class));
		assertThat(reference.getResource(), instanceOf(CssPackageResource.class));
	}

	@Test
	void createLessResourceReference()
	{
		RRR registry = new RRR();
		registry.setResourceReferenceFactory(new LessResourceReferenceTest.LessResourceReferenceFactory());
		ResourceReference.Key key = new ResourceReference.Key(ResourceReferenceRegistryTest.class.getName(),
				"LessResourceReference.less", null, null, null);
		ResourceReference reference = registry.createDefaultResourceReference(key);
		assertThat(reference, instanceOf(LessResourceReferenceTest.LessResourceReference.class));
		assertThat(reference.getResource(), instanceOf(LessResourceReferenceTest.LessPackageResource.class));
	}

	class RRR extends ResourceReferenceRegistry
	{
		// make it public for the test
		@Override
		public ResourceReference createDefaultResourceReference(ResourceReference.Key key) {
			return super.createDefaultResourceReference(key);
		}
	}
}
