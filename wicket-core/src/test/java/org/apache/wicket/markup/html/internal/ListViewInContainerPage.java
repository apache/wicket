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
package org.apache.wicket.markup.html.internal;

import java.util.ArrayList;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class ListViewInContainerPage extends BasePage 
{
	private static final long serialVersionUID = 1L;

	public ListViewInContainerPage(final PageParameters parameters) 
	{
		super(parameters);

		add(new Label("version", getApplication().getFrameworkSettings().getVersion()));

		ArrayList<Integer> values = new ArrayList<>();
		values.add(1);
		values.add(2);
		values.add(3);
		
		add(new ListView<Integer>("liste", values)
		{
			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(final ListItem<Integer> item)
			{
				final Label testenc = new Label("testenc", Model.of("enclosure " + item.getModelObject()));
				item.queue(testenc);

				final Label testlib = new Label("testlib", Model.of("no enclosure " + item.getModelObject()));
				item.queue(testlib);
			}
		});
    }
}
