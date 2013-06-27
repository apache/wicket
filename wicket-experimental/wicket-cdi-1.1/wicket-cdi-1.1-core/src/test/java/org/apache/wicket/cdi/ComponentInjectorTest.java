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

import javax.inject.Inject;

import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for ComponentInjector
 */
public class ComponentInjectorTest extends Assert
{
	private WicketTester tester;

	@Before
	public void before()
	{
		// starts an application so we can instantiate components
		tester = new WicketTester();
	}

	@After
	public void after()
	{
		tester.destroy();
		tester = null;
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-5226
	 */
	@Test
	public void innerNonStaticClass()
	{
            //Disabled for now
//		BeanManager beanManager = mock(BeanManager.class);
//		INonContextualManager nonContextualManager = mock(INonContextualManager.class);
//		AbstractCdiContainer cdiContainer = new AbstractCdiContainer(beanManager, nonContextualManager);
//		ComponentInjector injector = new ComponentInjector(cdiContainer);
//
//		TestNonStaticComponent component = new TestNonStaticComponent("someId");
//		assertNull(component.dependency);
//
//		injector.onInstantiation(component);
//
//		verify(nonContextualManager, never()).inject(any());
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-5226
	 */
	@Test
	public void innerStaticClass()
	{
            //Disabled for now need to add injectable test dependencies
//		BeanManager beanManager = mock(BeanManager.class);
//		INonContextualManager nonContextualManager = mock(INonContextualManager.class);
//		final String expectedValue = "injected";
//
//		doAnswer(new Answer<Void>()
//		{
//			@Override
//			public Void answer(InvocationOnMock invocation) throws Throwable
//			{
//				TestStaticComponent component = (TestStaticComponent) invocation.getArguments()[0];
//				component.dependency = expectedValue;
//
//				return null;
//			}
//		}).when(nonContextualManager).inject(any(TestStaticComponent.class));
//
//		AbstractCdiContainer cdiContainer = new AbstractCdiContainer(beanManager, nonContextualManager);
//		ComponentInjector injector = new ComponentInjector(cdiContainer);
//
//		TestStaticComponent component = new TestStaticComponent("someId");
//		assertNull(component.dependency);
//
//		injector.onInstantiation(component);
//
//		assertEquals(expectedValue, component.dependency);
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
		private String dependency;

		public TestStaticComponent(String id)
		{
			super(id);
		}
	}
}
