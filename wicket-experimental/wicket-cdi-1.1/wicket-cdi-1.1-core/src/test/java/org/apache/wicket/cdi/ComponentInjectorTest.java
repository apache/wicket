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

import org.apache.wicket.cdi.testapp.TestAppScope;
import org.apache.wicket.cdi.testapp.TestConversationBean;
import org.apache.wicket.cdi.testapp.TestQualifier;
import org.apache.wicket.markup.html.WebComponent;
import org.jglue.cdiunit.AdditionalClasses;
import org.junit.Test;

/**
 * Tests for ComponentInjector
 */
@AdditionalClasses({TestAppScope.class, TestConversationBean.class})
public class ComponentInjectorTest extends WicketCdiTestCase
{
	/**
	 * https://issues.apache.org/jira/browse/WICKET-5226
	 */
	@Test
	public void innerNonStaticClass()
	{
		TestNonStaticComponent component = new TestNonStaticComponent("someId");
		assertEquals(component.dependency, "Test String");
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-5226
	 */
	@Test
	public void innerStaticClass()
	{
		TestStaticComponent component = new TestStaticComponent("someId");
		assertEquals(component.dependency, "Test String");
	}

	@Test
	public void anonymousInnerClass()
	{

		WebComponent component = new WebComponent("someId")
		{
			@Inject
			@TestQualifier
			private String dependency;

			@Override
			public String toString()
			{
				return dependency;
			}
		};
		assertEquals(component.toString(), "Test String");
	}

	private class TestNonStaticComponent extends WebComponent
	{

		@Inject
		@TestQualifier
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
