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
package org.apache.wicket.core.request.mapper;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Application;
import org.apache.wicket.RequestListenerInterface;
import org.apache.wicket.core.request.handler.ListenerInterfaceRequestHandler;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.mapper.info.ComponentInfo;
import org.apache.wicket.request.mapper.info.PageComponentInfo;
import org.apache.wicket.request.mapper.info.PageInfo;
import org.apache.wicket.request.mapper.parameter.IPageParametersEncoder;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.mapper.parameter.PageParametersEncoder;
import org.apache.wicket.util.ClassProvider;
import org.apache.wicket.util.IProvider;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.reference.ClassReference;
import org.apache.wicket.util.string.Strings;

/**
 * Encoder for mounted URL. The mount path can contain parameter placeholders, i.e.
 * <code>/mount/${foo}/path</code>. In that case the appropriate segment from the URL will be
 * accessible as named parameter "foo" in the {@link PageParameters}. Similarly when the URL is
 * constructed, the second segment will contain the value of the "foo" named page parameter.
 * Optional parameters are denoted by using a # instead of $: <code>/mount/#{foo}/path/${bar}</code>
 * has an optional {@code foo} parameter, a fixed {@code /path/} part and a required {@code bar}
 * parameter. When in doubt, parameters are matched from left to right, where required parameters
 * are matched before optional parameters, and optional parameters eager (from left to right).
 * <p>
 * Decodes and encodes the following URLs:
 *
 * <pre>
 *  Page Class - Render (BookmarkablePageRequestHandler for mounted pages)
 *  /mount/point
 *  (these will redirect to hybrid alternative if page is not stateless)
 *
 *  IPage Instance - Render Hybrid (RenderPageRequestHandler for mounted pages)
 *  /mount/point?2
 *
 *  IPage Instance - Bookmarkable Listener (BookmarkableListenerInterfaceRequestHandler for mounted pages)
 *  /mount/point?2-click-foo-bar-baz
 *  /mount/point?2-5.click.1-foo-bar-baz (1 is behavior index, 5 is render count)
 *  (these will redirect to hybrid if page is not stateless)
 * </pre>
 *
 * @author Matej Knopp
 */
public class MountedMapper extends AbstractBookmarkableMapper
{
	private final IPageParametersEncoder pageParametersEncoder;

	private static class MountPathSegment
	{
		private int segmentIndex;
		private String fixedPart;
		private int minParameters;
		private int optionalParameters;

		public MountPathSegment(int segmentIndex)
		{
			this.segmentIndex = segmentIndex;
		}

		public void setFixedPart(String fixedPart)
		{
			this.fixedPart = fixedPart;
		}

		public void addRequiredParameter()
		{
			minParameters++;
		}

		public void addOptionalParameter()
		{
			optionalParameters++;
		}

		public int getSegmentIndex()
		{
			return segmentIndex;
		}

		public String getFixedPart()
		{
			return fixedPart;
		}

		public int getMinParameters()
		{
			return minParameters;
		}

		public int getOptionalParameters()
		{
			return optionalParameters;
		}

		public int getMaxParameters()
		{
			return getOptionalParameters() + getMinParameters();
		}

		public int getFixedPartSize()
		{
			return getFixedPart() == null ? 0 : 1;
		}

		@Override
		public String toString()
		{
			return "(" + getSegmentIndex() + ") " + getMinParameters() + "-" + getMaxParameters() +
				" " + (getFixedPart() == null ? "(end)" : getFixedPart());
		}
	}

	private final List<MountPathSegment> pathSegments;
	private final String[] mountSegments;

	/** bookmarkable page class. */
	private final IProvider<Class<? extends IRequestablePage>> pageClassProvider;

	/**
	 * Construct.
	 *
	 * @param mountPath
	 * @param pageClass
	 */
	public MountedMapper(String mountPath, Class<? extends IRequestablePage> pageClass)
	{
		this(mountPath, pageClass, new PageParametersEncoder());
	}

