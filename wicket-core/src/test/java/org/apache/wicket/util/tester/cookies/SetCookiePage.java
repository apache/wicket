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
package org.apache.wicket.util.tester.cookies;

import javax.servlet.http.Cookie;

import org.apache.wicket.Page;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.util.tester.DummyHomePage;

/**
 * a page which sets cookies and makes an optional redirect
 * @author mosmann
 */
public class SetCookiePage extends DummyHomePage
{
	private final Cookie cookie;
	private final Class<? extends Page> redirectToPageClass;

	public SetCookiePage(Cookie cookie,Class<? extends Page> redirectToPageClass)
	{
		this.cookie = cookie;
		this.redirectToPageClass = redirectToPageClass;
	}
	
	public SetCookiePage(Cookie cookie)
	{
		this(cookie,null);
	}
	
	protected void onInitialize() {
		super.onInitialize();

		WebResponse response = (WebResponse) getResponse();
		response.addCookie(cookie);
		
		if (redirectToPageClass != null)
		{
			setResponsePage(redirectToPageClass);
		}
	};
}
