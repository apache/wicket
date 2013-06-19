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
package org.apache.wicket;

import java.io.Serializable;

import org.apache.wicket.authorization.Action;
import org.apache.wicket.authorization.AuthorizationException;
import org.apache.wicket.authorization.IAuthorizationStrategy;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.request.component.IRequestableComponent;
import org.junit.Test;


/**
 * Authorization tests.
 * 
 * @author hillenius
 */
public class AuthorizationTest extends WicketTestCase
{
	/**
	 * Tests that a component can be created when authorization is allowed.
	 * 
	 * @throws Exception
	 */
	@Test
	public void createAllowedComponent() throws Exception
	{
		new WebComponent("component");
	}

	/**
	 * Tests that a component cannot be created when authorization is not allowed.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCreateDisallowedComponent() throws Exception
	{
		tester.getApplication()
			.getSecuritySettings()
			.setAuthorizationStrategy(new IAuthorizationStrategy.AllowAllAuthorizationStrategy()
			{
				@Override
				public <T extends IRequestableComponent> boolean isInstantiationAuthorized(
					Class<T> componentClass)
				{
					return false;
				}
			});
		try
		{
			new WebComponent("test");
			// bad: authorization should have failed
			fail("authorization check failed to throw an exception");
		}
		catch (AuthorizationException e)
		{
			// this is good: authorization should have failed
		}
	}

	/**
	 * Test that a component will be rendered when authorization is ok.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testRenderAllowedComponent() throws Exception
	{
		tester.getApplication()
			.getSecuritySettings()
			.setAuthorizationStrategy(new IAuthorizationStrategy.AllowAllAuthorizationStrategy());

		tester.startPage(AuthTestPage1.class);
		tester.assertRenderedPage(AuthTestPage1.class);
		tester.assertLabel("label", "wicked!");
	}

	/**
	 * Test that a component will be rendered when authorization is ok.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testRenderDisallowedComponent() throws Exception
	{
		tester.getApplication()
			.getSecuritySettings()
			.setAuthorizationStrategy(new IAuthorizationStrategy.AllowAllAuthorizationStrategy()
			{
				/**
				 * @see org.apache.wicket.authorization.IAuthorizationStrategy#isActionAuthorized(org.apache.wicket.Component,
				 *      org.apache.wicket.authorization.Action)
				 */
				@Override
				public boolean isActionAuthorized(Component component, Action action)
				{
					if (action == Component.RENDER && component instanceof Label)
					{
						return false;
					}
					return true;
				}
			});
		tester.startPage(AuthTestPage1.class);
		tester.assertRenderedPage(AuthTestPage1.class);
		tester.assertInvisible("label");
	}

	/**
	 * Test that a component will update it's model when authorization is ok.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testEnabledAllowedComponent() throws Exception
	{
		tester.getApplication()
			.getSecuritySettings()
			.setAuthorizationStrategy(new IAuthorizationStrategy.AllowAllAuthorizationStrategy());

		tester.startPage(AuthTestPage1.class);
		tester.assertRenderedPage(AuthTestPage1.class);
		tester.getRequest().getPostParameters().setParameterValue("stringInput", "test");
		tester.submitForm("form");
		tester.assertRenderedPage(AuthTestPage1.class);
		AuthTestPage1 page = (AuthTestPage1)tester.getLastRenderedPage();
		assertTrue(page.isSubmitted());
		Input input = page.getTestModel();
		assertNotNull(input.getStringInput());
		assertEquals("test", input.getStringInput());
	}

	/**
	 * Test that a component will update it's model when authorization is ok.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testEnabledDisallowedComponent() throws Exception
	{
		tester.getApplication()
			.getSecuritySettings()
			.setAuthorizationStrategy(new IAuthorizationStrategy.AllowAllAuthorizationStrategy()
			{
				/**
				 * @see org.apache.wicket.authorization.IAuthorizationStrategy#isActionAuthorized(org.apache.wicket.Component,
				 *      org.apache.wicket.authorization.Action)
				 */
				@Override
				public boolean isActionAuthorized(Component c, Action action)
				{
					if (action == Component.ENABLE && c instanceof TextField &&
						c.getId().equals("stringInput"))
					{
						return false;
					}
					return true;
				}
			});
		tester.startPage(AuthTestPage1.class);
		tester.assertRenderedPage(AuthTestPage1.class);
		tester.getRequest().getPostParameters().setParameterValue("form:stringInput", "test");
		try
		{
			tester.submitForm("form");
			Component component = tester.getComponentFromLastRenderedPage("form:stringInput");
			assertEquals("", component.getDefaultModelObjectAsString());
		}
		catch (WicketRuntimeException e)
		{
			// good
		}
	}

	/**
	 * Test page for authentication tests.
	 */
	public static class AuthTestPage1 extends WebPage
	{
		private static final long serialVersionUID = 1L;

		private Input input;

		private boolean submitted = false;

		/**
		 * Construct.
		 */
		public AuthTestPage1()
		{
			add(new Label("label", "wicked!"));
			add(new TestForm("form"));

		}

		/**
		 * Gets the test model.
		 * 
		 * @return the test model
		 */
		public Input getTestModel()
		{
			return input;
		}

		/**
		 * Gets whether the form was submitted.
		 * 
		 * @return whether the form was submitted
		 */
		public boolean isSubmitted()
		{
			return submitted;
		}

		/** test form. */
		private class TestForm extends Form<Input>
		{
			private static final long serialVersionUID = 1L;

			/**
			 * Construct.
			 * 
			 * @param id
			 */
			public TestForm(String id)
			{
				super(id);
				setDefaultModel(new CompoundPropertyModel<Input>(input = new Input()));
				add(new TextField<String>("stringInput"));
			}

			/**
			 * @see org.apache.wicket.markup.html.form.Form#onSubmit()
			 */
			@Override
			protected void onSubmit()
			{
				submitted = true;
			}
		}
	}

	/** simple input holder. */
	private static class Input implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private String stringInput;

		/**
		 * Gets stringInput.
		 * 
		 * @return stringInput
		 */
		public String getStringInput()
		{
			return stringInput;
		}

		/**
		 * Sets stringInput.
		 * 
		 * @param stringInput
		 *            stringInput
		 */
		public void setStringInput(String stringInput)
		{
			this.stringInput = stringInput;
		}
	}
}
