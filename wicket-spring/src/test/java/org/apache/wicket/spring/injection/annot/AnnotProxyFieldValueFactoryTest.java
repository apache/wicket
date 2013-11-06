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
package org.apache.wicket.spring.injection.annot;

import static org.hamcrest.CoreMatchers.instanceOf;

import java.lang.reflect.Field;

import org.apache.wicket.proxy.ILazyInitProxy;
import org.apache.wicket.spring.ISpringContextLocator;
import org.apache.wicket.spring.SpringBeanLocator;
import org.apache.wicket.spring.injection.util.Bean;
import org.apache.wicket.spring.injection.util.Bean2;
import org.apache.wicket.spring.injection.util.Injectable;
import org.apache.wicket.spring.injection.util.InjectableInterface;
import org.apache.wicket.spring.test.ApplicationContextMock;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

/**
 * Tests for BeanAnnotLocatorFactory
 * 
 * @author igor
 * 
 */
public class AnnotProxyFieldValueFactoryTest extends Assert
{
	ISpringContextLocator mockCtxLocator = new ISpringContextLocator()
	{
		private static final long serialVersionUID = 1L;

		@Override
		public ApplicationContext getSpringContext()
		{
			ApplicationContextMock mock = new ApplicationContextMock();
			mock.putBean(new Bean());
			mock.putBean("somebean", new Bean2());
			return mock;
		}
	};

	final InjectableInterface obj;

	/**
	 * Construct.
	 */
	public AnnotProxyFieldValueFactoryTest()
	{
		this(new Injectable());
	}

	protected AnnotProxyFieldValueFactoryTest(InjectableInterface injectable)
	{
		obj = injectable;
	}

	AnnotProxyFieldValueFactory factory = new AnnotProxyFieldValueFactory(mockCtxLocator);

	/**
	 * Test the factory
	 * 
	 * @throws Exception
	 */
	@Test
	public void testFactory() throws Exception
	{
		SpringBeanLocator locator = null;
		Object proxy = null;

		Field field = obj.getClass().getDeclaredField("nobean");
		proxy = factory.getFieldValue(field, obj);
		assertNull(proxy);

		field = obj.getClass().getDeclaredField("beanByClass");
		proxy = factory.getFieldValue(field, obj);
		locator = (SpringBeanLocator)((ILazyInitProxy)proxy).getObjectLocator();
		assertTrue(locator.getBeanType().equals(Bean.class));
		assertTrue(locator.getSpringContextLocator() == mockCtxLocator);
		assertThat(factory.getFieldValue(field, obj), instanceOf(ILazyInitProxy.class));

		field = obj.getClass().getDeclaredField("beanByName");
		proxy = factory.getFieldValue(field, obj);
		locator = (SpringBeanLocator)((ILazyInitProxy)proxy).getObjectLocator();
		assertTrue(locator.getBeanName().equals("somebean"));
		assertTrue(locator.getBeanType().equals(Bean2.class));
		assertTrue(locator.getSpringContextLocator() == mockCtxLocator);
		assertThat(factory.getFieldValue(field, obj), instanceOf(ILazyInitProxy.class));
	}

	/**
	 * test the cache, make sure the same proxy is returned for the same dependency it represents
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCache() throws Exception
	{
		Field field = obj.getClass().getDeclaredField("beanByClass");
		Object proxy1 = factory.getFieldValue(field, obj);
		Object proxy2 = factory.getFieldValue(field, obj);
		assertTrue(proxy1 == proxy2);

		field = obj.getClass().getDeclaredField("beanByName");
		proxy1 = factory.getFieldValue(field, obj);
		proxy2 = factory.getFieldValue(field, obj);
		assertTrue(proxy1 == proxy2);
	}

	/**
	 * Test creation fails with null springcontextlocator
	 */
	@Test
	public void testNullContextLocator()
	{
		try
		{
			new AnnotProxyFieldValueFactory(null);
			fail();
		}
		catch (IllegalArgumentException e)
		{
			// noop
		}
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testFailsIfBeanWithIdIsNotFound() throws Exception
	{
		InjectableWithReferenceToNonexistingBean obj = new InjectableWithReferenceToNonexistingBean();
		Field field = obj.getClass().getDeclaredField("nonExisting");
		try
		{
			final Bean bean = (Bean)factory.getFieldValue(field, obj);
			/*
			 * returned bean will not be null even though the bean is not found. what we get instead
			 * is a proxy. we invoke a method on the proxy in order to cause it to try to locate the
			 * bean and that is when it will fail
			 */
			bean.method();
			fail();
		}
		catch (RuntimeException e)
		{
		}
	}

	static class InjectableWithReferenceToNonexistingBean
	{
		@SpringBean(name = "nonExisting")
		private Bean nonExisting;
	}
}
