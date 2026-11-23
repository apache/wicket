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
package org.apache.wicket.util.tester;

import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

/**
 * Panel for {@link BaseWicketTesterTest}: its link carries an ajax behavior that is added
 * inside the panel, so a test cannot reach the behavior instance itself.
 */
public class ExecuteBehaviorTestPanel extends Panel
{
	private static final long serialVersionUID = 1L;

	private int count;

	public ExecuteBehaviorTestPanel(String id)
	{
		super(id);
	}

	public int getCount()
	{
		return count;
	}

	@Override
	protected void onInitialize()
	{
		super.onInitialize();

		add(new Label("counter", () -> Integer.toString(count))
			.setOutputMarkupId(true));
		WebMarkupContainer link = new WebMarkupContainer("link");
		link.add(new CountingBehavior());
		add(link);
	}

	static class CountingBehavior extends AjaxEventBehavior
	{
		private static final long serialVersionUID = 1L;

		CountingBehavior()
		{
			super("click");
		}

		@Override
		protected void onEvent(AjaxRequestTarget target)
		{
			ExecuteBehaviorTestPanel panel = getComponent().findParent(ExecuteBehaviorTestPanel.class);
			panel.count++;
			target.add(panel.get("counter"));
		}
	}

	static class OtherBehavior extends AjaxEventBehavior
	{
		private static final long serialVersionUID = 1L;

		OtherBehavior()
		{
			super("dblclick");
		}

		@Override
		protected void onEvent(AjaxRequestTarget target)
		{
		}
	}
}
