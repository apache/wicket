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

import static org.apache.wicket.bean.validation.customconstraint.PasswordConstraintAnnotation.CUSTOM_BUNDLE_KEY;
import static org.apache.wicket.bean.validation.customconstraint.PasswordConstraintAnnotation.DEFAULT_BUNDLE_KEY;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.apache.wicket.bean.validation.customconstraint.PasswordConstraintAnnotation;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.tester.WicketTesterScope;
import org.apache.wicket.validation.ValidationError;
import org.junit.Rule;
import org.junit.Test;

public class DefaultPropertyResolverTest
{
	@Rule
	public WicketTesterScope scope = new WicketTesterScope();

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

		TextField<?> component = new TextField<>("id", new PropertyModel<Bean1>(new Bean1(), "foo"));
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

		TextField<?> component = new TextField<>("id", new PropertyModel<BooleanBean>(
			new BooleanBean(), "foo"));
		Property property = resolver.resolveProperty(component);
		assertThat(property, not(nullValue()));
		assertThat(property.getName(), is("foo"));
		assertThat(property.getOwner().getName(), is(BooleanBean.class.getName()));
	}

	@Test
	public void hasOnlyField()
	{
		DefaultPropertyResolver resolver = new DefaultPropertyResolver();

		TextField<?> component = new TextField<>("id", new PropertyModel<Bean2>(new Bean2(), "foo"));
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

		TextField<?> component = new TextField<>("id", new PropertyModel<Bean3>(new Bean3(), "foo"));
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

		TextField<?> component = new TextField<>("id", new PropertyModel<Bean4>(new Bean4(), "foo"));
		Property property = resolver.resolveProperty(component);
		assertThat(property, not(nullValue()));
		assertThat(property.getName(), is("foo"));
		assertThat(property.getOwner().getName(), is(Bean4.class.getName()));
	}

	/**
	 * Test custom bundle mechanism of jsr 303
	 * 
	 * https://issues.apache.org/jira/browse/WICKET-5654
	 * 
	 * @throws Exception
	 */
	@Test
	public void testBundleKeysResolution() throws Exception
	{
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		Validator validator = factory.getValidator();
		DefaultViolationTranslator translator = new DefaultViolationTranslator();

		// test with a too short password
		BeanWithPassword bean = new BeanWithPassword("short");

		Set<ConstraintViolation<BeanWithPassword>> constraintViolations = validator.validate(bean);
		assertEquals(1, constraintViolations.size());

		@SuppressWarnings("unchecked")
		ConstraintViolation<BeanWithPassword> shortViolation = (ConstraintViolation<BeanWithPassword>)constraintViolations
			.toArray()[0];

		ValidationError error = translator.convert(shortViolation);

		checkErrorBundleKeys(error, DEFAULT_BUNDLE_KEY + ".String", DEFAULT_BUNDLE_KEY);

		// test with a password containing non-word chars
		bean.setPassword("notWord&%$£");

		constraintViolations = validator.validate(bean);
		assertEquals(1, constraintViolations.size());

		@SuppressWarnings("unchecked")
		ConstraintViolation<BeanWithPassword> nonWordviolation = (ConstraintViolation<BeanWithPassword>)constraintViolations.toArray()[0];

		error = translator.convert(nonWordviolation);

		checkErrorBundleKeys(error, CUSTOM_BUNDLE_KEY + ".String", CUSTOM_BUNDLE_KEY,
			DEFAULT_BUNDLE_KEY + ".String", DEFAULT_BUNDLE_KEY);

		// test with a valid password
		bean.setPassword("aValidPassword1234");

		constraintViolations = validator.validate(bean);
		assertEquals(0, constraintViolations.size());
	}

	/**
	 * Checks that validation error has the expected keys as bundle keys, in the order they are
	 * specified in {@code expectedKeys}.
	 * 
	 * @param error
	 * @param expectedKeys
	 */
	private void checkErrorBundleKeys(ValidationError error, String... expectedKeys)
	{
		List<String> keys = error.getKeys();

		assertEquals("The expected number for bundle keys is '" + expectedKeys.length
			+ "' but we have '" + keys.size() + "'", expectedKeys.length, keys.size());

		for (int i = 0; i < expectedKeys.length; i++)
		{
			String expectedKey = expectedKeys[i];

			assertTrue(keys.get(i).equals(expectedKey));
		}
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
	
	public static class BeanWithPassword
	 {
	     @PasswordConstraintAnnotation
	     private String password;

	 	public BeanWithPassword(String password)
	 	{
	 		this.password = password;
	 	}

	 	public String getPassword()
	 	{
	 		return password;
	 	}

	 	public void setPassword(String password)
	 	{
	 		this.password = password;
	 	}
	 }
}
