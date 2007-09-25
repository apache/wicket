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
package org.apache.wicket.examples;

import org.apache.wicket.PageMap;
import org.apache.wicket.RequestContext;
import org.apache.wicket.examples.debug.InspectorBug;
import org.apache.wicket.examples.source.SourcesPage;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.link.PopupSettings;
import org.apache.wicket.markup.html.panel.Panel;


/**
 * Navigation panel for the examples project.
 * 
 * @author Eelco Hillenius
 */
public final class WicketExampleHeader extends Panel
{
	/**
	 * Construct.
	 * 
	 * @param id
	 *            id of the component
	 * @param exampleTitle
	 *            title of the example
	 * @param page
	 *            The example page
	 */
	public WicketExampleHeader(String id, String exampleTitle, WebPage page)
	{
		super(id);
		add(new WebMarkupContainer("hideInPortlet")
		{
			/**
			 * @see org.apache.wicket.Component#isVisible()
			 */
			@Override
			public boolean isVisible()
			{
				return !RequestContext.get().isPortletRequest();
			}
		});		
		add(new InspectorBug("inspector", page));
		add(new Label("exampleTitle", exampleTitle));
		Link link = new Link("sources")
		{
			public void onClick()
			{
				setResponsePage(new SourcesPage(getPage().getClass()));
			}
		};
		add(link);

		PopupSettings settings = new PopupSettings(PageMap.forName("sources"), PopupSettings.RESIZABLE);
		settings.setWidth(800);
		settings.setHeight(600);
		settings.setWindowName("sources");
		link.setPopupSettings(settings);
	}
}
