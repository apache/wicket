/*
 * $Id$ $Revision:
 * 1.1 $ $Date$
 * ====================================================================
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

	private static final Log log = LogFactory.getLog(MockLoginPage.class);

	private Form form;

	private TextField textField;

	/**
	 * 
	 */
	public MockLoginPage()
	{
		super();
		add(new Label("label", "welcome please login"));
		add(form = new Form("form")
		{
			private static final long serialVersionUID = 1L;

			protected void onSubmit()
			{
				login(get("username").getModelObjectAsString().toString());
			}
		});
		form.add(textField = new TextField("username", new Model()));
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
