package org.apache.wicket.markup.html.form;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.panel.FeedbackPanel;

/**
 * @author dashorst
 */
public class LocalizedMessagePage extends WebPage
{
	/** */
	private static final long serialVersionUID = 1L;

	public Form form;
	public TextField integerField;
	public FeedbackPanel feedback;

	public LocalizedMessagePage()
	{
		add(form = new Form("form"));
		form.add(integerField = new TextField("integer", Integer.class));
		form.add(feedback = new FeedbackPanel("feedback"));
	}
}
