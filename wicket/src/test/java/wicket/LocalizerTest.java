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
package wicket;

import java.util.Locale;
import java.util.MissingResourceException;

import junit.framework.Assert;
import wicket.markup.html.basic.Label;
import wicket.model.Model;
import wicket.resource.DummyApplication;
import wicket.resource.loader.ComponentStringResourceLoader;
import wicket.settings.IResourceSettings;
import wicket.util.tester.WicketTester;
import wicket.util.value.ValueMap;

/**
 * Test cases for the <code>Localizer</code> class.
 * 
 * @author Chris Turner
 */
public class LocalizerTest extends WicketTestCase
{
	private IResourceSettings settings;

	private Localizer localizer;

	/**
	 * Create the test case.
	 * 
	 * @param message
	 *            The test name
	 */
	public LocalizerTest(String message)
	{
		super(message);
	}

	/**
	 * 
	 * @throws Exception
	 */
	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		tester = new WicketTester(new DummyApplication());
		
		settings = tester.getApplication().getResourceSettings();
		localizer = tester.getApplication().getResourceSettings().getLocalizer();
	}

	/**
	 * 
	 */
	public void testGetStringValidString()
	{
		Assert.assertEquals("Expected string should be returned", "This is a test", localizer
				.getString("test.string", null, null, null, null, "DEFAULT"));
	}

	/**
	 * 
	 */
	public void testGetStringMissingStringReturnDefault()
	{
		settings.setUseDefaultOnMissingResource(true);
		Assert.assertEquals("Default string should be returned", "DEFAULT", localizer.getString(
				"unknown.string", null, null, null, null, "DEFAULT"));
	}

	/**
	 * 
	 */
	public void testGetStringMissingStringNoDefault()
	{
		settings.setUseDefaultOnMissingResource(true);
		settings.setThrowExceptionOnMissingResource(false);

		Assert.assertEquals("Wrapped key should be returned on no default",
				"[Warning: String resource for 'unknown.string' not found]", localizer.getString(
						"unknown.string", null, null, null, null, null));
	}

	/**
	 * 
	 */
	public void testGetStringMissingStringDoNotUseDefault()
	{
		settings.setUseDefaultOnMissingResource(false);
		settings.setThrowExceptionOnMissingResource(false);
		Assert.assertEquals("Wrapped key should be returned on not using default and no exception",
				"[Warning: String resource for 'unknown.string' not found]", localizer.getString(
						"unknown.string", null, null, null, null, "DEFAULT"));
	}

	/**
	 * 
	 */
	public void testGetStringMissingStringExceptionThrown()
	{
		settings.setUseDefaultOnMissingResource(false);
		settings.setThrowExceptionOnMissingResource(true);
		try
		{
			localizer.getString("unknown.string", null, null, null, null, "DEFAULT");
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
	public void testGetStringPropertySubstitution()
	{
		ValueMap vm = new ValueMap();
		vm.put("user", "John Doe");
		Model model = new Model<ValueMap>(vm);
		Assert.assertEquals("Property substitution should occur", "Welcome, John Doe", localizer
				.getString("test.substitute", null, model, null, null, null));
	}

	/**
	 * 
	 */
	public void testInComponentConstructor()
	{
		new MyLabel(new MockPageWithOneComponent(), "component");
	}

	/**
	 * Unit test for bug number [1416582] Resource loading caches wrong.
	 */
	public void testTwoComponents()
	{
		Session.get().setLocale(Locale.ENGLISH);
		MyMockPage page = new MyMockPage();
		Application.get().getResourceSettings().addStringResourceLoader(
				new ComponentStringResourceLoader(Application.get()));

		Localizer localizer = Application.get().getResourceSettings().getLocalizer();
		String drop1 = localizer.getString("null", page.drop1);
		String drop2 = localizer.getString("null", page.drop2);

		assertEquals("value 1", drop1);
		assertEquals("value 2", drop2);
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
		 * @param parent
		 * @param id
		 */
		public MyLabel(MarkupContainer parent, final String id)
		{
			super(parent, id);

			Localizer localizer = Application.get().getResourceSettings().getLocalizer();

			// should work properly in a component constructor (without parent)
			// as well
			Assert.assertEquals("Expected string should be returned", "This is a test", localizer
					.getString("test.string", this, "DEFAULT"));

		}
	}
}
