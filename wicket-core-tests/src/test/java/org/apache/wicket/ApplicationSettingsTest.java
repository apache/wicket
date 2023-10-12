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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.protocol.http.mock.MockServletContext;
import org.apache.wicket.resource.loader.BundleStringResourceLoader;
import org.apache.wicket.resource.loader.ClassStringResourceLoader;
import org.apache.wicket.resource.loader.ComponentStringResourceLoader;
import org.apache.wicket.resource.loader.IStringResourceLoader;
import org.apache.wicket.resource.loader.InitializerStringResourceLoader;
import org.apache.wicket.resource.loader.PackageStringResourceLoader;
import org.apache.wicket.resource.loader.ValidatorStringResourceLoader;
import org.apache.wicket.settings.FrameworkSettings;
import org.apache.wicket.settings.ResourceSettings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/**
 * Test cases for the <code>ApplicationSettings</code> class.
 * 
 * @author Chris Turner
 */
class ApplicationSettingsTest
{

	/**
	 * detaches thread context
	 */
	@AfterEach
	void detachThreadContext()
	{
		ThreadContext.detach();
	}

	/**
	 * 
	 */
	@Test
	void testFrameworkVersion()
	{
		FrameworkSettings settings = new FrameworkSettings(new MockApplication());
		assertEquals("n/a", settings.getVersion());
	}

	/**
	 * @throws Exception
	 */
	@Test
	void testExceptionOnMissingResourceDefaultValue() throws Exception
	{
		ResourceSettings settings = new ResourceSettings(new MockApplication());
		assertTrue(settings.getThrowExceptionOnMissingResource(),
			"exceptionOnMissingResource should default to true");
	}

	/**
	 * @throws Exception
	 */
	@Test
	void testExceptionOnMissingResourceSetsCorrectly() throws Exception
	{
		ResourceSettings settings = new ResourceSettings(new MockApplication());
		settings.setThrowExceptionOnMissingResource(false);
		assertFalse(settings.getThrowExceptionOnMissingResource(),
			"exceptionOnMissingResource should have been set to false");
	}

	/**
	 * @throws Exception
	 */
	@Test
	void testUseDefaultOnMissingResourceDefaultValue() throws Exception
	{
		ResourceSettings settings = new ResourceSettings(new MockApplication());
		assertTrue(settings.getUseDefaultOnMissingResource(),
			"useDefaultOnMissingResource should default to true");
	}

	/**
	 * @throws Exception
	 */
	@Test
	void testUseDefaultOnMissingResourceSetsCorrectly() throws Exception
	{
		ResourceSettings settings = new ResourceSettings(new MockApplication());
		settings.setUseDefaultOnMissingResource(false);
		assertFalse(settings.getUseDefaultOnMissingResource(),
			"useDefaultOnMissingResource should have been set to false");
	}

	/**
	 * 
	 */
	@Test
	void testDefaultStringResourceLoaderSetup()
	{
		ResourceSettings settings = new ResourceSettings(new MockApplication());
		List<IStringResourceLoader> loaders = settings.getStringResourceLoaders();
		assertEquals(5, loaders.size(), "There should be 5 default loaders");
		assertTrue(loaders.get(0) instanceof ComponentStringResourceLoader,
			"First loader one should be the component one");
		assertTrue(loaders.get(1) instanceof PackageStringResourceLoader,
			"Second loader should be the package one");
		assertTrue(loaders.get(2) instanceof ClassStringResourceLoader,
			"Third loader should be the application one");
		assertTrue(loaders.get(3) instanceof ValidatorStringResourceLoader,
			"Fourth loader should be the validator one");
		assertTrue(loaders.get(4) instanceof InitializerStringResourceLoader,
			"Fifth should be the initializer one");
	}

	/**
	 * 
	 */
	@Test
	void testOverrideStringResourceLoaderSetup()
	{
		ResourceSettings settings = new ResourceSettings(new MockApplication());
		settings.getStringResourceLoaders().clear();
		settings.getStringResourceLoaders()
			.add(new BundleStringResourceLoader("org.apache.wicket.resource.DummyResources"));
		settings.getStringResourceLoaders().add(new ComponentStringResourceLoader());
		List<IStringResourceLoader> loaders = settings.getStringResourceLoaders();
		assertEquals(2, loaders.size(), "There should be 2 overridden loaders");
		assertTrue(loaders.get(0) instanceof BundleStringResourceLoader,
			"First loader one should be the bundle one");
		assertTrue(loaders.get(1) instanceof ComponentStringResourceLoader,
			"Second loader should be the component one");
	}

	/**
	 * 
	 */
	@Test
	void testLocalizer()
	{
		MockApplication dummy = new MockApplication();
		dummy.setName("test-app");
		dummy.setServletContext(new MockServletContext(dummy, ""));
		ThreadContext.setApplication(dummy);
		dummy.initApplication();
		Localizer localizer = dummy.getResourceSettings().getLocalizer();
		assertNotNull(localizer, "Localizer should be available");
		dummy.internalDestroy();
	}
}
