package org.apache.wicket.stateless.pages;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;


/**
 * @author marrink
 * 
 */
public class HomePage extends WebPage
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public HomePage()
	{
		add(new Label("welcome", "Welcome"));
		add(new Link("link")
		{
			private static final long serialVersionUID = 1L;

			public void onClick()
			{
				setVisible(true); // dummy op
			}
		});

	}

}
