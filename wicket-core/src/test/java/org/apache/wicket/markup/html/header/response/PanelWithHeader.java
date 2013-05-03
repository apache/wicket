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
package org.apache.wicket.markup.html.header.response;

import java.util.List;

import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.PriorityHeaderItem;
import org.apache.wicket.markup.head.StringHeaderItem;
import org.apache.wicket.markup.html.panel.Panel;

/**
 * Panel to be rendered in the body of the page, with a wicket:head and a contribution
 */
public class PanelWithHeader extends Panel
{
	private static final long serialVersionUID = 1L;

	private static class StringHeaderItemWithDependency extends StringHeaderItem
	{
		public StringHeaderItemWithDependency(CharSequence string)
		{
			super(string);
		}

		@Override
		public List<HeaderItem> getDependencies()
		{
			List<HeaderItem> dependencies = super.getDependencies();
			dependencies.add(StringHeaderItem.forString("<title>DependencyOfPriorityHeaderContributionInPanelWithHeader</title>\n"));
			return dependencies;
		}
	}

	/**
	 * Construct.
	 * 
	 * @param id
	 */
	public PanelWithHeader(String id)
	{
		super(id);
	}

	@Override
	public void renderHead(IHeaderResponse response)
	{
		response.render(StringHeaderItem.forString("<title>HeaderContributionInPanelWithHeader</title>\n"));
		response.render(new PriorityHeaderItem(new StringHeaderItemWithDependency(
			"<title>PriorityHeaderContributionInPanelWithHeader</title>\n")));
	}
}