	/**
	 * Construct.
	 *
	 * @param mountPath
	 * @param pageClassProvider
	 */
	@Deprecated
	public MountedMapper(String mountPath,
	                     ClassProvider<? extends IRequestablePage> pageClassProvider)
	{
		this(mountPath, new ClassReference(pageClassProvider.get()), new PageParametersEncoder());
	}

	/**
	 * Construct.
	 *
	 * @param mountPath
	 * @param pageClassProvider
	 */
	public MountedMapper(String mountPath,
	                     IProvider<Class<? extends IRequestablePage>> pageClassProvider)
	{
		this(mountPath, pageClassProvider, new PageParametersEncoder());
	}

	/**
	 * Construct.
	 *
	 * @param mountPath
	 * @param pageClass
	 * @param pageParametersEncoder
	 */
	public MountedMapper(String mountPath, Class<? extends IRequestablePage> pageClass,
		IPageParametersEncoder pageParametersEncoder)
	{
		this(mountPath, new ClassReference(pageClass), pageParametersEncoder);
	}

	/**
	 * Construct.
	 *
	 * @param mountPath
	 * @param pageClassProvider
	 * @param pageParametersEncoder
	 */
	@Deprecated
	public MountedMapper(String mountPath,
		ClassProvider<? extends IRequestablePage> pageClassProvider,
		IPageParametersEncoder pageParametersEncoder)
	{
		this(mountPath, new ClassReference(pageClassProvider.get()),
				pageParametersEncoder);
	}

	/**
	 * Construct.
	 *
	 * @param mountPath
	 * @param pageClassProvider
	 * @param pageParametersEncoder
	 */
	public MountedMapper(String mountPath,
	                     IProvider<Class<? extends IRequestablePage>> pageClassProvider,
	                     IPageParametersEncoder pageParametersEncoder)
	{
		Args.notEmpty(mountPath, "mountPath");
		Args.notNull(pageClassProvider, "pageClassProvider");
		Args.notNull(pageParametersEncoder, "pageParametersEncoder");

		this.pageParametersEncoder = pageParametersEncoder;
		this.pageClassProvider = pageClassProvider;
		mountSegments = getMountSegments(mountPath);
		pathSegments = getPathSegments(mountSegments);
	}

	private List<MountPathSegment> getPathSegments(String[] segments)
	{
		List<MountPathSegment> ret = new ArrayList<MountPathSegment>();
		int segmentIndex = 0;
		MountPathSegment curPathSegment = new MountPathSegment(segmentIndex);
		ret.add(curPathSegment);
		for (String curSegment : segments)
		{
			if (isFixedSegment(curSegment))
			{
				curPathSegment.setFixedPart(curSegment);
				curPathSegment = new MountPathSegment(segmentIndex + 1);
				ret.add(curPathSegment);
			}
			else if (getPlaceholder(curSegment) != null)
			{
				curPathSegment.addRequiredParameter();
			}
			else
			{
				curPathSegment.addOptionalParameter();
			}
			segmentIndex++;
		}
		return ret;
	}

	private boolean isFixedSegment(String segment)
	{
		return getOptionalPlaceholder(segment) == null && getPlaceholder(segment) == null;
	}

	/**
	 * @see AbstractBookmarkableMapper#parseRequest(org.apache.wicket.request.Request)
	 */
	@Override
	protected UrlInfo parseRequest(Request request)
	{
		Url url = request.getUrl();

		// when redirect to buffer/render is active and redirectFromHomePage returns true
		// check mounted class against the home page class. if it matches let wicket redirect
		// to the mounted URL
		if (redirectFromHomePage() && checkHomePage(url))
		{
			return new UrlInfo(null, getContext().getHomePageClass(), newPageParameters());
		}
		// check if the URL starts with the proper segments
		else if (urlStartsWith(url, mountSegments))
		{
			// try to extract page and component information from URL
			PageComponentInfo info = getPageComponentInfo(url);
			Class<? extends IRequestablePage> pageClass = getPageClass();
			PageParameters pageParameters = extractPageParameters(request, url);

			return new UrlInfo(info, pageClass, pageParameters);
		}
		else
		{
			return null;
		}
	}

