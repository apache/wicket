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
package org.apache.wicket.cdi;

import java.util.Collections;
import java.util.Map;

import javax.inject.Inject;

import org.apache.wicket.cdi.AbstractCdiContainer.ContainerSupport;
import org.apache.wicket.cdi.testapp.TestQualifier;
import org.apache.wicket.markup.html.WebComponent;
import org.junit.Test;

/**
 * Tests for ComponentInjector
 */
public class ComponentInjectorTest extends WicketCdiTestCase
{
	@Inject
	AbstractCdiContainer container;

	@Inject
	ComponentInjector componentInjector;

	@Test
	public void innerNonStaticClass()
	{
		TestNonStaticComponent component = new TestNonStaticComponent("someId");
		assertNull(component.dependency);
		componentInjector.onInstantiation(component);
		if (container.isFeatureSupported(ContainerSupport.NON_STATIC_INNER_CLASS_INJECTION))
		{
			assertNotNull(component.dependency);
		} else
		{
			assertNull(component.dependency);
		}
	}

	@Test
	public void innerNonStaticClassGlobalDisabled()
	{
		Map<String, String> params =
				Collections.singletonMap(
						ContainerSupport.NON_STATIC_INNER_CLASS_INJECTION.getInitParameterName(),
						"false");
		getTester(true, params);
		TestNonStaticComponent component = new TestNonStaticComponent("someId");
		assertNull(component.dependency);
		componentInjector.onInstantiation(component);
		assertNull(component.dependency);
	}

	@Test
	public void innerStaticClass()
	{
		TestStaticComponent component = new TestStaticComponent("someId");
		componentInjector.onInstantiation(component);
		assertEquals(component.dependency, "Test String");
	}

	@Test
	public void anonymousInnerClass()
	{

		WebComponent component = new WebComponent("someId")
		{
			@Inject
			private String dependency;

			@Override
			public String toString()
			{
				return dependency;
			}
		};
		componentInjector.onInstantiation(component);
		if (container.isFeatureSupported(ContainerSupport.ANONYMOUS_INNER_CLASS_INJECTION))
		{
			assertNotNull(component.toString());
		} else
		{
			assertNull(component.toString());
		}
	}

	@Test
	public void anonymousInnerClassGlobalDisabled()
	{
		Map<String, String> params =
				Collections.singletonMap(
						ContainerSupport.ANONYMOUS_INNER_CLASS_INJECTION.getInitParameterName(),
						"false");
		getTester(true, params);
		WebComponent component = new WebComponent("someId")
		{
			@Inject
			private String dependency;

			@Override
			public String toString()
			{
				return dependency;
			}
		};
		componentInjector.onInstantiation(component);
		assertNull(component.toString());
	}

	private class TestNonStaticComponent extends WebComponent
	{

		@Inject
		private String dependency;

		public TestNonStaticComponent(String id)
		{
			super(id);
		}
	}

	private static class TestStaticComponent extends WebComponent
	{

		@Inject
		@TestQualifier
		private String dependency;

		public TestStaticComponent(String id)
		{
			super(id);
		}
	}
}
