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
package org.apache.wicket.markup.html.list;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.WicketTestCase;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.util.ListModel;
import org.junit.Test;

/**
 * Test for ListView
 * 
 * @author Juergen Donnerstag
 */
public class ListViewTest extends WicketTestCase
{
	/**
	 * Create a predefined ListView
	 * 
	 * @param modelListSize
	 *            # of elements to go into the list
	 * @return list view
	 */
	private ListView<Integer> createListView(final int modelListSize)
	{
		ArrayList<Integer> modelList = new ArrayList<Integer>();
		for (int i = 0; i < modelListSize; i++)
		{
			modelList.add(i);
		}

		return new ListView<Integer>("listView", new ListModel<Integer>(modelList))
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(final ListItem<Integer> listItem)
			{
				// do nothing
			}
		};
	}

	/**
	 */
	@Test
	public void generics() {
		// a listView for numbers
		class NumberListView extends ListView<Number> {

			private static final long serialVersionUID = 1L;

			// since the given list is not changed actually, we can safely
			// accept lists accepting subtypes of numbers only
			public NumberListView(String id, IModel<? extends List<? extends Number>> model)
			{
				super(id, model);
			}

			@Override
			protected void populateItem(ListItem<Number> item)
			{
				// non-fancy display of the number
				add(new Label("label", item.getModel()));
			}
		};
		
		IModel<List<Integer>> integers = new ListModel<>(new ArrayList<Integer>());

		// pass list of integers to the number listView
		new NumberListView("integers", integers);
	}

	/**
	 * 
	 */
	@Test
	public void listView()
	{
		ListView<Integer> lv = createListView(4);
		assertEquals(4, lv.getList().size());
		assertEquals(4, lv.getViewSize());
		assertEquals(0, lv.getStartIndex());

		// This is the number of ListViews child-components
		assertEquals(0, lv.size());

		lv.setStartIndex(-1);
		assertEquals(0, lv.getStartIndex());

		lv.setStartIndex(3);
		assertEquals(3, lv.getStartIndex());

		// The upper boundary doesn't get tested, yet.
		lv.setStartIndex(99);
		assertEquals(0, lv.getStartIndex());

		lv.setViewSize(-1);
		assertEquals(4, lv.getViewSize());

		lv.setViewSize(0);
		assertEquals(0, lv.getViewSize());

		// The upper boundary doesn't get tested, yet.
		lv.setViewSize(99);
		assertEquals(4, lv.getViewSize());
		lv.setStartIndex(1);
		assertEquals(3, lv.getViewSize());
	}

	/**
	 * 
	 */
	@Test
	public void emptyListView()
	{
		// Empty tables
		ListView<?> lv = createListView(0);
		assertEquals(0, lv.getStartIndex());
		assertEquals(0, lv.getViewSize());

		// null tables are a special case used for table navigation
		// bar, where there is no underlying model necessary, as
		// listItem.getIndex() is equal to the required listItem.getModelObject()
		lv = new ListView<Void>("listView", new ListModel<Void>())
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(final ListItem<Void> listItem)
			{
				// do nothing
			}
		};
		assertEquals(0, lv.getStartIndex());
		assertEquals(0, lv.getViewSize());

		lv.setStartIndex(5);
		lv.setViewSize(10);
		assertEquals(0, lv.getStartIndex());
		assertEquals(10, lv.getViewSize());
	}
}
