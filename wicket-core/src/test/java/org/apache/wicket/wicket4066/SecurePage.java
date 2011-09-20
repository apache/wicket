package org.apache.wicket.wicket4066;

import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * The protected page.
 */
public class SecurePage extends WebPage
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 * 
	 * @param parameters
	 */
	public SecurePage(final PageParameters parameters)
	{
		super(parameters);
	}

	@Override
	protected void onBeforeRender()
	{
		if (((MySession)getSession()).isAnonymous())
		{
			throw new RestartResponseAtInterceptPageException(LoginPage.class);
		}
		super.onBeforeRender();
	}
}
