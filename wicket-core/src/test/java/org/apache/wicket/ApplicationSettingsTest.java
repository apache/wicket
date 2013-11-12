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

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Assert;

import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.protocol.http.mock.MockServletContext;
import org.apache.wicket.resource.loader.BundleStringResourceLoader;
import org.apache.wicket.resource.loader.ClassStringResourceLoader;
import org.apache.wicket.resource.loader.ComponentStringResourceLoader;
import org.apache.wicket.resource.loader.IStringResourceLoader;
import org.apache.wicket.resource.loader.InitializerStringResourceLoader;
import org.apache.wicket.resource.loader.PackageStringResourceLoader;
import org.apache.wicket.resource.loader.ValidatorStringResourceLoader;
import org.apache.wicket.settings.IResourceSettings;
import org.apache.wicket.settings.def.FrameworkSettings;
import org.apache.wicket.settings.def.ResourceSettings;
import org.junit.After;
import org.junit.Test;

/**
 * Test cases for the <code>ApplicationSettings</code> class.
 * 
 * @author Chris Turner
 */
public class ApplicationSettingsTest
{

	/**
	 * detaches thread context
	 */
	@After
	public void detachThreadContext()
	{
		ThreadContext.detach();
	}

	/**
	 * 
	 */
	@Test
	public void testFrameworkVersion()
	{
		FrameworkSettings settings = new FrameworkSettings(new MockApplication());
		assertEquals("n/a", settings.getVersion());
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testExceptionOnMissingResourceDefaultValue() throws Exception
	{
		IResourceSettings settings = new ResourceSettings(new MockApplication());
		Assert.assertTrue("exceptionOnMissingResource should default to true",
			settings.getThrowExceptionOnMissingResource());
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testExceptionOnMissingResourceSetsCorrectly() throws Exception
	{
		IResourceSettings settings = new ResourceSettings(new MockApplication());
		settings.setThrowExceptionOnMissingResource(false);
		Assert.assertFalse("exceptionOnMissingResource should have been set to false",
			settings.getThrowExceptionOnMissingResource());
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testUseDefaultOnMissingResourceDefaultValue() throws Exception
	{
		IResourceSettings settings = new ResourceSettings(new MockApplication());
		Assert.assertTrue("useDefaultOnMissingResource should default to true",
			settings.getUseDefaultOnMissingResource());
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testUseDefaultOnMissingResourceSetsCorrectly() throws Exception
	{
		IResourceSettings settings = new ResourceSettings(new MockApplication());
		settings.setUseDefaultOnMissingResource(false);
		Assert.assertFalse("useDefaultOnMissingResource should have been set to false",
			settings.getUseDefaultOnMissingResource());
	}

	/**
	 * 
	 */
	@Test
	public void testDefaultStringResourceLoaderSetup()
	{
		IResourceSettings settings = new ResourceSettings(new MockApplication());
		List<IStringResourceLoader> loaders = settings.getStringResourceLoaders();
		Assert.assertEquals("There should be 5 default loaders", 5, loaders.size());
		Assert.assertTrue("First loader one should be the component one",
			loaders.get(0) instanceof ComponentStringResourceLoader);
		Assert.assertTrue("Second loader should be the package one",
			loaders.get(1) instanceof PackageStringResourceLoader);
		Assert.assertTrue("Third loader should be the application one",
			loaders.get(2) instanceof ClassStringResourceLoader);
		Assert.assertTrue("Fourth loader should be the validator one",
			loaders.get(3) instanceof ValidatorStringResourceLoader);
		Assert.assertTrue("Fifth should be the initializer one",
			loaders.get(4) instanceof InitializerStringResourceLoader);
	}

	/**
	 * 
	 */
	@Test
	public void testOverrideStringResourceLoaderSetup()
	{
		IResourceSettings settings = new ResourceSettings(new MockApplication());
		settings.getStringResourceLoaders().clear();
		settings.getStringResourceLoaders().add(
			new BundleStringResourceLoader("org.apache.wicket.resource.DummyResources"));
		settings.getStringResourceLoaders().add(new ComponentStringResourceLoader());
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
	@Test
	public void testLocalizer()
	{
		MockApplication dummy = new MockApplication();
		dummy.setName("test-app");
		dummy.setServletContext(new MockServletContext(dummy, ""));
		ThreadContext.setApplication(dummy);
		dummy.initApplication();
		Localizer localizer = dummy.getResourceSettings().getLocalizer();
		Assert.assertNotNull("Localizer should be available", localizer);
	}
}
