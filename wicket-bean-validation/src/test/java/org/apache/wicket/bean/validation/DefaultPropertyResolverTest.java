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
package org.apache.wicket.bean.validation;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.tester.WicketTesterScope;
import org.junit.Rule;
import org.junit.Test;

public class DefaultPropertyResolverTest
{
	@Rule
	public static WicketTesterScope scope = new WicketTesterScope();

	public static class Bean1
	{
		private String foo;

		public String getFoo()
		{
			return foo;
		}
	}

	@Test
	public void hasFieldAndGetter()
	{
		DefaultPropertyResolver resolver = new DefaultPropertyResolver();

		TextField<?> component = new TextField<Bean1>("id", new PropertyModel<Bean1>(new Bean1(),
			"foo"));
		Property property = resolver.resolveProperty(component);
		assertThat(property, not(nullValue()));
		assertThat(property.getName(), is("foo"));
		assertThat(property.getOwner().getName(), is(Bean1.class.getName()));
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-5505
	 */
	@Test
	public void booleanHasFieldAndGetter()
	{
		DefaultPropertyResolver resolver = new DefaultPropertyResolver();

		TextField<BooleanBean> component = new TextField<BooleanBean>("id",
				new PropertyModel<BooleanBean>(new BooleanBean(), "foo"));
		Property property = resolver.resolveProperty(component);
		assertThat(property, not(nullValue()));
		assertThat(property.getName(), is("foo"));
		assertThat(property.getOwner().getName(), is(BooleanBean.class.getName()));
	}

	@Test
	public void hasOnlyField()
	{
		DefaultPropertyResolver resolver = new DefaultPropertyResolver();

		TextField<?> component = new TextField<Bean2>("id", new PropertyModel<Bean2>(new Bean2(),
			"foo"));
		Property property = resolver.resolveProperty(component);
		assertThat(property, not(nullValue()));
		assertThat(property.getName(), is("foo"));
		assertThat(property.getOwner().getName(), is(Bean2.class.getName()));
	}

	public static class Bean2
	{
		private String foo;
	}

	@Test
	public void hasOnlyGetter()
	{
		DefaultPropertyResolver resolver = new DefaultPropertyResolver();

		TextField<?> component = new TextField<Bean3>("id", new PropertyModel<Bean3>(new Bean3(),
			"foo"));
		Property property = resolver.resolveProperty(component);
		assertThat(property, not(nullValue()));
		assertThat(property.getName(), is("foo"));
		assertThat(property.getOwner().getName(), is(Bean3.class.getName()));
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-5506
	 */
	@Test
	public void getterHasPriorityOverField()
	{
		DefaultPropertyResolver resolver = new DefaultPropertyResolver();

		TextField<?> component = new TextField<>("id", new PropertyModel<Bean4>(new Bean4(),
				"foo"));
		Property property = resolver.resolveProperty(component);
		assertThat(property, not(nullValue()));
		assertThat(property.getName(), is("foo"));
		assertThat(property.getOwner().getName(), is(Bean4.class.getName()));
	}


	public static class Bean3
	{
		public String getFoo()
		{
			return "foo";
		}
	}

	/**
	 * WICKET-5505
	 */
	public static class BooleanBean
	{
		public boolean isFoo()
		{
			return false;
		}
	}

	public static class Bean4 extends Bean2
	{
		public String getFoo()
		{
			return "foo4";
		}
	}
}
