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
package org.apache.wicket.examples.portlet.menu;

import javax.portlet.PortletSession;

import org.apache.wicket.RequestContext;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.Loop;
import org.apache.wicket.protocol.http.portlet.PortletRequestContext;

/**
 * @author ate
 */
public class MenuPage extends WebPage
{
	public MenuPage()
	{
		add(new Loop("examples", WicketExamplesMenuApplication.getExamples().size()-1)
		{
			
			@Override
			protected void populateItem(LoopItem item)
			{
				final int index = item.getIteration();
				ExampleApplication ea = (ExampleApplication)WicketExamplesMenuApplication.getExamples().get(index+1);
				Link link = new Link("example")
				{
					@Override
					public void onClick()
					{
						int index = ((LoopItem)getParent()).getIteration();
						ExampleApplication ea = (ExampleApplication)WicketExamplesMenuApplication.getExamples().get(index+1);
						PortletSession session = ((PortletRequestContext)RequestContext.get()).getPortletRequest().getPortletSession();
						session.setAttribute(WicketExamplesMenuPortlet.EXAMPLE_APPLICATION_ATTR, ea);
					}
				};
				link.add(new Label("name", ea.getDisplayName()));
				item.add(link);
				item.add(new Label("description", ea.getDescription()));
			}
		});
	}
}
