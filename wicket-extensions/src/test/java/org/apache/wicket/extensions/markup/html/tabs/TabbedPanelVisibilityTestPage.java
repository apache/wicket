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
package org.apache.wicket.extensions.markup.html.tabs;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 */
public class TabbedPanelVisibilityTestPage extends WebPage
{

	final TabbedPanel tabbedPanel;

	public TabbedPanelVisibilityTestPage(int nbTabs, int nbTabsVisible)
	{
		List<ITab> tabs = new ArrayList<ITab>(nbTabs);
		for (int i = 0; i < nbTabs; i++)
		{
			tabs.add(new DummyTab(i < nbTabsVisible));
		}

		tabbedPanel = new TabbedPanel("tabbedPanel", tabs);
		add(tabbedPanel);
	}

	public static final class DummyTab implements ITab
	{
		private boolean visible;

		public DummyTab(final boolean visible)
		{
			this.visible = visible;
		}

		@Override
		public IModel<String> getTitle()
		{
			return Model.of("Dummy");
		}

		@Override
		public WebMarkupContainer getPanel(final String containerId)
		{
			return new EmptyPanel(containerId);
		}

		@Override
		public boolean isVisible()
		{
			return visible;
		}
	}
}
