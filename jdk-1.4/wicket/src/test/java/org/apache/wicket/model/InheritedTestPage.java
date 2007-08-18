package org.apache.wicket.model;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;

/**
 * Testpage for inherited models. TestCases should set the model on the page.
 */
public class InheritedTestPage extends WebPage
{
	private static final long serialVersionUID = 1L;

	/**
	 * public for querying in test case. Is the only component on this page,
	 * rendered in a h1 tag.
	 */
	public final Label label = new Label("label");

	/**
	 * Construct.
	 */
	public InheritedTestPage()
	{
		add(label);
	}
}
