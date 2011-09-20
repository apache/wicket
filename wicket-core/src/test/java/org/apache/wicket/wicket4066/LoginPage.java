package org.apache.wicket.wicket4066;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * 
 */
public class LoginPage extends WebPage
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 * 
	 * @param parameters
	 */
	public LoginPage(final PageParameters parameters)
	{
		super(parameters);

		add(new Form<Void>("form")
		{
			@Override
			public void onSubmit()
			{
				((MySession)getSession()).setAnonymous(false);

				if (!continueToOriginalDestination())
				{
					setResponsePage(SuccessPage.class);
				}
			}
		});
	}
}
