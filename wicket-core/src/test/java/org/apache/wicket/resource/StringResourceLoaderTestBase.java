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
package org.apache.wicket.resource;

import java.util.Locale;

import org.apache.wicket.Component;
import org.apache.wicket.resource.loader.IStringResourceLoader;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Abstract base class providing common test functionality to ensure that all loader implementations
 * comply with the contract of the loader interface.
 * 
 * @author Chris Turner
 */
public abstract class StringResourceLoaderTestBase extends Assert
{
	protected WicketTester tester;

	// The loader to test
	protected IStringResourceLoader loader;

	// The dummy component
	protected Component component;


	/**
	 * Abstract method to create the loader instance to be tested.
	 * 
	 * @return The loader instance to test
	 */
	protected abstract IStringResourceLoader createLoader();

	/**
	 * @throws Exception
	 */
	@Before
	public void before() throws Exception
	{
		tester = new WicketTester(new DummyApplication());
		component = new DummyComponent("test", tester.getApplication());
		DummyPage page = new DummyPage();
		page.add(component);
		loader = createLoader();
	}

	/**
	 * @throws Exception
	 */
	@After
	public void after() throws Exception
	{
		tester.destroy();
	}

	/**
	 * 
	 */
	@Test
	public void loaderValidKeyNoStyleDefaultLocale()
	{
		String s = loader.loadStringResource(component.getClass(), "test.string",
			Locale.getDefault(), null, null);
		Assert.assertEquals("Resource should be loaded", "This is a test", s);

		// And do it again to ensure caching path is exercised
		s = loader.loadStringResource(component.getClass(), "test.string", Locale.getDefault(),
			null, null);
		Assert.assertEquals("Resource should be loaded", "This is a test", s);
	}

	/**
	 * 
	 */
	@Test
	public void loaderInvalidKeyNoStyleDefaultLocale()
	{
		Assert.assertNull("Missing key should return null", loader.loadStringResource(
			component.getClass(), "unknown.string", Locale.getDefault(), null, null));
	}

	/**
	 * 
	 */
	@Test
	public void loaderValidKeyNoStyleAlternativeLocale()
	{
		String s = loader.loadStringResource(component.getClass(), "test.string", new Locale("zz"),
			null, null);
		Assert.assertEquals("Resource should be loaded", "Flib flob", s);
	}

	/**
	 * 
	 */
	@Test
	public void loaderInvalidKeyNoStyleAlternativeLocale()
	{
		Assert.assertNull("Missing key should return null", loader.loadStringResource(
			component.getClass(), "unknown.string", new Locale("zz"), null, null));
	}

	/**
	 * 
	 */
	@Test
	public void loaderValidKeyStyleNoLocale()
	{
		String s = loader.loadStringResource(component.getClass(), "test.string", null, "alt", null);
		Assert.assertEquals("Resource should be loaded", "Alt test string", s);
	}

	/**
	 * 
	 */
	@Test
	public abstract void loaderUnknownResources();
}
