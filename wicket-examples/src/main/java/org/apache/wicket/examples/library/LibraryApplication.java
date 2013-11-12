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
package org.apache.wicket.examples.library;

import org.apache.wicket.Page;
import org.apache.wicket.Session;
import org.apache.wicket.authorization.IAuthorizationStrategy;
import org.apache.wicket.authorization.strategies.page.SimplePageAuthorizationStrategy;
import org.apache.wicket.examples.WicketExampleApplication;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.settings.def.RequestCycleSettings;


/**
 * Application class for the library example.
 * 
 * @author Jonathan Locke
 */
public final class LibraryApplication extends WicketExampleApplication
{
	/**
	 * Constructor.
	 */
	public LibraryApplication()
	{
	}

	/**
	 * @see org.apache.wicket.Application#getHomePage()
	 */
	@Override
	public Class<? extends Page> getHomePage()
	{
		return Home.class;
	}

	/**
	 * @see org.apache.wicket.protocol.http.WebApplication#newSession(Request, Response)
	 */
	@Override
	public Session newSession(Request request, Response response)
	{
		return new LibrarySession(request);
	}

	/**
	 * @see org.apache.wicket.examples.WicketExampleApplication#init()
	 */
	@Override
	protected void init()
	{
		super.init();

		getResourceSettings().setThrowExceptionOnMissingResource(false);
		getRequestCycleSettings().setRenderStrategy(RequestCycleSettings.RenderStrategy.REDIRECT_TO_RENDER);

		// Install a simple page authorization strategy, that checks all pages
		// of type AuthenticatedWebPage.
		IAuthorizationStrategy authorizationStrategy = new SimplePageAuthorizationStrategy(
			AuthenticatedWebPage.class, SignIn.class)
		{
			@Override
			protected boolean isAuthorized()
			{
				// check whether the user is logged on
				return (((LibrarySession)Session.get()).isSignedIn());
			}
		};
		getSecuritySettings().setAuthorizationStrategy(authorizationStrategy);

		// install crypto mapper to encrypt all application urls
		// getSecuritySettings().setCryptFactory(new KeyInSessionSunJceCryptFactory());
		// ThreadsafeCompoundRequestMapper root = new ThreadsafeCompoundRequestMapper();
		// root.register(new CryptoMapper(getRootRequestMapper(), this));
		// setRootRequestMapper(root);
	}
}
