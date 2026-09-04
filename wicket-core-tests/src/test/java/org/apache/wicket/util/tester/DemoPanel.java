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

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

public class DemoPanel extends Panel {
	public DemoPanel(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new Label("label", () -> "Label"));
		add(new ListView<>("repeater", List.of("AAA", "BBB", "CCC", "DDD")) {
			@Override
			protected void populateItem(ListItem<String> item) {
				item.add(new Label("content", item.getModel()));
			}
		});
		add(new DemoPanelB("otherPanel"));
	}

	private class DemoPanelB extends Panel {
		public DemoPanelB(String id) {
			super(id);
		}

		@Override
		protected void onInitialize() {
			super.onInitialize();

			add(new Label("innerLabel", () -> "Inner Label"));
			add(new Label("label", () -> "Inner Label with same Wicket ID"));
		}
	}
}