	/*
	 * extract the PageParameters from URL if there are any
	 */
	private PageParameters extractPageParameters(Request request, Url url)
	{
		int[] matchedParameters = getMatchedSegmentSizes(url);
		int total = 0;
		for (int curMatchSize : matchedParameters)
			total += curMatchSize;
		PageParameters pageParameters = extractPageParameters(request, total, pageParametersEncoder);

		int skippedParameters = 0;
		for (int pathSegmentIndex = 0; pathSegmentIndex < pathSegments.size(); pathSegmentIndex++)
		{
			MountPathSegment curPathSegment = pathSegments.get(pathSegmentIndex);
			int matchSize = matchedParameters[pathSegmentIndex] - curPathSegment.getFixedPartSize();
			int optionalParameterMatch = matchSize - curPathSegment.getMinParameters();
			for (int matchSegment = 0; matchSegment < matchSize; matchSegment++)
			{
				if (pageParameters == null)
				{
					pageParameters = new PageParameters();
				}

				int curSegmentIndex = matchSegment + curPathSegment.getSegmentIndex();
				String curSegment = mountSegments[curSegmentIndex];
				String placeholder = getPlaceholder(curSegment);
				String optionalPlaceholder = getOptionalPlaceholder(curSegment);
				// extract the parameter from URL
				if (placeholder != null)
				{
					pageParameters.add(placeholder,
						url.getSegments().get(curSegmentIndex - skippedParameters));
				}
				else if (optionalPlaceholder != null && optionalParameterMatch > 0)
				{
					pageParameters.add(optionalPlaceholder,
						url.getSegments().get(curSegmentIndex - skippedParameters));
					optionalParameterMatch--;
				}
			}
			skippedParameters += curPathSegment.getMaxParameters() - matchSize;
		}
		return pageParameters;
	}

	@Override
	protected boolean urlStartsWith(Url url, String... segments)
	{
		if (url == null)
		{
			return false;
		}
		else
		{
			return getMatchedSegmentSizes(url) != null;
		}
	}

	private int[] getMatchedSegmentSizes(Url url)
	{
		int[] ret = new int[pathSegments.size()];
		int segmentIndex = 0;
		int pathSegmentIndex = 0;
		for (MountPathSegment curPathSegment : pathSegments.subList(0, pathSegments.size() - 1))
		{
			boolean foundFixedPart = false;
			segmentIndex += curPathSegment.getMinParameters();
			int max = Math.min(curPathSegment.getOptionalParameters() + 1,
				url.getSegments().size() - segmentIndex);

			for (int count = max - 1; count >= 0; count--)
			{
				if (url.getSegments()
					.get(segmentIndex + count)
					.equals(curPathSegment.getFixedPart()))
				{
					foundFixedPart = true;
					segmentIndex += count + 1;
					ret[pathSegmentIndex] = count + curPathSegment.getMinParameters() + 1;
					break;
				}
			}
			if (!foundFixedPart)
				return null;
			pathSegmentIndex++;
		}
		MountPathSegment lastSegment = pathSegments.get(pathSegments.size() - 1);
		segmentIndex += lastSegment.getMinParameters();
		if (segmentIndex > url.getSegments().size())
			return null;
		ret[pathSegmentIndex] = Math.min(lastSegment.getMaxParameters(), url.getSegments().size() -
			segmentIndex + lastSegment.getMinParameters());
		return ret;
	}

	protected PageParameters newPageParameters()
	{
		return new PageParameters();
	}

