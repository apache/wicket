/*
 * $Id$ $Revision$ $Date$
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
package wicket.request;

import javax.servlet.ServletException;

import wicket.IRequestTarget;
import wicket.Page;
import wicket.RequestCycle;
import wicket.Session;
import wicket.WicketRuntimeException;

/**
 * Target that denotes a page instance.
 * 
 * @author Eelco Hillenius
 */
public class PageRequestTarget implements IRequestTarget
{
	/** the page instance. */
	private final Page page;

	/**
	 * Construct.
	 * 
	 * @param page
	 *            the page instance
	 */
	public PageRequestTarget(Page page)
	{
		if (page == null)
		{
			throw new NullPointerException("argument page must be not null");
		}

		this.page = page;
	}

	/**
	 * Gets the page instance.
	 * 
	 * @return the page instance
	 */
	public final Page getPage()
	{
		return page;
	}

	/**
	 * @see wicket.IRequestTarget#respond(wicket.RequestCycle)
	 */
	public void respond(RequestCycle requestCycle)
	{
		// Should page be redirected to?
		if (requestCycle.getRedirect())
		{
			// Redirect to the page
			try
			{
				requestCycle.redirectTo(page);
			}
			catch (ServletException e)
			{
				throw new WicketRuntimeException(e);
			}
		}
		else
		{
			// Let page render itself
			page.doRender();
		}
	}

	/**
	 * Returns the session to synchronize on.
	 * 
	 * @see wicket.IRequestTarget#getSynchronizationLock()
	 */
	public Object getSynchronizationLock()
	{
		return Session.get();
	}

	/**
	 * @see wicket.IRequestTarget#cleanUp(wicket.RequestCycle)
	 */
	public void cleanUp(RequestCycle requestCycle)
	{
		page.internalEndRequest();
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return page.toString();
	}

}