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
package org.apache._wicket.request.encoder;

import java.lang.ref.WeakReference;

import org.apache._wicket.IPage;
import org.apache._wicket.PageParameters;
import org.apache._wicket.request.Url;
import org.apache._wicket.request.encoder.info.PageComponentInfo;
import org.apache._wicket.request.encoder.parameters.PageParametersEncoder;
import org.apache._wicket.request.encoder.parameters.SimplePageParametersEncoder;
import org.apache._wicket.request.request.Request;

/**
 * Encoder for mounted URL. The mount path can contain parameter placeholders, i.e.
 * <code>/mount/${foo}/path</code>. In that case the appropriate segment from the URL will be
 * accessible as named parameter "foo" in the {@link PageParameters}. Similarly when the URL is
 * constructed, the second segment will contain the value of the "foo" named page parameter.
 * <p>
 * Decodes and encodes the following URLs:
 * 
 * <pre>
 *  Page Class - Render (BookmarkablePageRequestHandler for mounted pages)
 *  /mount/point
 *  /mount/point?pageMap
 *  (these will redirect to hybrid alternative if page is not stateless)
 * 
 *  IPage Instance - Render Hybrid (RenderPageRequestHandler for mounted pages) 
 *  /mount/point?2
 *  /mount/point?2.4
 *  /mount/point?pageMap.2.4
 * 
 *  IPage Instance - Bookmarkable Listener (BookmarkableListenerInterfaceRequestHandler for mounted pages) 
 *  /mount/point?2-click-foo-bar-baz
 *  /mount/point?2.4-click-foo-bar-baz
 *  /mount/point?pageMap.2.4-click-foo-bar-baz
 *  /mount/point?2.4-click.1-foo-bar-baz (1 is behavior index)
 *  (these will redirect to hybrid if page is not stateless)
 * </pre>
 * 
 * @author Matej Knopp
 */
public class MountedEncoder extends AbstractBookmarkableEncoder
{
	private final PageParametersEncoder pageParametersEncoder;
	private final String[] mountSegments;

	/** bookmarkable page class. */
	protected final WeakReference<Class<? extends IPage>> pageClass;


	/**
	 * Construct.
	 * 
	 * @param mountPath
	 * @param pageClass
	 * @param pageParametersEncoder
	 */
	public MountedEncoder(String mountPath, Class<? extends IPage> pageClass,
		PageParametersEncoder pageParametersEncoder)
	{
		if (pageParametersEncoder == null)
		{
			throw new IllegalArgumentException("Argument 'pageParametersEncoder' may not be null.");
		}
		if (pageClass == null)
		{
			throw new IllegalArgumentException("Argument 'pageClass' may not be null.");
		}
		if (mountPath == null)
		{
			throw new IllegalArgumentException("Argument 'mountPath' may not be null.");
		}
		this.pageParametersEncoder = pageParametersEncoder;
		this.pageClass = new WeakReference<Class<? extends IPage>>(pageClass);
		this.mountSegments = getMountSegments(mountPath);
	}

	/**
	 * Construct.
	 * 
	 * @param mountPath
	 * @param pageClass
	 */
	public MountedEncoder(String mountPath, Class<? extends IPage> pageClass)
	{
		this(mountPath, pageClass, new SimplePageParametersEncoder());
	}

	@Override
	protected UrlInfo parseRequest(Request request)
	{
		Url url = request.getUrl();

		// when redirect to buffer/render is active and redirectFromHomePage returns true
		// check mounted class against the home page class. if it matches let wicket redirect
		// to the mounted URL
		if (redirectFromHomePage() && checkHomePage(url))
		{
			UrlInfo info = new UrlInfo(null, getContext().getHomePageClass(), newPageParameters());
			return info;
		}
		// check if the URL is long enough and starts with the proper segments
		else if (url.getSegments().size() >= mountSegments.length &&
			urlStartsWith(url, mountSegments))
		{
			// try to extract page and component information from URL
			PageComponentInfo info = getPageComponentInfo(url);

			Class<? extends IPage> pageClass = this.pageClass.get();

			// extract the PageParameters from URL if there are any
			PageParameters pageParameters = extractPageParameters(url,
				request.getRequestParameters(), mountSegments.length, pageParametersEncoder);

			// check if there are placeholders in mount segments
			for (int i = 0; i < mountSegments.length; ++i)
			{
				String placeholder = getPlaceholder(mountSegments[i]);
				if (placeholder != null)
				{
					// extract the parameter from URL
					pageParameters.addNamedParameter(placeholder, url.getSegments().get(i));
				}
			}

			return new UrlInfo(info, pageClass, pageParameters);
		}
		else
		{
			return null;
		}
	}

	protected PageParameters newPageParameters()
	{
		return new PageParameters();
	}

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

		for (int i = 0; i < mountSegments.length; ++i)
		{
			String placeholder = getPlaceholder(mountSegments[i]);
			if (placeholder != null)
			{
				url.getSegments().set(i, copy.getNamedParameter(placeholder).toString(""));
				copy.removeNamedParameter(placeholder);
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
			if (pageClass.get().equals(getContext().getHomePageClass()) && redirectFromHomePage())
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * If this method returns <code>true</code> and application home page class is same as the
	 * class mounted with this encoder, request to home page will result in a redirect to the
	 * mounted path.
	 * 
	 * @return whether this encode should respond to home page request when home page class is same
	 *         as mounted class.
	 */
	protected boolean redirectFromHomePage()
	{
		return true;
	}

	@Override
	protected boolean pageMustHaveBeenCreatedBookmarkable()
	{
		return false;
	}

	public int getMachingSegmentsCount(Request request)
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
}
