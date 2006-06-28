/*
 * $Id: MockHomePage.java 5844 2006-05-24 20:53:56 +0000 (Wed, 24 May 2006)
 * joco01 $ $Revision$ $Date: 2006-05-24 20:53:56 +0000 (Wed, 24 May
 * 2006) $ ====================================================================
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
		new Label(this, "label", "this page is secured");
		new PageLink(this, "link", PageA.class);
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
