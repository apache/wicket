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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.MissingResourceException;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.resource.DummyApplication;
import org.apache.wicket.resource.loader.ComponentStringResourceLoader;
import org.apache.wicket.settings.IResourceSettings;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.tester.WicketTester;
import org.apache.wicket.util.value.ValueMap;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test cases for the <code>Localizer</code> class.
 * 
 * @author Chris Turner
 */
public class LocalizerTest extends Assert
{

	private static class MyMockPage extends WebPage
	{
		private static final long serialVersionUID = 1L;

		DropDownChoice<String> drop1;
		DropDownChoice<String> drop2;

		/**
		 * Construct.
		 */
		public MyMockPage()
		{
			final Form<Void> form = new Form<Void>("form");
			add(form);

			String[] choices = { "choice1", "choice2" };
			drop1 = new DropDownChoice<String>("drop1", Arrays.asList(choices));
			drop2 = new DropDownChoice<String>("drop2", Arrays.asList(choices));

			form.add(drop1);
			form.add(drop2);
		}
	}

	private WicketTester tester;
	private IResourceSettings settings;

	protected Localizer localizer;

	/**
	 * 
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception
	{
		tester = new WicketTester(new DummyApplication());
		settings = tester.getApplication().getResourceSettings();
		localizer = tester.getApplication().getResourceSettings().getLocalizer();
	}

	@After
	public void tearDown() throws Exception
	{
		tester.destroy();
	}

	/**
	 * 
	 */
	@Test
	public void testGetStringValidString()
	{
		Assert.assertEquals("Expected string should be returned", "This is a test",
			localizer.getString("test.string", null, null, "DEFAULT"));
	}

	/**
	 * 
	 */
	@Test
	public void testGetStringMissingStringReturnDefault()
	{
		settings.setUseDefaultOnMissingResource(true);
		Assert.assertEquals("Default string should be returned", "DEFAULT",
			localizer.getString("unknown.string", null, null, "DEFAULT"));
	}

	/**
	 * 
	 */
	@Test
	public void testGetStringMissingStringNoDefault()
	{
		settings.setUseDefaultOnMissingResource(true);
		settings.setThrowExceptionOnMissingResource(false);

		Assert.assertEquals("Wrapped key should be returned on no default",
			"[Warning: Property for 'unknown.string' not found]",
			localizer.getString("unknown.string", null, null, null));
	}

	/**
	 * 
	 */
	@Test
	public void testGetStringMissingStringDoNotUseDefault()
	{
		settings.setUseDefaultOnMissingResource(false);
		settings.setThrowExceptionOnMissingResource(false);
		Assert.assertEquals("Wrapped key should be returned on not using default and no exception",
			"[Warning: Property for 'unknown.string' not found]",
			localizer.getString("unknown.string", null, null, "DEFAULT"));
	}

	/**
	 * 
	 */
	@Test
	public void testGetStringMissingStringExceptionThrown()
	{
		settings.setUseDefaultOnMissingResource(false);
		settings.setThrowExceptionOnMissingResource(true);
		try
		{
			localizer.getString("unknown.string", null, null, "DEFAULT");
			Assert.fail("MissingResourceException expected");
		}
		catch (MissingResourceException e)
		{
			// Expected result
		}
	}

	/**
	 * 
	 */
	@Test
	public void testGetStringPropertySubstitution()
	{
		Session.get().setLocale(Locale.GERMAN);

		ValueMap vm = new ValueMap();
		vm.put("user", "John Doe");
		vm.put("rating", 4.5);
		IModel<ValueMap> model = new Model<ValueMap>(vm);
		Assert.assertEquals("Property substitution should occur", "John Doe gives 4,5 stars",
			localizer.getString("test.substitute", null, model, null));
	}

	/**
	 * 
	 */
	@Test
	public void testInComponentConstructor()
	{
		new MyLabel("myLabel");
	}

	/**
	 * Unit test for bug number [1416582] Resource loading caches wrong.
	 */
	@Test
	public void testTwoComponents()
	{
		Session.get().setLocale(Locale.ENGLISH);
		MyMockPage page = new MyMockPage();
		Application.get()
			.getResourceSettings()
			.getStringResourceLoaders()
			.add(new ComponentStringResourceLoader());

		Localizer localizer = Application.get().getResourceSettings().getLocalizer();
		assertEquals("value 1", localizer.getString("null", page.drop1));
		assertEquals("value 2", localizer.getString("null", page.drop2));

		Session.get().setLocale(new Locale("nl"));
		assertEquals("waarde 1", localizer.getString("null", page.drop1));
		assertEquals("waarde 2", localizer.getString("null", page.drop2));

	}

	/**
	 * 
	 */
	@Test
	public void testGetStringUseModel()
	{
		Session.get().setLocale(Locale.GERMAN);

		HashMap<String, Object> model = new HashMap<String, Object>();
		model.put("user", "juergen");
		model.put("rating", 4.5);

		Assert.assertEquals("Expected string should be returned", "juergen gives 4,5 stars",
			localizer.getString("test.substitute", null, new PropertyModel<String>(model, null),
				"DEFAULT {user}"));

		Assert.assertEquals("Expected string should be returned", "DEFAULT juergen",
			localizer.getString("test.substituteDoesNotExist", null,
				new PropertyModel<HashMap<String, Object>>(model, null), "DEFAULT ${user}"));
	}

	/**
	 * See https://issues.apache.org/jira/browse/WICKET-1851
	 */
	@Test
	public void test_1851_1()
	{
		MyMockPage page = new MyMockPage();

		tester.getApplication().getResourceSettings().setThrowExceptionOnMissingResource(false);
		tester.getApplication().getResourceSettings().setUseDefaultOnMissingResource(false);

		String option = localizer.getStringIgnoreSettings("dummy.null", page.drop1, null, "default");
		assertEquals(option, "default");

		option = localizer.getStringIgnoreSettings("dummy.null", page.drop1, null, null);
		assertNull(option);
		if (Strings.isEmpty(option))
		{
			option = localizer.getString("null", page.drop1, "CHOOSE_ONE");
		}
		assertEquals(option, "value 1");

		tester.getApplication().getResourceSettings().setThrowExceptionOnMissingResource(false);
		tester.getApplication().getResourceSettings().setUseDefaultOnMissingResource(false);

		option = localizer.getString("dummy.null", page.drop1, null, "default");
		assertEquals(option, "[Warning: Property for 'dummy.null' not found]");

		tester.getApplication().getResourceSettings().setThrowExceptionOnMissingResource(true);
		tester.getApplication().getResourceSettings().setUseDefaultOnMissingResource(true);

		option = localizer.getString("dummy.null", page.drop1, null, "default");
		assertEquals(option, "default");

		try
		{
			localizer.getString("dummy.null", page.drop1, null, null);
			assertTrue("Expected an exception to happen", false);
		}
		catch (MissingResourceException ex)
		{
			assertEquals(
				ex.getMessage(),
				"Unable to find property: 'dummy.null' for component: form:drop1 [class=org.apache.wicket.markup.html.form.DropDownChoice]. Locale: null, style: null");
		}
	}

	/**
	 * Test label.
	 */
	public static class MyLabel extends Label
	{
		private static final long serialVersionUID = 1L;

		/**
		 * Construct.
		 * 
		 * @param id
		 */
		public MyLabel(final String id)
		{
			super(id);

			Localizer localizer = Application.get().getResourceSettings().getLocalizer();

			// should work properly in a component constructor (without parent)
			// as well
			Assert.assertEquals("Expected string should be returned", "This is a test",
				localizer.getString("test.string", this, "DEFAULT"));

		}
	}
}
