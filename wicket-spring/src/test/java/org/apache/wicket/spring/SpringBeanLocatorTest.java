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
package org.apache.wicket.spring;

import org.apache.wicket.core.util.lang.WicketObjects;
import org.apache.wicket.spring.test.ApplicationContextMock;
import org.apache.wicket.spring.test.SpringContextLocatorMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests {@link SpringBeanLocator}
 * 
 * @author ivaynberg
 * 
 */
public class SpringBeanLocatorTest extends Assert
{
	private ApplicationContextMock ctx;

	private ISpringContextLocator ctxLocator;

	/**
	 * 
	 */
	@Before
	public void before()
	{
		ctx = new ApplicationContextMock();
		ctxLocator = new SpringContextLocatorMock(ctx);
	}

	/**
	 * tests lookup of beans by class only
	 */
	@Test
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
	@Test
	public void testLookupByClassAfterDeserialization()
	{
		Bean bean = new Bean();

		ctx.putBean("bean", bean);

		SpringBeanLocator locator = WicketObjects.cloneObject(new SpringBeanLocator(
			Bean.class, ctxLocator));

		assertNotNull(locator.locateProxyTarget());
	}

	/**
	 * tests error if bean with class is not in the context
	 */
	@Test
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
	@Test
	public void testLookupByClassTooManyFound()
	{
		Bean bean = new Bean();
		ctx.putBean("somebean", bean);
		ctx.putBean("somebean2", bean);

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
	 * tests lookup by name
	 */
	@Test
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
	@Test
	public void testLookupByNameAfterDeserialization()
	{
		Bean bean = new Bean();
		ctx.putBean("bean", bean);

		SpringBeanLocator locator = WicketObjects.cloneObject(new SpringBeanLocator(
			"bean", Bean.class, ctxLocator));

		assertNotNull(locator.locateProxyTarget());
	}

	/**
	 * tests error if no bean with name found
	 */
	@Test
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
	@Test
	public void testConstructorArguments()
	{
		try
		{
			new SpringBeanLocator(null, ctxLocator);
			fail();
		}
		catch (IllegalArgumentException e)
		{
			// noop
		}

		try
		{
			new SpringBeanLocator(Bean.class, null);
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
	@Test
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
	@Test
	public void testEqualsAndHashcode()
	{
		SpringBeanLocator a = new SpringBeanLocator("bean", SpringBeanLocator.class, ctxLocator);
		SpringBeanLocator aprime = new SpringBeanLocator("bean", SpringBeanLocator.class,
			ctxLocator);

		SpringBeanLocator b = new SpringBeanLocator("bean2", SpringBeanLocator.class, ctxLocator);
		SpringBeanLocator c = new SpringBeanLocator("bean", SpringBeanLocatorTest.class, ctxLocator);

		SpringBeanLocator d = new SpringBeanLocator(SpringBeanLocator.class, ctxLocator);
		SpringBeanLocator dprime = new SpringBeanLocator(SpringBeanLocator.class, ctxLocator);

		SpringBeanLocator e = new SpringBeanLocator(SpringBeanLocatorTest.class, ctxLocator);

		assertEquals(a, aprime);
		assertEquals(aprime, a);
		assertEquals(a.hashCode(), aprime.hashCode());

		assertFalse(a.equals(b));
		assertFalse(a.equals(c));
		assertFalse(b.equals(c));

		assertEquals(d, dprime);
		assertEquals(dprime, d);
		ctx.putBean("locator", a); // we need to register a Bean of type d.getClass()
		assertEquals(d.hashCode(), dprime.hashCode());

		assertFalse(a.equals(d));
		assertFalse(d.equals(e));

		assertFalse(a.equals(ctxLocator));
	}

}
