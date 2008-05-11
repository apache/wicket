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
package org.apache.wicket.request.target.coding;

import java.lang.ref.WeakReference;

import org.apache.wicket.Application;
import org.apache.wicket.IRedirectListener;
import org.apache.wicket.IRequestTarget;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.Session;
import org.apache.wicket.protocol.http.PageExpiredException;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.request.WebRequestCodingStrategy;
import org.apache.wicket.request.RequestParameters;
import org.apache.wicket.request.target.component.BookmarkableListenerInterfaceRequestTarget;
import org.apache.wicket.request.target.component.BookmarkablePageRequestTarget;
import org.apache.wicket.request.target.component.PageRequestTarget;
import org.apache.wicket.request.target.component.listener.ListenerInterfaceRequestTarget;
import org.apache.wicket.util.string.AppendingStringBuffer;
import org.apache.wicket.util.string.Strings;


/**
 * An URL coding strategy that encodes the mount point, page parameters and page instance
 * information into the URL. The benefits compared to mounting page with
 * {@link BookmarkablePageRequestTargetUrlCodingStrategy} are that the mount point is preserved even
 * after invoking listener interfaces (thus you don't lose bookmarkability after clicking links) and
 * that for ajax only pages the state is preserved on refresh.
 * <p>
 * The url with {@link HybridUrlCodingStrategy} looks like /mount/path/param1/value1.3. or
 * /mount/path/param1/value1.3.2 where 3 is page Id and 2 is version number.
 * <p>
 * Also to preserve state on refresh with ajax-only pages the {@link HybridUrlCodingStrategy} does
 * an immediate redirect after hitting bookmarkable URL, e.g. it immediately redirects from
 * /mount/path to /mount/path.3 where 3 is the next page id. This preserves the page instance on
 * subsequent page refresh.
 * 
 * @author Matej Knopp
 */
public class HybridUrlCodingStrategy extends AbstractRequestTargetUrlCodingStrategy
{
	/** bookmarkable page class. */
	protected final WeakReference/* <Class> */pageClassRef;

	private final boolean redirectOnBookmarkableRequest;

	/**
	 * Construct.
	 * 
	 * @param mountPath
	 * @param pageClass
	 * @param redirectOnBookmarkableRequest
	 *            whether after hitting the page with URL in bookmarkable form it should be
	 *            redirected to hybrid URL - needed for ajax to work properly after page refresh
	 */
	public HybridUrlCodingStrategy(String mountPath, Class pageClass,
		boolean redirectOnBookmarkableRequest)
	{
		super(mountPath);
		pageClassRef = new WeakReference(pageClass);
		this.redirectOnBookmarkableRequest = redirectOnBookmarkableRequest;
	}

	/**
	 * Construct.
	 * 
	 * @param mountPath
	 * @param pageClass
	 */
	public HybridUrlCodingStrategy(String mountPath, Class pageClass)
	{
		this(mountPath, pageClass, true);
	}

	/**
	 * Returns the amount of trailing slashes in the given string
	 * 
	 * @param seq
	 * @return
	 */
	private int getTrailingSlashesCount(CharSequence seq)
	{
		int count = 0;
		for (int i = seq.length() - 1; i >= 0; --i)
		{
			if (seq.charAt(i) == '/')
			{
				++count;
			}
			else
			{
				break;
			}
		}
		return count;
	}

	/**
	 * Returns whether after hitting bookmarkable url the request should be redirected to a hybrid
	 * URL. This is recommended for pages with Ajax.
	 * 
	 * @return
	 */
	protected boolean isRedirectOnBookmarkableRequest()
	{
		return redirectOnBookmarkableRequest;
	}

	/**
	 * Returns whether to redirect when there is pageMap specified in bookmarkable URL
	 * 
	 * @return
	 */
	protected boolean alwaysRedirectWhenPageMapIsSpecified()
	{
		// returns true if the pageId is unique, so we can get rid of the
		// pageMap name in the url this way
		return Application.exists() &&
			Application.get().getSessionSettings().isPageIdUniquePerSession();
	}

