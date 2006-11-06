/*
 * $Id: ApplicationSettingsTest.java 5443 2006-04-17 20:02:21 +0000 (Mon, 17 Apr
 * 2006) jdonnerstag $ $Revision$ $Date: 2006-04-17 20:02:21 +0000 (Mon,
 * 17 Apr 2006) $
 * 
 * ==================================================================== Licensed
 * under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the
 * License at
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

import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;
import wicket.resource.DummyApplication;
import wicket.resource.loader.BundleStringResourceLoader;
import wicket.resource.loader.ClassStringResourceLoader;
import wicket.resource.loader.ComponentStringResourceLoader;
import wicket.resource.loader.IStringResourceLoader;
import wicket.settings.Settings;
import wicket.util.tester.WicketTester;

/**
 * Test cases for the <code>ApplicationSettings</code> class.
 * 
 * @author Chris Turner
 */
public class ApplicationSettingsTest extends TestCase
{
	/**
	 * @param message
	 *            The name of the test being run
	 */
	public ApplicationSettingsTest(final String message)
	{
		super(message);
	}

	/**
	 * 
	 */
	public void testFrameworkVersion()
	{
		Settings settings = new Settings(new DummyApplication());
		assertEquals("n/a", settings.getVersion());
	}

	/**
	 * @throws Exception
	 */
	public void testExceptionOnMissingResourceDefaultValue() throws Exception
	{
		Settings settings = new Settings(new DummyApplication());
		Assert.assertTrue("exceptionOnMissingResource should default to true", settings
				.getThrowExceptionOnMissingResource());
	}

	/**
	 * @throws Exception
	 */
	public void testExceptionOnMissingResourceSetsCorrectly() throws Exception
	{
		Settings settings = new Settings(new DummyApplication());
		settings.setThrowExceptionOnMissingResource(false);
		Assert.assertFalse("exceptionOnMissingResource should have been set to false", settings
				.getThrowExceptionOnMissingResource());
	}

	/**
	 * @throws Exception
	 */
	public void testUseDefaultOnMissingResourceDefaultValue() throws Exception
	{
		Settings settings = new Settings(new DummyApplication());
		Assert.assertTrue("useDefaultOnMissingResource should default to true", settings
				.getUseDefaultOnMissingResource());
	}

	/**
	 * @throws Exception
	 */
	public void testUseDefaultOnMissingResourceSetsCorrectly() throws Exception
	{
		Settings settings = new Settings(new DummyApplication());
		settings.setUseDefaultOnMissingResource(false);
		Assert.assertFalse("useDefaultOnMissingResource should have been set to false", settings
				.getUseDefaultOnMissingResource());
	}

	/**
	 * 
	 */
	public void testDefaultStringResourceLoaderSetup()
	{
		Settings settings = new Settings(new DummyApplication());
		List<IStringResourceLoader> loaders = settings.getStringResourceLoaders();
		Assert.assertEquals("There should be 2 default loaders", 2, loaders.size());
		Assert.assertTrue("First loader one should be the component one",
				loaders.get(0) instanceof ComponentStringResourceLoader);
		Assert.assertTrue("Second loader should be the tester one",
				loaders.get(1) instanceof ClassStringResourceLoader);
	}

	/**
	 * 
	 */
	public void testOverrideStringResourceLoaderSetup()
	{
		Application dummy = new DummyApplication();
		dummy.init();
		Settings settings = new Settings(dummy);
		settings.addStringResourceLoader(new BundleStringResourceLoader(
				"wicket.resource.DummyResources"));
		settings.addStringResourceLoader(new ComponentStringResourceLoader(dummy));
		List<IStringResourceLoader> loaders = settings.getStringResourceLoaders();
		Assert.assertEquals("There should be 2 overridden loaders", 2, loaders.size());
		Assert.assertTrue("First loader one should be the bundle one",
				loaders.get(0) instanceof BundleStringResourceLoader);
		Assert.assertTrue("Second loader should be the component one",
				loaders.get(1) instanceof ComponentStringResourceLoader);
	}

	/**
	 * 
	 */
	public void testLocalizer()
	{
		WicketTester tester = new WicketTester();
		Assert.assertNotNull("Localizer should be available", tester.getApplication()
				.getResourceSettings().getLocalizer());
	}
}