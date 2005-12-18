/*
 * $Id: DefaultExceptionResponseProcessor.java,v 1.1 2005/11/27 09:20:16 eelco12
 * Exp $ $Revision$ $Date$
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.request.compound;

import wicket.Application;
import wicket.ApplicationSettings;
import wicket.IPageFactory;
import wicket.IRequestTarget;
import wicket.Page;
import wicket.RequestCycle;
import wicket.Session;
import wicket.WicketRuntimeException;
import wicket.markup.html.pages.ExceptionErrorPage;
import wicket.request.IPageRequestTarget;

/**
 * Default implementation of response processor strategy that just calls
 * {@link wicket.IRequestTarget#respond(RequestCycle)}.
 * 
 * @author Eelco Hillenius
 */
public final class DefaultExceptionResponseProcessor implements IExceptionResponseStrategy
{

	/**
	 * Construct.
	 */
	public DefaultExceptionResponseProcessor()
	{
	}

	/**
	 * @see wicket.request.compound.IExceptionResponseStrategy#respond(wicket.RequestCycle,
	 *      java.lang.Exception)
	 */
	public void respond(RequestCycle requestCycle, Exception e)
	{
		// If application doesn't want debug info showing up for users
		final Session session = requestCycle.getSession();
		final Application application = session.getApplication();
		final ApplicationSettings settings = application.getSettings();
		if (settings.getUnexpectedExceptionDisplay() != ApplicationSettings.SHOW_NO_EXCEPTION_PAGE)
		{
			Class internalErrorPageClass = application.getPages().getInternalErrorPage();
			Page responsePage = requestCycle.getResponsePage();
			Class responseClass = responsePage != null ? responsePage.getClass() : null;

			if (responseClass != internalErrorPageClass
					&& settings.getUnexpectedExceptionDisplay() == ApplicationSettings.SHOW_INTERNAL_ERROR_PAGE)
			{
				// Show internal error page
				final IPageFactory pageFactory;
				IRequestTarget requestTarget = requestCycle.getRequestTarget();
				if (requestTarget instanceof IPageRequestTarget)
				{
					pageFactory = session.getPageFactory(((IPageRequestTarget)requestTarget)
							.getPage());
				}
				else
				{
					pageFactory = session.getPageFactory();
				}
				requestCycle.setResponsePage(pageFactory.newPage(internalErrorPageClass));
			}
			else if (responseClass != ExceptionErrorPage.class)
			{
				// Show full details
				requestCycle.setResponsePage(new ExceptionErrorPage(e, requestCycle
						.getResponsePage()));
			}
			else
			{
				// give up while we're ahead!
				throw new WicketRuntimeException("Internal Error: Could not render error page "
						+ internalErrorPageClass, e);
			}

			// We generally want to redirect the response because we
			// were in the middle of rendering and the page may end up
			// looking like spaghetti otherwise
			//requestCycle.redirectTo(requestCycle.getResponsePage());
			requestCycle.setRedirect(true);
		}
	}
}