	/**
	 * @see org.apache.wicket.request.target.coding.IRequestTargetUrlCodingStrategy#decode(org.apache.wicket.request.RequestParameters)
	 */
	public IRequestTarget decode(RequestParameters requestParameters)
	{
		String parametersFragment = requestParameters.getPath().substring(getMountPath().length());

		// try to extract page info
		PageInfoExtraction extraction = extractPageInfo(parametersFragment);

		PageInfo pageInfo = extraction.getPageInfo();
		String pageMapName = pageInfo != null ? pageInfo.getPageMapName() : null;
		Integer pageVersion = pageInfo != null ? pageInfo.getVersionNumber() : null;
		Integer pageId = pageInfo != null ? pageInfo.getPageId() : null;

		// decode parameters
		PageParameters parameters = new PageParameters(decodeParameters(
			extraction.getUrlAfterExtraction(), requestParameters.getParameters()));

		if (requestParameters.getPageMapName() == null)
		{
			requestParameters.setPageMapName(pageMapName);
		}
		else
		{
			pageMapName = requestParameters.getPageMapName();
		}

		// do some extra work for checking whether this is a normal request to a
		// bookmarkable page, or a request to a stateless page (in which case a
		// wicket:interface parameter should be available
		final String interfaceParameter = (String)parameters.remove(WebRequestCodingStrategy.INTERFACE_PARAMETER_NAME);

		// we need to remember the amount of trailing slashes after the redirect
		// (otherwise we'll break relative urls)
		int originalUrlTrailingSlashesCount = getTrailingSlashesCount(extraction.getUrlAfterExtraction());

		boolean redirect = isRedirectOnBookmarkableRequest();
		if (Strings.isEmpty(pageMapName) != true && alwaysRedirectWhenPageMapIsSpecified())
		{
			redirect = true;
		}

		if (interfaceParameter != null)
		{
			// stateless listener interface
			WebRequestCodingStrategy.addInterfaceParameters(interfaceParameter, requestParameters);
			return new BookmarkableListenerInterfaceRequestTarget(pageMapName,
				(Class)pageClassRef.get(), parameters, requestParameters.getComponentPath(),
				requestParameters.getInterfaceName(), requestParameters.getVersionNumber());
		}
		else if (pageId == null)
		{
			// bookmarkable page request
			return new HybridBookmarkablePageRequestTarget(pageMapName, (Class)pageClassRef.get(),
				parameters, originalUrlTrailingSlashesCount, redirect);
		}
		else
		// hybrid url
		{
			Page page;

			if (Strings.isEmpty(pageMapName) && Application.exists() &&
				Application.get().getSessionSettings().isPageIdUniquePerSession())
			{
				page = Session.get().getPage(pageId.intValue(),
					pageVersion != null ? pageVersion.intValue() : 0);
			}
			else
			{
				page = Session.get().getPage(pageMapName, "" + pageId,
					pageVersion != null ? pageVersion.intValue() : 0);
			}

			// check if the found page match the required class
			if (page != null && page.getClass().equals(pageClassRef.get()))
			{
				requestParameters.setInterfaceName(IRedirectListener.INTERFACE.getName());
				RequestCycle.get().getRequest().setPage(page);
				return new PageRequestTarget(page);
			}
			else
			{
				// we didn't find the page, act as bookmarkable page request -
				// create new instance, but only if there is no callback to a non-existing page
				if (requestParameters.getInterface() != null)
				{
					handleExpiredPage(pageMapName, (Class)pageClassRef.get(),
						originalUrlTrailingSlashesCount, redirect);
				}
				return new HybridBookmarkablePageRequestTarget(pageMapName,
					(Class)pageClassRef.get(), parameters, originalUrlTrailingSlashesCount,
					redirect);
			}
		}

	}

