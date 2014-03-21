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
import org.apache.wicket.jmx.PageSettingsMBean;


/**
 * Exposes Application related functionality for JMX.
 * 
 * @author eelcohillenius
 */
public class PageSettings implements PageSettingsMBean
{
	private final Application application;

	/**
	 * Create.
	 * 
	 * @param application
	 */
	public PageSettings(final Application application)
	{
		this.application = application;
	}

	/**
	 * @see org.apache.wicket.jmx.PageSettingsMBean#getVersionPagesByDefault()
	 */
	@Override
	public boolean getVersionPagesByDefault()
	{
		return application.getPageSettings().getVersionPagesByDefault();
	}

	/**
	 * @see org.apache.wicket.jmx.PageSettingsMBean#setVersionPagesByDefault(boolean)
	 */
	@Override
	public void setVersionPagesByDefault(final boolean pagesVersionedByDefault)
	{
		application.getPageSettings().setVersionPagesByDefault(pagesVersionedByDefault);
	}
}
