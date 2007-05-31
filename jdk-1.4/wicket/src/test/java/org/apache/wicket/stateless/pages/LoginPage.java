package org.apache.wicket.stateless.pages;

import org.apache.wicket.markup.html.WebPage;

/**
 * 
 * @author marrink
 */
public class LoginPage extends WebPage
{

	private static final long serialVersionUID = 1L;


	/**
	 * Constructor.
	 */
	public LoginPage()
	{
		setStatelessHint(true);
		String panelId = "signInPanel";
		newUserPasswordSignInPanel(panelId);
	}

	/**
	 * Creeert een sign in panel voor instellingen die hun authenticatie enkel baseren op
	 * username/wachtwoord.
	 * @param panelId
	 * @param info
	 */
	private void newUserPasswordSignInPanel(String panelId)
	{
		add(new UsernamePasswordSignInPanel(panelId));
	}
}
