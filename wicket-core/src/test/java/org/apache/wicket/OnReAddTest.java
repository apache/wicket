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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.util.tester.WicketTesterScope;
import org.junit.Rule;
import org.junit.Test;

public class OnReAddTest
{
	@Rule
	public WicketTesterScope scope = new WicketTesterScope();

	private boolean onReAddCalled = false;
	private boolean onInitializeCalled = false;

	@Test
	public void onFirstAddInitializeIsCalled()
	{
		Page page = createPage();
		page.internalInitialize();
		page.add(createUninitializedProbe());
		assertFalse(onReAddCalled);
		assertTrue(onInitializeCalled);
	}

	@Test
	public void nothingIsCalledWithoutConnectionToPage()
	{
		MarkupContainer container = createContainer();
		container.internalInitialize();
		container.add(createUninitializedProbe());
		assertFalse(onReAddCalled);
		assertFalse(onInitializeCalled);
	}

	@Test
	public void uninitializedComponentIsInitializedOnConnectionToPage()
	{
		// "old", initialized container + "new" uninitialized component:
		// oninitialize should be called on the component when the container
		// is added to the page, not before.
		MarkupContainer container = createContainer();
		container.internalInitialize();
		container.add(createUninitializedProbe());
		assertFalse(onReAddCalled);
		assertFalse(onInitializeCalled);
		WebPage page = createPage();
		page.internalInitialize();
		page.add(container);
		assertFalse(onReAddCalled);
		assertTrue(onInitializeCalled);
	}

	@Test
	public void onReAddIsOnlyCalledAfterRemove()
	{
		Page page = createPage();
		page.internalInitialize();
		Component probe = createUninitializedProbe();
		page.add(probe);
		assertFalse(onReAddCalled);
		assertTrue(onInitializeCalled);
		onInitializeCalled = false;
		page.internalInitialize();
		assertFalse(onReAddCalled);
		assertFalse(onInitializeCalled);
		page.remove(probe);
		assertFalse(onReAddCalled);
		assertFalse(onInitializeCalled);
		page.add(probe);
		assertTrue(onReAddCalled);
		assertFalse(onInitializeCalled);
	}
	
	@Test
	public void initializeIsCalledOnFirstAdd_OnReAddIsCalledAfterEachRemoveAndAdd()
	{
		Page page = createPage();
		page.internalInitialize();
		Component probe = createUninitializedProbe();
		page.add(probe);
		assertFalse(onReAddCalled);
		assertTrue(onInitializeCalled);
		onInitializeCalled = false;
		page.internalInitialize();
		assertFalse(onReAddCalled);
		assertFalse(onInitializeCalled);
		page.remove(probe);
		assertFalse(onReAddCalled);
		assertFalse(onInitializeCalled);
		page.add(probe);
		assertTrue(onReAddCalled);
		assertFalse(onInitializeCalled);
		onReAddCalled = false;
		page.internalInitialize();
		// just another initialize run shouldn't call onReAdd nor onInitialize. onReAdd should only be called
		// after remove and add
		assertFalse(onReAddCalled);
		assertFalse(onInitializeCalled);
		page.remove(probe);
		page.add(probe);
		assertTrue(onReAddCalled);
		assertFalse(onInitializeCalled);
	}

	@Test
	public void onReAddRecursesToChildrenLikeOnInitialize()
	{
		Page page = createPage();
		page.internalInitialize();
		Component probe = createNestedProbe();
		page.add(probe);
		assertFalse(onReAddCalled);
		assertTrue(onInitializeCalled);
		onInitializeCalled = false;
		probe.remove();
		assertFalse(onInitializeCalled);
		assertFalse(onReAddCalled);
		page.add(probe);
		assertFalse(onInitializeCalled);
		assertTrue(onReAddCalled);
	}

	@Test
	public void onReAddEnforcesSuperCall()
	{
		Page page = createPage();
		page.internalInitialize();
		Label brokenProbe = new Label("foo")
		{
			@Override
			protected void onReAdd()
			{
				; // I should call super, but since I don't, this should throw an exception
			}
		};
		brokenProbe.internalInitialize();
		page.add(brokenProbe);
		page.remove(brokenProbe);
		try
		{
			page.add(brokenProbe);
			fail("should have thrown exception");
		} catch (IllegalStateException e)
		{
			assertTrue(e.getMessage().contains("super.onReAdd"));
		}
	}

	private Component createUninitializedProbe()
	{
		return new Label("foo")
		{
			@Override
			protected void onReAdd()
			{
				super.onReAdd();
				onReAddCalled = true;
			}

			@Override
			protected void onInitialize()
			{
				super.onInitialize();
				onInitializeCalled = true;
			}
		};
	}

	private Component createInitializedProbe()
	{
		Component probe = createUninitializedProbe();
		probe.internalInitialize();
		return probe;
	}

	private WebPage createPage()
	{
		return new WebPage()
		{
		};
	}

	private Component createNestedProbe()
	{
		return createContainer().add(createUninitializedProbe());
	}

	private MarkupContainer createContainer()
	{
		return new WebMarkupContainer("bar");
	}
}
