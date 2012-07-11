package org.apache.wicket.examples.basic.helloworld;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;

public class HelloWorldPage extends WebPage {
	private static final long serialVersionUID = 1L;

	public HelloWorldPage() {
		add(new Label("msg", "Hello, World!"));
	}
}
