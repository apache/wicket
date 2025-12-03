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

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.lang.reflect.Field;
import java.util.stream.Stream;

import org.apache.wicket.proxy.ILazyInitProxy;
import org.apache.wicket.proxy.IProxyTargetLocator;
import org.apache.wicket.spring.test.ApplicationContextMock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.support.AbstractBeanDefinition;

import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * Tests for AnnotProxyFieldValueFactory
 * 
 * @author igor
 * @author hosea
 */
public class AnnotProxyFieldValueFactoryTest
{

	@ParameterizedTest
	@MethodSource("beans")
	public void shouldCreateProxyForBeanName(final Object obj) throws Exception
	{
		final Bean somebean = new Bean();
		final ApplicationContextMock applicationContext = new ApplicationContextMock();
		// add two beans to make sure wiring by name is different by wiring by class
		applicationContext.putBean(new Bean());
		applicationContext.putBean("somebean", somebean);
		final AnnotProxyFieldValueFactory factory = new AnnotProxyFieldValueFactory(() -> applicationContext);
		final Field beanByClassField = obj.getClass().getDeclaredField("beanByName");
		final Object beanByClassProxy = factory.getFieldValue(beanByClassField, obj);
		final ILazyInitProxy lazyInitProxy = assertInstanceOf(ILazyInitProxy.class, beanByClassProxy);
		final IProxyTargetLocator beanByClassLocator = lazyInitProxy.getObjectLocator();
		assertSame(somebean, beanByClassLocator.locateProxyTarget());
	}

	@ParameterizedTest
	@MethodSource("beans")
	public void shouldThrowExceptionIfBeanNameNotFound(final Object obj) throws Exception
	{
		final Bean somebean = new Bean();
		final ApplicationContextMock applicationContext = new ApplicationContextMock();
		// add two beans to make sure wiring by name is different by wiring by class
		applicationContext.putBean(new Bean());
		applicationContext.putBean("wrongNameBean", somebean);
		final AnnotProxyFieldValueFactory factory = new AnnotProxyFieldValueFactory(() -> applicationContext);

		final Field beanByClassField = obj.getClass().getDeclaredField("beanByName");
		Assertions.assertThrows(IllegalStateException.class,  () -> factory.getFieldValue(beanByClassField, obj));
	}

	@ParameterizedTest
	@MethodSource("beans")
	public void shouldCreateProxyForClass(final Object obj) throws Exception
	{
		final Bean bean = new Bean();
		final ApplicationContextMock applicationContext = new ApplicationContextMock();
		applicationContext.putBean(bean);
		final AnnotProxyFieldValueFactory factory = new AnnotProxyFieldValueFactory(() -> applicationContext);

		final Field beanByClassField = obj.getClass().getDeclaredField("beanByClass");
		final Object proxy = factory.getFieldValue(beanByClassField, obj);
		final ILazyInitProxy lazyInitProxy = assertInstanceOf(ILazyInitProxy.class, proxy);
		final IProxyTargetLocator beanByClassLocator = lazyInitProxy.getObjectLocator();
		assertSame(bean, beanByClassLocator.locateProxyTarget());
	}

	@ParameterizedTest
	@MethodSource("beans")
	public void shouldThrowException_beanNameAmbiguous(final Object obj) throws Exception
	{
		final Bean primaryBean = new Bean();
		final ApplicationContextMock applicationContext = new ApplicationContextMock();
		// add two beans to make ambiguous
		applicationContext.putBean("anyBean", new Bean());
		applicationContext.putBean("primaryBean", primaryBean);
		final AnnotProxyFieldValueFactory factory = new AnnotProxyFieldValueFactory(() -> applicationContext);

		final Field beanByClassField = obj.getClass().getDeclaredField("beanByClass");
		Assertions.assertThrows(IllegalStateException.class, () -> factory.getFieldValue(beanByClassField, obj));
	}

