/*
 * $Id: MockLoginPage.java 5844 2006-05-24 20:53:56 +0000 (Wed, 24 May 2006)
 * joco01 $ $Revision$ $Date: 2006-05-25 22:52:19 +0000 (Thu, 25 May
 * 2006) $ ====================================================================
 * Copyright (c) 2005, Topicus B.V. All rights reserved.
 */

package wicket.markup.html.form.login;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.Application;
import wicket.Session;
import wicket.markup.html.WebPage;
import wicket.markup.html.basic.Label;
import wicket.markup.html.form.Form;
import wicket.markup.html.form.TextField;
import wicket.markup.html.form.login.InterceptTest.MySession;
import wicket.model.Model;

/**
 * @author marrink
 */
public class MockLoginPage extends WebPage
{
	private static final long serialVersionUID = 1L;

	private static Log log = LogFactory.getLog(MockLoginPage.class);

	private Form form;

	private TextField textField;

	/**
	 * 
	 */
	public MockLoginPage()
	{
		super();
		new Label(this, "label", "welcome please login");
		form = new Form(this, "form")
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit()
			{
				login(get("username").getModelObjectAsString().toString());
			}
		};
		textField = new TextField<String>(form, "username", new Model<String>());
	}

	/**
	 * 
	 * @param username
	 * @return boolean
	 */
	public boolean login(String username)
	{
		((MySession)Session.get()).setUsername(username);
		if (!continueToOriginalDestination())
		{
			setResponsePage(Application.get().getHomePage());
		}
		return true;
	}

	/**
	 * 
	 * @return form
	 */
	public final Form getForm()
	{
		return form;
	}

	/**
	 * 
	 * @return textfield
	 */
	public final TextField getTextField()
	{
		return textField;
	}
}