	/**
	 * Handles the case where a non-bookmarkable url with a hybrid base refers to a page that is no
	 * longer in session. eg <code>/context/hybrid-mount.0.23?wicket:interface=...</code>. The
	 * default behavior is to throw a <code>PageExpiredException</code>.
	 * 
	 * This method can be overwritten to, for example, return the user to a new instance of the
	 * bookmarkable page that was mounted using hybrid strategy - this, however, should only be used
	 * in cases where the page expects no page parameters because they are no longer available.
	 * 
	 * @param pageMapName
	 *            page map name this page is mounted in
	 * @param pageClass
	 *            class of mounted page
	 * @param trailingSlashesCount
	 *            count of trailing slsahes in the url
	 * @param redirect
	 *            whether or not a redirect should be issued
	 * @return request target used to handle this situation
	 */
	protected IRequestTarget handleExpiredPage(String pageMapName, Class pageClass,
		int trailingSlashesCount, boolean redirect)
	{
		throw new PageExpiredException(
			"Request cannot be processed. The target page does not exist anymore.");
	}

	/**
	 * Returns the number of trailing slashes in the url when the page in request target was created
	 * or null if the number can't be determined.
	 * 
	 * @param requestTarget
	 * @return
	 */
	private Integer getOriginalOriginalTrailingSlashesCount(IRequestTarget requestTarget)
	{
		if (requestTarget instanceof ListenerInterfaceRequestTarget)
		{
			ListenerInterfaceRequestTarget target = (ListenerInterfaceRequestTarget)requestTarget;
			Page page = target.getPage();
			return (Integer)page.getMetaData(ORIGINAL_TRAILING_SLASHES_COUNT_METADATA_KEY);
		}
		return null;
	}

	/**
	 * Extracts the PageParameters from given request target
	 * 
	 * @param requestTarget
	 * @return
	 */
	private PageParameters getPageParameters(IRequestTarget requestTarget)
	{
		if (requestTarget instanceof BookmarkablePageRequestTarget)
		{
			BookmarkablePageRequestTarget target = (BookmarkablePageRequestTarget)requestTarget;
			return target.getPageParameters();
		}
		else if (requestTarget instanceof ListenerInterfaceRequestTarget)
		{
			ListenerInterfaceRequestTarget target = (ListenerInterfaceRequestTarget)requestTarget;
			Page page = target.getPage();
			return getInitialPagePageParameters(page);
		}
		else
		{
			return null;
		}
	}

	/**
	 * Extracts the PageInfo from given request target
	 * 
	 * @param requestTarget
	 * @return
	 */
	private PageInfo getPageInfo(IRequestTarget requestTarget)
	{
		if (requestTarget instanceof BookmarkablePageRequestTarget)
		{
			BookmarkablePageRequestTarget target = (BookmarkablePageRequestTarget)requestTarget;
			if (target.getPageMapName() != null)
			{
				return new PageInfo(null, null, target.getPageMapName());
			}
			else
			{
				return null;
			}
		}
		else if (requestTarget instanceof ListenerInterfaceRequestTarget)
		{
			ListenerInterfaceRequestTarget target = (ListenerInterfaceRequestTarget)requestTarget;
			Page page = target.getPage();
			return new PageInfo(new Integer(page.getNumericId()), new Integer(
				page.getCurrentVersionNumber()), page.getPageMapName());
		}
		else
		{
			return null;
		}
	}

	/**
	 * Sets the initial page parameters for page instance. Use this only if you know what you are
	 * doing.
	 * 
	 * @param page
	 * @param pageParameters
	 */
	public static void setInitialPageParameters(Page page, PageParameters pageParameters)
	{
		page.setMetaData(PAGE_PARAMETERS_META_DATA_KEY, pageParameters);
	}

	/**
	 * @param page
	 * @return
	 */
	public static PageParameters getInitialPagePageParameters(Page page)
	{
		return (PageParameters)page.getMetaData(PAGE_PARAMETERS_META_DATA_KEY);
	}

