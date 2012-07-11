package org.apache.wicket.examples.basic.linkcounter;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.Model;

public class LinkCounterPage extends WebPage {
	private static final long serialVersionUID = 1L;

	private int counter = 0;

	private Label counterLabel;

	public LinkCounterPage() {
		add(counterLabel = new Label("counter", Model.of(counter)));
		add(new Link<Void>("link") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick() {
				counter++;
				counterLabel.setDefaultModelObject(counterLabel);
			}
		});
	}
}
