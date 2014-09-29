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
package org.apache.wicket.protocol.https;

import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.Session;
import org.apache.wicket.core.request.handler.IPageClassRequestHandler;
import org.apache.wicket.request.IRequestCycle;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.IRequestMapper;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.mapper.IRequestMapperDelegate;
import org.apache.wicket.util.collections.ClassMetaCache;
import org.apache.wicket.util.lang.Args;

/**
 * A {@link IRequestMapper} that will issue a redirect to secured communication (over https) if the
 * page resolved by {@linkplain #delegate} is annotated with @{@link RequireHttps}
 * 
 * <p>
 * To setup it:
 * 
 * <pre>
 * public class MyApplication extends WebApplication
 * {
 * 	public void init()
 * 	{
 * 		super.init();
 * 
 * 		getRootRequestMapperAsCompound().add(new MountedMapper(&quot;secured&quot;, HttpsPage.class));
 * 		mountPage(SomeOtherPage.class);
 * 
 * 		// notice that in most cases this should be done as the
 * 		// last mounting-related operation because it replaces the root mapper
 * 		setRootRequestMapper(new HttpsMapper(getRootRequestMapper(), new HttpsConfig(80, 443)));
 * 	}
 * }
 * </pre>
 * 
 * any request to <em>http://hostname:httpPort/secured</em> will be redirected to
 * <em>https://hostname:httpsPort/secured</em>
 * 
 * @author igor
 */
public class HttpsMapper implements IRequestMapperDelegate
{
	private final HttpsConfig config;
	private final IRequestMapper delegate;
	private final ClassMetaCache<Scheme> cache = new ClassMetaCache<Scheme>();

