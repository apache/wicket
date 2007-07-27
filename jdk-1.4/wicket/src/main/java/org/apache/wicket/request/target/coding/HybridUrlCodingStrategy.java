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

import org.apache.wicket.IRedirectListener;
import org.apache.wicket.IRequestTarget;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.Session;
import org.apache.wicket.protocol.http.request.WebRequestCodingStrategy;
import org.apache.wicket.request.RequestParameters;
import org.apache.wicket.request.target.component.BookmarkableListenerInterfaceRequestTarget;
import org.apache.wicket.request.target.component.BookmarkablePageRequestTarget;
import org.apache.wicket.request.target.component.PageRequestTarget;
import org.apache.wicket.request.target.component.listener.ListenerInterfaceRequestTarget;
import org.apache.wicket.util.string.AppendingStringBuffer;
import org.apache.wicket.util.string.Strings;


/**
 * @author Matej Knopp
 */
public class HybridUrlCodingStrategy extends AbstractRequestTargetUrlCodingStrategy
{
	/** bookmarkable page class. */
	protected final WeakReference/* <Class> */pageClassRef;


	/**
	 * Construct.
	 * 
	 * @param mountPath
	 * @param pageClass
	 */
	public HybridUrlCodingStrategy(String mountPath, Class pageClass)
	{
		super(mountPath);
		pageClassRef = new WeakReference(pageClass);
	}


