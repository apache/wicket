/*
 * $Id: BookmarkablePageRequestTarget.java,v 1.3 2005/12/07 00:52:26 ivaynberg
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
package wicket.request.target;

import wicket.IPageFactory;
import wicket.Page;
import wicket.PageParameters;
import wicket.RequestCycle;
import wicket.request.IBookmarkablePageRequestTarget;
import wicket.request.IRequestCycleProcessor;

/**
 * Default implementation of {@link IBookmarkablePageRequestTarget}. Target
 * that denotes a page that is to be created from the provided page class. This
 * is typically used for redirects to bookmarkable pages or mounted pages.
 * 
 * @author Eelco Hillenius
 * @author Igor Vaynberg (ivaynberg)
 */
public class BookmarkablePageRequestTarget implements IBookmarkablePageRequestTarget, IAccessCheck
{
	/** the page that was created in response for cleanup */
	private Page page;

	/** the class of the page. */
	private final Class pageClass;

	/** optional page map name. */
	private final String pageMapName;

	/** optional page parameters. */
	private final PageParameters pageParameters;

	/**
	 * Construct.
	 * 
	 * @param pageClass
	 *            the class of the page
	 */
	public BookmarkablePageRequestTarget(Class pageClass)
	{
		this(null, pageClass);
	}

	/**
	 * Construct.
	 * 
	 * @param pageClass
	 *            the class of the page
	 * @param pageParameters
	 *            optional page parameters
	 */
	public BookmarkablePageRequestTarget(Class pageClass, PageParameters pageParameters)
	{
		this(null, pageClass, pageParameters);
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
	public BookmarkablePageRequestTarget(String pageMapName, Class pageClass)
	{
		this(null, pageClass, null);
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
	public BookmarkablePageRequestTarget(String pageMapName, Class pageClass,
			PageParameters pageParameters)
	{
		if (pageClass == null)
		{
			throw new IllegalArgumentException("Argument pageClass must be not null");
		}

		if (!Page.class.isAssignableFrom(pageClass))
		{
			throw new IllegalArgumentException("Argument pageClass must be an instance of "
					+ Page.class.getName());
		}
		this.pageClass = pageClass;
		this.pageParameters = pageParameters;
		this.pageMapName = pageMapName;
	}

	/**
	 * @see wicket.request.target.IAccessCheck#checkAccess(RequestCycle)
	 * @deprecated
	 */
	public boolean checkAccess(RequestCycle requestCycle)
	{
		Page page = getPage(requestCycle);
		if (page != null)
		{
			return page.checkAccess();
		}
		return true;
	}

	/**
	 * @see wicket.IRequestTarget#cleanUp(wicket.RequestCycle)
	 */
	public void cleanUp(RequestCycle requestCycle)
	{
		page = null;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj)
	{
		boolean equal = false;
		if (obj != null && (obj instanceof BookmarkablePageRequestTarget))
		{
			BookmarkablePageRequestTarget that = (BookmarkablePageRequestTarget)obj;
			if (pageClass.equals(that.pageClass))
			{
				boolean mapMatch = false;

				if (pageMapName != null)
				{
					mapMatch = (that.pageMapName != null && pageMapName.equals(that.pageMapName));
				}
				else
				{
					mapMatch = (that.pageMapName == null);
				}

				equal = mapMatch;
			}
		}
		return equal;
	}

	/**
	 * @see wicket.request.IBookmarkablePageRequestTarget#getPageClass()
	 */
	public final Class getPageClass()
	{
		return pageClass;
	}

	/**
	 * @see wicket.request.IBookmarkablePageRequestTarget#getPageMapName()
	 */
	public final String getPageMapName()
	{
		return pageMapName;
	}

	/**
	 * @see wicket.request.IBookmarkablePageRequestTarget#getPageParameters()
	 */
	public final PageParameters getPageParameters()
	{
		return pageParameters;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode()
	{
		int result = "BookmarkablePageRequestTarget".hashCode();
		result += pageClass.hashCode();
		result += pageMapName != null ? pageMapName.hashCode() : 0;
		return 17 * result;
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
				IRequestCycleProcessor processor = requestCycle.getProcessor();
				String redirectUrl = processor.getRequestCodingStrategy().encode(requestCycle,
						new BookmarkablePageRequestTarget(pageClass, pageParameters));
				requestCycle.getResponse().redirect(redirectUrl);
			}
			else
			{
				requestCycle.setUpdateSession(true);

				page = getPage(requestCycle);

				// let the page render itself
				page.doRender();
			}
		}
	}

	/**
	 * @see wicket.IRequestTarget#getLock(RequestCycle)
	 */
	public Object getLock(RequestCycle requestCycle)
	{
		// we need to lock when we are not redirecting, i.e. we are
		// actually rendering the page
		return !requestCycle.getRedirect() ? requestCycle.getSession() : null;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return "BookmarkablePageRequestTarget@" + hashCode() + "{pageClass=" + pageClass.getName()
				+ "}";
	}

	/**
	 * Constructs a new instance of a page given its class name
	 * 
	 * @param pageClass
	 *            class name of the page to be created
	 * @param requestCycle
	 *            request cycle
	 * @return new instance of page
	 */
	protected Page newPage(Class pageClass, RequestCycle requestCycle)
	{
		PageParameters params = pageParameters;

		// TODO General: The parameters should already have been resolved?
		/*
		 * if (isMounted()) { //decode page parameters from url Request request =
		 * requestCycle.getRequest(); String urlFragment =
		 * request.getPath().substring(mountPath.length()); params =
		 * paramsEncoder.decode(urlFragment); }
		 */
		// construct a new instance using the default page factory
		IPageFactory pageFactory = requestCycle.getApplication().getSessionSettings()
				.getPageFactory();

		if (params==null||params.size() == 0)
		{
			return pageFactory.newPage(pageClass);
		}
		else
		{
			return pageFactory.newPage(pageClass, params);
		}
	}

	/**
	 * Gets a newly constructed page if we are not in a redirect.
	 * 
	 * @param requestCycle
	 *            the request cycle
	 * @return the page
	 */
	private final Page getPage(RequestCycle requestCycle)
	{
		if (page == null && pageClass != null && !requestCycle.getRedirect())
		{
			page = newPage(pageClass, requestCycle);
		}
		return page;
	}
}