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
package org.apache.wicket.examples.authentication2;

import org.apache.wicket.Page;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.Session;
import org.apache.wicket.authorization.IAuthorizationStrategy;
import org.apache.wicket.examples.WicketExampleApplication;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.component.IRequestableComponent;


/**
 * Forms example.
 * 
 * @author Jonathan Locke
 */
public final class SignIn2Application extends WicketExampleApplication
{
	/**
	 * Constructor.
	 */
	public SignIn2Application()
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
	 * @see org.apache.wicket.protocol.http.WebApplication#newSession(org.apache.wicket.request.Request,
	 *      Response)
	 */
	@Override
	public Session newSession(Request request, Response response)
	{
		return new SignIn2Session(request);
	}

	/**
	 * @see org.apache.wicket.examples.WicketExampleApplication#init()
	 */
	@Override
	protected void init()
	{
		super.init();

		// Register the authorization strategy
		getSecuritySettings().setAuthorizationStrategy(new IAuthorizationStrategy.AllowAllAuthorizationStrategy()
		{
			public <T extends IRequestableComponent> boolean isInstantiationAuthorized(
				Class<T> componentClass)
			{
				// Check if the new Page requires authentication (implements the marker interface)
				if (AuthenticatedWebPage.class.isAssignableFrom(componentClass))
				{
					// Is user signed in?
					if (((SignIn2Session)Session.get()).isSignedIn())
					{
						// okay to proceed
						return true;
					}

					// Intercept the request, but remember the target for later.
					// Invoke Component.continueToOriginalDestination() after successful logon to
					// continue with the target remembered.
					throw new RestartResponseAtInterceptPageException(SignIn2.class);
				}

				// okay to proceed
				return true;
			}
		});
	}
}
