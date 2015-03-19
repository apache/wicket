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
package org.apache.wicket.markup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.RefreshingView;
import org.apache.wicket.markup.repeater.util.ModelIteratorAdapter;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.junit.Test;

/**
 * Tests child's {@link Component#onRemove} is on refresh
 * 
 * @author igor
 */
public class RefreshingViewOnRemoveTest extends WicketTestCase
{
	private final List<TestComponent> components = new ArrayList<TestComponent>();
	private int round = 1;

	/**
	 * test()
	 */
	@Test
	public void test()
	{
		tester.startPage(new TestPage());

		// check everything was detached
		for (TestComponent c : components)
		{
			assertTrue("Component " + c + " is not detached.", c.detached);
		}

		round++;
		tester.startPage(tester.getLastRenderedPage());

		// check everything was detached
		for (TestComponent c : components)
		{
			assertTrue(c.detached);
		}

		// check we have round 1 and round 2 components
		boolean round1 = false;
		boolean round2 = false;
		for (TestComponent c : components)
		{
			if (c.round == 1)
				round1 = true;
			if (c.round == 2)
				round2 = true;
		}
		assertTrue(round1);
		assertTrue(round2);

		// check onremove was called on all round 1 components
		for (TestComponent c : components)
		{
			if (c.round == 1)
				assertTrue(c.removed);
		}

	}

	protected Component newComponent(String id)
	{
		TestComponent c = new TestComponent(id, round);
		components.add(c);
		return c;
	}

	static class TestComponent extends Label
	{
		private static final long serialVersionUID = 1L;
		public boolean detached = false;
		public boolean removed = false;
		public final int round;

		public TestComponent(String id, int round)
		{
			super(id, id);
			this.round = round;
		}

		@Override
		protected void onDetach()
		{
			super.onDetach();
			detached = true;
		}

		@Override
		protected void onRemove()
		{
			super.onRemove();
			removed = true;
		}
	}

	class TestPage extends WebPage implements IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;

		public TestPage()
		{
			add(new RefreshingView<Integer>("repeater")
			{
				private static final long serialVersionUID = 1L;

				@Override
				protected Iterator<IModel<Integer>> getItemModels()
				{
					return new ModelIteratorAdapter<Integer>(Arrays.asList(new Integer[] { 1, 2 })
						.iterator())
					{
						@Override
						protected IModel<Integer> model(Integer object)
						{
							return Model.of(object);
						}
					};
				}

				@Override
				protected void populateItem(Item<Integer> item)
				{
					item.add(newComponent("label"));
				}
			});
		}


		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream(
				"<html><body><ul><li wicket:id='repeater'><span wicket:id='label'></span></li></ul></body></html>");
		}
	}
}
