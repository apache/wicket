package org.apache.wicket;

import java.awt.Dimension;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.captcha.kittens.KittenCaptchaPanel;
import org.apache.wicket.markup.html.WebPage;

/**
 * Kitten captcha example
 */
public class HomePage extends WebPage
{
	private static final long serialVersionUID = 1L;

	private final KittenCaptchaPanel captcha;
	private int errors;

	/**
	 * Constructor that is invoked when page is invoked without a session.
	 * 
	 * @param parameters
	 *            Page parameters
	 */
	public HomePage(final PageParameters parameters)
	{
		add(captcha = new KittenCaptchaPanel("captcha", new Dimension(400, 200)));
		
		// In a real application, you'd check the kittens in a form
		add(new AjaxLink("checkKittens")
		{
			private static final long serialVersionUID = 642245961797905032L;

			@Override
			public void onClick(final AjaxRequestTarget target)
			{
				if (!isSpamBot() && captcha.allKittensSelected())
				{
					target.appendJavascript("alert('you win! happy kittens!');");
				}
				else
				{
					errors++;
					if (isSpamBot())
					{
						target.appendJavascript("alert('spammer alert');");
					}
					else
					{
						target.appendJavascript("alert('please try again');");
					}
					target.addComponent(captcha);
				}
				captcha.reset();
			}
		});
	}

	boolean isSpamBot()
	{
		return errors > 3;
	}
}