	/**
	 * Constructor
	 * 
	 * @param delegate
	 * @param config
	 */
	public HttpsMapper(IRequestMapper delegate, HttpsConfig config)
	{
		this.delegate = Args.notNull(delegate, "delegate");
		this.config = config;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IRequestMapper getDelegateMapper()
	{
		return delegate;
	}

	@Override
	public final int getCompatibilityScore(Request request)
	{
		return delegate.getCompatibilityScore(request);
	}


	@Override
	public final IRequestHandler mapRequest(Request request)
	{
		IRequestHandler handler = delegate.mapRequest(request);

		Scheme desired = getDesiredSchemeFor(handler);
		Scheme current = getSchemeOf(request);
		if (!desired.isCompatibleWith(current))
		{
			// we are currently on the wrong scheme for this handler

			// construct a url for the handler on the correct scheme
			String url = createRedirectUrl(handler, request, desired);

			// replace handler with one that will redirect to the created url
			handler = createRedirectHandler(url);
		}
		return handler;
	}

	@Override
	public final Url mapHandler(IRequestHandler handler)
	{
		return mapHandler(handler, RequestCycle.get().getRequest());
	}

	/**
	 * Creates the {@link IRequestHandler} that will be responsible for the redirect
	 * 
	 * @param url
	 * @return request handler
	 */
	protected IRequestHandler createRedirectHandler(String url)
	{
		return new RedirectHandler(url, config);
	}

	/**
	 * Construts a redirect url that should switch the user to the specified {@code scheme}
	 * 
	 * @param handler
	 *            request handler being accessed
	 * @param request
	 *            current request
	 * @param scheme
	 *            desired scheme for the redirect url
	 * @return url
	 */
	protected String createRedirectUrl(IRequestHandler handler, Request request, Scheme scheme)
	{
		HttpServletRequest req = (HttpServletRequest)((WebRequest)request).getContainerRequest();
		String url = scheme.urlName() + "://";
		url += req.getServerName();
		if (!scheme.usesStandardPort(config))
		{
			url += ":" + scheme.getPort(config);
		}
		url += req.getRequestURI();
		if (req.getQueryString() != null)
		{
			url += "?" + req.getQueryString();
		}
		return url;
	}


	/**
	 * Creates a url for the handler. Modifies it with the correct {@link Scheme} if necessary.
	 * 
	 * @param handler
	 * @param request
	 * @return url
	 */
	final Url mapHandler(IRequestHandler handler, Request request)
	{
		Url url = delegate.mapHandler(handler);

		Scheme desired = getDesiredSchemeFor(handler);
		Scheme current = getSchemeOf(request);
		if (!desired.isCompatibleWith(current))
		{
			// the generated url does not have the correct scheme, set it (which in turn will cause
			// the url to be rendered in its full representation)
			url.setProtocol(desired.urlName());
			url.setPort(desired.getPort(config));
		}
		return url;
	}


	/**
	 * Figures out which {@link Scheme} should be used to access the request handler
	 * 
	 * @param handler
	 *            request handler
	 * @return {@link Scheme}
	 */
	protected Scheme getDesiredSchemeFor(IRequestHandler handler)
	{
		if (handler instanceof IPageClassRequestHandler)
		{
			return getDesiredSchemeFor(((IPageClassRequestHandler)handler).getPageClass());
		}
		return Scheme.ANY;
	}

	/**
	 * Determines the {@link Scheme} of the request
	 * 
	 * @param request
	 * @return {@link Scheme#HTTPS} or {@link Scheme#HTTP}
	 */
	protected Scheme getSchemeOf(Request request)
	{
		HttpServletRequest req = (HttpServletRequest) request.getContainerRequest();

		if ("https".equalsIgnoreCase(req.getScheme()))
		{
			return Scheme.HTTPS;
		}
		else if ("http".equalsIgnoreCase(req.getScheme()))
		{
			return Scheme.HTTP;
		}
		else
		{
			throw new IllegalStateException("Could not resolve protocol for request: " + req);
		}
	}

	/**
	 * Determines which {@link Scheme} should be used to access the page
	 * 
	 * @param pageClass
	 *            type of page
	 * @return {@link Scheme}
	 */
	protected Scheme getDesiredSchemeFor(Class<? extends IRequestablePage> pageClass)
	{
		Scheme SCHEME = cache.get(pageClass);
		if (SCHEME == null)
		{
			if (hasSecureAnnotation(pageClass))
			{
				SCHEME = Scheme.HTTPS;
			}
			else
			{
				SCHEME = Scheme.HTTP;
			}
			cache.put(pageClass, SCHEME);
		}
		return SCHEME;
	}

	/**
	 * @return config with which this mapper was created
	 */
	public final HttpsConfig getConfig()
	{
		return config;
	}

	/**
	 * Checks if the specified {@code type} has the {@link RequireHttps} annotation
	 * 
	 * @param type
	 * @return {@code true} iff {@code type} has the {@link RequireHttps} annotation
	 */
	private boolean hasSecureAnnotation(Class<?> type)
	{
		if (type.getAnnotation(RequireHttps.class) != null)
		{
			return true;
		}

		for (Class<?> iface : type.getInterfaces())
		{
			if (hasSecureAnnotation(iface))
			{
				return true;
			}
		}

		if (type.getSuperclass() != null)
		{
			return hasSecureAnnotation(type.getSuperclass());
		}
		return false;
	}


	/**
	 * Handler that takes care of redirecting
	 * 
	 * @author igor
	 */
	public static class RedirectHandler implements IRequestHandler
	{
		private final String url;
		private final HttpsConfig config;

		/**
		 * Constructor
		 * 
		 * @param config
		 *            https config
		 * @param url
		 *            redirect location
		 */
		public RedirectHandler(String url, HttpsConfig config)
		{
			this.url = Args.notNull(url, "url");
			this.config = Args.notNull(config, "config");
		}

		/**
		 * @return redirect location
		 */
		public String getUrl()
		{
			return url;
		}

		@Override
		public void respond(IRequestCycle requestCycle)
		{
			String location = url;

			if (location.startsWith("/"))
			{
				// context-absolute url
				location = requestCycle.getUrlRenderer().renderContextRelativeUrl(location);
			}

			if (config.isPreferStateful())
			{
				// we need to persist the session before a redirect to https so the session lasts
				// across both http and https calls.
				Session.get().bind();
			}

			WebResponse response = (WebResponse)requestCycle.getResponse();
			response.sendRedirect(location);
		}

		@Override
		public void detach(IRequestCycle requestCycle)
		{
		}

	}
}
