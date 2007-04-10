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
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.resource.loader.IStringResourceLoader;
import org.apache.wicket.util.tester.WicketTester;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * Abstract base class providing common test functionality to ensure that all loader
 * implementations comply with the contract of the loader interface.
 * @author Chris Turner
 */
public abstract class StringResourceLoaderTestBase extends TestCase
{
	WicketTester tester;
	// The loader to test
	protected IStringResourceLoader loader;

	// The dummy application
	protected WebApplication application;

	// The dummy component
	protected Component component;

	/**
	 * Create the test case.
	 * @param message The name of the test
	 */
	protected StringResourceLoaderTestBase(String message)
	{
		super(message);
	}

	/**
	 * Abstract method to create the loader instance to be tested.
	 * @return The loader instance to test
	 */
	protected abstract IStringResourceLoader createLoader();

	protected void setUp() throws Exception
	{
		super.setUp();
		this.application = new DummyApplication();
		// Initialize the application
		tester = new WicketTester(this.application);
		this.component = new DummyComponent("test", this.application);
		DummyPage page = new DummyPage();
		page.add(this.component);
		this.loader = createLoader();
	}
	protected void tearDown() throws Exception
	{
		tester.destroy();
	}

	/**
	 * 
	 */
	public void testLoaderValidKeyNoStyleDefaultLocale()
	{
		String s = loader.loadStringResource(component.getClass(), "test.string", Locale.getDefault(), null);
		Assert.assertEquals("Resource should be loaded", "This is a test", s);

		// And do it again to ensure caching path is exercised
		s = loader.loadStringResource(component.getClass(), "test.string", Locale.getDefault(), null);
		Assert.assertEquals("Resource should be loaded", "This is a test", s);
	}

	/**
	 * 
	 */
	public void testLoaderInvalidKeyNoStyleDefaultLocale()
	{
		Assert.assertNull("Missing key should return null", loader.loadStringResource(component.getClass(), "unknown.string",
				Locale.getDefault(), null));
	}

	/**
	 * 
	 */
	public void testLoaderValidKeyNoStyleAlternativeLocale()
	{
		String s = loader.loadStringResource(component.getClass(), "test.string", new Locale("zz"), null);
		Assert.assertEquals("Resource should be loaded", "Flib flob", s);
	}

	/**
	 * 
	 */
	public void testLoaderInvalidKeyNoStyleAlternativeLocale()
	{
		Assert.assertNull("Missing key should return null", loader.loadStringResource(component.getClass(), "unknown.string",
				new Locale("zz"), null));
	}

	/**
	 * 
	 */
	public void testLoaderValidKeyStyleNoLocale()
	{
		String s = loader.loadStringResource(component.getClass(), "test.string", null, "alt");
		Assert.assertEquals("Resource should be loaded", "Alt test string", s);
	}

	/**
	 * 
	 */
	public abstract void testLoaderUnknownResources();

}

// 
