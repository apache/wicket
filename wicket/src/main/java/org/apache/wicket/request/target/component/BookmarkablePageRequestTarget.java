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
package org.apache.wicket.request.target.component;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.wicket.IPageFactory;
import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.request.IRequestCycleProcessor;

/**
 * Default implementation of {@link IBookmarkablePageRequestTarget}. Target that denotes a page
 * that is to be created from the provided page class. This is typically used for redirects to
 * bookmarkable pages or mounted pages.
 * 
 * @author Eelco Hillenius
 * @author Igor Vaynberg (ivaynberg)
 */
public class BookmarkablePageRequestTarget implements IBookmarkablePageRequestTarget
{
	/** the page that was created in response for cleanup */
	private Page<?> page;

	/** the class of the page. */
	private final WeakReference<Class<? extends Page<?>>> pageClassRef;

	/** optional page map name. */
	private final String pageMapName;

	/** optional page parameters. */
	private final PageParameters pageParameters;

	/**
	 * Construct.
	 * 
	 * @param <C>
	 * 
	 * @param pageClass
	 *            the class of the page
	 */
	public <C extends Page<?>> BookmarkablePageRequestTarget(Class<C> pageClass)
	{
		this(null, pageClass);
	}

	/**
	 * Construct.
	 * 
	 * @param <C>
	 * 
	 * @param pageClass
	 *            the class of the page
	 * @param pageParameters
	 *            optional page parameters
	 */
	public <C extends Page<?>> BookmarkablePageRequestTarget(Class<C> pageClass,
		PageParameters pageParameters)
	{
		this(null, pageClass, pageParameters);
	}

	/**
	 * Construct.
	 * 
	 * @param <C>
	 * 
	 * @param pageMapName
	 *            optional page map name
	 * 
	 * @param pageClass
	 *            the class of the page
	 */
	public <C extends Page<?>> BookmarkablePageRequestTarget(String pageMapName, Class<C> pageClass)
	{
		this(null, pageClass, null);
	}

	/**
	 * Construct.
	 * 
	 * @param <C>
	 *            type of page
	 * 
	 * @param pageMapName
	 *            optional page map name
	 * @param pageClass
	 *            the class of the page
	 * @param pageParameters
	 *            optional page parameters
	 */
	public <C extends Page<?>> BookmarkablePageRequestTarget(String pageMapName,
		Class<C> pageClass, PageParameters pageParameters)
	{
		if (pageClass == null)
		{
			throw new IllegalArgumentException("Argument pageClass must be not null");
		}

		if (!Page.class.isAssignableFrom(pageClass))
		{
			throw new IllegalArgumentException("Argument pageClass must be an instance of " +
				Page.class.getName());
		}
		pageClassRef = new WeakReference<Class<? extends Page<?>>>(pageClass);
		this.pageParameters = (pageParameters == null) ? new PageParameters() : pageParameters;
		this.pageMapName = pageMapName;
	}

	/**
	 * @see org.apache.wicket.IRequestTarget#detach(org.apache.wicket.RequestCycle)
	 */
	public void detach(RequestCycle requestCycle)
	{
		if (page != null)
		{
			page.detach();
		}
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj)
	{
		boolean equal = false;
		if (obj != null && (obj instanceof BookmarkablePageRequestTarget))
		{
			BookmarkablePageRequestTarget that = (BookmarkablePageRequestTarget)obj;
			if (getPageClass().equals(that.getPageClass()))
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
	 * @return The page that was created, null if the response did not happen yet
	 */
	public final Page<?> getPage()
	{
		return page;
	}

	protected final void setPage(Page<?> page)
	{
		this.page = page;
	}


	/**
	 * @see org.apache.wicket.request.target.component.IBookmarkablePageRequestTarget#getPageClass()
	 */
	public final Class<? extends Page<?>> getPageClass()
	{
		return pageClassRef.get();
	}

	/**
	 * @see org.apache.wicket.request.target.component.IBookmarkablePageRequestTarget#getPageMapName()
	 */
	public final String getPageMapName()
	{
		return pageMapName;
	}

	/**
	 * @see org.apache.wicket.request.target.component.IBookmarkablePageRequestTarget#getPageParameters()
	 */
	public final PageParameters getPageParameters()
	{
		return pageParameters;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		int result = "BookmarkablePageRequestTarget".hashCode();
		result += getPageClass().hashCode();
		result += pageMapName != null ? pageMapName.hashCode() : 0;
		return 17 * result;
	}

	/**
	 * @see org.apache.wicket.request.target.IEventProcessor#processEvents(org.apache.wicket.RequestCycle)
	 */
	public void processEvents(RequestCycle requestCycle)
	{
		if (!requestCycle.isRedirect())
		{
			page = getPage(requestCycle);
		}
	}

	/**
	 * @see org.apache.wicket.IRequestTarget#respond(org.apache.wicket.RequestCycle)
	 */
	public void respond(RequestCycle requestCycle)
	{
		if (pageClassRef != null && pageClassRef.get() != null)
		{
			if (requestCycle.isRedirect())
			{
				IRequestCycleProcessor processor = requestCycle.getProcessor();
				String redirectUrl = processor.getRequestCodingStrategy()
					.encode(requestCycle, this)
					.toString();
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
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "[BookmarkablePageRequestTarget@" + hashCode() + " pageClass=" +
			getPageClass().getName() + "]";
	}

	/**
	 * Constructs a new instance of a page given its class name
	 * 
	 * @param <C>
	 *            type of page
	 * 
	 * @param pageClass
	 *            class name of the page to be created
	 * @param requestCycle
	 *            request cycle
	 * @return new instance of page
	 */
	protected <C extends Page<?>> Page<?> newPage(final Class<C> pageClass,
		final RequestCycle requestCycle)
	{
		// Construct a new instance using the default page factory
		IPageFactory pageFactory = requestCycle.getApplication()
			.getSessionSettings()
			.getPageFactory();

		if (pageParameters == null || pageParameters.size() == 0)
		{
			return pageFactory.newPage(pageClass);
		}
		else
		{
			// Add bookmarkable params in for WICKET-400.
			final Map<String, String[]> requestMap = requestCycle.getRequest().getParameterMap();
			for (Entry<String, Object> entry : pageParameters.entrySet())
			{
				final Object value = entry.getValue();
				if (value.getClass().isArray())
				{
					final Object[] objects = (Object[])value;
					final String[] strings = new String[objects.length];
					for (int i = 0; i < objects.length; i++)
					{
						strings[i] = objects[i].toString();
					}
					requestMap.put(entry.getKey(), strings);
				}
				else
				{
					requestMap.put(entry.getKey(), new String[] { value.toString() });
				}
			}
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
	protected final Page<?> getPage(RequestCycle requestCycle)
	{
		if (page == null && !requestCycle.isRedirect())
		{
			page = newPage(getPageClass(), requestCycle);
		}
		return page;
	}

}