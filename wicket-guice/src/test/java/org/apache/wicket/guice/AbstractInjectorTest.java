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

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Named;

import org.apache.wicket.Session;
import org.apache.wicket.ThreadContext;
import org.apache.wicket.core.util.lang.WicketObjects;
import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.mock.MockWebRequest;
import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.protocol.http.mock.MockServletContext;
import org.apache.wicket.request.Url;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

/**
 */
public abstract class AbstractInjectorTest extends Assert
{

	protected abstract TestNoComponentInterface newTestNoComponent();

	protected abstract TestComponentInterface newTestComponent(String id);

	private final MockApplication app = new MockApplication();

	@Before
	public void before()
	{
		app.setServletContext(new MockServletContext(app, null));

		ThreadContext.setApplication(app);

		app.setName(getClass().getName());
		app.initApplication();

		Session session = new WebSession(new MockWebRequest(Url.parse("/")));
		app.getSessionStore().bind(null, session);
		ThreadContext.setSession(session);

		GuiceComponentInjector injector = new GuiceComponentInjector(app, new Module()
		{
			@Override
			public void configure(final Binder binder)
			{
				binder.bind(ITestService.class).to(TestService.class);
				binder.bind(ITestService.class)
						.annotatedWith(Red.class)
						.to(TestServiceRed.class);
				binder.bind(ITestService.class)
						.annotatedWith(Blue.class)
						.to(TestServiceBlue.class);
				binder.bind(new TypeLiteral<Map<String, String>>()
				{
				}).toProvider(new Provider<Map<String, String>>()
				{
					@Override
					public Map<String, String> get()
					{
						Map<String, String> strings = new HashMap<>();

						strings.put(ITestService.RESULT, ITestService.RESULT);

						return strings;
					}
				});

				binder.bind(String.class).annotatedWith(Names.named("named1")).toInstance("NAMED_1");
				binder.bind(String.class).annotatedWith(Names.named("named2")).toInstance("NAMED_2");

				binder.bind(String.class).annotatedWith(new Jsr330Named("named1")).toInstance("NAMED_1");
				binder.bind(String.class).annotatedWith(new Jsr330Named("named2")).toInstance("NAMED_2");
				binder.bind(EvilTestService.class).toInstance(new EvilTestService("evil123", 5));
		   }

		});
		app.getComponentInstantiationListeners().add(injector);
	}

	@After
	public void after()
	{
		app.internalDestroy();
		ThreadContext.detach();
	}

	/**
	 * testInjectionAndSerialization()
	 */
	@Test
	public void testInjectionAndSerialization()
	{
		// Create a new component, which should be automatically injected,
		// and test to make sure the injection has worked.
		TestComponentInterface testComponent = newTestComponent("id");
		doChecksForComponent(testComponent);

		// Serialize and deserialize the object, and check it still works.
		TestComponentInterface clonedComponent = (TestComponentInterface)WicketObjects.cloneObject(testComponent);
		doChecksForComponent(clonedComponent);

		// Test injection of a class that does not extend Component
		TestNoComponentInterface noncomponent = newTestNoComponent();
		doChecksForNoComponent(noncomponent);
	}

	private void doChecksForNoComponent(final TestNoComponentInterface noncomponent)
	{
		assertEquals(ITestService.RESULT_RED, noncomponent.getString());
	}

	private void doChecksForComponent(final TestComponentInterface component)
	{
		assertEquals(ITestService.RESULT, component.getInjectedField().getString());
		assertEquals(null, component.getInjectedOptionalField());
		assertEquals(ITestService.RESULT_RED, component.getInjectedFieldRed().getString());
		assertEquals(ITestService.RESULT_BLUE, component.getInjectedFieldBlue().getString());

		assertEquals(ITestService.RESULT, component.getInjectedFieldProvider().get().getString());

		assertEquals(ITestService.RESULT,
			component.getInjectedTypeLiteralField().get(ITestService.RESULT));

		assertEquals("NAMED_1", component.getNamed1());
		assertEquals("NAMED_2", component.getNamed2());
		assertEquals("evil123", component.getEvilId());
	}

	/**
	 * Helper class to make binding of an instance of javax.inject.Named less verbose
	 */
	private static class Jsr330Named implements Named
	{
		private final String name;

		private Jsr330Named(String name)
		{
			this.name = name;
		}

		@Override
		public String value()
		{
			return name;
		}

		@Override
		public Class<? extends Annotation> annotationType()
		{
			return Named.class;
		}
	}
}
