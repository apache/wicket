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

/**
 * Tests for AnnotProxyFieldValueFactory
 * https://issues.apache.org/jira/browse/WICKET-7170
 * 
 * @author hosea
 */
public class AnnotProxyFieldValueFactoryDefaultCandidateTest
{

	@ParameterizedTest
	@MethodSource("beans")
	public void shouldCreateProxyForUniqueDefaultCandidate_beanNameAmbiguous(final Object obj) throws Exception {
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


	private static Stream<Object> beans() {
		return Stream.of(new SpringBeanInjectable(), new JakartaInjectInjectable());
	}

	/**
	 * Test creation fails with null springcontextlocator
	 */
	@Test
	public void testNullContextLocator()
	{
		Assertions.assertThrows(IllegalArgumentException.class, () -> new AnnotProxyFieldValueFactory(null));
	}


	public static class JakartaInjectInjectable
	{
		@Inject
		private Bean beanByClass;

		@Override
		public String toString() {
			return "JakartaInjectInjectable";
		}
	}

	public static class SpringBeanInjectable
	{
		@SpringBean
		private Bean beanByClass;

		@Override
		public String toString() {
			return "SpringBeanInjectable";
		}
	}

	/**
	 * Mock spring bean
	 */
	public static class Bean
	{
	}
}
