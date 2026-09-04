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
package org.apache.wicket.core.util.string.componentrenderer;

import java.util.Arrays;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;

/**
 * A page for testing https://issues.apache.org/jira/browse/WICKET-5325
 */
class PageWithRepeater extends WebPage implements IMarkupResourceStreamProvider
{
	PageWithRepeater()
	{
		add(new ListView<String>("listView", Arrays.asList("one", "two")) {

			@Override
			protected void populateItem(ListItem<String> item)
			{
				item.add(new Label("label", item.getModelObject()));
			}
		});
	}

	@Override
	public IResourceStream getMarkupResourceStream(MarkupContainer container, Class<?> containerClass)
	{
		return new StringResourceStream("<html><body><div wicket:id='listView'><span wicket:id='label'></span></div></body></html>");
	}
}
