package org.apache.wicket.wicket4066;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * The page that should be shown after successful login
 */
public class SuccessPage extends WebPage
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 * 
	 * @param parameters
	 */
	public SuccessPage(final PageParameters parameters)
	{
		super(parameters);
	}
}
