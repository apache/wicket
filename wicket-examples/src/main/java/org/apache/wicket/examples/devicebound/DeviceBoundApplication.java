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
package org.apache.wicket.examples.devicebound;

import java.time.Duration;

import org.apache.wicket.Page;
import org.apache.wicket.authroles.authentication.AbstractAuthenticatedWebSession;
import org.apache.wicket.authroles.authentication.AuthenticatedWebApplication;
import org.apache.wicket.dbsc.DeviceBoundSessions;
import org.apache.wicket.markup.html.WebPage;

/**
 * Signs users in and binds their sessions to the device, with a bound cookie that lives a minute
 * so its refreshes can be watched.
 */
public class DeviceBoundApplication extends AuthenticatedWebApplication
{
	@Override
	public Class<? extends Page> getHomePage()
	{
		return DeviceBoundPage.class;
	}

	@Override
	protected Class<? extends AbstractAuthenticatedWebSession> getWebSessionClass()
	{
		return DeviceBoundWebSession.class;
	}

	@Override
	protected Class<? extends WebPage> getSignInPageClass()
	{
		return DeviceBoundSignInPage.class;
	}

	@Override
	protected void init()
	{
		super.init();

		getDebugSettings().setDevelopmentUtilitiesEnabled(true);

		new DeviceBoundSessions().setCookieLifetime(Duration.ofMinutes(1)).install(this);
	}
}
