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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

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
import org.apache.wicket.settings.ResourceSettings;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.tester.WicketTester;
import org.apache.wicket.util.value.ValueMap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test cases for the <code>Localizer</code> class.
 * 
 * @author Chris Turner
 */
class LocalizerTest
{

	Localizer localizer;
	private WicketTester tester;
	private ResourceSettings settings;

	/**
	 *
	 * @throws Exception
	 */
	@BeforeEach
	void setUp() throws Exception
	{
		tester = new WicketTester(new DummyApplication());
		settings = tester.getApplication().getResourceSettings();
		localizer = tester.getApplication().getResourceSettings().getLocalizer();
	}

	@AfterEach
	void tearDown() throws Exception
	{
		tester.destroy();
	}

	/**
	 *
	 */
	@Test
	void testGetStringValidString()
	{
		assertEquals("This is a test", localizer.getString("test.string", null, null, "DEFAULT"),
			"Expected string should be returned");
	}

	/**
	 *
	 */
	@Test
	void testGetStringMissingStringReturnDefault()
	{
		settings.setUseDefaultOnMissingResource(true);
		assertEquals("DEFAULT", localizer.getString("unknown.string", null, null, "DEFAULT"),
			"Default string should be returned");
	}

	/**
	 *
	 */
	@Test
	void testGetStringMissingStringNoDefault()
	{
		settings.setUseDefaultOnMissingResource(true);
		settings.setThrowExceptionOnMissingResource(false);

		assertEquals("[Warning: Property for 'unknown.string' not found]",
			localizer.getString("unknown.string", null, null, null),
			"Wrapped key should be returned on no default");
	}

	/**
	 *
	 */
	@Test
	void testGetStringMissingStringDoNotUseDefault()
	{
		settings.setUseDefaultOnMissingResource(false);
		settings.setThrowExceptionOnMissingResource(false);
		assertEquals("[Warning: Property for 'unknown.string' not found]",
			localizer.getString("unknown.string", null, null, "DEFAULT"),
			"Wrapped key should be returned on not using default and no exception");
	}

	/**
	 *
	 */
	@Test
	void testGetStringMissingStringExceptionThrown()
	{
		settings.setUseDefaultOnMissingResource(false);
		settings.setThrowExceptionOnMissingResource(true);
		try
		{
			localizer.getString("unknown.string", null, null, "DEFAULT");
			fail("MissingResourceException expected");
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
	void testGetStringPropertySubstitution()
	{
		Session.get().setLocale(Locale.GERMAN);

		ValueMap vm = new ValueMap();
		vm.put("user", "John Doe");
		vm.put("rating", 4.5);
		IModel<ValueMap> model = new Model<ValueMap>(vm);
		assertEquals("John Doe gives 4,5 stars",
			localizer.getString("test.substitute", null, model, null),
			"Property substitution should occur");
	}

	/**
	 *
	 */
	@Test
	void testInComponentConstructor()
	{
		new MyLabel("myLabel");
	}

	/**
	 * Unit test for bug number [1416582] Resource loading caches wrong.
	 */
	@Test
	void testTwoComponents()
	{
		Session.get().setLocale(Locale.ENGLISH);
		MyMockPage page = new MyMockPage();
		Application.get().getResourceSettings().getStringResourceLoaders().add(
			new ComponentStringResourceLoader());

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
	void testGetStringUseModel()
	{
		Session.get().setLocale(Locale.GERMAN);

		HashMap<String, Object> model = new HashMap<String, Object>();
		model.put("user", "juergen");
		model.put("rating", 4.5);

		assertEquals(
			"juergen gives 4,5 stars", localizer.getString("test.substitute", null,
				new PropertyModel<String>(model, null), "DEFAULT {user}"),
			"Expected string should be returned");

		assertEquals("DEFAULT juergen",
			localizer.getString("test.substituteDoesNotExist", null,
				new PropertyModel<HashMap<String, Object>>(model, null), "DEFAULT ${user}"),
			"Expected string should be returned");
	}

	/**
	 * See https://issues.apache.org/jira/browse/WICKET-1851
	 */
	@Test
	void test_1851_1()
	{
		MyMockPage page = new MyMockPage();

		tester.getApplication().getResourceSettings().setThrowExceptionOnMissingResource(false);
		tester.getApplication().getResourceSettings().setUseDefaultOnMissingResource(false);

		String option = localizer.getStringIgnoreSettings("dummy.null", page.drop1, null,
			"default");
		assertEquals("default", option);

		option = localizer.getStringIgnoreSettings("dummy.null", page.drop1, null, null);
		assertNull(option);
		if (Strings.isEmpty(option))
		{
			option = localizer.getString("null", page.drop1, "CHOOSE_ONE");
		}
		assertEquals("value 1", option);

		tester.getApplication().getResourceSettings().setThrowExceptionOnMissingResource(false);
		tester.getApplication().getResourceSettings().setUseDefaultOnMissingResource(false);

		option = localizer.getString("dummy.null", page.drop1, null, "default");
		assertEquals(option, "[Warning: Property for 'dummy.null' not found]");

		tester.getApplication().getResourceSettings().setThrowExceptionOnMissingResource(true);
		tester.getApplication().getResourceSettings().setUseDefaultOnMissingResource(true);

		option = localizer.getString("dummy.null", page.drop1, null, "default");
		assertEquals("default", option);

		try
		{
			localizer.getString("dummy.null", page.drop1, null, null);
			assertTrue(false, "Expected an exception to happen");
		}
		catch (MissingResourceException ex)
		{
			assertEquals(
				"Unable to find property: 'dummy.null' for component: form:drop1 [class=org.apache.wicket.markup.html.form.DropDownChoice]. Locale: null, style: null",
				ex.getMessage());
		}
	}

	public static class MyMockPage extends WebPage
	{
		private static final long serialVersionUID = 1L;

		DropDownChoice<String> drop1;
		DropDownChoice<String> drop2;

		/**
		 * Construct.
		 */
		MyMockPage()
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
		MyLabel(final String id)
		{
			super(id);

			Localizer localizer = Application.get().getResourceSettings().getLocalizer();

			// should work properly in a component constructor (without parent)
			// as well
			assertEquals("This is a test",
				localizer.getString("test.string", this, "DEFAULT"), "Expected string should be returned");

		}
	}
}
