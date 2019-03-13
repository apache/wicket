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
package org.apache.wicket.markup.repeater;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;

/**
 * Test for {@link AbstractPageableView}. 
 */
public class AbstractPageableViewTest extends WicketTestCase
{
	private static int count = 5;
	
	/**
	 */
	@Test
	public void cachedItemCount()
	{
		View view = new View("f");
		
		assertEquals(5, view.getItemCount());
		
		count = 6;

		assertEquals("still 5 cached", 5, view.getItemCount());
		
		view.beforeRender();

		assertEquals("cached cleared before render", 6, view.getItemCount());

		byte[] bytes = tester.getApplication().getFrameworkSettings().getSerializer().serialize(view);
		
		view = (View)tester.getApplication().getFrameworkSettings().getSerializer().deserialize(bytes);
		
		count = 7;
		
		assertEquals("cached cleared when deserialized", 7, view.getItemCount());
		
		view.detach();
		
		count = 8;
		
		assertEquals("cached cleared when detached", 8, view.getItemCount());
	}	

	static class View extends AbstractPageableView<Integer>
	{
		public View(String id)
		{
			super(id);
		}

		@Override
		protected void populateItem(Item<Integer> item)
		{
		}
		
		@Override
		protected long internalGetItemCount()
		{
			return count;
		}
		
		@Override
		protected Iterator<IModel<Integer>> getItemModels(long offset, long size)
		{
			List<IModel<Integer>> models = new ArrayList<>();
			for (int m = 0; m < count; m++) {
				models.add(Model.of(m));
			}
			return models.iterator();
		}
	}
}