/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket;

import java.util.MissingResourceException;

import wicket.ApplicationSettings;
import wicket.Component;
import wicket.Application;
import wicket.Localizer;
import wicket.model.IModel;
import wicket.model.Model;
import wicket.resource.ApplicationStringResourceLoader;
import wicket.resource.DummyApplication;
import wicket.util.value.ValueMap;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * Test cases for the <code>Localizer</code> class.
 * @author Chris Turner
 */
public class LocalizerTest extends TestCase
{

	private Application application;

	private ApplicationSettings settings;

	private Localizer localizer;

	/**
	 * Create the test case.
	 * @param message The test name
	 */
	public LocalizerTest(String message)
	{
		super(message);
	}

	protected void setUp() throws Exception
	{
		super.setUp();
		application = new DummyApplication();
		settings = application.getSettings();
		settings.addStringResourceLoader(new ApplicationStringResourceLoader(application));
		localizer = application.getLocalizer();
	}

	/**
	 * 
	 *
	 */
	public void testGetStringValidString()
	{
		Assert.assertEquals("Expected string should be returned", "This is a test", localizer
				.getString("test.string", null, null, null, null, "DEFAULT"));
	}

	/**
	 * 
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
	 *
	 */
	public void testGetStringMissingStringNoDefault()
	{
		settings.setUseDefaultOnMissingResource(true);
		settings.setThrowExceptionOnMissingResource(false);
		Assert.assertEquals("Wrapped key should be returned on no default", "??unknown.string??",
				localizer.getString("unknown.string", null, null, null, null, null));
	}

	/**
	 * 
	 *
	 */
	public void testGetStringMissingStringDoNotUseDefault()
	{
		settings.setUseDefaultOnMissingResource(false);
		settings.setThrowExceptionOnMissingResource(false);
		Assert.assertEquals("Wrapped key should be returned on not using default and no exception",
				"??unknown.string??", localizer.getString("unknown.string", null, null, null, null,
						"DEFAULT"));
	}

	/**
	 * 
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
	 *
	 */
	public void testGetStringOGNLSubstitution()
	{
		ValueMap vm = new ValueMap();
		vm.put("user", "John Doe");
		Model model = new Model(vm);
		Assert.assertEquals("OGNL substitution should occur", "Welcome, John Doe", localizer
				.getString("test.substitute", null, model, null, null, null));
	}

	/**
	 * 
	 *
	 */
	public void testAllOtherMethodsDelegateCorrectly()
	{
		Assert.assertEquals("This is a test", localizer.getString("test.string", (Component) null,
				"DEFAULT"));
		Assert.assertEquals("This is a test", localizer.getString("test.string", (Component) null));
		Assert.assertEquals("This is a test", localizer.getString("test.string", null, null,
				"DEFAULT"));
		Assert.assertEquals("This is a test", localizer.getString("test.string", (Component) null,
				(IModel) null));
		Assert.assertEquals("This is a test", localizer.getString("test.string", "DEFAULT"));
		Assert.assertEquals("This is a test", localizer.getString("test.string"));
		Assert.assertEquals("This is a test", localizer.getString("test.string", (IModel) null,
				"DEFAULT"));
		Assert.assertEquals("This is a test", localizer.getString("test.string", (IModel) null));
	}
}