	public IRequestTarget decode(RequestParameters requestParameters)
	{
		String parametersFragment = requestParameters.getPath().substring(getMountPath().length());

		PageInfoExtraction extraction = extractPageInfo(parametersFragment);

		PageInfo pageInfo = extraction.getPageInfo();
		String pageMapName = pageInfo != null ? pageInfo.getPageMapName() : null;
		Integer pageVersion = pageInfo != null ? pageInfo.getVersionNumber() : null;
		Integer pageId = pageInfo != null ? pageInfo.getPageId() : null;

		PageParameters parameters = new PageParameters(decodeParameters(extraction
				.getUrlAfterExtraction(), requestParameters.getParameters()));

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
		final String interfaceParameter = (String)parameters
				.remove(WebRequestCodingStrategy.INTERFACE_PARAMETER_NAME);

		if (interfaceParameter != null)
		{
			// stateless listener interface
			WebRequestCodingStrategy.addInterfaceParameters(interfaceParameter, requestParameters);
			return new BookmarkableListenerInterfaceRequestTarget(pageMapName, (Class)pageClassRef
					.get(), parameters, requestParameters.getComponentPath(), requestParameters
					.getInterfaceName());
		}
		else if (pageId == null)
		{
			// bookmarkable page request
			return new HybridBookmarkablePageRequestTarget(pageMapName, (Class)pageClassRef.get(),
					parameters);
		}
		else
		// hybrid url
		{
			Page page = Session.get().getPage(pageMapName, "" + pageId,
					pageVersion != null ? pageVersion.intValue() : 0);

			// check if the found page match the required class
			if (page != null && page.getClass().equals(pageClassRef.get()))
			{
				requestParameters.setInterfaceName(IRedirectListener.INTERFACE.getName());
				RequestCycle.get().getRequest().setPage(page);
				return new PageRequestTarget(page);
			}
			else
			{
				return new HybridBookmarkablePageRequestTarget(pageMapName, (Class)pageClassRef.get(),
						parameters);
			}
		}

	}


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
			return (PageParameters)page.getMetaData(PAGE_PARAMETERS_META_DATA_KEY);
		}
		else
		{
			return null;
		}
	}

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
			return new PageInfo(new Integer(page.getNumericId()), new Integer(page
					.getCurrentVersionNumber()), page.getPageMapName());
		}
		else
		{
			return null;
		}
	}


	private static final PageParametersMetadataKey PAGE_PARAMETERS_META_DATA_KEY = new PageParametersMetadataKey();

	private static class PageParametersMetadataKey extends MetaDataKey
	{
		/**
		 * Construct.
		 */
		public PageParametersMetadataKey()
		{
			super(PageParameters.class);
		}

		private static final long serialVersionUID = 1L;

	};

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
		appendParameters(url, parameters);

		return addPageInfo(url.toString(), pageInfo);
	}

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
		int lastIndexLeft = url.lastIndexOf('(');
		int lastIndexRight = url.lastIndexOf(')');
		if (lastIndexLeft != -1 && lastIndexRight != -1 && lastIndexLeft < lastIndexRight &&
				lastIndexRight - lastIndexLeft > 0 && lastIndexRight == url.length() - 1 &&
				url.charAt(lastIndexLeft - 1) == '/')
		{
			String infoSubstring = url.substring(lastIndexLeft + 1, lastIndexRight);
			PageInfo info = PageInfo.parsePageInfo(infoSubstring);
			if (info != null)
			{
				return new PageInfoExtraction(url.substring(0, lastIndexLeft - 1), info);
			}
		}
		return new PageInfoExtraction(url, null);
	}

	protected String addPageInfo(String url, PageInfo pageInfo)
	{
		if (pageInfo != null)
		{
			if (url.endsWith("/") == false)
			{
				url = url + "/";
			}
			return url + "(" + pageInfo.toString() + ")";
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
	 * <li>pageId:version
	 * <li>:pageMap
	 * <li>pageMap:pageId: (the first colon distingues between this and the
	 * previous one)
	 * <li>pageMap:pageId:version
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

		public String toString()
		{
			AppendingStringBuffer buffer = new AppendingStringBuffer(5);
			if (pageMapName != null)
			{
				if (versionNumber == null && pageId == null)
				{
					buffer.append(":");
				}
				buffer.append(pageMapName);
				if (pageId != null)
				{
					buffer.append(":");
				}
			}
			if (pageId != null)
			{
				buffer.append(pageId);
			}
			if ((versionNumber == null || versionNumber.intValue() == 0) && pageMapName != null)
			{
				buffer.append(":");
			}
			if (versionNumber != null && versionNumber.intValue() != 0)
			{
				buffer.append(":");
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
		 * @param src
		 * @return
		 */
		public static PageInfo parsePageInfo(String src)
		{
			if (src == null || src.length() == 0)
			{
				return null;
			}

			String segments[] = Strings.split(src, ':');

			if (segments.length > 3)
			{
				return null;
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
			else if (segments.length == 2 && segments[0].length() == 0)
			{
				// :pageMapName
				return new PageInfo(null, null, segments[1]);
			}
			else
			{
				if (segments[2].length() == 0 && isNumber(segments[1]))
				{
					// pageMapName:pageId:
					return new PageInfo(Integer.valueOf(segments[1]), new Integer(0), segments[0]);
				}
				else if (isNumber(segments[1]) && isNumber(segments[2]))
				{
					// pageMapName:pageId:pageVersion
					return new PageInfo(Integer.valueOf(segments[1]), Integer.valueOf(segments[2]),
							segments[0]);
				}
			}

			return null;
		}

	};

	private static class HybridBookmarkablePageRequestTarget extends BookmarkablePageRequestTarget
	{
		/**
		 * Construct.
		 * @param pageMapName
		 * @param pageClass
		 * @param pageParameters
		 */
		public HybridBookmarkablePageRequestTarget(String pageMapName, Class pageClass, PageParameters pageParameters)
		{
			super(pageMapName, pageClass, pageParameters);
		}
		
		protected Page newPage(Class pageClass, RequestCycle requestCycle)
		{
			Page page = super.newPage(pageClass, requestCycle);
			page.setMetaData(PAGE_PARAMETERS_META_DATA_KEY, getPageParameters());
			return page;
		}
		
		public void respond(RequestCycle requestCycle)
		{
			super.respond(requestCycle);
			if (requestCycle.isRedirect() == false)
			{
				Page page = getPage(requestCycle);
				if (page.isPageStateless() == false)
				{
					requestCycle.redirectTo(page);
				}
			}
		}
	};

	public String toString()
	{
		return "HybridUrlCodingStrategy[page=" + pageClassRef.get() + "]";
	}

}