	/**
	 * Meta data key to store PageParameters in page instance. This is used to save the
	 * PageParameters that were used to create the page instance so that later they can be used when
	 * generating page URL
	 */
	public static final PageParametersMetaDataKey PAGE_PARAMETERS_META_DATA_KEY = new PageParametersMetaDataKey();

	private static class PageParametersMetaDataKey extends MetaDataKey<PageParameters>
	{
		private static final long serialVersionUID = 1L;
	};

	// used to store number of trailing slashes in page url (prior the PageInfo)
	// part. This is necessary to maintain
	// the exact number of slashes after page redirect, so that we don't break
	// things that rely on URL depth
	// (other mounted URLs)
	private static final OriginalUrlTrailingSlashesCountMetaDataKey ORIGINAL_TRAILING_SLASHES_COUNT_METADATA_KEY = new OriginalUrlTrailingSlashesCountMetaDataKey();

	private static class OriginalUrlTrailingSlashesCountMetaDataKey extends MetaDataKey<Integer>
	{
		private static final long serialVersionUID = 1L;
	}

	/**
	 * Fix the amount of trailing slashes in the specified buffer.
	 * 
	 * @param buffer
	 * @param desiredCount
	 */
	private void fixTrailingSlashes(AppendingStringBuffer buffer, int desiredCount)
	{
		int current = getTrailingSlashesCount(buffer);
		if (current > desiredCount)
		{
			buffer.setLength(buffer.length() - (current - desiredCount));
		}
		else if (desiredCount > current)
		{
			int toAdd = desiredCount - current;
			while (toAdd > 0)
			{
				buffer.append("/");
				--toAdd;
			}
		}
	}

	/**
	 * @see org.apache.wicket.request.target.coding.IRequestTargetUrlCodingStrategy#encode(org.apache.wicket.IRequestTarget)
	 */
	public CharSequence encode(IRequestTarget requestTarget)
	{
		if (matches(requestTarget) == false)
		{
			throw new IllegalArgumentException("Unsupported request target type.");
		}

		PageParameters parameters = getPageParameters(requestTarget);
		PageInfo pageInfo = getPageInfo(requestTarget);

		final AppendingStringBuffer url = new AppendingStringBuffer(40);
		url.append(getMountPath());

		// there are cases where the parameters are null
		if (parameters != null)
		{
			appendParameters(url, parameters);
		}

		// check whether we know if the initial URL ended with slash
		Integer trailingSlashesCount = getOriginalOriginalTrailingSlashesCount(requestTarget);
		if (trailingSlashesCount != null)
		{
			fixTrailingSlashes(url, trailingSlashesCount.intValue());
		}

		return addPageInfo(url.toString(), pageInfo);
	}

	/**
	 * @see org.apache.wicket.request.target.coding.IRequestTargetUrlCodingStrategy#matches(org.apache.wicket.IRequestTarget)
	 */
	public boolean matches(IRequestTarget requestTarget)
	{
		if (requestTarget instanceof BookmarkablePageRequestTarget)
		{
			BookmarkablePageRequestTarget target = (BookmarkablePageRequestTarget)requestTarget;
			return target.getPageClass().equals(pageClassRef.get());
		}
		else if (requestTarget instanceof ListenerInterfaceRequestTarget)
		{
			ListenerInterfaceRequestTarget target = (ListenerInterfaceRequestTarget)requestTarget;
			return target.getPage().getClass().equals(pageClassRef.get()) &&
				target.getRequestListenerInterface().equals(IRedirectListener.INTERFACE);
		}
		return false;
	}

	/**
	 * Class that encapsulates {@link PageInfo} instance and the URL part prior the PageInfo part
	 * 
	 * @author Matej Knopp
	 */
	protected static class PageInfoExtraction
	{
		private final String urlAfterExtraction;

		private final PageInfo pageInfo;

		/**
		 * Construct.
		 * 
		 * @param urlAfterExtraction
		 * @param pageInfo
		 */
		public PageInfoExtraction(String urlAfterExtraction, PageInfo pageInfo)
		{
			this.urlAfterExtraction = urlAfterExtraction;
			this.pageInfo = pageInfo;
		}

