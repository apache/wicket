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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Locale;

import org.apache.wicket.Component;
import org.apache.wicket.resource.loader.IStringResourceLoader;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Abstract base class providing common test functionality to ensure that all loader implementations
 * comply with the contract of the loader interface.
 * 
 * @author Chris Turner
 */
public abstract class StringResourceLoaderTestBase
{
	WicketTester tester;

	// The loader to test
	protected IStringResourceLoader loader;

	// The dummy component
	Component component;


	/**
	 * Abstract method to create the loader instance to be tested.
	 * 
	 * @return The loader instance to test
	 */
	protected abstract IStringResourceLoader createLoader();

	/**
	 * @throws Exception
	 */
	@BeforeEach
	void before() throws Exception
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
	@AfterEach
	void after() throws Exception
	{
		tester.destroy();
	}

	/**
	 * 
	 */
	@Test
	void loaderValidKeyNoStyleDefaultLocale()
	{
		String s = loader.loadStringResource(component.getClass(), "test.string",
			Locale.getDefault(), null, null);
		assertEquals("This is a test", s, "Resource should be loaded");

		// And do it again to ensure caching path is exercised
		s = loader.loadStringResource(component.getClass(), "test.string", Locale.getDefault(),
			null, null);
		assertEquals("This is a test", s, "Resource should be loaded");
	}

	/**
	 * 
	 */
	@Test
	void loaderInvalidKeyNoStyleDefaultLocale()
	{
		assertNull(loader.loadStringResource(component.getClass(), "unknown.string",
			Locale.getDefault(), null, null), "Missing key should return null");
	}

	/**
	 * 
	 */
	@Test
	void loaderValidKeyNoStyleAlternativeLocale()
	{
		String s = loader.loadStringResource(component.getClass(), "test.string", new Locale("zz"),
			null, null);
		assertEquals("Flib flob", s, "Resource should be loaded");
	}

	/**
	 * 
	 */
	@Test
	void loaderInvalidKeyNoStyleAlternativeLocale()
	{
		assertNull(loader.loadStringResource(component.getClass(), "unknown.string",
			new Locale("zz"), null, null), "Missing key should return null");
	}

	/**
	 * 
	 */
	@Test
	void loaderValidKeyStyleNoLocale()
	{
		String s = loader.loadStringResource(component.getClass(), "test.string", null, "alt",
			null);
		assertEquals("Alt test string", s, "Resource should be loaded");
	}

	/**
	 * 
	 */
	@Test
	public abstract void loaderUnknownResources();
}
