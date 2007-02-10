/*
 * $Id$
 * $Revision$
 * $Date$
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.spring.injection.annot;

import java.lang.reflect.Field;

import junit.framework.TestCase;

import org.springframework.context.ApplicationContext;

import wicket.proxy.ILazyInitProxy;
import wicket.spring.ISpringContextLocator;
import wicket.spring.SpringBeanLocator;
import wicket.spring.injection.annot.AnnotProxyFieldValueFactory;
import wicket.spring.injection.util.Bean;
import wicket.spring.injection.util.Bean2;
import wicket.spring.injection.util.Injectable;
import wicket.spring.test.ApplicationContextMock;

/**
 * Tests for BeanAnnotLocatorFactory
 * 
 * @author igor
 * 
 */
public class AnnotProxyFieldValueFactoryTest extends TestCase {
	ISpringContextLocator mockCtxLocator = new ISpringContextLocator() {
		private static final long serialVersionUID = 1L;

		public ApplicationContext getSpringContext() {
			ApplicationContextMock mock = new ApplicationContextMock();
			mock.putBean(new Bean());
			mock.putBean("somebean", new Bean2());
			return mock;
		}
	};

	Injectable obj = new Injectable();

	AnnotProxyFieldValueFactory factory = new AnnotProxyFieldValueFactory(
			mockCtxLocator);

	/**
	 * Test the factory
	 * 
	 * @throws Exception
	 */
	public void testFactory() throws Exception {
		SpringBeanLocator locator = null;
		Object proxy = null;

		Field field = obj.getClass().getDeclaredField("nobean");
		proxy = factory.getFieldValue(field, obj);
		assertNull(proxy);

		field = obj.getClass().getDeclaredField("beanByClass");
		proxy = factory.getFieldValue(field, obj);
		locator = (SpringBeanLocator) ((ILazyInitProxy) proxy)
				.getObjectLocator();
		assertTrue(locator.getBeanName() == null
				|| locator.getBeanName().length() == 0);
		assertTrue(locator.getBeanType().equals(Bean.class));
		assertTrue(locator.getSpringContextLocator() == mockCtxLocator);
		assertTrue(factory.getFieldValue(field, obj) instanceof ILazyInitProxy);

		field = obj.getClass().getDeclaredField("beanByName");
		proxy = factory.getFieldValue(field, obj);
		locator = (SpringBeanLocator) ((ILazyInitProxy) proxy)
				.getObjectLocator();
		assertTrue(locator.getBeanName().equals("somebean"));
		assertTrue(locator.getBeanType().equals(Bean2.class));
		assertTrue(locator.getSpringContextLocator() == mockCtxLocator);
		assertTrue(factory.getFieldValue(field, obj) instanceof ILazyInitProxy);
	}

	/**
	 * test the cache, make sure the same proxy is returned for the same
	 * dependency it represents
	 * 
	 * @throws Exception
	 */
	public void testCache() throws Exception {
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
	public void testNullContextLocator() {
		try {
			new AnnotProxyFieldValueFactory(null);
			fail();
		} catch (IllegalArgumentException e) {
			// noop
		}
	}
}