	@ParameterizedTest
	@MethodSource("beans")
	public void shouldCreateProxyForUniquePrimary_beanNameAmbiguous(final Object obj) throws Exception
	{
		final Bean primaryBean = new Bean();
		final ApplicationContextMock applicationContext = new ApplicationContextMock();
		// add two beans to make ambiguous
		applicationContext.putBean("anyBean", new Bean());
		applicationContext.putBean("primaryBean", primaryBean);
		applicationContext.getBeanFactory().getBeanDefinition("primaryBean").setPrimary(true);
		final AnnotProxyFieldValueFactory factory = new AnnotProxyFieldValueFactory(() -> applicationContext);

		final Field beanByClassField = obj.getClass().getDeclaredField("beanByClass");
		final Object beanByClassProxy = factory.getFieldValue(beanByClassField, obj);
		final ILazyInitProxy lazyInitProxy = assertInstanceOf(ILazyInitProxy.class, beanByClassProxy);
		final IProxyTargetLocator beanByClassLocator = lazyInitProxy.getObjectLocator();
		assertSame(primaryBean, beanByClassLocator.locateProxyTarget());
	}

	@ParameterizedTest
	@MethodSource("beans")
	public void shouldCreateProxyForFieldname_beanNameAmbiguous(final Object obj) throws Exception
	{
		final Bean bean = new Bean();
		final ApplicationContextMock applicationContext = new ApplicationContextMock();
		applicationContext.putBean(new Bean());
		applicationContext.putBean("beanByClass", bean);
		final AnnotProxyFieldValueFactory factory = new AnnotProxyFieldValueFactory(() -> applicationContext);

		final Field beanByClassField = obj.getClass().getDeclaredField("beanByClass");
		final Object proxy = factory.getFieldValue(beanByClassField, obj);
		final ILazyInitProxy lazyInitProxy = assertInstanceOf(ILazyInitProxy.class, proxy);
		final IProxyTargetLocator beanByClassLocator = lazyInitProxy.getObjectLocator();
		assertSame(bean, beanByClassLocator.locateProxyTarget());
	}

	@ParameterizedTest
	@MethodSource("beans")
	public void shouldIgnoreUnannotatedFields(final Object obj) throws Exception
	{
		final AnnotProxyFieldValueFactory factory = new AnnotProxyFieldValueFactory(ApplicationContextMock::new);

		final Field beanByClassField = obj.getClass().getDeclaredField("nobean");
		final Object beanByClassProxy = factory.getFieldValue(beanByClassField, obj);
		assertNull(beanByClassProxy);
	}

	// https://issues.apache.org/jira/browse/WICKET-7170
	@ParameterizedTest
	@MethodSource("beans")
	public void shouldCreateProxyForUniqueDefaultCandidate_beanNameAmbiguous(final Object obj) throws Exception
	{
		final Bean defaultCandidate = new Bean();
		final ApplicationContextMock applicationContext = new ApplicationContextMock();
		// add two beans to make ambiguous
		applicationContext.putBean("anyBean", new Bean());
		applicationContext.putBean("primaryBean", defaultCandidate);
		final AbstractBeanDefinition abstractBeanDefinition = (AbstractBeanDefinition) applicationContext.getBeanFactory().getBeanDefinition("anyBean");
		abstractBeanDefinition.setDefaultCandidate(false);
		final AnnotProxyFieldValueFactory factory = new AnnotProxyFieldValueFactory(() -> applicationContext);

		final Field beanByClassField = obj.getClass().getDeclaredField("beanByClass");
		final Object beanByClassProxy = factory.getFieldValue(beanByClassField, obj);
		final ILazyInitProxy lazyInitProxy = assertInstanceOf(ILazyInitProxy.class, beanByClassProxy);
		final IProxyTargetLocator beanByClassLocator = lazyInitProxy.getObjectLocator();
		assertSame(defaultCandidate, beanByClassLocator.locateProxyTarget());
	}

	/**
	 * test the cache, make sure the same proxy is returned for the same dependency it represents
	 */
	@ParameterizedTest
	@MethodSource("beans")
	public void testCacheForBeanName(final Object obj) throws Exception
	{
		final ApplicationContextMock applicationContext = new ApplicationContextMock();
		applicationContext.putBean(new Bean());
		final AnnotProxyFieldValueFactory factory = new AnnotProxyFieldValueFactory(() -> applicationContext);

		final Field field = obj.getClass().getDeclaredField("beanByClass");
		final Object proxy1 = factory.getFieldValue(field, obj);
		final Object proxy2 = factory.getFieldValue(field, obj);
		assertSame(proxy1, proxy2);
	}

