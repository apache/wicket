/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.wicket.examples.signin;

import org.apache.wicket.Component;
import org.apache.wicket.Request;
import org.apache.wicket.Response;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.Session;
import org.apache.wicket.authorization.Action;
import org.apache.wicket.authorization.IAuthorizationStrategy;
import org.apache.wicket.examples.WicketExampleApplication;


/**
 * Forms example.
 * 
 * @author Jonathan Locke
 */
public final class SignInApplication extends WicketExampleApplication
{
	/**
	 * Constructor.
	 */
	public SignInApplication()
	{
	}

	/**
	 * @see org.apache.wicket.Application#getHomePage()
	 */
	public Class getHomePage()
	{
		return Home.class;
	}

	/**
	 * @see org.apache.wicket.protocol.http.WebApplication#newSession(Request,
	 *      Response)
	 */
	public Session newSession(Request request, Response response)
	{
		return new SignInSession(SignInApplication.this, request);
	}

	/**
	 * @see org.apache.wicket.examples.WicketExampleApplication#init()
	 */
	protected void init()
	{
		getSecuritySettings().setAuthorizationStrategy(new IAuthorizationStrategy()
		{
			public boolean isActionAuthorized(Component component, Action action)
			{
				return true;
			}

			public boolean isInstantiationAuthorized(Class componentClass)
			{
				if (AuthenticatedWebPage.class.isAssignableFrom(componentClass))
				{
					// Is user signed in?
					if (((SignInSession)Session.get()).isSignedIn())
					{
						// okay to proceed
						return true;
					}

					// Force sign in
					throw new RestartResponseAtInterceptPageException(SignIn.class);
				}
				return true;
			}
		});
	}

}
