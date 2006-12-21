/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket.request.target.component;

import wicket.IPageFactory;
import wicket.Page;
import wicket.PageParameters;
import wicket.RequestCycle;
import wicket.request.IRequestCycleProcessor;

/**
 * Default implementation of {@link IBookmarkablePageRequestTarget}. Target
 * that denotes a page that is to be created from the provided page class. This
 * is typically used for redirects to bookmarkable pages or mounted pages.
 * 
 * @author Eelco Hillenius
 * @author Igor Vaynberg (ivaynberg)
 */
public class BookmarkablePageRequestTarget implements IBookmarkablePageRequestTarget
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
		this.pageParameters = (pageParameters == null) ? new PageParameters() : pageParameters;
		this.pageMapName = pageMapName;
	}

	/**
	 * @see wicket.IRequestTarget#detach(wicket.RequestCycle)
	 */
	public void detach(RequestCycle requestCycle)
	{
		if (page != null)
		{
			page.internalDetach();
		}
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
	 * @return The page that was created, null if the response did not happen
	 *         yet
	 */
	public final Page getPage()
	{
		return page;
	}

	/**
	 * @see wicket.request.target.component.IBookmarkablePageRequestTarget#getPageClass()
	 */
	public final Class getPageClass()
	{
		return pageClass;
	}

	/**
	 * @see wicket.request.target.component.IBookmarkablePageRequestTarget#getPageMapName()
	 */
	public final String getPageMapName()
	{
		return pageMapName;
	}

	/**
	 * @see wicket.request.target.component.IBookmarkablePageRequestTarget#getPageParameters()
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
	 * @see wicket.request.target.IEventProcessor#processEvents(wicket.RequestCycle)
	 */
	public void processEvents(RequestCycle requestCycle)
	{
		if (!requestCycle.getRedirect())
		{
			requestCycle.setUpdateSession(true);
			page = getPage(requestCycle);
		}
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
				String redirectUrl = processor.getRequestCodingStrategy()
						.encode(requestCycle, this).toString();
				requestCycle.getResponse().redirect(redirectUrl);
			}
			else
			{
				// Let the page render itself
				getPage(requestCycle).renderPage();
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
		return "[BookmarkablePageRequestTarget@" + hashCode() + " pageClass=" + pageClass.getName()
				+ "]";
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
	protected Page newPage(final Class pageClass, final RequestCycle requestCycle)
	{
		// Construct a new instance using the default page factory
		IPageFactory pageFactory = requestCycle.getApplication().getSessionSettings()
				.getPageFactory();

		if (pageParameters == null || pageParameters.size() == 0)
		{
			return pageFactory.newPage(pageClass);
		}
		else
		{
			return pageFactory.newPage(pageClass, pageParameters);
		}
	}

	/**
	 * Gets a newly constructed page if we are not in a redirect.
	 * 
	 * @param requestCycle
	 *            the request cycle
	 * @return the page
	 */
	protected final Page getPage(RequestCycle requestCycle)
	{
		if (page == null && pageClass != null && !requestCycle.getRedirect())
		{
			page = newPage(pageClass, requestCycle);
		}
		return page;
	}

}