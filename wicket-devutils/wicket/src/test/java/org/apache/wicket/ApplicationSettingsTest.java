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

import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.apache.wicket.resource.loader.BundleStringResourceLoader;
import org.apache.wicket.resource.loader.ClassStringResourceLoader;
import org.apache.wicket.resource.loader.ComponentStringResourceLoader;
import org.apache.wicket.resource.loader.IStringResourceLoader;
import org.apache.wicket.resource.loader.PackageStringResourceLoader;
import org.apache.wicket.resource.loader.ValidatorStringResourceLoader;
import org.apache.wicket.settings.Settings;
import org.apache.wicket.util.tester.WicketTester;

/**
 * Test cases for the <code>ApplicationSettings</code> class.
 * 
 * @author Chris Turner
 */
public class ApplicationSettingsTest extends TestCase
{
	WicketTester tester;

	@Override
	protected void setUp() throws Exception
	{
		tester = new WicketTester();
	}

	@Override
	protected void tearDown() throws Exception
	{
		tester.destroy();
	}

	/**
	 * 
	 */
	public void testFrameworkVersion()
	{
		Settings settings = new Settings(tester.getApplication());
		assertEquals("n/a", settings.getVersion());
	}

	/**
	 * @throws Exception
	 */
	public void testExceptionOnMissingResourceDefaultValue() throws Exception
	{
		Settings settings = new Settings(tester.getApplication());
		Assert.assertTrue("exceptionOnMissingResource should default to true",
			settings.getThrowExceptionOnMissingResource());
	}

	/**
	 * @throws Exception
	 */
	public void testExceptionOnMissingResourceSetsCorrectly() throws Exception
	{
		Settings settings = new Settings(tester.getApplication());
		settings.setThrowExceptionOnMissingResource(false);
		Assert.assertFalse("exceptionOnMissingResource should have been set to false",
			settings.getThrowExceptionOnMissingResource());
	}

	/**
	 * @throws Exception
	 */
	public void testUseDefaultOnMissingResourceDefaultValue() throws Exception
	{
		Settings settings = new Settings(tester.getApplication());
		Assert.assertTrue("useDefaultOnMissingResource should default to true",
			settings.getUseDefaultOnMissingResource());
	}

	/**
	 * @throws Exception
	 */
	public void testUseDefaultOnMissingResourceSetsCorrectly() throws Exception
	{
		Settings settings = new Settings(tester.getApplication());
		settings.setUseDefaultOnMissingResource(false);
		Assert.assertFalse("useDefaultOnMissingResource should have been set to false",
			settings.getUseDefaultOnMissingResource());
	}

	/**
	 * 
	 */
	public void testDefaultStringResourceLoaderSetup()
	{
		Settings settings = new Settings(tester.getApplication());
		List<IStringResourceLoader> loaders = settings.getStringResourceLoaders();
		Assert.assertEquals("There should be 4 default loaders", 4, loaders.size());
		Assert.assertTrue("First loader one should be the component one",
			loaders.get(0) instanceof ComponentStringResourceLoader);
		Assert.assertTrue("Second loader should be the application one",
			loaders.get(1) instanceof PackageStringResourceLoader);
		Assert.assertTrue("Third loader should be the application one",
			loaders.get(2) instanceof ClassStringResourceLoader);
		Assert.assertTrue("Fourth loader should be the validator one",
			loaders.get(3) instanceof ValidatorStringResourceLoader);
	}

	/**
	 * 
	 */
	public void testOverrideStringResourceLoaderSetup()
	{
		Application dummy = tester.getApplication();
		Settings settings = new Settings(dummy);
		settings.getStringResourceLoaders().clear();
		settings.addStringResourceLoader(new BundleStringResourceLoader(
			"org.apache.wicket.resource.DummyResources"));
		settings.addStringResourceLoader(new ComponentStringResourceLoader());
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
		Application dummy = tester.getApplication();
		Assert.assertNotNull("Localizer should be available", dummy.getResourceSettings()
			.getLocalizer());
	}
}