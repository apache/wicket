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
package org.apache.wicket.jmx;

/**
 * Exposes Application related functionality for JMX.
 * 
 * @author eelcohillenius
 */
public class CookieValuePersisterSettings implements CookieValuePersisterSettingsMBean
{
	private final org.apache.wicket.Application application;

	/**
	 * Create.
	 * 
	 * @param application
	 */
	public CookieValuePersisterSettings(org.apache.wicket.Application application)
	{
		this.application = application;
	}

	/**
	 * @see org.apache.wicket.jmx.CookieValuePersisterSettingsMBean#getComment()
	 */
	public String getComment()
	{
		return application.getSecuritySettings().getCookieValuePersisterSettings().getComment();
	}

	/**
	 * @see org.apache.wicket.jmx.CookieValuePersisterSettingsMBean#getDomain()
	 */
	public String getDomain()
	{
		return application.getSecuritySettings().getCookieValuePersisterSettings().getDomain();
	}

	/**
	 * @see org.apache.wicket.jmx.CookieValuePersisterSettingsMBean#getMaxAge()
	 */
	public int getMaxAge()
	{
		return application.getSecuritySettings().getCookieValuePersisterSettings().getMaxAge();
	}

	/**
	 * @see org.apache.wicket.jmx.CookieValuePersisterSettingsMBean#getSecure()
	 */
	public boolean getSecure()
	{
		return application.getSecuritySettings().getCookieValuePersisterSettings().getSecure();
	}

	/**
	 * @see org.apache.wicket.jmx.CookieValuePersisterSettingsMBean#getVersion()
	 */
	public int getVersion()
	{
		return application.getSecuritySettings().getCookieValuePersisterSettings().getVersion();
	}

	/**
	 * @see org.apache.wicket.jmx.CookieValuePersisterSettingsMBean#setComment(java.lang.String)
	 */
	public void setComment(String comment)
	{
		application.getSecuritySettings().getCookieValuePersisterSettings().setComment(comment);
	}

	/**
	 * @see org.apache.wicket.jmx.CookieValuePersisterSettingsMBean#setDomain(java.lang.String)
	 */
	public void setDomain(String domain)
	{
		application.getSecuritySettings().getCookieValuePersisterSettings().setDomain(domain);
	}

	/**
	 * @see org.apache.wicket.jmx.CookieValuePersisterSettingsMBean#setMaxAge(int)
	 */
	public void setMaxAge(int maxAge)
	{
		application.getSecuritySettings().getCookieValuePersisterSettings().setMaxAge(maxAge);
	}

	/**
	 * @see org.apache.wicket.jmx.CookieValuePersisterSettingsMBean#setSecure(boolean)
	 */
	public void setSecure(boolean secure)
	{
		application.getSecuritySettings().getCookieValuePersisterSettings().setSecure(secure);
	}

	/**
	 * @see org.apache.wicket.jmx.CookieValuePersisterSettingsMBean#setVersion(int)
	 */
	public void setVersion(int version)
	{
		application.getSecuritySettings().getCookieValuePersisterSettings().setVersion(version);
	}
}
