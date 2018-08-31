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
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.resource.loader.ComponentStringResourceLoader;
import org.apache.wicket.resource.loader.IStringResourceLoader;
import org.junit.jupiter.api.Test;

/**
 * Test case for the <code>ComponentStringResourceLoader</code> class.
 * 
 * @author Chris Turner
 */
public class ComponentStringResourceLoaderTest extends StringResourceLoaderTestBase
{

	/**
	 * Create and return the loader instance
	 * 
	 * @return The loader instance to test
	 */
	@Override
    protected IStringResourceLoader createLoader()
	{
		return new ComponentStringResourceLoader();
	}

	/**
	 * @see org.apache.wicket.resource.StringResourceLoaderTestBase#testLoaderUnknownResources()
	 */
	@Override
	@Test
    public void loaderUnknownResources()
	{
		Component c = new DummyComponent("hello", tester.getApplication())
		{
			private static final long serialVersionUID = 1L;
		};
		DummyPage page = new DummyPage();
		page.add(c);
		IStringResourceLoader loader = new ComponentStringResourceLoader();
		assertNull(loader.loadStringResource(c.getClass(), "test.string.bad", Locale.getDefault(),
			null, null), "Missing resource should return null");
	}

	/**
	 * 
	 */
	@Test
	void nullComponent()
	{
		assertNull(loader.loadStringResource((Component)null, "test.string", Locale.getDefault(),
			null, null), "Null component should skip resource load");
	}

	/**
	 * 
	 */
	@Test
	void multiLevelEmbeddedComponentLoadFromComponent()
	{
		DummyPage p = new DummyPage();
		Panel panel = new EmptyPanel("panel");
		p.add(panel);
		DummyComponent c = new DummyComponent("hello", tester.getApplication());
		panel.add(c);
		IStringResourceLoader loader = new ComponentStringResourceLoader();
		assertEquals("Component string", loader
			.loadStringResource(c.getClass(), "component.string", Locale.getDefault(), null, null), "Valid resourse string should be found");
	}

	/**
	 * 
	 */
	@Test
	void loadDirectFromPage()
	{
		DummyPage p = new DummyPage();
		IStringResourceLoader loader = new ComponentStringResourceLoader();
		assertEquals("Another string", loader.loadStringResource(p.getClass(),
			"another.test.string", Locale.getDefault(), null, null),
			"Valid resourse string should be found");
	}

	/**
	 * 
	 */
	@Test
	void searchClassHierarchyFromPage()
	{
		DummySubClassPage p = new DummySubClassPage();
		IStringResourceLoader loader = new ComponentStringResourceLoader();
		assertEquals("SubClass Test String", loader.loadStringResource(p.getClass(),
			"subclass.test.string", Locale.getDefault(), null, null),
			"Valid resource string should be found");
		assertEquals("Another string", loader.loadStringResource(p.getClass(),
			"another.test.string", Locale.getDefault(), null, null),
			"Valid resource string should be found");
	}
}
