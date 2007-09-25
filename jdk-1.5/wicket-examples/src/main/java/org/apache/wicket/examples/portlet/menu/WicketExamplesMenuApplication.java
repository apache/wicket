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
package org.apache.wicket.examples.portlet.menu;

import java.util.Collections;
import java.util.List;

import javax.portlet.PortletMode;
import javax.servlet.ServletContext;

import org.apache.wicket.RequestContext;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.portlet.PortletRequestContext;

/**
 * @author Ate Douma
 */
public class WicketExamplesMenuApplication extends WebApplication
{
	private static List examples;
	private static ServletContext servletContext;

	public static List getExamples()
	{		
		if (examples == null)
		{
			examples = (List)servletContext.getAttribute(WicketExamplesMenuPortlet.EXAMPLES);
		}
		return examples != null ? examples : Collections.EMPTY_LIST;
	}
	
	/**
	 * @see org.apache.wicket.Application#getHomePage()
	 */
	@Override
	public Class getHomePage()
	{
		PortletRequestContext prc = (PortletRequestContext)RequestContext.get();
		if (PortletMode.EDIT.equals(prc.getPortletRequest().getPortletMode()))
		{
			return EditPage.class;
		}
		return MenuPage.class;
	}
	
	/**
	 * @see org.apache.wicket.protocol.http.WebApplication#init()
	 */
	@Override
	protected void init()
	{
		mountBookmarkablePage("/menu", MenuPage.class);
		mountBookmarkablePage("/header", HeaderPage.class);
		mountBookmarkablePage("/edit", EditPage.class);
		servletContext = getWicketFilter().getFilterConfig().getServletContext();
	}

}
