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