	@ParameterizedTest
	@MethodSource("beans")
	public void testCacheForClass(final Object obj) throws Exception
	{
		final ApplicationContextMock applicationContext = new ApplicationContextMock();
		applicationContext.putBean(new Bean());
		applicationContext.putBean("somebean", new Bean());
		final AnnotProxyFieldValueFactory factory = new AnnotProxyFieldValueFactory(() -> applicationContext);

		final Field field = obj.getClass().getDeclaredField("beanByName");
		final Object proxy1 = factory.getFieldValue(field, obj);
		final Object proxy2 = factory.getFieldValue(field, obj);
		assertSame(proxy1, proxy2);
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-5686
	 * @throws Exception
	 */
	@ParameterizedTest
	@MethodSource("beans")
	public void required(final Object obj) throws Exception
	{
		final AnnotProxyFieldValueFactory factory = new AnnotProxyFieldValueFactory(ApplicationContextMock::new);
		final Field field = obj.getClass().getDeclaredField("beanByClass");
		Assertions.assertThrows(IllegalStateException.class, () -> factory.getFieldValue(field, obj));
	}

	@Test
	public void optional() throws Exception
	{
		final AnnotProxyFieldValueFactory factory = new AnnotProxyFieldValueFactory(ApplicationContextMock::new);
		final SpringBeanInjectable springBeanInjectable = new SpringBeanInjectable();
		final Field field = springBeanInjectable.getClass().getDeclaredField("optional");
		Assertions.assertNull(factory.getFieldValue(field, springBeanInjectable));
	}

	@ParameterizedTest
	@MethodSource("beans")
	public void lookupNonProxy(final Object obj) throws Exception
	{
		final Bean bean = new Bean();
		final ApplicationContextMock applicationContext = new ApplicationContextMock();
		applicationContext.putBean(bean);
		final boolean wrapInProxies = false;
		final AnnotProxyFieldValueFactory factory = new AnnotProxyFieldValueFactory(() -> applicationContext, wrapInProxies);

		final Field beanByClassField = obj.getClass().getDeclaredField("beanByClass");
		final Object value = factory.getFieldValue(beanByClassField, obj);
		assertSame(bean, value);
	}

	private static Stream<Object> beans() {
		return Stream.of(new SpringBeanInjectable(), new JakartaInjectInjectable());
	}

	/**
	 * Test creation fails with null springcontextlocator
	 * Mock for an object with some Jakarta-Inject annotations
	 */
   @Test
   public void testNullContextLocator()
   {
	   Assertions.assertThrows(IllegalArgumentException.class, () -> new AnnotProxyFieldValueFactory(null));
   }

	/**
	 * Class with Jakarta-Inject annotations for several scenarios:
	 * <UL>
	 *     <LI>@{@link Inject} inject by class</LI>
	 *     <LI>@{@link Inject} inject by name / @{@link Named}</LI>
	 *     <LI>no possible injection as not annotated</LI>
	 * </UL>
	 * Same property names for the same scenarios as in {@link SpringBeanInjectable}
	 * so both beans can be used with the same test methods, this makes @ParameterizedTest - Tests possible
	 */
	public static class JakartaInjectInjectable
	{
		private Bean nobean;

		@Inject
		private Bean beanByClass;

		@Inject
		@Named("somebean")
		private Bean beanByName;

		@Override
		public String toString() 
		{
			return "JakartaInjectInjectable";
		}
	}

	/**
	 * Class with SpringBean annotations for several scenarios:
	 * * <UL>
	 * 		<LI>@{@link SpringBean} inject by class</LI>
	 * 		<LI>@{@link SpringBean} inject by name / "name=..."</LI>
	 * 	    <LI>no possible injection as not annotated</LI>
	 * 	</UL>
	 * 	Same property names for the same scenarios as in {@link JakartaInjectInjectable},
	 * 	so both beans can be used with the same test methods, this makes @ParameterizedTest - Tests possible
	 *
	 * 	Additional this class has an optional injection to test the required=false feature
	 */
	public static class SpringBeanInjectable
	{
		private Bean nobean;

		@SpringBean
		private Bean beanByClass;

		@SpringBean(name = "somebean")
		private Bean beanByName;

		@SpringBean(required = false)
		private Bean optional;

		@Override
		public String toString() {
			return "SpringBeanInjectable";
		}
	}

	/**
	 * Just a type.
	 */
	public static class Bean
	{
	}
}
