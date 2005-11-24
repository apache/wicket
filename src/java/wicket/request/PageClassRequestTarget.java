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

import wicket.IRequestTarget;
import wicket.Page;
import wicket.PageParameters;
import wicket.RequestCycle;
import wicket.Session;

/**
 * Target that denotes a page that is to be created from the provided page
 * class.
 * 
 * @author Eelco Hillenius
 */
public class PageClassRequestTarget implements IRequestTarget
{
	/** the class of the page. */
	private final Class pageClass;

	/** optional page parameters. */
	private final PageParameters pageParameters;

	/**
	 * Construct.
	 * 
	 * @param pageClass
	 *            the class of the page
	 */
	public PageClassRequestTarget(Class pageClass)
	{
		this(pageClass, null);
	}

	/**
	 * Construct.
	 * 
	 * @param pageClass
	 *            the class of the page
	 * @param pageParameters
	 *            optional page parameters
	 */
	public PageClassRequestTarget(Class pageClass, PageParameters pageParameters)
	{
		if (pageClass == null)
		{
			throw new NullPointerException("argument pageClass must be not null");
		}

		if (!Page.class.isAssignableFrom(pageClass))
		{
			throw new IllegalArgumentException("pageClass must be an instance of "
					+ Page.class.getName());
		}
		this.pageClass = pageClass;
		this.pageParameters = pageParameters;
	}

	/**
	 * @see wicket.IRequestTarget#respond(wicket.RequestCycle)
	 */
	public void respond(RequestCycle requestCycle)
	{
		if (pageClass != null)
		{
			String redirectUrl = requestCycle.urlFor(pageClass, pageParameters);
			requestCycle.getResponse().redirect(redirectUrl);
		}
	}

	/**
	 * @see wicket.IRequestTarget#cleanUp(wicket.RequestCycle)
	 */
	public void cleanUp(RequestCycle requestCycle)
	{
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
	 * Gets the page class.
	 * 
	 * @return the page class
	 */
	public Class getPageClass()
	{
		return pageClass;
	}

	/**
	 * Gets the optional page parameters.
	 * 
	 * @return the page parameters or null
	 */
	public PageParameters getPageParameters()
	{
		return pageParameters;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return pageClass.getName();
	}
}