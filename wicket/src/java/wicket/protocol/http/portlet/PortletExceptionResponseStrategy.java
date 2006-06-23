/*
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
package wicket.protocol.http.portlet;
/**
 * Portlet implementation of
 * {@link wicket.request.compound.IExceptionResponseStrategy}. Depending on the
 * setting it returns a (pluggable) exception page or a blank page, and it
 * passes the exception through to the current request cycle by calling
 * {@link wicket.RequestCycle#onRuntimeException(Page, RuntimeException)}.
 * 
 * @author Janne Hietam&auml;ki
 * @author Eelco Hillenius
 * @author Johan Compagner
 * @author Igor Vaynberg (ivaynberg)
 */

import wicket.Application;
import wicket.IPageFactory;
import wicket.IRequestTarget;
import wicket.Page;
import wicket.RequestCycle;
import wicket.RestartResponseAtInterceptPageException;
import wicket.Session;
import wicket.WicketRuntimeException;
import wicket.authorization.AuthorizationException;
import wicket.protocol.http.portlet.pages.ExceptionErrorPortletPage;
import wicket.request.compound.IExceptionResponseStrategy;
import wicket.request.target.component.IPageRequestTarget;
import wicket.settings.IExceptionSettings;

/**
 * @author Janne Hietam&auml;ki
 *
 */
public class PortletExceptionResponseStrategy implements IExceptionResponseStrategy
{
	/**
	 * Construct.
	 */
	public PortletExceptionResponseStrategy()
	{
	}

	/**
	 * @see wicket.request.compound.IExceptionResponseStrategy#respond(wicket.RequestCycle,
	 *      java.lang.RuntimeException)
	 */
	public final void respond(final RequestCycle requestCycle, final RuntimeException e)
	{
		// If application doesn't want debug info showing up for users
		final Session session = requestCycle.getSession();
		final Application application = session.getApplication();
		final IExceptionSettings settings = application.getExceptionSettings();
		final Page responsePage = requestCycle.getResponsePage();

		Page override = onRuntimeException(responsePage, e);
		if (override != null)
		{
			requestCycle.setResponsePage(override);
		}
		else if(e instanceof AuthorizationException)
		{
			// are authorization exceptions always thrown before the real render?
			// else we need to make a page (see below) or set it hard to a redirect.
			Class accessDeniedPageClass = application.getApplicationSettings().getAccessDeniedPage();

			throw new RestartResponseAtInterceptPageException(accessDeniedPageClass);
		}
		else if (settings.getUnexpectedExceptionDisplay() != IExceptionSettings.SHOW_NO_EXCEPTION_PAGE)
		{
			Class internalErrorPageClass = application.getApplicationSettings().getInternalErrorPage();
			Class responseClass = responsePage != null ? responsePage.getClass() : null;

			if (responseClass != internalErrorPageClass
					&& settings.getUnexpectedExceptionDisplay() == IExceptionSettings.SHOW_INTERNAL_ERROR_PAGE)
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
			else if (responseClass != ExceptionErrorPortletPage.class)
			{
				// Show full details
				requestCycle.setResponsePage(new ExceptionErrorPortletPage(e, responsePage));
			}
			else
			{
				// give up while we're ahead!
				throw new WicketRuntimeException("Internal Error: Could not render error page "
						+ internalErrorPageClass, e);
			}
		}
	}

	/**
	 * This method is called when a runtime exception is thrown, just before the
	 * actual handling of the runtime exception. This implemention passes the
	 * call through to
	 * {@link RequestCycle#onRuntimeException(Page, RuntimeException)}. Note
	 * that if you override this method or provide a whole new implementation of
	 * {@link IExceptionResponseStrategy} alltogether,
	 * {@link RequestCycle#onRuntimeException(Page, RuntimeException)} will not
	 * be supported.
	 * 
	 * @param page
	 *            Any page context where the exception was thrown
	 * @param e
	 *            The exception
	 * @return Any error page to redirect to
	 */
	protected Page onRuntimeException(final Page page, final RuntimeException e)
	{
		return RequestCycle.get().onRuntimeException(page, e);
	}
}