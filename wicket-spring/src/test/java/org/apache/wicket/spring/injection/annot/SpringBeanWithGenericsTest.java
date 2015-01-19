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

import java.util.Arrays;
import java.util.List;

import org.apache.wicket.spring.BeanWithGeneric;
import org.apache.wicket.util.tester.DummyHomePage;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

public class SpringBeanWithGenericsTest extends Assert
{
	private WicketTester tester;
	private AnnotationConfigApplicationContext ctx;

	/**
	 * @throws Exception
	 */
	@Before
	public void before() throws Exception
	{
		tester = new WicketTester();

		ctx = new AnnotationConfigApplicationContext();
		ctx.register(ConfigContextWithGenerics.class);
		ctx.refresh();

		SpringComponentInjector springInjector = new SpringComponentInjector(
			tester.getApplication(), ctx);

		tester.getApplication().getComponentInstantiationListeners().add(springInjector);
	}

	@Test
	public void genericAsQualifier() throws Exception
	{
		AnnotatedBeanGenericQualifier page = 
			tester.startPage(new AnnotatedBeanGenericQualifier());

		assertNotNull(page.getBean());
	}

	@Test
	public void listOfGenerics() throws Exception
	{
		AnnotatedListOfBeanGenericQualifier page = 
			tester.startPage(new AnnotatedListOfBeanGenericQualifier());

		assertNotNull(page.getBeans());
		assertEquals(2, page.getBeans().size());
	}
	
	@Test
	public void listField() throws Exception
	{
		AnnotatedListField page = 
			tester.startPage(new AnnotatedListField());

		assertNotNull(page.getStringsList());
		assertEquals(3, page.getStringsList().size());
	}
	
	@Test
	public void listOfTypedGenerics() throws Exception
	{
		AnnotatedListOfBeanTypeQualifier page = 
			tester.startPage(new AnnotatedListOfBeanTypeQualifier());

		assertNotNull(page.getBeans());
		assertEquals(1, page.getBeans().size());
	}

	class AnnotatedBeanGenericQualifier extends DummyHomePage
	{
		@SpringBean
		private BeanWithGeneric<String> bean;

		public BeanWithGeneric<String> getBean()
		{
			return bean;
		}
	}

	class AnnotatedListOfBeanGenericQualifier extends DummyHomePage
	{
		@SpringBean
		private List<BeanWithGeneric<?>> beans;

		public List<BeanWithGeneric<?>> getBeans()
		{
			return beans;
		}
	}
	
	class AnnotatedListOfBeanTypeQualifier extends DummyHomePage
	{
		@SpringBean
		private List<BeanWithGeneric<Integer>> beans;

		public List<BeanWithGeneric<Integer>> getBeans()
		{
			return beans;
		}
	}

	class AnnotatedListField extends DummyHomePage
	{
		@SpringBean
		private List<String> stringsList;

		public List<String> getStringsList()
		{
			return stringsList;
		}
	}

	@Configuration
	public static class ConfigContextWithGenerics
	{
		@Bean
		public BeanWithGeneric<String> stringBean()
		{
			return new BeanWithGeneric<>();
		}

		@Bean
		public BeanWithGeneric<Integer> nestedBean()
		{
			return new BeanWithGeneric<>();
		}

		@Bean
		public List<String> strings()
		{
			return Arrays.asList("foo", "bar", "baz");
		}
	}
}
