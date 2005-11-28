/*
 * $Id$
 * $Revision$ $Date$
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

import wicket.IPageFactory;
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

	/** optional page map name. */
	private final String pageMapName;

	/**
	 * Construct.
	 * 
	 * @param pageClass
	 *            the class of the page
	 */
	public PageClassRequestTarget(Class pageClass)
	{
		this(null, pageClass);
	}

	/**
	 * Construct.
	 * 
	 * @param pageMapName
	 *            optional page map name
	 * 
	 * @param pageClass
	 *            the class of the page
	 */
	public PageClassRequestTarget(String pageMapName, Class pageClass)
	{
		this(null, pageClass, null);
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
		this(null, pageClass, pageParameters);
	}

	/**
	 * Construct.
	 * 
	 * @param pageMapName
	 *            optional page map name
	 * @param pageClass
	 *            the class of the page
	 * @param pageParameters
	 *            optional page parameters
	 */
	public PageClassRequestTarget(String pageMapName, Class pageClass, PageParameters pageParameters)
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
		if (pageParameters != null && (!pageParameters.isEmpty()))
		{
			this.pageParameters = pageParameters;
		}
		else
		{
			this.pageParameters = null; // only set when non-empty
			// to avoid problems with hashing and equals
		}
		this.pageMapName = pageMapName;
	}

	/**
	 * @see wicket.IRequestTarget#respond(wicket.RequestCycle)
	 */
	public void respond(RequestCycle requestCycle)
	{
		if (pageClass != null)
		{
			if (requestCycle.getRedirect())
			{
				IRequestCycleProcessor processor = requestCycle.getRequestCycleProcessor();
				String redirectUrl = processor.getRequestEncoder().encode(requestCycle,
						new PageClassRequestTarget(pageClass, pageParameters));
				requestCycle.getResponse().redirect(redirectUrl);
			}
			else
			{
				requestCycle.setUpdateCluster(true);

				// construct a new instance using the default page factory
				IPageFactory pageFactory = requestCycle.getApplication().getSettings()
						.getDefaultPageFactory();
				final Page page = pageFactory.newPage(pageClass, pageParameters);

				// let the page render itself
				page.render();
			}
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
	public final Class getPageClass()
	{
		return pageClass;
	}

	/**
	 * @see wicket.request.IPageClassRequestTarget#getPageParameters()
	 */
	public final PageParameters getPageParameters()
	{
		return pageParameters;
	}

	/**
	 * @see wicket.request.IPageClassRequestTarget#getPageMapName()
	 */
	public final String getPageMapName()
	{
		return pageMapName;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj)
	{
		boolean equal = false;
		if (obj instanceof PageClassRequestTarget)
		{
			PageClassRequestTarget that = (PageClassRequestTarget)obj;
			if (pageClass.equals(that.pageClass))
			{
				boolean parametersMatch = false;
				if (pageParameters != null)
				{
					parametersMatch = (that.pageParameters != null && pageParameters
							.equals(that.pageParameters));
				}
				else
				{
					parametersMatch = (that.pageParameters == null);
				}
				boolean mapMatch = false;
				if (pageMapName != null)
				{
					mapMatch = (that.pageMapName != null && pageMapName.equals(that.pageMapName));
				}
				else
				{
					mapMatch = (that.pageMapName == null);
				}
				equal = parametersMatch && mapMatch;
			}
		}
		return equal;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode()
	{
		int result = "PageClassRequestTarget".hashCode();
		result += pageClass.hashCode();
		result += pageParameters != null ? pageParameters.hashCode() : 0;
		result += pageMapName != null ? pageMapName.hashCode() : 0;
		return 17 * result;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return "PageClassRequestTarget@" + hashCode() + "{pageClass=" + pageClass.getName() + "}";
	}
}