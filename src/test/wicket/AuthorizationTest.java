/*
 * $Id$
 * $Revision$ $Date$
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

package wicket;

import java.io.Serializable;

import junit.framework.TestCase;
import wicket.authorization.AuthorizationException;
import wicket.authorization.IAuthorizationStrategy;
import wicket.markup.html.WebComponent;
import wicket.markup.html.WebPage;
import wicket.markup.html.basic.Label;
import wicket.markup.html.form.Form;
import wicket.markup.html.form.TextField;
import wicket.model.CompoundPropertyModel;
import wicket.util.tester.WicketTester;

/**
 * Authorization tests.
 * 
 * @author hillenius
 */
public class AuthorizationTest extends TestCase
{
	/**
	 * Construct.
	 */
	public AuthorizationTest()
	{
		super();
	}

	/**
	 * Construct.
	 * 
	 * @param name
	 */
	public AuthorizationTest(String name)
	{
		super(name);
	}

	/**
	 * Sets up this test.
	 * 
	 * @throws Exception
	 */
	protected void setUp() throws Exception
	{
		super.setUp();
	}

	/**
	 * Tests that a component can be created when authorization is allowed.
	 * 
	 * @throws Exception
	 */
	public void testCreateAllowedComponent() throws Exception
	{
		WicketTester app = new WicketTester()
		{
			public IAuthorizationStrategy getAuthorizationStrategy()
			{
				return new DummyAuthorizationStrategy();
			}
		};
		WebComponent c = new WebComponent("test");
	}

	/**
	 * Tests that a component cannot be created when authorization is not
	 * allowed.
	 * 
	 * @throws Exception
	 */
	public void testCreateDisallowedComponent() throws Exception
	{
		WicketTester app = new WicketTester()
		{
			public IAuthorizationStrategy getAuthorizationStrategy()
			{
				return new DummyAuthorizationStrategy()
				{

					public boolean allowInstantiation(Class c)
					{
						return false;
					}
				};
			}
		};
		try
		{
			WebComponent c = new WebComponent("test");
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
	public void testRenderAllowedComponent() throws Exception
	{
		WicketTester app = new WicketTester()
		{
			public IAuthorizationStrategy getAuthorizationStrategy()
			{
				return new DummyAuthorizationStrategy();
			}
		};
		app.startPage(AuthTestPage1.class);
		app.assertRenderedPage(AuthTestPage1.class);
		app.assertLabel("label", "wicked!");
	}

	/**
	 * Test that a component will be rendered when authorization is ok.
	 * 
	 * @throws Exception
	 */
	public void testRenderDisallowedComponent() throws Exception
	{
		WicketTester app = new WicketTester()
		{
			public IAuthorizationStrategy getAuthorizationStrategy()
			{
				return new DummyAuthorizationStrategy()
				{
					public boolean allowRender(Component c)
					{
						if (c instanceof Label)
						{
							return false;
						}
						return true;
					}
				};
			}
		};
		app.startPage(AuthTestPage1.class);
		app.assertRenderedPage(AuthTestPage1.class);
		app.assertInvisible("label");
	}

	/**
	 * Test that a component will update it's model when authorization is ok.
	 * 
	 * @throws Exception
	 */
	public void testEnabledAllowedComponent() throws Exception
	{
		WicketTester app = new WicketTester()
		{
			public IAuthorizationStrategy getAuthorizationStrategy()
			{
				return new DummyAuthorizationStrategy();
			}
		};
		app.startPage(AuthTestPage1.class);
		app.assertRenderedPage(AuthTestPage1.class);
		app.setParameterForNextRequest("form:stringInput", "test");
		app.submitForm("form");
		app.assertRenderedPage(AuthTestPage1.class);
		AuthTestPage1 page = (AuthTestPage1)app.getLastRenderedPage();
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
	public void testEnabledDisallowedComponent() throws Exception
	{
		WicketTester app = new WicketTester()
		{
			public IAuthorizationStrategy getAuthorizationStrategy()
			{
				return new DummyAuthorizationStrategy()
				{
					public boolean allowEnabledState(Component c)
					{
						if (c instanceof TextField && c.getId().equals("stringInput"))
						{
							return false;
						}
						return true;
					}
				};
			}
		};
		app.startPage(AuthTestPage1.class);
		app.assertRenderedPage(AuthTestPage1.class);
		app.setParameterForNextRequest("form:stringInput", "test");
		try
		{
			app.submitForm("form");
			fail("model update should not have been allowed"); // bad
		}
		catch (WicketRuntimeException e)
		{
			// good
		}

	}

	/**
	 * noop strategy so we don't have to implement the whole interface every
	 * time.
	 */
	private static class DummyAuthorizationStrategy implements IAuthorizationStrategy
	{
		/**
		 * @see wicket.authorization.IAuthorizationStrategy#allowInstantiation(java.lang.Class)
		 */
		public boolean allowInstantiation(Class c)
		{
			return true;
		}

		/**
		 * @see wicket.authorization.IAuthorizationStrategy#allowRender(wicket.Component)
		 */
		public boolean allowRender(Component c)
		{
			return true;
		}

		/**
		 * @see wicket.authorization.IAuthorizationStrategy#allowEnabledState(wicket.Component)
		 */
		public boolean allowEnabledState(Component c)
		{
			return true;
		}
	}

	/**
	 * Test page for authentication tests.
	 */
	private static class AuthTestPage1 extends WebPage
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
		private class TestForm extends Form
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
				setModel(new CompoundPropertyModel(input = new Input()));
				add(new TextField("stringInput"));
			}

			/**
			 * @see wicket.markup.html.form.Form#onSubmit()
			 */
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
