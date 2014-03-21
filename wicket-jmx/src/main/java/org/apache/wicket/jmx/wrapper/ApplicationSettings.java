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
import org.apache.wicket.jmx.ApplicationSettingsMBean;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.util.lang.Classes;

/**
 * Exposes Application related functionality for JMX.
 * 
 * @author eelcohillenius
 */
public class ApplicationSettings implements ApplicationSettingsMBean
{
	private final Application application;

	/**
	 * Create.
	 * 
	 * @param application
	 */
	public ApplicationSettings(final Application application)
	{
		this.application = application;
	}

	/**
	 * @see org.apache.wicket.jmx.ApplicationSettingsMBean#getAccessDeniedPage()
	 */
	@Override
	public String getAccessDeniedPage()
	{
		return Classes.name(application.getApplicationSettings().getAccessDeniedPage());
	}

	/**
	 * @see org.apache.wicket.jmx.ApplicationSettingsMBean#getClassResolver()
	 */
	@Override
	public String getClassResolver()
	{
		return Stringz.className(application.getApplicationSettings().getClassResolver());
	}

	@Override
	public String getDefaultMaximumUploadSize()
	{
		return application.getApplicationSettings().getDefaultMaximumUploadSize().toString();
	}

	/**
	 * @see org.apache.wicket.jmx.ApplicationSettingsMBean#getInternalErrorPage()
	 */
	@Override
	public String getInternalErrorPage()
	{
		return Classes.name(application.getApplicationSettings().getInternalErrorPage());
	}

	/**
	 * @see org.apache.wicket.jmx.ApplicationSettingsMBean#getPageExpiredErrorPage()
	 */
	@Override
	public String getPageExpiredErrorPage()
	{
		return Classes.name(application.getApplicationSettings().getPageExpiredErrorPage());
	}

	/**
	 * @see org.apache.wicket.jmx.ApplicationSettingsMBean#getUnexpectedExceptionDisplay()
	 */
	@Override
	public String getUnexpectedExceptionDisplay()
	{
		return application.getExceptionSettings().getUnexpectedExceptionDisplay().toString();
	}

	/**
	 * @see org.apache.wicket.jmx.ApplicationSettingsMBean#setDefaultMaximumUploadSize(java.lang.String)
	 */
	@Override
	public void setDefaultMaximumUploadSize(final String defaultUploadSize)
	{
		application.getApplicationSettings().setDefaultMaximumUploadSize(
			Bytes.valueOf(defaultUploadSize));
	}
}
