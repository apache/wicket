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
package org.apache.wicket.settings.def;

import org.apache.wicket.Application;
import org.apache.wicket.IPageFactory;
import org.apache.wicket.Session;
import org.apache.wicket.settings.ISessionSettings;

/**
 * 
 * @author Jonathan Locke
 * @author Chris Turner
 * @author Eelco Hillenius
 * @author Juergen Donnerstag
 * @author Johan Compagner
 * @author Igor Vaynberg (ivaynberg)
 * @author Martijn Dashorst
 * @author James Carman
 */
public class SessionSettings implements ISessionSettings
{
	/**
	 * @see org.apache.wicket.settings.ISessionSettings#getPageFactory()
	 * @deprecated Use {@link Session#getPageFactory()}
	 */
	@Deprecated
	public IPageFactory getPageFactory()
	{
		IPageFactory pageFactory = null;

		if (Application.exists())
		{
			pageFactory = Application.get().getPageFactory();
		}

		return pageFactory;
	}

	/**
	 * @see org.apache.wicket.settings.ISessionSettings#setPageFactory(org.apache.wicket.IPageFactory)
	 * @deprecated Use {@link Application#newPageFactory()} instead.
	 */
	@Deprecated
	public void setPageFactory(final IPageFactory defaultPageFactory)
	{
	}
}
