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
package org.apache.wicket.extensions.tester.ajax.markup.html;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.AjaxLazyLoadPanel;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;

/**
 */
public class AjaxLazyLoadPanelPage extends WebPage
{
	private static final long serialVersionUID = 1L;
	
	public boolean contentReady = false;
	
	public final AjaxLazyLoadPanel<Component> panel;

	public final AjaxLink<Void> link;

	/**
	 * Construct.
	 */
	public AjaxLazyLoadPanelPage()
	{
		panel = new AjaxLazyLoadPanel<Component>("panel")
		{
			@Override
			protected boolean isContentReady()
			{
				return contentReady;
			}
			
			@Override
			public Component getLoadingComponent(String id)
			{
				return new Label(id, "LOADING");
			}
			
			@Override
			public Component getLazyLoadComponent(String id)
			{
				return new Label(id, "LOADED");
			}
		};
		add(panel);
		
		link = new AjaxLink<Void>("link")
		{
			@Override
			public void onClick(AjaxRequestTarget target)
			{
				if (panel.isVisible() == false) {
					panel.setVisible(true);
				}
				
				target.add(panel);
			}
		};
		add(link);
	}
}
