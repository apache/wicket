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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.wicket.spring.BeanWithGeneric;
import org.apache.wicket.util.tester.DummyHomePage;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Andrea Del Bene
 *
 */
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

		List<BeanWithGeneric<?>> beans = page.getBeans();
		
		assertNotNull(beans);
		assertEquals(2, beans.size());
		
		assertTrue(beans.contains(ctx.getBean("stringBean")));
		assertTrue(beans.contains(ctx.getBean("integerBean")));
	}

	@Test
	public void listOfStringGenerics() throws Exception
	{
		AnnotatedListOfBeanStringGenericQualifier page =
				tester.startPage(new AnnotatedListOfBeanStringGenericQualifier());

		List<BeanWithGeneric<String>> beans = page.getBeans();

		assertThat(beans, is(notNullValue()));
		assertThat(beans.size(), is(1));

		BeanWithGeneric<String> stringBean = (BeanWithGeneric<String>) ctx.getBean("stringBean");
		assertThat(beans, hasItem(stringBean));
	}
	
	@Test
	public void mapOfGenerics() throws Exception
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
	public void setOfGenerics() throws Exception
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
	public void listField() throws Exception
	{
		AnnotatedListField page = 
			tester.startPage(new AnnotatedListField());

		assertNotNull(page.getStringsList());
		assertEquals(3, page.getStringsList().size());

		ArrayList<String> arrayListStrings = page.getArrayListStrings();
		assertThat(arrayListStrings, is(notNullValue()));
		assertThat(arrayListStrings.size(), is(3));
		assertThat(arrayListStrings.get(0), is(equalTo("one")));
		assertThat(arrayListStrings.get(1), is(equalTo("two")));
		assertThat(arrayListStrings.get(2), is(equalTo("three")));

		ArrayList<Integer> arrayListIntegers = page.getArrayListIntegers();
		assertThat(arrayListIntegers, is(notNullValue()));
		assertThat(arrayListIntegers.size(), is(3));
		assertThat(arrayListIntegers.get(0), is(1));
		assertThat(arrayListIntegers.get(1), is(2));
		assertThat(arrayListIntegers.get(2), is(3));

		MyList<String> myList = page.getMyList();
		assertThat(myList, is(notNullValue()));
		assertThat(myList.size(), is(1));
		assertThat(myList.get(0), is("one"));
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

	class AnnotatedListField extends DummyHomePage
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
		private ArrayList<Integer> arrayListIntegers;

		public ArrayList<Integer> getArrayListIntegers()
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
		public List<String> strings()
		{
			return Arrays.asList("foo", "bar", "baz");
		}

		@Bean
		public ArrayList<String> arrayListStrings()
		{
			ArrayList<String> arrayList = new ArrayList();
			arrayList.add("one");
			arrayList.add("two");
			arrayList.add("three");
			return arrayList;
		}

		@Bean
		public ArrayList<Integer> arrayListIntegers()
		{
			ArrayList<Integer> arrayList = new ArrayList();
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
	}

	public static class MyList<T> extends ArrayList<T> {}
}
