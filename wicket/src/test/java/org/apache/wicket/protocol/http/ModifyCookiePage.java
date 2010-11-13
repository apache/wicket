package org.apache.wicket.protocol.http;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.request.http.WebResponse;

import javax.servlet.http.Cookie;

public class ModifyCookiePage extends WebPage
{
	private static final long serialVersionUID = 3884508803470168634L;
	
	public static final String CREATE_COOKIE_ID = "createCookie";
	public static final String COOKIE_NAME = "wicketTest";
	public static final String COOKIE_VALUE = "1";

	private WebResponse getWebResponse()
	{
		return (WebResponse) getResponse();
	}

	@Override
	protected void onInitialize()
	{
		super.onInitialize();

		add(new Link<Void>(CREATE_COOKIE_ID)
		{
			private static final long serialVersionUID = 6762033052623200948L;

			@Override
			public void onClick()
			{
				getWebResponse().addCookie(new Cookie(COOKIE_NAME, COOKIE_VALUE));
			}
		});
	}
}