		/**
		 * @return
		 */
		public PageInfo getPageInfo()
		{
			return pageInfo;
		}

		/**
		 * @return
		 */
		public String getUrlAfterExtraction()
		{
			return urlAfterExtraction;
		}
	}

	/**
	 * Extracts the PageInfo string.
	 * 
	 * @param url
	 * @return
	 */
	protected PageInfoExtraction extractPageInfo(String url)
	{
		int begin = url.lastIndexOf(getBeginSeparator());
		PageInfo last = null;
		String lastSubstring = "";
		while (begin != -1)
		{
			String substring = url.substring(begin);
			if (substring.length() > getBeginSeparator().length() + getEndSeparator().length() &&
				substring.startsWith(getBeginSeparator()) && substring.endsWith(getEndSeparator()))
			{
				String pageInfoString = substring.substring(getBeginSeparator().length(), //
					substring.length() - getEndSeparator().length());
				PageInfo info = PageInfo.parsePageInfo(pageInfoString);
				if (info != null)
				{
					last = info;
					lastSubstring = substring;
				}
				else
				{
					break;
				}
			}
			begin = url.lastIndexOf(getBeginSeparator(), begin - 1);
		}
		if (last != null)
		{
			return new PageInfoExtraction(url.substring(0, url.length() - lastSubstring.length()),
				last);
		}
		else
		{
			return new PageInfoExtraction(url, null);
		}
	}

	protected String getBeginSeparator()
	{
		return ".";
	}

	protected String getEndSeparator()
	{
		return "";
	}

	/**
	 * Encodes the PageInfo part to the URL
	 * 
	 * @param url
	 * @param pageInfo
	 * @return
	 */
	protected String addPageInfo(String url, PageInfo pageInfo)
	{
		if (pageInfo != null)
		{
			return url + getBeginSeparator() + pageInfo.toString() + getEndSeparator();
		}
		else
		{
			return url;
		}
	}

	/**
	 * Possible string representation of PageInfo:
	 * <ul>
	 * <li>pageId
	 * <li>pageId.version
	 * <li>pageMap (only if pageMap starts with a letter)
	 * <li>.pageMap
	 * <li>pageMap.pageId.version
	 * <li>pageMap.pageId (only if pageMap name starts with a letter)
	 * </ul>
	 * 
	 * @author Matej Knopp
	 */
	protected static class PageInfo
	{
		private final Integer pageId;
		private final Integer versionNumber;
		private final String pageMapName;

		/**
		 * Construct.
		 * 
		 * @param pageId
		 * @param versionNumber
		 * @param pageMapName
		 */
		public PageInfo(Integer pageId, Integer versionNumber, String pageMapName)
		{
			if ((pageId == null && (versionNumber != null || pageMapName == null)) ||
				(versionNumber == null && (pageId != null || pageMapName == null)))
			{
				throw new IllegalArgumentException(
					"Either both pageId and versionNumber must be null or none of them.");
			}
			this.pageId = pageId;
			this.versionNumber = versionNumber;
			this.pageMapName = pageMapName;
		}

		/**
		 * @return
		 */
		public Integer getPageId()
		{
			return pageId;
		}

		/**
		 * @return
		 */
		public Integer getVersionNumber()
		{
			return versionNumber;
		}

		/**
		 * @return
		 */
		public String getPageMapName()
		{
			return pageMapName;
		}

		private static char getPageInfoSeparator()
		{
			return '.';
		}

