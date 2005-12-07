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
import wicket.Request;
import wicket.RequestCycle;

/**
 * Default implementation of {@link IBookmarkablePageRequestTarget}. Target that
 * denotes a page that is to be created from the provided page class. This is
 * typically used for redirects to bookmarkable pages or mounted pages.
 * 
 * @author Eelco Hillenius
 * @author Igor Vaynberg (ivaynberg)
 */
public class BookmarkablePageRequestTarget implements IBookmarkablePageRequestTarget
{
	/** the page this target was mounted on, if any */
	private final String mountPath;

	/** page parameters encoder, if any */
	private final IPageParametersEncoder paramsEncoder;

	/** the class of the page. */
	private final Class pageClass;

	/** optional page parameters. */
	private final PageParameters pageParameters;

	/** the page that was created in response for cleanup */
	private Page page;

	/** optional page map name. */
	private final String pageMapName;

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
	 * @param pageClass
	 *            the class of the page
	 * @param pageParameters
	 *            optional page parameters
	 */
	public BookmarkablePageRequestTarget(String pageMapName, Class pageClass, PageParameters pageParameters)
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

		mountPath = null;
		paramsEncoder = null;
	}

	// TODO create a one for all constructor

	/**
	 * Constructor used to create a mounted page class request target
	 * 
	 * @param pageClass
	 * @param path
	 * @param encoder
	 */
	public BookmarkablePageRequestTarget(Class pageClass, String path, IPageParametersEncoder encoder) {
		this(null, pageClass, path, encoder);
	}

	/**
	 * Constructor used to create a mounted page class request target for a certain pagemap
	 * @param pageMapName 
	 * @param pageClass
	 * @param path
	 * @param encoder
	 */
	public BookmarkablePageRequestTarget(String pageMapName, Class pageClass, String path, IPageParametersEncoder encoder)
	{
		this.pageMapName = pageMapName;
		pageParameters = null;

		this.pageClass = pageClass;
		mountPath = path;
		paramsEncoder = encoder;
	}


	/**
	 * Returns assigned page parameters encoder
	 * 
	 * @return assigned page parameters encoder
	 */
	public IPageParametersEncoder getParamsEncoder()
	{
		return paramsEncoder;
	}

	/**
	 * Method to check if this target has been mounted
	 * 
	 * @return true if this target is mounted, false otherwise
	 */
	public boolean isMounted()
	{
		return mountPath != null;
	}

	/**
	 * @see wicket.request.IAccessCheckingTarget#checkAccess(RequestCycle)
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


	private Page getPage(RequestCycle requestCycle)
	{
		if (page == null && pageClass != null && !requestCycle.getRedirect())
		{
			page = newPage(pageClass, requestCycle);
		}
		return page;
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
		if (isMounted())
		{
			// decode page parameters from url
			Request request = requestCycle.getRequest();
			String urlFragment = request.getPath().substring(mountPath.length());
			params = paramsEncoder.decode(urlFragment);

		}
		// construct a new instance using the default page factory
		IPageFactory pageFactory = requestCycle.getApplication().getSettings()
				.getDefaultPageFactory();
		return pageFactory.newPage(pageClass, params);
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
						new BookmarkablePageRequestTarget(pageClass, pageParameters));
				requestCycle.getResponse().redirect(redirectUrl);
			}
			else
			{
				requestCycle.setUpdateCluster(true);

				page = getPage(requestCycle);

				// let the page render itself
				page.doRender();
			}
		}
	}

	/**
	 * @see wicket.IRequestTarget#cleanUp(wicket.RequestCycle)
	 */
	public void cleanUp(RequestCycle requestCycle)
	{
		if (isMounted())
		{
			// if this is a mounted page we clean it so that a refresh in the
			// browser will recreate the page
			// TODO same should be done for bookmarkable pages, are all pages
			// accessed through here bookmarkable or mounted?
			page = null;
		}
		// don't have to call page.internalEndRequest() because page.doRender()
		// is always called for this target
	}

	/**
	 * @see wicket.request.IBookmarkablePageRequestTarget#getPageClass()
	 */
	public final Class getPageClass()
	{
		return pageClass;
	}

	/**
	 * @see wicket.request.IBookmarkablePageRequestTarget#getPageParameters()
	 */
	public final PageParameters getPageParameters()
	{
		return pageParameters;
	}

	/**
	 * @see wicket.request.IBookmarkablePageRequestTarget#getPageMapName()
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
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode()
	{
		int result = "PageClassRequestTarget".hashCode();
		result += pageClass.hashCode();
		result += pageMapName != null ? pageMapName.hashCode() : 0;
		return 17 * result;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return "PageClassRequestTarget@" + hashCode() + "{pageClass=" + pageClass.getName()
				+ ", mountPath=" + mountPath + "}";
	}
}