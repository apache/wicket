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
package wicket.spring;

import junit.framework.TestCase;
import wicket.protocol.http.MockWebApplication;
import wicket.spring.test.ApplicationContextMock;
import wicket.spring.test.SpringContextLocatorMock;
import wicket.util.lang.Objects;
import wicket.util.tester.WicketTester;

/**
 * Tests {@link SpringBeanLocator}
 * 
 * @author ivaynberg
 * 
 */
public class SpringBeanLocatorTest extends TestCase
{
	private ApplicationContextMock ctx;

	private ISpringContextLocator ctxLocator;

	@Override
	protected void setUp() throws Exception
	{
		ctx = new ApplicationContextMock();
		ctxLocator = new SpringContextLocatorMock(ctx);
	}

	/**
	 * tests lookup of beans by class only
	 */
	public void testLookupByClass()
	{
		Bean bean = new Bean();

		ctx.putBean("bean", bean);

		SpringBeanLocator locator = new SpringBeanLocator(Bean.class, ctxLocator);
		assertTrue(locator.locateProxyTarget() == bean);
	}

	/**
	 * tests if lookup by class is still working after deserialization
	 */
	public void testLookupByClassAfterDeserialization()
	{
		new MockWebApplication(new WicketTester.DummyWebApplication(), "/");

		Bean bean = new Bean();

		ctx.putBean("bean", bean);

		SpringBeanLocator locator = (SpringBeanLocator) Objects
				.cloneObject(new SpringBeanLocator(Bean.class, ctxLocator));

		assertNotNull(locator.locateProxyTarget());
	}

	/**
	 * tests error if bean with class is not in the context
	 */
	public void testLookupByClassNotFound()
	{
		SpringBeanLocator locator = new SpringBeanLocator(Bean.class, ctxLocator);
		try
		{
			locator.locateProxyTarget();
			fail();
		}
		catch (IllegalStateException e)
		{
			// noop
		}
	}

	/**
	 * tests error when more then one bean of the same class found
	 */
	public void testLookupByClassTooManyFound()
	{
		Bean bean = new Bean();
		ctx.putBean("bean1", bean);
		ctx.putBean("bean2", bean);

		SpringBeanLocator locator = new SpringBeanLocator(Bean.class, ctxLocator);
		try
		{
			locator.locateProxyTarget();
			fail();
		}
		catch (IllegalStateException e)
		{
			// noop
		}

	}

	/**
	 * tests defaulting of lookup to bean name. when more then one bean of the
	 * requested class if found and the id is not explicitly specified, we
	 * default to a bean that has the same type name as the id it is registered
	 * with.
	 */
	public void testLookupDefaultToName()
	{
		Bean bean = new Bean();
		ctx.putBean("bean", bean);
		ctx.putBean("bean2", new Bean());

		SpringBeanLocator locator = new SpringBeanLocator(Bean.class, ctxLocator);
		assertTrue(bean == (Bean) locator.locateProxyTarget());

	}

	/**
	 * tests lookup by name
	 */
	public void testLookupByName()
	{
		Bean bean = new Bean();
		ctx.putBean("bean", bean);

		SpringBeanLocator locator = new SpringBeanLocator("bean", Bean.class, ctxLocator);
		assertTrue(locator.locateProxyTarget() == bean);

	}

	/**
	 * tests lookup by name after locator has been deserialized
	 */
	public void testLookupByNameAfterDeserialization()
	{
		new MockWebApplication(new WicketTester.DummyWebApplication(), "/");

		Bean bean = new Bean();
		ctx.putBean("bean", bean);

		SpringBeanLocator locator = (SpringBeanLocator) Objects
				.cloneObject(new SpringBeanLocator("bean", Bean.class, ctxLocator));

		assertNotNull(locator.locateProxyTarget());
	}

	/**
	 * tests error if no bean with name found
	 */
	public void testLookupByNameNotFound()
	{
		SpringBeanLocator locator = new SpringBeanLocator("bean", Bean.class, ctxLocator);
		try
		{
			locator.locateProxyTarget();
			fail();
		}
		catch (IllegalStateException e)
		{
			// noop
		}
	}

	/**
	 * tests constructor argument checks
	 */
	public void testConstructorArguments()
	{
		try
		{
			SpringBeanLocator locator = new SpringBeanLocator(null, ctxLocator);
			fail();
		}
		catch (IllegalArgumentException e)
		{
			// noop
		}

		try
		{
			SpringBeanLocator locator = new SpringBeanLocator(Bean.class, null);
			fail();
		}
		catch (IllegalArgumentException e)
		{
			// noop
		}
	}

	/**
	 * tests error when context not found
	 */
	public void testContextNotFound()
	{
		SpringContextLocatorMock ctxLocator = new SpringContextLocatorMock(null);
		SpringBeanLocator locator = new SpringBeanLocator(Bean.class, ctxLocator);
		try
		{
			locator.locateProxyTarget();
		}
		catch (IllegalStateException e)
		{
			// noop
		}
	}

	/**
	 * tests equals and hashcode contracts
	 */
	public void testEqualsAndHashcode()
	{
		SpringBeanLocator a = new SpringBeanLocator("bean", SpringBeanLocator.class,
				ctxLocator);
		SpringBeanLocator aprime = new SpringBeanLocator("bean", SpringBeanLocator.class,
				ctxLocator);

		SpringBeanLocator b = new SpringBeanLocator("bean2", SpringBeanLocator.class,
				ctxLocator);
		SpringBeanLocator c = new SpringBeanLocator("bean", SpringBeanLocatorTest.class,
				ctxLocator);

		SpringBeanLocator d = new SpringBeanLocator(SpringBeanLocator.class, ctxLocator);
		SpringBeanLocator dprime = new SpringBeanLocator(SpringBeanLocator.class,
				ctxLocator);

		SpringBeanLocator e = new SpringBeanLocator(SpringBeanLocatorTest.class,
				ctxLocator);

		assertEquals(a, aprime);
		assertEquals(aprime, a);
		assertEquals(a.hashCode(), aprime.hashCode());

		assertFalse(a.equals(b));
		assertFalse(a.equals(c));
		assertFalse(b.equals(c));

		assertEquals(d, dprime);
		assertEquals(dprime, d);
		assertEquals(d.hashCode(), dprime.hashCode());

		assertFalse(a.equals(d));
		assertFalse(d.equals(e));

		assertFalse(a.equals(ctxLocator));
	}

}
