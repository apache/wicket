/*
 * $Id$
 * $Revision$
 * $Date$
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

import wicket.Page;
import wicket.PageParameters;
import wicket.RequestCycle;

/**
 * Default implementation of {@link IPageClassRequestTarget}. Target that
 * denotes a page that is to be created from the provided page class. This is
 * typically used for redirects to bookmarkable pages.
 * 
 * @author Eelco Hillenius
 */
public class PageClassRequestTarget implements IPageClassRequestTarget
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
	 * @see wicket.request.IPageClassRequestTarget#getPageClass()
	 */
	public Class getPageClass()
	{
		return pageClass;
	}

	/**
	 * @see wicket.request.IPageClassRequestTarget#getPageParameters()
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