		/**
		 * <ul>
		 * <li>pageId
		 * <li>pageId.version
		 * <li>pageMap (only in if pagemap starts with a letter)
		 * <li>.pageMap
		 * <li>pageMap.pageId (only in if pageMap name starts with a letter)
		 * <li>pageMap.pageId.version
		 * </ul>
		 */
		@Override
		public String toString()
		{
			String pageMapName = this.pageMapName;

			// we don't need to encode the pageMapName when the pageId is unique
			// per session
			if (pageMapName != null && pageId != null && Application.exists() &&
				Application.get().getSessionSettings().isPageIdUniquePerSession())
			{
				pageMapName = null;
			}

			AppendingStringBuffer buffer = new AppendingStringBuffer(5);

			final boolean pmEmpty = Strings.isEmpty(pageMapName);
			final boolean pmContainsLetter = !pmEmpty && !isNumber(pageMapName);


			if (pageId != null && pmEmpty && versionNumber.intValue() == 0)
			{
				// pageId
				buffer.append(pageId);
			}
			else if (pageId != null && pmEmpty && versionNumber.intValue() != 0)
			{
				// pageId.version
				buffer.append(pageId);
				buffer.append(getPageInfoSeparator());
				buffer.append(versionNumber);
			}
			else if (pageId == null && pmContainsLetter)
			{
				// pageMap (must start with letter)
				buffer.append(pageMapName);
			}
			else if (pageId == null && !pmEmpty && !pmContainsLetter)
			{
				// .pageMap
				buffer.append(getPageInfoSeparator());
				buffer.append(pageMapName);
			}
			else if (pmContainsLetter && pageId != null && versionNumber.intValue() == 0)
			{
				// pageMap.pageId (pageMap must start with a letter)
				buffer.append(pageMapName);
				buffer.append(getPageInfoSeparator());
				buffer.append(pageId);
			}
			else if (!pmEmpty && pageId != null)
			{
				// pageMap.pageId.pageVersion
				buffer.append(pageMapName);
				buffer.append(getPageInfoSeparator());
				buffer.append(pageId);
				buffer.append(getPageInfoSeparator());
				buffer.append(versionNumber);
			}

			return buffer.toString();
		}

		/**
		 * Method that rigidly checks if the string consists of digits only.
		 * 
		 * @param string
		 * @return
		 */
		private static boolean isNumber(String string)
		{
			if (string == null || string.length() == 0)
			{
				return false;
			}
			for (int i = 0; i < string.length(); ++i)
			{
				if (Character.isDigit(string.charAt(i)) == false)
				{
					return false;
				}
			}
			return true;
		}

		/**
		 * <ul>
		 * <li>pageId
		 * <li>pageId.version
		 * <li>pageMap (only in if pagemap starts with a letter)
		 * <li>.pageMap
		 * <li>pageMap.pageId (only in if pageMap name starts with a letter)
		 * <li>pageMap.pageId.version
		 * </ul>
		 * 
		 * @param src
		 * @return
		 */
		public static PageInfo parsePageInfo(String src)
		{
			if (src == null || src.length() == 0)
			{
				return null;
			}

			String segments[] = Strings.split(src, getPageInfoSeparator());

			if (segments.length > 3)
			{
				return null;
			}

			// go trhough the segments to determine if they don't contains invalid characters
			for (int i = 0; i < segments.length; ++i)
			{
				for (int j = 0; j < segments[i].length(); ++j)
				{
					char c = segments[i].charAt(j);
					if (!Character.isLetterOrDigit(c) && c != '-' && c != '_')
					{
						return null;
					}
				}
			}

			if (segments.length == 1 && isNumber(segments[0]))
			{
				// pageId
				return new PageInfo(Integer.valueOf(segments[0]), new Integer(0), null);
			}
			else if (segments.length == 2 && isNumber(segments[0]) && isNumber(segments[1]))
			{
				// pageId:pageVersion
				return new PageInfo(Integer.valueOf(segments[0]), Integer.valueOf(segments[1]),
					null);
			}
			else if (segments.length == 1 && !isNumber(segments[0]))
			{
				// pageMap (starts with letter)
				return new PageInfo(null, null, segments[0]);
			}
			else if (segments.length == 2 && segments[0].length() == 0)
			{
				// .pageMap
				return new PageInfo(null, null, segments[1]);
			}
			else if (segments.length == 2 && !isNumber(segments[0]) && isNumber(segments[1]))
			{
				// pageMap.pageId (pageMap starts with letter)
				return new PageInfo(Integer.valueOf(segments[1]), new Integer(0), segments[0]);
			}
			else if (segments.length == 3)
			{
				if (segments[2].length() == 0 && isNumber(segments[1]))
				{
					// we don't encode it like this, but we still should be able
					// to parse it
					// pageMapName.pageId.
					return new PageInfo(Integer.valueOf(segments[1]), new Integer(0), segments[0]);
				}
				else if (isNumber(segments[1]) && isNumber(segments[2]))
				{
					// pageMapName.pageId.pageVersion
					return new PageInfo(Integer.valueOf(segments[1]), Integer.valueOf(segments[2]),
						segments[0]);
				}
			}

			return null;
		}

	};

