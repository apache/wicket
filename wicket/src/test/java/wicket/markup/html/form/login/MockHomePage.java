/*
 * $Id$ $Revision$
 * $Date$
 * ====================================================================
 * Copyright (c) 2005, Topicus B.V. All rights reserved.
 */

package wicket.markup.html.form.login;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.Session;
import wicket.markup.html.WebPage;
import wicket.markup.html.basic.Label;
import wicket.markup.html.form.login.InterceptTest.MySession;
import wicket.markup.html.link.PageLink;

/**
 * @author marrink
 */
public class MockHomePage extends WebPage
{
	private static final long serialVersionUID = 1L;

	private static final Log log = LogFactory.getLog(MockHomePage.class);

	/**
	 * 
	 */
	public MockHomePage()
	{
		super();
		add(new Label("label", "this page is secured"));
		add(new PageLink("link", PageA.class));
	}

	/**
	 * 
	 * @return boolean
	 */
	public boolean logoff()
	{
		((MySession)Session.get()).setUsername(null);
		return true;
	}
}
