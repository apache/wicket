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
package wicket.jmx;

import wicket.util.lang.Classes;

/**
 * Exposes Application related functionality for JMX.
 * 
 * @author eelcohillenius
 */
public class ApplicationSettings implements ApplicationSettingsMBean
{
	private final wicket.Application application;

	/**
	 * Create.
	 * 
	 * @param application
	 */
	public ApplicationSettings(wicket.Application application)
	{
		this.application = application;
	}

	/**
	 * @see wicket.jmx.ApplicationSettingsMBean#getAccessDeniedPage()
	 */
	public String getAccessDeniedPage()
	{
		return Classes.name(application.getApplicationSettings().getAccessDeniedPage());
	}

	/**
	 * @see wicket.jmx.ApplicationSettingsMBean#getClassResolver()
	 */
	public String getClassResolver()
	{
		return Stringz.className(application.getApplicationSettings().getClassResolver());
	}

	/**
	 * @see wicket.jmx.ApplicationSettingsMBean#getContextPath()
	 */
	public String getContextPath()
	{
		return application.getApplicationSettings().getContextPath();
	}

	/**
	 * @see wicket.jmx.ApplicationSettingsMBean#getConverterFactory()
	 */
	public String getConverterLocatorFactory()
	{
		return Stringz.className(application.getApplicationSettings().getConverterLocatorFactory());
	}

	/**
	 * @see wicket.jmx.ApplicationSettingsMBean#getInternalErrorPage()
	 */
	public String getInternalErrorPage()
	{
		return Classes.name(application.getApplicationSettings().getInternalErrorPage());
	}

	/**
	 * @see wicket.jmx.ApplicationSettingsMBean#getPageExpiredErrorPage()
	 */
	public String getPageExpiredErrorPage()
	{
		return Classes.name(application.getApplicationSettings().getPageExpiredErrorPage());
	}

	/**
	 * @see wicket.jmx.ApplicationSettingsMBean#getUnexpectedExceptionDisplay()
	 */
	public String getUnexpectedExceptionDisplay()
	{
		return application.getApplicationSettings().getUnexpectedExceptionDisplay().toString();
	}
}
