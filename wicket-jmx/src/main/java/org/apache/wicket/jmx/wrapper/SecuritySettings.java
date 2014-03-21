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
package org.apache.wicket.jmx.wrapper;

import org.apache.wicket.Application;
import org.apache.wicket.jmx.SecuritySettingsMBean;

/**
 * Exposes Application related functionality for JMX.
 * 
 * @author eelcohillenius
 */
public class SecuritySettings implements SecuritySettingsMBean
{
	private final Application application;

	/**
	 * Create.
	 * 
	 * @param application
	 */
	public SecuritySettings(final Application application)
	{
		this.application = application;
	}

	/**
	 * @see org.apache.wicket.jmx.SecuritySettingsMBean#getAuthorizationStrategy()
	 */
	@Override
	public String getAuthorizationStrategy()
	{
		return Stringz.className(application.getSecuritySettings().getAuthorizationStrategy());
	}

	/**
	 * @see org.apache.wicket.jmx.SecuritySettingsMBean#getCryptFactory()
	 */
	@Override
	public String getCryptFactory()
	{
		return Stringz.className(application.getSecuritySettings().getCryptFactory());
	}

	/**
	 * @see org.apache.wicket.jmx.SecuritySettingsMBean#getUnauthorizedComponentInstantiationListener()
	 */
	@Override
	public String getUnauthorizedComponentInstantiationListener()
	{
		return Stringz.className(application.getSecuritySettings()
			.getUnauthorizedComponentInstantiationListener());
	}
}
