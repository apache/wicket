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
package org.apache.wicket.examples.compref;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;

/**
 * Tests the component reference page of a component using models beside its default model.
 *
 * @author reiern70
 */
public class MultipleModelsPageTest extends WicketTestCase
{
	/**
	 * The panel has its default model plus the three it registers.
	 */
	@Test
	void panelRegistersItsModels()
	{
		tester.startPage(MultipleModelsPage.class);
		tester.assertRenderedPage(MultipleModelsPage.class);

		assertEquals(4, card().getModels().size());
	}

	/**
	 * The registered models are detached with the default model at the end of the request,
	 * although the panel overrides no onDetach.
	 */
	@Test
	void modelsAreDetachedAfterTheRequest()
	{
		tester.startPage(MultipleModelsPage.class);

		for (IModel<?> model : card().getModels())
		{
			if (model instanceof LoadableDetachableModel)
			{
				assertFalse(((LoadableDetachableModel<?>)model).isAttached(),
					"still attached: " + model);
			}
		}
	}

	/**
	 * The texts come from CustomerCardPanel.properties, the order status through a key built
	 * from the model.
	 */
	@Test
	void textsComeFromTheBundle()
	{
		tester.startPage(MultipleModelsPage.class);

		tester.assertLabel("card:title", "Customer card");
		tester.assertLabel("card:name", "Wile E. Coyote");
		tester.assertLabel("card:orders:0:number", "ACME-1");
		tester.assertLabel("card:orders:0:status", "paid");
		tester.assertLabel("card:orders:1:status", "open");
	}

	/**
	 * The derived model reads through the orders model.
	 */
	@Test
	void unpaidTotalIsDerivedFromTheOrders()
	{
		tester.startPage(MultipleModelsPage.class);

		// ACME-2 and ACME-3 are unpaid
		tester.assertModelValue("card:unpaidTotal", 45 + 980);
	}

	/**
	 * Selecting another customer reloads the models built on the default model.
	 */
	@Test
	void selectingACustomerReloadsTheDerivedModels()
	{
		tester.startPage(MultipleModelsPage.class);
		tester.clickLink("customers:1:select");

		tester.assertLabel("card:name", "Road Runner");
		assertEquals(1, orders().getViewSize());
		tester.assertModelValue("card:unpaidTotal", 0);
	}

	/**
	 * The setter replaces the orders model and shows only the unpaid ones.
	 */
	@Test
	void replacingTheOrdersModelChangesWhatIsShown()
	{
		tester.startPage(MultipleModelsPage.class);
		assertEquals(3, orders().getViewSize());

		tester.clickLink("unpaidOnly");
		assertEquals(2, orders().getViewSize());

		tester.clickLink("allOrders");
		assertEquals(3, orders().getViewSize());
	}

	/**
	 * replaceAdditionalModel unregisters the model it replaces, so repeatedly replacing does not
	 * pile models up on the component.
	 */
	@Test
	void replacingAModelDoesNotRegisterItTwice()
	{
		tester.startPage(MultipleModelsPage.class);

		tester.clickLink("unpaidOnly");
		tester.clickLink("allOrders");
		tester.clickLink("unpaidOnly");

		assertEquals(4, card().getModels().size());
	}

	private CustomerCardPanel card()
	{
		return (CustomerCardPanel)tester.getComponentFromLastRenderedPage("card");
	}

	private ListView<?> orders()
	{
		return (ListView<?>)tester.getComponentFromLastRenderedPage("card:orders");
	}
}
