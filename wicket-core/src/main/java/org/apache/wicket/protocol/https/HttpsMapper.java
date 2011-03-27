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

import org.apache.wicket.Session;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.IRequestMapper;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Url;
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
 */
public class HttpsMapper implements IRequestMapper
{
	/**
	 * The original request mapper that will actually resolve the page
	 */
	private final IRequestMapper delegate;

	/**
	 * The object that brings the settings for communication over https
	 */
	private final HttpsConfig httpsConfig;

	/**
	 * A helper that will check the resolved page for @{@link RequireHttps} annotation
	 */
	private final HttpsRequestChecker checker;

	/**
	 * Construct.
	 * 
	 * @param delegate
	 * @param httpsConfig
	 */
	public HttpsMapper(final IRequestMapper delegate, final HttpsConfig httpsConfig)
	{
		Args.notNull(delegate, "delegate");
		Args.notNull(httpsConfig, "httpsConfig");

		this.delegate = delegate;
		this.httpsConfig = httpsConfig;
		checker = new HttpsRequestChecker();
	}

	/**
	 * {@inheritDoc}
	 */
	public IRequestHandler mapRequest(final Request request)
	{
		IRequestHandler requestHandler = delegate.mapRequest(request);

		if (requestHandler != null)
		{
			final IRequestHandler httpsHandler = checker.checkSecureIncoming(requestHandler,
				httpsConfig);

			// XXX do we need to check if httpsHandler is instance of SwitchProtocolRequestHandler
			if (httpsConfig.isPreferStateful())
			{
				// we need to persist the session before a redirect to https so the session lasts
				// across both http and https calls.
				Session.get().bind();
			}

			requestHandler = httpsHandler;
		}

		return requestHandler;
	}

	/**
	 * {@inheritDoc}
	 */
	public int getCompatibilityScore(final Request request)
	{
		return delegate.getCompatibilityScore(request);
	}

	/**
	 * {@inheritDoc}
	 */
	public Url mapHandler(IRequestHandler requestHandler)
	{
		Url url = delegate.mapHandler(requestHandler);
		switch (checker.getProtocol(requestHandler))
		{
			case HTTP :
				url.setProtocol("http");
				url.setPort(httpsConfig.getHttpPort());
				break;
			case HTTPS :
				url.setProtocol("https");
				url.setPort(httpsConfig.getHttpsPort());
				break;
		}
		return url;
	}
}
