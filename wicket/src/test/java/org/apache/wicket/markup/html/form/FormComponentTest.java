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
package org.apache.wicket.markup.html.form;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.IDetachable;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.WicketTester;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;

/**
 * 
 */
public class FormComponentTest extends TestCase
{
	private WicketTester wicketTester;

	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		wicketTester = new WicketTester();
	}

	public void testArrayType()
	{
		final FormComponent<?> fc = new TextField<String>("foo");
		fc.setType(String[].class);
		Assert.assertSame(String[].class, fc.getType());
	}

	public void testMultiDimentionalArrayType()
	{
		final FormComponent<?> fc = new TextField<String>("foo");
		fc.setType(String[][][].class);
		Assert.assertSame(String[][][].class, fc.getType());
	}

	public void testPrimitiveArrayType()
	{
		final FormComponent<?> fc = new TextField<String>("foo");
		fc.setType(boolean[].class);
		Assert.assertSame(boolean[].class, fc.getType());
	}

	public void testGetDefaultlabel()
	{
		wicketTester.startPage(TestPage1.class);
		TestPage1 page = (TestPage1)wicketTester.getLastRenderedPage();
		assertEquals("set", page.field1.getDefaultLabel());
		assertEquals("field2", page.field2.getDefaultLabel());
	}

	public void testValidatorsDetach()
	{
		class TestValidator<T> implements IValidator<T>, IDetachable
		{
			boolean detached = false;

			public void detach()
			{
				detached = true;
			}

			public void validate(IValidatable<T> validatable)
			{
			}
		}

		TextField<String> field = new TextField<String>("s", Model.of(""));
		TestValidator<String> v1 = new TestValidator();
		TestValidator<String> v2 = new TestValidator();
		field.add(v1).add(v2);
		field.detach();
		assertTrue(v1.detached);
		assertTrue(v2.detached);
	}

	@Override
	protected void tearDown() throws Exception
	{
		wicketTester.destroy();
		wicketTester = null;
		super.tearDown();
	}

	public static class TestPage1 extends WebPage implements IMarkupResourceStreamProvider
	{
		public final TextField field1, field2;

		public TestPage1()
		{
			Form form = new Form("form");
			add(form);
			form.add(field1 = new TextField("field1"));
			form.add(field2 = new TextField("field2"));
		}

		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream(
				"<html><body><form wicket:id='form'><input wicket:id='field1' type='text'/><input wicket:id='field2' type='text'/></form></body></html>");
		}
	}
}
