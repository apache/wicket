package org.apache.wicket.queueing;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

public class IncorrectCloseTagPanel extends Panel
{
	private static final long serialVersionUID = 1L;

	public IncorrectCloseTagPanel(String id)
	{
		super(id);
		add(new Label("test", " test <img src=\"img.png\" />").setEscapeModelStrings(false));
	}
}
