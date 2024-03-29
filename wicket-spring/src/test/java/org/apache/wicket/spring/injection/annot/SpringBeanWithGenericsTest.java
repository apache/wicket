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

import jakarta.inject.Inject;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.spring.BeanWithGeneric;
import org.apache.wicket.util.tester.DummyHomePage;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Andrea Del Bene
 *
 */
class SpringBeanWithGenericsTest
{
	private WicketTester tester;
	private AnnotationConfigApplicationContext ctx;

	@BeforeEach
	void before()
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
	void genericAsQualifier()
	{
		AnnotatedBeanGenericQualifier page = 
			tester.startPage(new AnnotatedBeanGenericQualifier());

		assertNotNull(page.getBean());
	}

	@Test
	void listOfGenerics()
	{
		AnnotatedListOfBeanGenericQualifier page = 
			tester.startPage(new AnnotatedListOfBeanGenericQualifier());

		List<BeanWithGeneric<?>> beans = page.getBeans();
		
		assertNotNull(beans);
		assertEquals(2, beans.size());
		
		assertTrue(beans.contains(ctx.getBean("stringBean")));
		assertTrue(beans.contains(ctx.getBean("integerBean")));
	}

	@Test
	void listOfStringGenerics()
	{
		AnnotatedListOfBeanStringGenericQualifier page =
				tester.startPage(new AnnotatedListOfBeanStringGenericQualifier());

		List<BeanWithGeneric<String>> beans = page.getBeans();

		assertNotNull(beans);
		assertEquals(1, beans.size());

		BeanWithGeneric<String> stringBean = (BeanWithGeneric<String>) ctx.getBean("stringBean");
		assertTrue(beans.contains(stringBean));
	}
	
	@Test
	void mapOfGenerics()
	{
		AnnotatedMapOfBeanGenericQualifier page = 
			tester.startPage(new AnnotatedMapOfBeanGenericQualifier());

		Map<String, BeanWithGeneric<?>> beans = page.getBeans();

		assertNotNull(beans);
		assertEquals(2, beans.size());

		assertTrue(beans.containsKey("stringBean"));
		assertTrue(beans.containsKey("integerBean"));
	}
	
	@Test
	void setOfGenerics()
	{
		AnnotatedSetOfBeanGenericQualifier page = 
			tester.startPage(new AnnotatedSetOfBeanGenericQualifier());

		Set<BeanWithGeneric<?>> beans = page.getBeans();
		
		assertNotNull(beans);
		assertEquals(2, beans.size());
		
		assertTrue(beans.contains(ctx.getBean("stringBean")));
		assertTrue(beans.contains(ctx.getBean("integerBean")));
	}
	
	@Test
	void listField()
	{
		AnnotatedListField page =
			tester.startPage(new AnnotatedListField());

		List<String> stringsList = page.getStringsList();
		assertNotNull(stringsList);
		assertEquals(3, stringsList.size());
		assertEquals("foo", stringsList.get(0));
		assertEquals("bar", stringsList.get(1));
		assertEquals("baz", stringsList.get(2));
		
		ArrayList<String> arrayListStrings = page.getArrayListStrings();
		assertNotNull(arrayListStrings);
		assertEquals(3, arrayListStrings.size());
		assertEquals("one", arrayListStrings.get(0));
		assertEquals("two", arrayListStrings.get(1));
		assertEquals("three", arrayListStrings.get(2));

		List<Integer> arrayListIntegers = page.getArrayListIntegers();
		assertNotNull(arrayListIntegers);
		assertEquals(3, arrayListIntegers.size());
		assertEquals(Integer.valueOf(1), arrayListIntegers.get(0));
		assertEquals(Integer.valueOf(2), arrayListIntegers.get(1));
		assertEquals(Integer.valueOf(3), arrayListIntegers.get(2));

		MyList<String> myList = page.getMyList();
		assertNotNull(myList);
		assertEquals(1, myList.size());
		assertEquals("one", myList.get(0));
	}

	@Test
	void listOfTypedGenerics()
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

	class AnnotatedListOfBeanStringGenericQualifier extends DummyHomePage
	{
		@SpringBean
		private List<BeanWithGeneric<String>> beans;

		public List<BeanWithGeneric<String>> getBeans()
		{
			return beans;
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
	
	class AnnotatedMapOfBeanGenericQualifier extends DummyHomePage
	{
		@SpringBean
		private Map<String, BeanWithGeneric<?>> beans;

		public Map<String, BeanWithGeneric<?>> getBeans()
		{
			return beans;
		}
	}

	class AnnotatedSetOfBeanGenericQualifier extends DummyHomePage
	{
		@SpringBean
		private Set<BeanWithGeneric<?>> beans;

		public Set<BeanWithGeneric<?>> getBeans()
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

	static class AnnotatedListField extends DummyHomePage
	{
		@SpringBean
		private List<String> stringsList;

		public List<String> getStringsList()
		{
			return stringsList;
		}

		@SpringBean
		private ArrayList<String> arrayListStrings;

		public ArrayList<String> getArrayListStrings()
		{
			return arrayListStrings;
		}

		@SpringBean
		private List<Integer> arrayListIntegers;

		public List<Integer> getArrayListIntegers()
		{
			return arrayListIntegers;
		}

		@SpringBean
		private MyList<String> myList;

		public MyList<String> getMyList()
		{
			return myList;
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
		public BeanWithGeneric<Integer> integerBean()
		{
			return new BeanWithGeneric<>();
		}

		@Bean
		public List<String> stringsList()
		{
			return List.of("foo", "bar", "baz");
		}

		@Bean
		public ArrayList<String> arrayListStrings()
		{
			ArrayList<String> arrayList = new ArrayList<>();
			arrayList.add("one");
			arrayList.add("two");
			arrayList.add("three");
			return arrayList;
		}

		@Bean
		public List<Integer> arrayListIntegers()
		{
			ArrayList<Integer> arrayList = new ArrayList<>();
			arrayList.add(1);
			arrayList.add(2);
			arrayList.add(3);
			return arrayList;
		}

		@Bean
		public MyList<String> myList() {
			MyList<String> myList = new MyList<>();
			myList.add("one");
			return myList;
		}

		@Bean
		public List<Boolean> booleans() {
			return List.of(true, false);
		}
	}

	private static class Wicket7086 {
		@Inject
		private List<Boolean> injected;
	}

	/**
	 * <a href="https://issues.apache.org/jira/browse/WICKET-7086">WICKET-7086</a>
	 */
	@Test
	void manualInjectionOfTypeWithGenerics() {
		tester.startPage(new AnnotatedListField());

		// Created by hand, not by Spring, fields are not injected.
		Wicket7086 manuallyInjected = new Wicket7086();

		// Manual injection.
		Injector.get().inject(manuallyInjected);

		Boolean item = manuallyInjected.injected.get(0);
		assertTrue(item);
	}

	public static class MyList<T> extends ArrayList<T> {}
}