	@Override
	public Url mapHandler(IRequestHandler requestHandler)
	{
		Url url = super.mapHandler(requestHandler);

		if (url == null && requestHandler instanceof ListenerInterfaceRequestHandler &&
			getRecreateMountedPagesAfterExpiry())
		{
			ListenerInterfaceRequestHandler handler = (ListenerInterfaceRequestHandler)requestHandler;
			IRequestablePage page = handler.getPage();
			if (checkPageInstance(page))
			{
				String componentPath = handler.getComponentPath();
				RequestListenerInterface listenerInterface = handler.getListenerInterface();

				Integer renderCount = null;
				if (listenerInterface.isIncludeRenderCount())
				{
					renderCount = page.getRenderCount();
				}

				PageInfo pageInfo = getPageInfo(handler);
				ComponentInfo componentInfo = new ComponentInfo(renderCount,
					requestListenerInterfaceToString(listenerInterface), componentPath,
					handler.getBehaviorIndex());
				PageComponentInfo pageComponentInfo = new PageComponentInfo(pageInfo, componentInfo);
				PageParameters parameters = new PageParameters(page.getPageParameters());
				UrlInfo urlInfo = new UrlInfo(pageComponentInfo, page.getClass(),
					parameters.mergeWith(handler.getPageParameters()));
				url = buildUrl(urlInfo);
			}
		}

		return url;
	}

	boolean getRecreateMountedPagesAfterExpiry()
	{
		return Application.get().getPageSettings().getRecreateMountedPagesAfterExpiry();
	}

	/**
	 * @see AbstractBookmarkableMapper#buildUrl(AbstractBookmarkableMapper.UrlInfo)
	 */
	@Override
	protected Url buildUrl(UrlInfo info)
	{
		Url url = new Url();
		for (String s : mountSegments)
		{
			url.getSegments().add(s);
		}
		encodePageComponentInfo(url, info.getPageComponentInfo());

		PageParameters copy = new PageParameters(info.getPageParameters());

		int dropped = 0;
		for (int i = 0; i < mountSegments.length; ++i)
		{
			String placeholder = getPlaceholder(mountSegments[i]);
			String optionalPlaceholder = getOptionalPlaceholder(mountSegments[i]);
			if (placeholder != null)
			{
				url.getSegments().set(i - dropped, copy.get(placeholder).toString(""));
				copy.remove(placeholder);
			}
			else if (optionalPlaceholder != null)
			{
				if (copy.getNamedKeys().contains(optionalPlaceholder))
				{
					url.getSegments().set(i - dropped, copy.get(optionalPlaceholder).toString(""));
					copy.remove(optionalPlaceholder);
				}
				else
				{
					url.getSegments().remove(i - dropped);
					dropped++;
				}
			}
		}

		return encodePageParameters(url, copy, pageParametersEncoder);
	}

	/**
	 * Check if the URL is for home page and the home page class match mounted class. If so,
	 * redirect to mounted URL.
	 *
	 * @param url
	 * @return request handler or <code>null</code>
	 */
	private boolean checkHomePage(Url url)
	{
		if (url.getSegments().isEmpty() && url.getQueryParameters().isEmpty())
		{
			// this is home page
			if (getPageClass().equals(getContext().getHomePageClass()) && redirectFromHomePage())
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * If this method returns <code>true</code> and application home page class is same as the class
	 * mounted with this encoder, request to home page will result in a redirect to the mounted
	 * path.
	 *
	 * @return whether this encode should respond to home page request when home page class is same
	 *         as mounted class.
	 */
	protected boolean redirectFromHomePage()
	{
		return true;
	}

	/**
	 * @see AbstractBookmarkableMapper#pageMustHaveBeenCreatedBookmarkable()
	 */
	@Override
	protected boolean pageMustHaveBeenCreatedBookmarkable()
	{
		return false;
	}

	/**
	 * @see AbstractBookmarkableMapper#getCompatibilityScore(org.apache.wicket.request.Request)
	 */
	@Override
	public int getCompatibilityScore(Request request)
	{
		if (urlStartsWith(request.getUrl(), mountSegments))
		{
			return mountSegments.length;
		}
		else
		{
			return 0;
		}
	}

	/**
	 * @see AbstractBookmarkableMapper#checkPageClass(java.lang.Class)
	 */
	@Override
	protected boolean checkPageClass(Class<? extends IRequestablePage> pageClass)
	{
		return pageClass.equals(this.getPageClass());
	}

	private Class<? extends IRequestablePage> getPageClass()
	{
		return pageClassProvider.get();
	}

	@Override
	public String toString()
	{
		return "MountedMapper [mountSegments=" + Strings.join("/", mountSegments) + "]";
	}
}
