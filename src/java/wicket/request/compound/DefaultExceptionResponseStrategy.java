/*
 * $Id: DefaultExceptionResponseStrategy.java,v 1.1 2005/11/27 09:20:16 eelco12
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
import wicket.IPageFactory;
import wicket.IRequestTarget;
import wicket.Page;
import wicket.RequestCycle;
import wicket.Session;
import wicket.WicketRuntimeException;
import wicket.markup.html.pages.ExceptionErrorPage;
import wicket.request.IPageRequestTarget;
import wicket.settings.IExceptionSettings;
import wicket.settings.Settings;

/**
 * Default implementation of
 * {@link wicket.request.compound.IExceptionResponseStrategy}. Depending on the
 * setting it returns a (pluggable) exception page or a blank page, and it
 * passes the exception through to the current request cycle by calling
 * {@link wicket.RequestCycle#onRuntimeException(Page, RuntimeException)}.
 * 
 * @author Eelco Hillenius
 */
public class DefaultExceptionResponseStrategy implements IExceptionResponseStrategy
{

	/**
	 * Construct.
	 */
	public DefaultExceptionResponseStrategy()
	{
	}

	/**
	 * @see wicket.request.compound.IExceptionResponseStrategy#respond(wicket.RequestCycle,
	 *      java.lang.RuntimeException)
	 */
	public final void respond(RequestCycle requestCycle, RuntimeException e)
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
			requestCycle.setRedirect(true);
		}
		else if (settings.getUnexpectedExceptionDisplay() != Settings.SHOW_NO_EXCEPTION_PAGE)
		{
			Class internalErrorPageClass = application.getRequiredPageSettings().getInternalErrorPage();
			Class responseClass = responsePage != null ? responsePage.getClass() : null;

			if (responseClass != internalErrorPageClass
					&& settings.getUnexpectedExceptionDisplay() == Settings.SHOW_INTERNAL_ERROR_PAGE)
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
			requestCycle.setRedirect(true);
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
	protected Page onRuntimeException(Page page, RuntimeException e)
	{
		return RequestCycle.get().onRuntimeException(page, e);
	}
}
