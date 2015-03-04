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
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test cases for the <code>KeyReplacingLocalizer</code> class.
 * 
 * @author Rob Sonke
 */
public class KeyReplacingLocalizerTest extends Assert
{
	
	private WicketTester tester;
	private ResourceSettings settings;

	protected KeyReplacingLocalizer localizer;

	/**
	 * 
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception
	{
		tester = new WicketTester(new DummyApplication());
		settings = tester.getApplication().getResourceSettings();
		localizer = new KeyReplacingLocalizer();
		settings.setLocalizer(localizer);
	}

	@After
	public void tearDown() throws Exception
	{
		tester.destroy();
	}

	@Test
	public void testGetStringWithNestedKey()
	{
		Assert.assertEquals("Expected string should be returned", "This is a test with a nested string",
			localizer.getString("test.nested.string", null, null, "DEFAULT"));
	}

	@Test
	public void testGetStringWithNestedNestedKey()
	{
		Assert.assertEquals("Expected string should be returned", "Testing multi level nesting: This is a test with a nested string",
			localizer.getString("test.nested.nested.string", null, null, "DEFAULT"));
	}
}
