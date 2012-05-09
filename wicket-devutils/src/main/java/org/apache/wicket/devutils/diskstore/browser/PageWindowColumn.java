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
package org.apache.wicket.devutils.diskstore.browser;

import org.apache.wicket.Application;
import org.apache.wicket.devutils.diskstore.DebugDiskDataStore;
import org.apache.wicket.devutils.diskstore.DebugPageManagerProvider;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.serialize.ISerializer;

/**
 * A column that shows the page attributes (id, name, size)
 */
class PageWindowColumn extends PropertyColumn<PageWindowDescription, String>
{
	/**
	 * Construct.
	 * 
	 * @param displayModel
	 *            the header
	 * @param propertyExpression
	 *            the page attribute
	 */
	public PageWindowColumn(IModel<String> displayModel, String propertyExpression)
	{
		super(displayModel, propertyExpression);
	}

	@Override
	public void populateItem(Item<ICellPopulator<PageWindowDescription>> cellItem,
		String componentId, IModel<PageWindowDescription> rowModel)
	{
		String label;
		PageWindowDescription windowDescription = rowModel.getObject();
		if ("name".equals(getPropertyExpression()))
		{
			int pageId = windowDescription.getId();
			DebugPageManagerProvider pageManagerProvider = (DebugPageManagerProvider)Application.get()
				.getPageManagerProvider();
			DebugDiskDataStore dataStore = pageManagerProvider.getDataStore();
			String sessionId = windowDescription.getSessionId();
			byte[] data = dataStore.getData(sessionId, pageId);
			ISerializer serializer = Application.get().getFrameworkSettings().getSerializer();
			Object page = serializer.deserialize(data);
			label = page.getClass().getName();
		}
		else if ("id".equals(getPropertyExpression()))
		{
			label = Integer.toString(windowDescription.getId());
		}
		else if ("size".equals(getPropertyExpression()))
		{
			label = Integer.toString(windowDescription.getSize());
		}
		else
		{
			label = "unknown: " + getPropertyExpression();
		}

		cellItem.add(new Label(componentId, label));
	}
}
