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
package org.apache.wicket.examples.authentication3;

import org.apache.wicket.Page;
import org.apache.wicket.authroles.authentication.AbstractAuthenticatedWebSession;
import org.apache.wicket.authroles.authentication.AuthenticatedWebApplication;
import org.apache.wicket.csp.CSPDirective;
import org.apache.wicket.markup.html.WebPage;


/**
 * A role-authorized, authenticated web application in just a few lines of code.
 * 
 * @author Jonathan Locke
 */
public class MyAuthenticatedWebApplication extends AuthenticatedWebApplication
{
	@Override
	public Class<? extends Page> getHomePage()
	{
		return HomePage.class;
	}

	@Override
	protected Class<? extends AbstractAuthenticatedWebSession> getWebSessionClass()
	{
		return MyAuthenticatedWebSession.class;
	}

	@Override
	protected Class<? extends WebPage> getSignInPageClass()
	{
		return MySignInPage.class;
	}

	@Override
	protected void init()
	{
		super.init();
		getDebugSettings().setDevelopmentUtilitiesEnabled(true);

		getCspSettings().blocking()
		                .strict()
		                .reportBack()
		                .add(CSPDirective.STYLE_SRC,
				"https://maxcdn.bootstrapcdn.com/font-awesome/4.3.0/css/font-awesome.min.css")
		                .add(CSPDirective.FONT_SRC, "https://maxcdn.bootstrapcdn.com");
	}
}
