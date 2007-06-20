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
package org.apache.wicket.guice;

import junit.framework.TestCase;

import org.apache.wicket.Application;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.MockWebApplication;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.lang.Objects;

import com.google.inject.Binder;
import com.google.inject.Module;

public class GuiceInjectorTest extends TestCase
{
	public void testInjectionAndSerialization()
	{
		MockWebApplication mockApp = new MockWebApplication(new WebApplication() {
			@Override
			protected void outputDevelopmentModeWarning()
			{
				// Do nothing.
			}
			@Override
			public Class<WebPage> getHomePage()
			{
				return null;
			}
		}, null);
		
		// Make a new webapp and injector, and register the injector with the
		// webapp as a component instantiation listener.
		Application app = mockApp.getApplication();
		
		try
		{
			Application.set(app);
			GuiceComponentInjector injector = new GuiceComponentInjector(app, new Module()
			{
			
				public void configure(Binder binder)
				{
					binder.bind(ITestService.class).to(TestService.class);
				}
			
			});
			app.addComponentInstantiationListener(injector);
			
			// Create a new component. This should be automatically injected with the ITestService implementation.
			TestComponent testComponent = new TestComponent("id");
			
			// Make sure the service is injected.
			assertEquals(ITestService.EXPECTED_RESULT, testComponent.getTestService().getString());
			
			// Serialize and deserialize the object, and check it still works.
			TestComponent copiedComponent = (TestComponent)Objects.cloneObject(testComponent);
			assertEquals(ITestService.EXPECTED_RESULT, copiedComponent.getTestService().getString());
		}
		finally
		{
			Application.unset();
		}
	}
}
