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

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.junit.Test;

/**
 * Test for ajax handler.
 * 
 * @author Juergen Donnerstag
 */
public class ComponentTest extends WicketTestCase
{

	/**
	 * Tests the number of detach calls on a Page, Component, Behavior and Model during a normal
	 * request.
	 * 
	 * @throws Exception
	 */
	@Test
	public void detachPage() throws Exception
	{
		executeTest(TestDetachPage.class, "TestDetachPageExpectedResult.html");
		TestDetachPage page = (TestDetachPage)tester.getLastRenderedPage();
		assertTrue(page.getNrComponentDetachModelCalls() > 0);
		assertTrue(page.getNrComponentDetachModelsCalls() > 0);
		assertTrue(page.getNrComponentOnDetachCalls() > 0);
		assertTrue(page.getNrPageDetachModelCalls() > 0);
		assertTrue(page.getNrPageDetachModelsCalls() > 0);
		assertTrue(page.getNrPageOnDetachCalls() > 0);
		assertTrue(page.getNrModelDetachCalls() > 0);
		assertTrue(page.getNrAjaxBehaviorDetachModelCalls() > 0);
	}

	/**
	 * Tests the number of detach calls on a Page, Component, Behavior and Model during an Ajax
	 * request.
	 * 
	 * @throws Exception
	 */
	@Test
	public void detachPageAjaxRequest() throws Exception
	{
		executeTest(TestDetachPage.class, "TestDetachPageExpectedResult.html");
		TestDetachPage page = (TestDetachPage)tester.getLastRenderedPage();

		assertTrue(page.getNrComponentDetachModelCalls() > 0);
		assertTrue(page.getNrComponentDetachModelsCalls() > 0);
		assertTrue(page.getNrComponentOnDetachCalls() > 0);
		assertTrue(page.getNrPageDetachModelCalls() > 0);
		assertTrue(page.getNrPageDetachModelsCalls() > 0);
		assertTrue(page.getNrPageOnDetachCalls() > 0);
		assertTrue(page.getNrModelDetachCalls() > 0);
		assertTrue(page.getNrAjaxBehaviorDetachModelCalls() > 0);

		AjaxEventBehavior behavior = page.getAjaxBehavior();
		executeBehavior(behavior, "TestDetachPageAjaxResult.html");
		assertTrue(1 <= page.getNrComponentDetachModelCalls());
		assertTrue(1 <= page.getNrComponentDetachModelsCalls());
		assertTrue(1 <= page.getNrComponentOnDetachCalls());
		assertTrue(1 <= page.getNrPageDetachModelCalls());
		assertTrue(1 <= page.getNrPageDetachModelsCalls());
		assertTrue(1 <= page.getNrPageOnDetachCalls());
		assertTrue(1 <= page.getNrModelDetachCalls());
		assertTrue(1 <= page.getNrAjaxBehaviorDetachModelCalls());
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void renderHomePage_1() throws Exception
	{
		executeTest(TestPage_1.class, "TestPageExpectedResult_1.html");
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-4468
	 *
	 * Verifies that a stateful component can pretend to be stateless if both conditions are
	 * fulfilled:
	 * <ol>
	 *     <li>it is either invisible or disabled (or both)</li>
	 *     <li>it cannot call any listener interface method while invisible/disabled</li>
	 * </ol>
	 */
	@Test
	public void isStateless()
	{
		Behavior statefulBehavior = new Behavior()
		{
			@Override
			public boolean getStatelessHint(Component component)
			{
				return false;
			}
		};

		WebComponent component = new WebComponent("someId");

		// by default every component is stateless
		assertTrue(component.isStateless());

		// make the component stateful
		component.add(statefulBehavior);
		assertFalse(component.isStateless());

		// invisible component cannot be requested by default so it
		// can pretend being stateless
		component.setVisible(false);
		assertTrue(component.isStateless());

		// same for disabled component
		component.setVisible(true).setEnabled(false);
		assertTrue(component.isStateless());

		// make the component such that it can call listener interface
		// methods no matter whether it is visible or enabled
		component = new WebComponent("someId") {
			@Override
			public boolean canCallListenerInterface(Method method)
			{
				return true;
			}
		};
		component.add(statefulBehavior);
		component.setVisible(false);
		assertFalse(component.isStateless());
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-4483
	 *
	 * setDefaultModel() should call modelChanging/modelChanged only if the new model
	 * is different that the old one. The same as setDefaultModelObject().
	 */
	@Test
	public void modelChange()
	{
		final AtomicBoolean modelChanging = new AtomicBoolean(false);
		final AtomicBoolean modelChanged = new AtomicBoolean(false);

		WebComponent component = new WebComponent("someId")
		{
			@Override
			protected void onModelChanging()
			{
				super.onModelChanging();
				modelChanging.set(true);
			}

			@Override
			protected void onModelChanged()
			{
				super.onModelChanged();
				modelChanged.set(true);
			}
		};

		assertNull(component.getDefaultModel());
		IModel<Integer> model = Model.of(1);

		// set a model which is different that the old one (old = null, new = non-null)
		component.setDefaultModel(model);
		assertTrue(modelChanging.getAndSet(false));
		assertTrue(modelChanged.getAndSet(false));

		// set the same instance - no change notifications should happen
		component.setDefaultModel(model);
		assertFalse(modelChanging.get());
		assertFalse(modelChanged.get());
	}
}