	/**
	 * BookmarkablePage request target that does a redirect after bookmarkable page was rendered
	 * (only if the bookmarkable page is stateful though)
	 * 
	 * @author Matej Knopp
	 */
	public static class HybridBookmarkablePageRequestTarget extends BookmarkablePageRequestTarget
	{
		private final int originalUrlTrailingSlashesCount;
		private final boolean redirect;

		/**
		 * Construct.
		 * 
		 * @param pageMapName
		 * @param pageClass
		 * @param pageParameters
		 * @param originalUrlTrailingSlashesCount
		 * @param redirect
		 */
		public HybridBookmarkablePageRequestTarget(String pageMapName, Class pageClass,
			PageParameters pageParameters, int originalUrlTrailingSlashesCount, boolean redirect)
		{
			super(pageMapName, pageClass, pageParameters);
			this.originalUrlTrailingSlashesCount = originalUrlTrailingSlashesCount;
			this.redirect = redirect;
		}

		@Override
		protected Page newPage(Class pageClass, RequestCycle requestCycle)
		{
			Page page = super.newPage(pageClass, requestCycle);
			page.setMetaData(PAGE_PARAMETERS_META_DATA_KEY, new PageParameters(getPageParameters()));
			page.setMetaData(ORIGINAL_TRAILING_SLASHES_COUNT_METADATA_KEY, new Integer(
				originalUrlTrailingSlashesCount));
			return page;
		}

		@Override
		public void respond(RequestCycle requestCycle)
		{
			Page page = getPage(requestCycle);
			if (page.isPageStateless() == false && redirect)
			{
				requestCycle.redirectTo(page);
			}
			else
			{
				super.respond(requestCycle);
			}
		}
	};

	/**
	 * @see org.apache.wicket.request.target.coding.AbstractRequestTargetUrlCodingStrategy#matches(java.lang.String)
	 */
	@Override
	public boolean matches(String path)
	{
		RequestCycle rc = RequestCycle.get();

		// the null check is necessary, as this method is first time called from WicketFilter when
		// no RequestCycle exists yet
		if (rc != null && ((WebRequest)rc.getRequest()).isAjax())
		{
			// HybridUrlCodingStrategy doesn't make sense for ajax request
			return false;
		}

		if (path.startsWith(getMountPath()))
		{
			/*
			 * We need to match /mount/point or /mount/point/with/extra/path, but not
			 * /mount/pointXXX
			 */
			String remainder = path.substring(getMountPath().length());
			if (remainder.length() == 0 || remainder.startsWith("/"))
			{
				return true;
			}
			/*
			 * We also need to accept /mount/point(XXX)
			 */
			if (remainder.length() > getBeginSeparator().length() + getEndSeparator().length() &&
				remainder.startsWith(getBeginSeparator()) && remainder.endsWith(getEndSeparator()))
			{
				String substring = remainder.substring(getBeginSeparator().length(), //
					remainder.length() - getEndSeparator().length());
				PageInfo info = PageInfo.parsePageInfo(substring);
				if (info != null)
				{
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public String toString()
	{
		return "HybridUrlCodingStrategy[page=" + pageClassRef.get() + "]";
	}

}
